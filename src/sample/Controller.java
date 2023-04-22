package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import sample.objekte.PieceCircle;
import sample.objekte.PieceRectangle;
import sample.utilities.Vektor;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static java.lang.Math.*;

/* controller ist die zuständig für Programm Logik. Es muss für alle Fenster (FXML Datei einen Controller legen). Zum Beispiel
Button soll was machen, wenn man drauf klickt. Dann wird dies hier programmeirt.*/
public class Controller {
    @FXML
    private AnchorPane ganzerFeld;

    @FXML
    private Slider slider_gravitation, slider_windstärke_richtung, slider_startgeschwindigkeit, slider_startwinkel;

    @FXML
    private Label label_gravitation, label_windstärke_richtung, label_startgeschwindigkeit, label_startwinkel, status_text, startRichtung;

    @FXML
    private Text text_richtung_blaue_murmel, text_gesch_blaue_murmel_x, text_gesch_blaue_murmel_y;

    @FXML
    private Circle blaue_murmel, joystick;

    @FXML
    private Rectangle startbahn;

    @FXML
    private ImageView image_play_button;

    @FXML
    private Button startButton;

    private double startGeschwindigkeit = 0.0, startwinkelKugel = 270.0, windbeschleunigungA = 0.0;
    private PieceCircle pieceCircleBlaueMurmel, pieceCircleJoystick;
    private ArrayList<PieceCircle> listPieceCircle = new ArrayList<PieceCircle>();
    private ArrayList<PieceRectangle> listPieceRectangle = new ArrayList<PieceRectangle>();

    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static DecimalFormat df1 = new DecimalFormat("#.#");
    private final int fpx = 100;

