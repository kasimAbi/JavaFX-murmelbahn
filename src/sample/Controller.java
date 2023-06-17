package sample;

import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
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
    private Text text_richtung_gruene_murmel, text_gesch_gruene_murmel_x, text_gesch_gruene_murmel_y;

    @FXML
    private Circle blaue_murmel, joystick, gruene_murmel;

    @FXML
    private Rectangle startbahn, bahn1, bahn2, bahn3, bahn4;

    @FXML
    private ImageView image_play_button;

    @FXML
    private Button startButton, chosed_blue, chosed_green;

    int chosed = 0;

    @FXML
    private Line line;

    private double startGeschwindigkeit = 0.0, startwinkelKugel = 270.0, windbeschleunigungA = 0.0;
    private PieceCircle pieceCircleBlaueMurmel, pieceCircleJoystick, pieceCircleGrueneMurmel;
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

    private PieceCircle startPositionGrüneMurmel;
    private PieceCircle startPositionBlaueMurmel;

    // Energieverlust
    double k = 0.75;

    public void initial(){
        slider_gravitation.valueProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1){
                gravitation.y = -t1.doubleValue();
                label_gravitation.setText(df2.format(Math.abs(gravitation.y)) + " m/s²");
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
            }
        });

        slider_startwinkel.valueProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1){
                label_startwinkel.setText((t1.intValue() == 0.0 ? "" : "-") + t1.intValue() + "°");
                startbahn.setRotate(t1.intValue());
            }
        });

        pieceCircleJoystick = new PieceCircle(joystick.getLayoutX(), joystick.getLayoutY(), joystick.getRadius(), joystick, new Vektor());
        pieceCircleBlaueMurmel = new PieceCircle(blaue_murmel.getLayoutX() / xPixelFürEinMeter, blaue_murmel.getLayoutY() / xPixelFürEinMeter, blaue_murmel.getRadius(),blaue_murmel, new Vektor());
        listPieceCircle.add(pieceCircleBlaueMurmel);
        pieceCircleGrueneMurmel = new PieceCircle(gruene_murmel.getLayoutX() / xPixelFürEinMeter, gruene_murmel.getLayoutY() / xPixelFürEinMeter, gruene_murmel.getRadius(),gruene_murmel, new Vektor());
        listPieceCircle.add(pieceCircleGrueneMurmel);

        //listPieceCircle.get(0).gewicht = 30;

        startPositionBlaueMurmel = new PieceCircle(blaue_murmel.getLayoutX() / xPixelFürEinMeter, blaue_murmel.getLayoutY() / xPixelFürEinMeter, blaue_murmel.getRadius(),blaue_murmel, new Vektor());
        startPositionGrüneMurmel = new PieceCircle(gruene_murmel.getLayoutX() / xPixelFürEinMeter, gruene_murmel.getLayoutY() / xPixelFürEinMeter, gruene_murmel.getRadius(),gruene_murmel, new Vektor());

        for(int index = 0; index < listPieceCircle.size(); index++){
            drawCircle(index);
        }

        PieceRectangle pieceRectangle = new PieceRectangle(startbahn.getLayoutX(), startbahn.getLayoutY(), ((int) startbahn.getHeight()), ((int) startbahn.getWidth()), startbahn);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn1.getLayoutX(), bahn1.getLayoutY(), ((int) bahn1.getHeight()), ((int) bahn1.getWidth()), bahn1);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn2.getLayoutX(), bahn2.getLayoutY(), ((int) bahn2.getHeight()), ((int) bahn2.getWidth()), bahn2);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn3.getLayoutX(), bahn3.getLayoutY(), ((int) bahn3.getHeight()), ((int) bahn3.getWidth()), bahn3);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn4.getLayoutX(), bahn4.getLayoutY(), ((int) bahn4.getHeight()), ((int) bahn4.getWidth()), bahn4);
        listPieceRectangle.add(pieceRectangle);
    }

    public void uebernehmen(){
        Vektor direction = new Vektor(1, 0);
        direction.rotate(360 - startwinkelKugel);
        //if(listPieceCircle.get(0).getGeschwindigkeitV().länge() == 0.0 && startGeschwindigkeit == 0.0){
        //    listPieceCircle.get(0).setGeschwindigkeitV(direction);
        //}else{
        //    listPieceCircle.get(0).setGeschwindigkeitV(Vektor.scalareMultiplikationProdukt(direction,
        //            listPieceCircle.get(0).getGeschwindigkeitV().länge() == 0.0 ? startGeschwindigkeit : listPieceCircle.get(0).getGeschwindigkeitV().länge()));
        //}
        listPieceCircle.get(chosed).setGeschwindigkeitV(Vektor.scalareMultiplikationProdukt(direction, startGeschwindigkeit));
        zuruecksetzen(chosed);
    }

    @FXML
    public void resetten(){
        Vektor nullVektor = new Vektor(0, 0);
        for(int index = 0; index < listPieceCircle.size(); index++){
            listPieceCircle.get(index).setGeschwindigkeitV(nullVektor);
            listPieceCircle.get(index).setBeschleunigungA(nullVektor);
            listPieceCircle.get(index).vorherigeDeltaXY = new Vektor(nullVektor.x, nullVektor.y);
            zuruecksetzen(index);
        }
        listPieceCircle.get(0).position = new Vektor(startPositionBlaueMurmel.position.x, startPositionBlaueMurmel.position.y);
        listPieceCircle.get(1).position = new Vektor(startPositionGrüneMurmel.position.x, startPositionGrüneMurmel.position.y);

        gravitation = new Vektor(0, -9.81);
        label_gravitation.setText(df2.format(Math.abs(gravitation.y)) + " m/s²");
        slider_gravitation.setValue(9.81);

        windbeschleunigungA = 0.0;
        label_windstärke_richtung.setText(df1.format(windbeschleunigungA) + " m/s²");
        windRichtung.normalisierung();
        slider_windstärke_richtung.setValue(0.0);

        drawCircle(0);
        drawCircle(1);
    }

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
        startRichtung.setText((360 - winkelInt) + "°");
        startwinkelKugel = winkelInt;

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

        drawCircle(0);

        buttonPlay = false;
        startButton.setText("Start");
        File file = new File("src/images/resume.png");
        Image image = new Image(file.toURI().toString());
        image_play_button.setImage(image);

        showRichtungInLinie(line, 0);
        zuruecksetzen(0);
    }

    @FXML
    private void onBlaueMurmelReleased(MouseEvent event){ }

    @FXML
    private void onGrueneMurmelDragged(MouseEvent event){
        // Aktuelle Positionen setzen
        pieceCircleGrueneMurmel.position.x = event.getSceneX() / xPixelFürEinMeter;
        pieceCircleGrueneMurmel.position.y = (ganzerFeld.getHeight() - event.getSceneY()) / xPixelFürEinMeter;

        drawCircle(1);

        buttonPlay = false;
        startButton.setText("Start");
        File file = new File("src/images/resume.png");
        Image image = new Image(file.toURI().toString());
        image_play_button.setImage(image);
        zuruecksetzen(1);
    }

    @FXML
    private void onGrueneMurmelReleased(MouseEvent event){ }

    public void setChosed_blue(){
        chosed_blue.setDisable(true);
        chosed_green.setDisable(false);
        chosed = 0;
    }

    public void setChosed_green(){
        chosed_blue.setDisable(false);
        chosed_green.setDisable(true);
        chosed = 1;
    }

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
            collisionMurmelMurmel();
            for(int index = 0; index < listPieceCircle.size(); index++){
                isCollision(index);
                move(index);
            }
            showRichtungMurmeln();
            showGeschwindigkeit();
        }
    }

    private void showRichtungMurmeln(){
        text_richtung_blaue_murmel.setText("Richtung: " + (360 - listPieceCircle.get(0).getRichtungPositiv()) + "°");
        showRichtungInLinie(line, 0);
        text_richtung_gruene_murmel.setText("Richtung: " + (360 - listPieceCircle.get(1).getRichtungPositiv()) + "°");
    }

    private void showRichtungInLinie(Line line1, int index){
        // X-Position der Murmel
        double x = listPieceCircle.get(index).getCircle().getLayoutX();
        // Y-Position der Murmel
        double y = listPieceCircle.get(index).getCircle().getLayoutY();
        // Richtung der Kugel
        double richtung = Math.toRadians(-(360 - listPieceCircle.get(index).getRichtungPositiv()));
        // Linie um den oberen Endpunkt rotieren
        line1.setStartX(x);
        line1.setStartY(y);
        double x_rotiert = 10 * cos(richtung);
        double y_rotiert = 10 * sin(richtung);
        line1.setEndX(x + x_rotiert);
        line1.setEndY(y + y_rotiert);
    }

    private void showGeschwindigkeit(){
        text_gesch_blaue_murmel_x.setText("Velocity x: " + df2.format(Math.abs(listPieceCircle.get(0).getGeschwindigkeitV().x)) + "m/s");
        text_gesch_blaue_murmel_y.setText("Velocity y: " + df2.format(Math.abs(listPieceCircle.get(0).getGeschwindigkeitV().y)) + "m/s");

        text_gesch_gruene_murmel_x.setText("Velocity x: " + df2.format(Math.abs(listPieceCircle.get(1).getGeschwindigkeitV().x)) + "m/s");
        text_gesch_gruene_murmel_y.setText("Velocity y: " + df2.format(Math.abs(listPieceCircle.get(1).getGeschwindigkeitV().y)) + "m/s");
    }

    private void move(int index){
        Vektor beschleunigungA = new Vektor(0, 0);
        Vektor geschwindigkeitV = listPieceCircle.get(index).getGeschwindigkeitV();
        Vektor strecke = new Vektor();

        // Wenn die Kugel rollt
        if (listPieceCircle.get(index).isRolling){
            //System.out.println("es rollt: " + index);
            // Winkel richtung Rechteck
            double rectangleWinkel = 360.0 - listPieceRectangle.get(listPieceCircle.get(index).rollingDetails[1]).getRectangle().getRotate();
            double alpha = Math.toRadians(rectangleWinkel);

            // Winkel entgegen Rechteck
            double rectangleWinkelEntgegen = (rectangleWinkel - 180.0) % 360.0;
            double alpha_Entgegen = Math.toRadians(rectangleWinkelEntgegen);

            // Winkel Senkrecht zum Rechteck
            double rectangleWinkelSenkrecht = (rectangleWinkel - 90.0) % 360.0;
            double alpha_N = Math.toRadians(rectangleWinkelSenkrecht);

            // Winkel Senkrecht zum Rechteck (wenn Rechteck Oberfläche nach unten zeigt)
            double rectangleWinkelSenkrecht_entgegen = (rectangleWinkel + 90.0) % 360.0;
            double alpha_N_entgegen = Math.toRadians(rectangleWinkelSenkrecht_entgegen);

            //System.out.print("alpha: " + rectangleWinkel);
            //System.out.println(", alpha entgegen: " + rectangleWinkelEntgegen);
            //System.out.print("alpha senkrecht: " + rectangleWinkelSenkrecht);
            //System.out.println(", alpha senkrecht entgegen: " + rectangleWinkelSenkrecht_entgegen);

            Vektor windA = new Vektor(0, 0);
            if(windbeschleunigungA < 0.0){
                windA = new Vektor(Math.abs(windbeschleunigungA) * cos(alpha_Entgegen), 0);
            }else {
                windA = new Vektor(Math.abs(windbeschleunigungA) * cos(alpha), 0);
            }

            /**
             * Beschleunigungskomponente
             * Hangabtriebskraft-Komponente der Beschleunigung: a_H = Gravitation * sin(alpha)
             * Normalkraft-Komponente der Beschleunigung: a_N = gravitation * cos(alpha)
             *
             * Bremsung durch die Reibung
             * μ = Reibungskoeffizient
             * Reibungskraft-Komponente der Beschleunigung: a_R = (Gewicht * cos(alpha)) * μ
             * Resultierende Beschleunigung: a_ges = a_H - a_R
             */
                //System.out.println("rollt runter");
                // a_H = Gravitation * sin(alpha)
                double a_H = gravitation.y * Math.sin(alpha);
                // a_H in Vektor umwandeln - richtung der Kugel
                Vektor a_H_Vektor = new Vektor(a_H * cos(alpha), a_H * sin(alpha));

                // a_N = gravitation * cos(alpha)
                double a_N = gravitation.y * Math.cos(alpha);
                // a_H in Vektor umwandeln - Senkrecht zum Rechteck
                Vektor a_N_Vektor = new Vektor(a_N * cos(alpha_N), a_N * sin(alpha_N));
                if(rectangleWinkel > 270){
                    a_N_Vektor = new Vektor(a_N * cos(alpha_N_entgegen), a_N * sin(alpha_N_entgegen));
                }

                // a_R = (Gewicht * cos(alpha)) * μ
                Vektor a_R_Vektor = Vektor.scalareMultiplikationProdukt(a_N_Vektor, listPieceCircle.get(index).reibungskoeffizient);

                // ar = velocity der kugel
                Vektor a_R_Neu = new Vektor(geschwindigkeitV.x, geschwindigkeitV.y);
                // ar normalisiert
                a_R_Neu.normalisierung();
                // ar * |aRVektor| * -1
                a_R_Neu.scalareMultiplikationProdukt(a_R_Vektor.länge() * -1);

                Vektor a_H_Neu = new Vektor(geschwindigkeitV.x, geschwindigkeitV.y);
                a_H_Neu.normalisierung();
                if(geschwindigkeitV.y < 0){
                    a_H_Neu.scalareMultiplikationProdukt(a_H_Vektor.länge());
                }else {
                    a_H_Neu.scalareMultiplikationProdukt(a_H_Vektor.länge() * -1);
                }

                Vektor a_Ges = Vektor.addition(a_H_Neu, a_R_Neu);
                //System.out.println("ges: " + a_Ges.länge());

                if ((a_Ges.länge() < 0.2 || Double.isNaN(a_Ges.länge())) && (rectangleWinkel == 0 || rectangleWinkel == 180 || rectangleWinkel == 360)){
                    a_Ges.x = 0;
                    a_Ges.y = 0;
                    geschwindigkeitV.x = 0;
                    geschwindigkeitV.y = 0;
                    listPieceCircle.get(index).getGeschwindigkeitV().scalareMultiplikationProdukt(0);
                }else if(Math.abs(geschwindigkeitV.x) < 0.02 && (rectangleWinkel == 0 || rectangleWinkel == 180 || rectangleWinkel == 360)){
                    a_Ges.x = 0;
                    a_Ges.y = 0;
                    geschwindigkeitV.x = 0;
                    geschwindigkeitV.y = 0;
                    listPieceCircle.get(index).getGeschwindigkeitV().scalareMultiplikationProdukt(0);
                }

                beschleunigungA = a_Ges;
        }/* Wenn die Kugel nicht rollt */ else {
            //System.out.println("Freier Fall");
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

        // x Pixel sind ein Meter - Berechnung
        pixelZurMeter(strecke);

        // Geschwindigkeit v = v0 + a * t
        geschwindigkeitV.addition(Vektor.scalareMultiplikationProdukt(beschleunigungA, deltaTime));

        listPieceCircle.get(index).setGeschwindigkeitV(geschwindigkeitV);
        listPieceCircle.get(index).setBeschleunigungA(beschleunigungA);
        listPieceCircle.get(index).updateVorherigePosition();
        listPieceCircle.get(index).position.addition(strecke);
        listPieceCircle.get(index).vorherigeDeltaXY = new Vektor(listPieceCircle.get(index).deltaXY.x, listPieceCircle.get(index).deltaXY.y);
        listPieceCircle.get(index).updateDeltaXY();

        drawCircle(index);
    }

    //wandel in pixel/s
    public void pixelZurMeter(Vektor vektor){ vektor.scalareMultiplikationProdukt(xPixelFürEinMeter); }

    public void drawCircle(int index){
        // Positioniert die Murmel nach der neuen Berechnung
        if(listPieceCircle.get(index).position.x > (ganzerFeld.getWidth() - listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter){ listPieceCircle.get(index).position.x = (ganzerFeld.getWidth() - listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter; listPieceCircle.get(index).getGeschwindigkeitV().x *= -1; }
        else if(listPieceCircle.get(index).position.x < (150 + listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter){ listPieceCircle.get(index).position.x = (150 + listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter; listPieceCircle.get(index).getGeschwindigkeitV().x *= -1; }
        if(listPieceCircle.get(index).position.y > (ganzerFeld.getHeight() - listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter){ listPieceCircle.get(index).position.y = (ganzerFeld.getHeight() - listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter; }
        else if(listPieceCircle.get(index).position.y < listPieceCircle.get(index).getRadius() / xPixelFürEinMeter ){ listPieceCircle.get(index).position.y = (listPieceCircle.get(index).getRadius()) / xPixelFürEinMeter; listPieceCircle.get(index).getGeschwindigkeitV().y *= -1; }
        listPieceCircle.get(index).draw(ganzerFeld.getHeight(), xPixelFürEinMeter);
    }

    public void collisionMurmelMurmel() {
        // Erste Murmel
        PieceCircle murmel1 = listPieceCircle.get(0);
        double murmel1X = murmel1.position.x;
        double murmel1Y = murmel1.position.y;
        double murmel1Radius = murmel1.getRadius() / xPixelFürEinMeter;
        double m1 = murmel1.gewicht;

        // Zweite Murmel
        PieceCircle murmel2 = listPieceCircle.get(1);
        double murmel2X = murmel2.position.x;
        double murmel2Y = murmel2.position.y;
        double murmel2Radius = murmel2.getRadius() / xPixelFürEinMeter;
        double m2 = murmel2.gewicht;

        // Berechnung der Abstände
        double distanceX = murmel2X - murmel1X;
        double distanceY = murmel2Y - murmel1Y;
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        double deltaMurmel1X = murmel1.vorherigePosition.x;
        double deltaMurmel1Y = murmel1.vorherigePosition.y;

        double deltaMurmel2X = murmel2.vorherigePosition.x;
        double deltaMurmel2Y = murmel2.vorherigePosition.y;

        double deltaDistanzX = deltaMurmel2X - deltaMurmel1X;
        double deltaDistanzY = deltaMurmel2Y - deltaMurmel1Y;
        double deltaDistance = Math.sqrt(deltaDistanzX * deltaDistanzX + deltaDistanzY * deltaDistanzY);

        //System.out.println("distanz: " + distance + ", deltaDitanz: " + deltaDistance);

        // Überlappung feststellen
        if (distance < (murmel1Radius + murmel2Radius) && distance < deltaDistance) {
            for (int index = 0; index < listPieceCircle.size(); index++){
                listPieceCircle.get(index).isRolling = false;
                listPieceCircle.get(index).isCollision = false;
            }

            // Kollisionswinkel berechnen
            double collisionAngle = -Math.atan2(distanceY, distanceX);
            Vektor normalVector = new Vektor(-Math.cos(collisionAngle), Math.sin(collisionAngle));

            // Energieverlust berechnen
            murmel1.getGeschwindigkeitV().scalareMultiplikationProdukt(k);
            murmel2.getGeschwindigkeitV().scalareMultiplikationProdukt(k);

            Vektor v_parallel_1 = Vektor.projektion(murmel1.getGeschwindigkeitV(), normalVector);
            Vektor v_parallel_2 = Vektor.projektion(murmel2.getGeschwindigkeitV(), normalVector);

            // v_senkrecht = v - v_parallel
            Vektor v_senkrecht_1 = Vektor.subtraktion(murmel1.getGeschwindigkeitV(), v_parallel_1);
            Vektor v_senkrecht_2 = Vektor.subtraktion(murmel2.getGeschwindigkeitV(), v_parallel_2);


            // Berechnung der Massenverhältnisse
            double massSum = m1 + m2;

            /**
             * Formel von Wikipedia: https://de.wikipedia.org/wiki/Sto%C3%9F_(Physik)#Elastischer_Sto%C3%9F
             * Diese Formel gilt aber nur für Kollisionen die 1 Dimensional sind. So muss sie erweitert werden.
             * v1'_parallel = (2 * (m1 * v1_parallel + m2 * v2_parallel) / (m1 + m2)) - v1_parallel
             * v2'_parallel = (2 * (m1 * v1_parallel + m2 * v2_parallel) / (m1 + m2)) - v2_parallel
             *
             * Erweiterung:
             * v1' = v1'_parallel + v1_senkrecht
             * v2' = v2'_parallel + v2_senkrecht
             */

            // m1v1 = m1 * v1
            Vektor m1v1 = Vektor.scalareMultiplikationProdukt(v_parallel_1, m1);
            // m2v2 = m2 * v2
            Vektor m2v2 = Vektor.scalareMultiplikationProdukt(v_parallel_2, m2);

            // Rechenweg_teil_1 =  m1v1 + m2v2
            Vektor v_strich_parallel = Vektor.addition(m1v1, m2v2);
            // Rechenweg_teil_2 = Rechenweg_teil_1 / (m1 + m2)
            v_strich_parallel.scalareMultiplikationProdukt(1 / massSum);
            // Rechenweg_teil_3 = Rechenweg_teil_2 * 2
            v_strich_parallel.scalareMultiplikationProdukt(2.0);

            // v1'_parallel = Rechenweg_teil_3 - v1_parallel
            Vektor v1_strich_parallel = Vektor.subtraktion(v_strich_parallel, v_parallel_1);
            // Nun ist die Formel für v1'_parallel komplett: v1'_parallel = (2 * (m1 * v1_parallel + m2 * v2_parallel) / (m1 + m2)) - v1_parallel
            // Formelerweiterung: v1' = v1'_parallel + v1_senkrecht
            Vektor v1_strich = Vektor.addition(v1_strich_parallel, v_senkrecht_1);

            // v1'_parallel = Rechenweg_teil_3 - v2_parallel
            Vektor v2_strich_parallel = Vektor.subtraktion(v_strich_parallel, v_parallel_2);
            // Nun ist die Formel für v1'_parallel komplett: v2'_parallel = (2 * (m1 * v1_parallel + m2 * v2_parallel) / (m1 + m2)) - v2_parallel
            // Formelerweiterung: v2' = v2'_parallel + v2_senkrecht
            Vektor v2_strich = Vektor.addition(v2_strich_parallel, v_senkrecht_2);


            listPieceCircle.get(0).setGeschwindigkeitV(v1_strich);
            listPieceCircle.get(1).setGeschwindigkeitV(v2_strich);



            /*
            // Kollisionsnormalen berechnen
            double collisionNormalX = distanceX / distance;
            double collisionNormalY = distanceY / distance;

            // Relativgeschwindigkeit berechnen
            murmel1.getGeschwindigkeitV().scalareMultiplikationProdukt(k);
            murmel2.getGeschwindigkeitV().scalareMultiplikationProdukt(k);
            double relativeVelocityX = murmel2.getGeschwindigkeitV().x - murmel1.getGeschwindigkeitV().x;
            double relativeVelocityY = murmel2.getGeschwindigkeitV().y - murmel1.getGeschwindigkeitV().y;

            // Projektion der Relativgeschwindigkeit auf die Kollisionsnormalen
            double normalRelativeVelocity = relativeVelocityX * collisionNormalX + relativeVelocityY * collisionNormalY;

            double restitution = 1.0;

            // Kollisionsimpuls berechnen
            double impulseMagnitude = (-(1 + restitution) * normalRelativeVelocity) / (1 / murmel1Mass + 1 / murmel2Mass);
            double impulseX = impulseMagnitude * collisionNormalX;
            double impulseY = impulseMagnitude * collisionNormalY;

            // Kollisionsimpuls auf die Murmeln anwenden
            murmel1.applyImpulse(new Vektor(-impulseX, -impulseY));
            murmel2.applyImpulse(new Vektor(impulseX, impulseY));

            // Korrektur der Überlappung (Separating Axis Theorem)
            double overlap = (murmel1Radius + murmel2Radius) - distance;
            double overlapX = overlap * collisionNormalX;
            double overlapY = overlap * collisionNormalY;
            double totalInverseMass = 1 / murmel1Mass + 1 / murmel2Mass;
            double correctionX = overlapX * (1 / murmel1Mass) / totalInverseMass;
            double correctionY = overlapY * (1 / murmel1Mass) / totalInverseMass;
            murmel1.position.x -= correctionX;
            murmel1.position.y -= correctionY;
            murmel2.position.x += correctionX;
            murmel2.position.y += correctionY;
            */
        }
    }

    public boolean isCollision(int index) {
        // jedes Rechteck wird mit jedem Kugel verglichen
        for (int indexRectangle = 0; indexRectangle < listPieceRectangle.size(); indexRectangle++) {
            // mittelpunkt des rechtecks übernehmen, da es den startwert von links unten nimmt (x und y positionen).
            double[] vektorRechteckMittelpunkt = new double[]{(listPieceRectangle.get(indexRectangle).getX() +
                    listPieceRectangle.get(indexRectangle).getB() / 2.0), (listPieceRectangle.get(indexRectangle).getY() +
                    listPieceRectangle.get(indexRectangle).getA() / 2.0)};
            // winkel des rechtecks bekommen.
            double rechteckRotation = listPieceRectangle.get(indexRectangle).getWinkel();

            // Tatsächlichen Abstand von Rechteck zur Kugel nach der Höhe und breite messen
            double tatsächlicherAbstandHöhe = abstandKugelZumMittelLinie(vektorRechteckMittelpunkt, rechteckRotation, listPieceCircle.get(index));
            double tatsächlicherAbstandBreite = abstandKugelZumMittelLinie(vektorRechteckMittelpunkt, rechteckRotation +
                    90, listPieceCircle.get(index));

            // Abstand messen, bei der sich die Kugel mit dem Rechteck berühren würden
            double abstandHöheBerührung = listPieceCircle.get(index).getRadius() + listPieceRectangle.get(indexRectangle).getA() / 2.0;
            double abstandBreiteBerührung = listPieceCircle.get(index).getRadius() + listPieceRectangle.get(indexRectangle).getB() / 2.0;

            // Prüfen ob die Entfernung des Murmels über die Höhe bzw. Breite des Rechtecks ist.
            if (tatsächlicherAbstandHöhe < abstandHöheBerührung && tatsächlicherAbstandBreite < abstandBreiteBerührung) {
                // Prüfen ob die Kugel sich an den Eckpunkten befinden
                double cornerDistSq = Math.pow(Math.max(tatsächlicherAbstandHöhe - listPieceRectangle.get(indexRectangle).getA() / 2.0, 0), 2) +
                        Math.pow(Math.max(tatsächlicherAbstandBreite - listPieceRectangle.get(indexRectangle).getB() / 2.0, 0), 2);
                if (cornerDistSq <= Math.pow(listPieceCircle.get(index).getRadius(), 2)) {
                    if (!listPieceCircle.get(index).isRolling) {
                        // Normalenvektor berechnen (-rechteckRotation, weil Winkel in Uhrzeigersinn ist).
                        Vektor normalVector = calculateNormalVector(-rechteckRotation);

                        // Projektion von v auf Normalenvektor
                        // v_parallel = v
                        Vektor v_parallel = Vektor.projektion(listPieceCircle.get(index).getGeschwindigkeitV(), normalVector);

                        // Formel anwenden: v'_parallel = -v_parallel
                        Vektor v_strich_parallel = Vektor.scalareMultiplikationProdukt(v_parallel, -1);
                        v_strich_parallel.scalareMultiplikationProdukt(k);

                        // v_senkrecht = v - v_parallel
                        Vektor v_senkrecht = Vektor.subtraktion(listPieceCircle.get(index).getGeschwindigkeitV(), v_parallel);

                        // v'_senkrecht bleibt unverändert: v'_senkrecht = v_senkrecht
                        Vektor v_strich_senkrecht = v_senkrecht;
                        // Vektoren zusammensetzen: v' = v'_parallel + v'_senkrecht
                        Vektor v_strich = new Vektor(v_strich_parallel.x, v_strich_parallel.y);
                        v_strich.addition(v_strich_senkrecht);
                        Vektor new_v = new Vektor(v_strich.x, v_strich.y);

                        double tatsächlicherAbstandHöheOhneAbs = abstandKugelZumMittelLinieOhneAbs(vektorRechteckMittelpunkt, rechteckRotation, listPieceCircle.get(index));
                        double tatsächlicherAbstandBreiteOhneAbs = abstandKugelZumMittelLinieOhneAbs(vektorRechteckMittelpunkt, rechteckRotation +
                                90, listPieceCircle.get(index));

                        Vektor normaleNachOben = calculateNormalVector(rechteckRotation);
                        Vektor normaleNachRechts = calculateNormalVector(rechteckRotation + 270);
                        Vektor normaleNachUnten = calculateNormalVector(rechteckRotation + 180);
                        Vektor normaleNachLinks = calculateNormalVector(rechteckRotation + 90);
                        Point2D normalePO = new Point2D(normaleNachOben.x, -normaleNachOben.y);
                        Point2D normalePU = new Point2D(normaleNachUnten.x, -normaleNachUnten.y);
                        Point2D normalePR = new Point2D(normaleNachRechts.x, -normaleNachRechts.y);
                        Point2D normalePL = new Point2D(normaleNachLinks.x, -normaleNachLinks.y);
                        double angle = 0.0;

                        if(Math.abs(Math.abs(abstandBreiteBerührung) - Math.abs(tatsächlicherAbstandBreite)) <= 5){
                            if (tatsächlicherAbstandBreiteOhneAbs < 0.0){
                                angle = new Point2D(listPieceCircle.get(index).getGeschwindigkeitV().x, listPieceCircle.get(index).getGeschwindigkeitV().y).angle(normalePR);
                            }else if (tatsächlicherAbstandBreiteOhneAbs > 0.0){
                                angle = new Point2D(listPieceCircle.get(index).getGeschwindigkeitV().x, listPieceCircle.get(index).getGeschwindigkeitV().y).angle(normalePL);
                            }
                        }else if (tatsächlicherAbstandHöheOhneAbs > 0.0){
                            angle = new Point2D(listPieceCircle.get(index).getGeschwindigkeitV().x, listPieceCircle.get(index).getGeschwindigkeitV().y).angle(normalePO);
                        }else if (tatsächlicherAbstandHöheOhneAbs < 0.0){
                            angle = new Point2D(listPieceCircle.get(index).getGeschwindigkeitV().x, listPieceCircle.get(index).getGeschwindigkeitV().y).angle(normalePU);
                        }

                        // Wenn v'_parallel eine Länge kleiner 0.1 hat, um ins rollen überzugehen
                        if (v_strich_parallel.länge() < 0.1 && !(Math.abs(Math.abs(abstandBreiteBerührung) - Math.abs(tatsächlicherAbstandBreite)) <= 5)) {
                            //System.out.println("rechteckrotation: " + rechteckRotation + ", tatH: " + tatsächlicherAbstandHöheOhneAbs);
                            if((rechteckRotation < 90 && tatsächlicherAbstandHöheOhneAbs > 0.0) || (rechteckRotation > 90 && tatsächlicherAbstandHöheOhneAbs < 0.0)){
                                listPieceCircle.get(index).isRolling = true;
                                // Der Code ist erstmal nicht wichtig, sagt nur das die Kugel rollt (Doppelt gemoppelt, aber eventuell später brauchbar)
                                listPieceCircle.get(index).rollingDetails[0] = 1;
                                // Auf welchem Rechteck die Kugel rollt speichern
                                listPieceCircle.get(index).rollingDetails[1] = indexRectangle;
                                listPieceCircle.get(index).setGeschwindigkeitV(v_senkrecht);
                                //System.out.println("rollt");
                            }

                        } else if(angle <= 90 || Double.isNaN(angle)) /* Wenn Kugel abprallt */ {
                            //System.out.println("seite");

                            if(Math.abs(Math.abs(abstandBreiteBerührung) - Math.abs(tatsächlicherAbstandBreite)) <= 5){
                                new_v.scalareMultiplikationProdukt(-1);
                            }
                            // aktualisierung der Geschwindigkeit des kugels
                            listPieceCircle.get(index).setGeschwindigkeitV(new_v);
                        }
                    }else{
                        if(listPieceCircle.get(index).rollingDetails[1] != indexRectangle){
                            zuruecksetzen(index);
                            indexRectangle--;
                        }
                    }
                }else /* Wenn die Kugel aufhört zu rollen */ {
                    zuruecksetzen(index);
                    //System.out.println("ecke");

                    double tatsächlicherAbstandHöheOhneAbs = abstandKugelZumMittelLinieOhneAbs(vektorRechteckMittelpunkt, rechteckRotation, listPieceCircle.get(index));
                    double tatsächlicherAbstandBreiteOhneAbs = abstandKugelZumMittelLinieOhneAbs(vektorRechteckMittelpunkt, rechteckRotation +
                            90, listPieceCircle.get(index));

                    boolean oben_unten = false;
                    boolean links_rechts = false;

                    if (tatsächlicherAbstandBreiteOhneAbs < 0.0){
                        links_rechts = true;
                    }else if (tatsächlicherAbstandBreiteOhneAbs > 0.0){
                        links_rechts = false;
                    }
                    if (tatsächlicherAbstandHöheOhneAbs > 0.0){
                        oben_unten = true;
                    }else if (tatsächlicherAbstandHöheOhneAbs < 0.0){
                        oben_unten = false;
                    }
                    Vektor eckpunkt = new Vektor(vektorRechteckMittelpunkt[0], vektorRechteckMittelpunkt[1]);
                    if(oben_unten && links_rechts){
                         Vektor r1 = new Vektor(cos(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0,
                                sin(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0);
                        Vektor r2 = new Vektor(cos(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0,
                                sin(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0);
                        r1.scalareMultiplikationProdukt(-1);
                        r2.scalareMultiplikationProdukt(-1);
                        r1.addition(r2);
                        eckpunkt.addition(r1);
                    }else if(!oben_unten && links_rechts){
                        Vektor r1 = new Vektor(cos(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0,
                                sin(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0);
                        Vektor r2 = new Vektor(cos(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0,
                                sin(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0);
                        r1.scalareMultiplikationProdukt(-1);
                        r1.addition(r2);
                        eckpunkt.addition(r1);
                    }else if(oben_unten && !links_rechts){
                        Vektor r1 = new Vektor(cos(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0,
                                sin(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0);
                        Vektor r2 = new Vektor(cos(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0,
                                sin(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0);
                        r1.scalareMultiplikationProdukt(-1);
                        r1.addition(r2);
                        eckpunkt.subtraktion(r1);
                    }else if(!oben_unten && !links_rechts){
                        Vektor r1 = new Vektor(cos(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0,
                                sin(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0);
                        Vektor r2 = new Vektor(cos(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0,
                                sin(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0);
                        r1.scalareMultiplikationProdukt(-1);
                        r2.scalareMultiplikationProdukt(-1);
                        r1.addition(r2);
                        eckpunkt.subtraktion(r1);
                    }

                    Vektor circle = new Vektor(listPieceCircle.get(index).getCircle().getLayoutX(), listPieceCircle.get(index).getCircle().getLayoutY());
                    Vektor normale = Vektor.subtraktion(eckpunkt, circle);
                    normale.y *= -1;
                    normale.normalisierung();
                    //System.out.println("normale x: " + normale.x + ", " + normale.y);
                    Point2D normaleP = new Point2D(normale.x, normale.y);
                    Point2D velocity = new Point2D(listPieceCircle.get(index).getGeschwindigkeitV().x, listPieceCircle.get(index).getGeschwindigkeitV().y);
                    double angle = normaleP.angle(velocity);
                    //System.out.println("angle: " + angle);

                    // Normalenvektor berechnen (-rechteckRotation, weil Winkel in Uhrzeigersinn ist).
                    Vektor normalVector = normale;

                    // Projektion von v auf Normalenvektor
                    // v_parallel = v
                    Vektor v_parallel = Vektor.projektion(listPieceCircle.get(index).getGeschwindigkeitV(), normalVector);

                    // Formel anwenden: v'_parallel = -v_parallel
                    Vektor v_strich_parallel = Vektor.scalareMultiplikationProdukt(v_parallel, -1);
                    v_strich_parallel.scalareMultiplikationProdukt(k);

                    // v_senkrecht = v - v_parallel
                    Vektor v_senkrecht = Vektor.subtraktion(listPieceCircle.get(index).getGeschwindigkeitV(), v_parallel);

                    // v'_senkrecht bleibt unverändert: v'_senkrecht = v_senkrecht
                    Vektor v_strich_senkrecht = v_senkrecht;
                    // Vektoren zusammensetzen: v' = v'_parallel + v'_senkrecht
                    Vektor v_strich = new Vektor(v_strich_parallel.x, v_strich_parallel.y);
                    v_strich.addition(v_strich_senkrecht);
                    Vektor new_v = new Vektor(v_strich.x, v_strich.y);

                    // Aktualisieren Sie die Geschwindigkeit des Kreisform-Objekts in der Liste
                    if (angle <= 90) {
                        listPieceCircle.get(index).setGeschwindigkeitV(new_v);
                        return true;
                    }
                }
            }else{
                if(listPieceCircle.get(index).rollingDetails[1] == indexRectangle){
                    zuruecksetzen(index);
                }
            }
        }
        return false;
    }

    public void zuruecksetzen(int index){
        listPieceCircle.get(index).rollingDetails[0] = 0;
        listPieceCircle.get(index).rollingDetails[1] = -1;
        listPieceCircle.get(index).isRolling = false;
        listPieceCircle.get(index).isCollision = false;
    }

    public Vektor calculateNormalVector(double rectangleRotation) {
        // Berechnen Sie den Normalenvektor basierend auf der Rotation des Rechtecks
        double[] normalVector = new double[2];
        normalVector[0] = -Math.sin(Math.toRadians(rectangleRotation));
        normalVector[1] = Math.cos(Math.toRadians(rectangleRotation));

        return new Vektor(normalVector[0], normalVector[1]);
    }

    public double abstandKugelZumMittelLinie(double[] vektorRechteckMittelpunkt, double rechteckRotation, PieceCircle pieceCircle){
        // Erstellt den Mittelpunkt des Kugels
        double[] vektorMurmelMittelpunkt = new double[]{pieceCircle.getCircle().getLayoutX(), pieceCircle.getCircle().getLayoutY()};

        // Zieht eine Linie auf dem Rechteck
        double geradeX = Math.cos(Math.toRadians(rechteckRotation));
        double geradeY = Math.sin(Math.toRadians(rechteckRotation));

        // Die Linie als Vektor
        double[] vektorGeraden = new double[]{geradeX, geradeY};

        /* Abstand zwischen dem Mittelpunkt der Kugel und Mittelpunkt des Rechtecks berechnen, indem die Vektoren
        miteinander subtrahiert werden. */
        double[] verbindungsvektor = vektorenSubtraktion(vektorMurmelMittelpunkt, vektorRechteckMittelpunkt);

        // Abstand zwischen dem Punkt und der Linie berechnen
        double kreuzprodukt = kreuzprodukt(verbindungsvektor, vektorGeraden);

        // Abstand von der Kugel zur Mittellinie zurückgeben.
        // |a|/sqrt(x²+y²) = |kreuzprodukt|/wurzel(x-gerade² + y-gerade²)
        return Math.abs(kreuzprodukt)/Math.sqrt(Math.pow(vektorGeraden[0], 2) + Math.pow(vektorGeraden[1], 2));
    }


    public double abstandKugelZumMittelLinieOhneAbs(double[] vektorRechteckMittelpunkt, double rechteckRotation, PieceCircle pieceCircle){
        // Erstellt den Mittelpunkt des Kugels
        double[] vektorMurmelMittelpunkt = new double[]{pieceCircle.getCircle().getLayoutX(), pieceCircle.getCircle().getLayoutY()};

        // Zieht eine Linie auf dem Rechteck
        double geradeX = Math.cos(Math.toRadians(rechteckRotation));
        double geradeY = Math.sin(Math.toRadians(rechteckRotation));

        // Die Linie als Vektor
        double[] vektorGeraden = new double[]{geradeX, geradeY};

        /* Abstand zwischen dem Mittelpunkt der Kugel und Mittelpunkt des Rechtecks berechnen, indem die Vektoren
        miteinander subtrahiert werden. */
        double[] verbindungsvektor = vektorenSubtraktion(vektorMurmelMittelpunkt, vektorRechteckMittelpunkt);

        // Abstand zwischen dem Punkt und der Linie berechnen
        double kreuzprodukt = kreuzprodukt(verbindungsvektor, vektorGeraden);

        // Abstand von der Kugel zur Mittellinie zurückgeben.
        // |a|/sqrt(x²+y²) = |kreuzprodukt|/wurzel(x-gerade² + y-gerade²)
        return (kreuzprodukt)/Math.sqrt(Math.pow(vektorGeraden[0], 2) + Math.pow(vektorGeraden[1], 2));
    }

    public double[] vektorenSubtraktion(double[] vek_1, double[] vek_2){
        vek_1[0] -= vek_2[0];
        vek_1[1] -= vek_2[1];
        return vek_1;
    }

    public double kreuzprodukt(double[] vek_1, double[] vek_2){
        return vek_1[0] * vek_2[1] - vek_2[0] * vek_1[1];
    }
}