    // Anzahl Pixel entsprechen einen Meter
    private final double joystickRadiusZurGrenze = 40.0, xPixelFürEinMeter = 5.0, deltaTime = (1000.0/fpx/1000.0);  // 1000ms = 1s / frames / 1000.0 weil man in sekunden haben will.
    private boolean buttonPlay = false, started = false;
    private Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(deltaTime), e -> physic_loop()));

    // Physik elemente
    // Gravitation in - Variable
    private Vektor gravitation = new Vektor(0, -9.81);
    // wind richtung wählen
    private Vektor windRichtung = new Vektor(1, 0);     // nur links und rechts

    public void initial(){
        slider_gravitation.valueProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1){
                gravitation.y = t1.doubleValue();
                label_gravitation.setText(df2.format(gravitation.y) + " m/s²");
            }
        });

        slider_windstärke_richtung.valueProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1){
                if(Math.abs(t1.doubleValue()) < 0.2){
                    windbeschleunigungA = 0.0;
                }else {
                    windbeschleunigungA = t1.doubleValue();
                }
                label_windstärke_richtung.setText(df1.format(windbeschleunigungA) + " m/s²");
                windRichtung.normalisierung();
            }
        });

        slider_startgeschwindigkeit.valueProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1){
                startGeschwindigkeit = t1.doubleValue();
                label_startgeschwindigkeit.setText(df2.format(startGeschwindigkeit) + " m/s.");
                Vektor direction = new Vektor(1, 0);
                // hier anpassen
                direction.rotate(listPieceCircle.get(0).getRichtung());
                listPieceCircle.get(0).setGeschwindigkeitV(Vektor.scalareMultiplikationProdukt(direction, startGeschwindigkeit));
            }
        });

        slider_startwinkel.valueProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1){
                label_startwinkel.setText((360 - t1.intValue()) % 360 + "°");
                startbahn.setRotate(t1.intValue());
            }
        });

        pieceCircleJoystick = new PieceCircle(joystick.getLayoutX(), joystick.getLayoutY(), joystick.getRadius(), joystick, new Vektor());
        pieceCircleBlaueMurmel = new PieceCircle(blaue_murmel.getLayoutX() / xPixelFürEinMeter, blaue_murmel.getLayoutY() / xPixelFürEinMeter, blaue_murmel.getRadius(),blaue_murmel, new Vektor());
        listPieceCircle.add(pieceCircleBlaueMurmel);
        listPieceCircle.get(0).draw(ganzerFeld.getHeight(), xPixelFürEinMeter);

        PieceRectangle pieceRectangle = new PieceRectangle(startbahn.getLayoutX(), startbahn.getLayoutY(), ((int) startbahn.getHeight()), ((int) startbahn.getWidth()), startbahn);
        listPieceRectangle.add(pieceRectangle);
    }

    // Für Testzwecke
    @FXML
    private Button is_rolling_button;
    public void kugelRollt(){
        if(!listPieceCircle.get(0).isRolling){
            is_rolling_button.setText("Es rollt");
            listPieceCircle.get(0).isRolling = true;
        }else {
            is_rolling_button.setText("Es fällt");
            listPieceCircle.get(0).isRolling = false;
        }
    }
    // Bis hier

    @FXML
    private void displayPosition(MouseEvent event){ status_text.setText("Maus Position: X = " + event.getSceneX() + ", Y = " + event.getSceneY()); }

    @FXML
    private void onJoystickDragged(MouseEvent event){
        // Aktuelle Position des Joysticks plus die Entfernung bis zur Mausposition
        double newPositionX = pieceCircleJoystick.position.x + event.getX();
        double newPositionY = pieceCircleJoystick.position.y + event.getY();

        // Berechnung der Hypotenuse zwischen der aktuellen Position und der neuen Position
        double hypotenuse = Math.sqrt(Math.pow(newPositionX, 2.0) + Math.pow(newPositionY, 2.0));

        // Berechnung des Winkels
        double winkel = Math.toDegrees(Math.atan2(newPositionY, newPositionX));
        winkel = (winkel + 360.0) % 360.0; // Normalisierung des Winkels auf den Bereich [0, 360)

        // Wenn die Hypotenuse größer als der Joystick-Radius ist, beschränken wir die Position des Joysticks auf die Grenze
        if (hypotenuse > joystickRadiusZurGrenze){
            newPositionX = Math.cos(Math.toRadians(winkel)) * joystickRadiusZurGrenze;
            newPositionY = Math.sin(Math.toRadians(winkel)) * joystickRadiusZurGrenze;
        }else { winkel = 90.0; }

        // Aktualisieren des Joystick-Winkels und der Geschwindigkeitsrichtung der Kugel
        int winkelInt = (int) Math.abs(winkel);
        startRichtung.setText("Rollwinkel: " + (360 - winkelInt) + "°");
        startwinkelKugel = winkelInt;

        Vektor direction = new Vektor(1, 0);
        direction.rotate(360 - startwinkelKugel);
        if(listPieceCircle.get(0).getGeschwindigkeitV().länge() == 0.0 && startGeschwindigkeit == 0.0){
            listPieceCircle.get(0).setGeschwindigkeitV(direction);
        }else{
            listPieceCircle.get(0).setGeschwindigkeitV(Vektor.scalareMultiplikationProdukt(direction,
                    listPieceCircle.get(0).getGeschwindigkeitV().länge() == 0.0 ? startGeschwindigkeit : listPieceCircle.get(0).getGeschwindigkeitV().länge()));
        }

        // Aktualisierung der Joystick-Position und Neuzeichnung
        pieceCircleJoystick.position.x = newPositionX;
        pieceCircleJoystick.position.y = newPositionY;
        pieceCircleJoystick.drawJoystick();
    }

    @FXML
    private void onJoystickReleased(MouseEvent event){
        // Translate Position zurücksetzen.
        pieceCircleJoystick.position.x = (0.0);
        pieceCircleJoystick.position.y = (0.0);
        pieceCircleJoystick.drawJoystick();
    }

    @FXML
    private void onBlaueMurmelDragged(MouseEvent event){
        // Aktuelle Positionen setzen
        pieceCircleBlaueMurmel.position.x = event.getSceneX() / xPixelFürEinMeter;
        pieceCircleBlaueMurmel.position.y = (ganzerFeld.getHeight() - event.getSceneY()) / xPixelFürEinMeter;

        if(listPieceCircle.get(0).position.x > (ganzerFeld.getWidth() - listPieceCircle.get(0).getRadius()) / xPixelFürEinMeter){ listPieceCircle.get(0).position.x = (ganzerFeld.getWidth() - listPieceCircle.get(0).getRadius()) / xPixelFürEinMeter; listPieceCircle.get(0).getGeschwindigkeitV().x = 0.0; }
        else if(listPieceCircle.get(0).position.x < (150 + listPieceCircle.get(0).getRadius()) / xPixelFürEinMeter){ listPieceCircle.get(0).position.x = (150 + listPieceCircle.get(0).getRadius()) / xPixelFürEinMeter; listPieceCircle.get(0).getGeschwindigkeitV().x = 0.0; }
        if(listPieceCircle.get(0).position.y > (ganzerFeld.getHeight() - listPieceCircle.get(0).getRadius()) / xPixelFürEinMeter){ listPieceCircle.get(0).position.y = (ganzerFeld.getHeight() - listPieceCircle.get(0).getRadius()) / xPixelFürEinMeter; }
        else if(listPieceCircle.get(0).position.y < listPieceCircle.get(0).getRadius() / xPixelFürEinMeter ){ listPieceCircle.get(0).position.y = (listPieceCircle.get(0).getRadius()) / xPixelFürEinMeter; listPieceCircle.get(0).getGeschwindigkeitV().y = 0.0; }
        pieceCircleBlaueMurmel.draw(ganzerFeld.getHeight(), xPixelFürEinMeter);

        buttonPlay = false;
        startButton.setText("Start");
        File file = new File("src/images/resume.png");
        Image image = new Image(file.toURI().toString());
        image_play_button.setImage(image);
    }

    @FXML
    private void onBlaueMurmelReleased(MouseEvent event){ }

    // Start und Stop Knopf
    public void startAndPlay(){
        // startet das ScheduleAtFixedRate
        if(!started){
            // Animationsschleife
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
            started = true;
            buttonPlay = true;
            // setzt bild für den start-stop button
            File file = new File("src/images/pause.png");
            Image image = new Image(file.toURI().toString());
            image_play_button.setImage(image);
            startButton.setText("Pause");
        }else if(buttonPlay){
            // wenn button geklickt  wurde, soll buttonPlay false sein, damit die physic_loop() die animation pausiert.
            buttonPlay = false;
            File file = new File("src/images/resume.png");
            Image image = new Image(file.toURI().toString());
            image_play_button.setImage(image);
            timeline.pause();
            startButton.setText("Start");
        }else {
            // animation fortlaufen lassen.
            buttonPlay = true;
            File file = new File("src/images/pause.png");
            Image image = new Image(file.toURI().toString());
            image_play_button.setImage(image);
            timeline.play();
            startButton.setText("Pause");
        }
    }

    public void physic_loop(){
        // wenn buttonPlay true ist, soll animation weiter laufen.
        if(buttonPlay){
            for(int index = 0; index < listPieceCircle.size(); index++){
                move(index);
            }
            showRichtungMurmeln();
            showGeschwindigkeit();
        }
    }

    private void showRichtungMurmeln(){
        text_richtung_blaue_murmel.setText("Blaue-Murmel Richtung: " + (360 - listPieceCircle.get(0).getRichtungPositiv()) + "°");
    }

    private void showGeschwindigkeit(){
        text_gesch_blaue_murmel_x.setText("Blaue-Murmel Geschwindigkeit x: " + df2.format(Math.abs(listPieceCircle.get(0).getGeschwindigkeitV().x)) + "m/s");
        text_gesch_blaue_murmel_y.setText("Blaue-Murmel Geschwindigkeit y: " + df2.format(Math.abs(listPieceCircle.get(0).getGeschwindigkeitV().y)) + "m/s");
    }

    private void move(int index){
        Vektor beschleunigungA = listPieceCircle.get(index).getBeschleunigungA();
        Vektor geschwindigkeitV = listPieceCircle.get(index).getGeschwindigkeitV();
        Vektor strecke = new Vektor();

        // Wenn die Kugel rollt
        if (listPieceCircle.get(index).isRolling){
            /**
             * Für die erste Abgabe kontrollieren wir, wie sich die Kugel verhält, wenn sie rollt. Indem Fall stellen
             * wir uns ein unsichtbares Rechteck mit einem frei rotierbaren Winkel vor, welches mit dem blau-lilanem
             * Joystick einstellbar ist und der eingestellte Winkel des unsichtbaren Rechtecks über dem Joystick steht.
             * Die Kugel simuliert entsprechend das Rollen.
              */
            int rectangleWinkel = listPieceCircle.get(index).getRichtung();
            double alpha = Math.toRadians(rectangleWinkel);

            // h für Höhe
            double h = listPieceCircle.get(index).deltaXY.y;

            /**
             * Energie und Energieerhaltung
             * W = Energie
             * m = Masse
             * v = Geschwindigkeit
             * g = Gravitationsbeschleunigung
             * h = Höhe
             * Kinetische Energie (Bewegungsenergie): Wkin = 0.5 * m * v²
             * Potentielle Energie (Lageenergie): Wpot = m * g * h
             *
             * Erst bei der zweiten Abgabe wichtig
             */
            // Kinetische Energie (Bewegungsenergie): Wkin = 0.5 * m * v²
            double W_Kin = 0.5 * listPieceCircle.get(index).gewicht * Math.pow(geschwindigkeitV.länge(), 2);
            // Potentielle Energie (Lageenergie): Wpot = m * g * h
            double W_Pot = listPieceCircle.get(index).gewicht * gravitation.y * listPieceCircle.get(index).deltaXY.y;

            // W_ges = W_pot
            double W_Ges = Math.sqrt(W_Pot + W_Kin);

            double W_Ges_x = W_Ges * cos(alpha);
            double W_Ges_y = W_Ges * sin(alpha);
            Vektor W_Ges_Vektor = new Vektor(W_Ges_x, W_Ges_y);

            // geschwindigkeitV = W_Ges_Vektor;

            /**
             * Reibung
             * Kraft: F_G = Gravitation, da wir nur eine Kraft haben
             * Hangabtriebskraft: F_GH = F_G * sin(alpha)
             * Normalkraft: F_GN = F_G * cos(alpha)
             */
            // F_G = Gravitation
            double F_G = gravitation.y;
            // F_GH = F_G * sin(alpha)
            double F_GH = F_G * sin(alpha);
            // F_GN = F_G * cos(alpha)
            double F_GN = F_G * cos(alpha);

            // F = m * a

            /**
             * Beschleunigungskomponente
             * Hangabtriebskraft-Komponente der Beschleunigung: a_H = Gravitation * sin(alpha)
             * Normalkraft-Komponente der Beschleunigung: a_N = gravitation * cos(alpha)
             *
             * Bremsung durch die Reibung
             * μ = Reibungskoeffizient
             * Reibungskraft-Komponente der Beschleunigung: a_R = (Gewicht * cos(alpha)) * μ
             * Resultierende Beschleunigung: a_res = a_H - a_R
             */
            // a_H = Gravitation * sin(alpha)
            double a_H = gravitation.y * sin(alpha);
            // a_N = gravitation * cos(alpha)
            double a_N = gravitation.y * cos(alpha);
            // a_R = (Gewicht * cos(alpha)) * μ
            double a_R = a_N * listPieceCircle.get(index).reibungskoeffizient;
            // Resultierende Beschleunigung: a_res = a_H - a_R
            double a_res = a_H - a_R;

            // Beschleunigung für x-Richtung berechnen
            double a_res_x = a_res * cos(alpha);
            // Beschleunigung für y-Richtung berechnen
            double a_res_y = a_res * sin(alpha);
            beschleunigungA = new Vektor(a_res_x, a_res_y);
            // beschleunigungA.addition(new Vektor(windStärke, 0));



            // Richtung in die Kugel rollt berechnen
            // Vektor direction = new Vektor(1, 0);
            // direction.rotate(rectangleWinkel);
            // geschwindigkeitV.scalareMultiplikationProdukt(direction, geschwindigkeitV.länge());

        }/* Wenn die Kugel nicht rollt */ else {
            /* Relevante Formeln für die erste Abgabe ________________________________________________________________*/
            /**
             * Freier Fall
             * Beschleunigung ist Konstant und es wirkt aktuell nur die Gravitation und Windbeschleunigung
             * a = gravitation + windStärke
             */
            // Die Beschleunigung a (beschleunigungA) enthält die Gravitationsbeschleunigung und die Windbeschleunigung
            beschleunigungA = new Vektor(gravitation.x, gravitation.y);
            beschleunigungA.addition(new Vektor(windbeschleunigungA, 0));
        }

        /**
         * Gesamtformel für die Strecke s = s0 + v * t + 0.5 * a * t²
         * Formel für die Strecke in zwei Teile aufteilen:
         * Teil 1: s01 = s0 + v * t
         * Teil 2: s0 = s01 + 0.5 * a * t²
         */
        // Teil 1 der Formel: s01 = s0 + v * t
        strecke.addition(Vektor.scalareMultiplikationProdukt(geschwindigkeitV, deltaTime));
        // Teil 2 der Formel: s0 = s01 + 0.5 * a * t²
        strecke.addition(Vektor.scalareMultiplikationProdukt(beschleunigungA, 0.5 * Math.pow(deltaTime, 2)));
        System.out.println("\nstrecke x: " + strecke.x);
        System.out.println("strecke y: " + strecke.y);

        // x Pixel sind ein Meter - Berechnung
        pixelZurMeter(strecke);

        // Geschwindigkeit v = v0 + a * t
        geschwindigkeitV.addition(Vektor.scalareMultiplikationProdukt(beschleunigungA, deltaTime));

        listPieceCircle.get(index).setGeschwindigkeitV(geschwindigkeitV);
        listPieceCircle.get(index).setBeschleunigungA(beschleunigungA);
        listPieceCircle.get(index).updateVorherigePosition();
        listPieceCircle.get(index).position.addition(new Vektor(strecke.x, strecke.y));
        listPieceCircle.get(index).vorherigeDeltaXY = listPieceCircle.get(index).deltaXY;
        listPieceCircle.get(index).updateDeltaXY();

        // Positioniert die Murmel nach der neuen Berechnung
        if(listPieceCircle.get(index).position.x > (ganzerFeld.getWidth() - listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter){ listPieceCircle.get(index).position.x = (ganzerFeld.getWidth() - listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter; listPieceCircle.get(index).getGeschwindigkeitV().x = 0.0; }
        else if(listPieceCircle.get(index).position.x < (150 + listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter){ listPieceCircle.get(index).position.x = (150 + listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter; listPieceCircle.get(index).getGeschwindigkeitV().x = 0.0; }
        if(listPieceCircle.get(index).position.y > (ganzerFeld.getHeight() - listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter){ listPieceCircle.get(index).position.y = (ganzerFeld.getHeight() - listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter; }
        else if(listPieceCircle.get(index).position.y < listPieceCircle.get(index).getRadius() / xPixelFürEinMeter ){ listPieceCircle.get(index).position.y = (listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter; listPieceCircle.get(index).getGeschwindigkeitV().y = 0.0; }
        listPieceCircle.get(index).draw(ganzerFeld.getHeight(), xPixelFürEinMeter);
    }

    //wandel in pixel/s
    public void pixelZurMeter(Vektor vektor){ vektor.scalareMultiplikationProdukt(xPixelFürEinMeter); }
}
