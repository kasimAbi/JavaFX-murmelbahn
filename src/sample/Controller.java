package sample;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
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
    private Slider slider_gravitation, slider_windstärke_richtung, slider_startgeschwindigkeit;

    @FXML
    private Slider slider_rechteckWinkel1, slider_gewicht, slider_rechteckLaenge;

    @FXML
    private Label label_gravitation, label_windstärke_richtung, label_startgeschwindigkeit, label_startwinkel, status_text, startRichtung, label_gewicht;

    @FXML
    private Text text_richtung_blaue_murmel, text_gesch_blaue_murmel_x, text_gesch_blaue_murmel_y;
    @FXML
    private Text text_richtung_gruene_murmel, text_gesch_gruene_murmel_x, text_gesch_gruene_murmel_y;

    @FXML
    private Circle blaue_murmel, joystick, gruene_murmel;

    @FXML
    private Rectangle startbahn, bahn1, bahn2, bahn3, bahn4, bahn5, bahn6, bahn7, bahn8, bahn9, bahn10, bahn11, bahn12, bahn13, bahn14, bahn15, bahn16, bahn17, bahn18, bahn19, bahn20;

    @FXML
    private ImageView image_play_button;

    @FXML
    private Button startButton, chosed_blue, chosed_green;

    int chosed = 0, chosedRectange = 0, addedRectange = 0;

    @FXML
    private Line line;

    private double startGeschwindigkeit = 0.0, startwinkelKugel = 270.0, windbeschleunigungA = 0.0;
    private PieceCircle pieceCircleBlaueMurmel, pieceCircleJoystick, pieceCircleGrueneMurmel;
    private ArrayList<PieceCircle> listPieceCircle = new ArrayList<PieceCircle>();
    private ArrayList<PieceRectangle> listPieceRectangle = new ArrayList<PieceRectangle>();
    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static DecimalFormat df1 = new DecimalFormat("#.#");
    private final int fpx = 1000;

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

    public void initial() {
        slider_gravitation.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                gravitation.y = -t1.doubleValue();
                label_gravitation.setText(df2.format(Math.abs(gravitation.y)) + " m/s²");
            }
        });

        slider_windstärke_richtung.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (Math.abs(t1.doubleValue()) < 0.2) {
                    windbeschleunigungA = 0.0;
                } else {
                    windbeschleunigungA = t1.doubleValue();
                }
                label_windstärke_richtung.setText(df1.format(windbeschleunigungA) + " m/s²");
                windRichtung.normalisierung();
            }
        });

        slider_startgeschwindigkeit.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                startGeschwindigkeit = t1.doubleValue();
                label_startgeschwindigkeit.setText(df2.format(startGeschwindigkeit) + " m/s");
            }
        });

        slider_gewicht.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                label_gewicht.setText(df2.format(t1.doubleValue()) + " kg");
            }
        });

        pieceCircleJoystick = new PieceCircle(joystick.getLayoutX(), joystick.getLayoutY(), joystick.getRadius(), joystick, new Vektor());
        pieceCircleBlaueMurmel = new PieceCircle(blaue_murmel.getLayoutX() / xPixelFürEinMeter, blaue_murmel.getLayoutY() / xPixelFürEinMeter, blaue_murmel.getRadius(), blaue_murmel, new Vektor());
        listPieceCircle.add(pieceCircleBlaueMurmel);
        pieceCircleGrueneMurmel = new PieceCircle(gruene_murmel.getLayoutX() / xPixelFürEinMeter, gruene_murmel.getLayoutY() / xPixelFürEinMeter, gruene_murmel.getRadius(), gruene_murmel, new Vektor());
        listPieceCircle.add(pieceCircleGrueneMurmel);

        //listPieceCircle.get(0).setRadius(20);
        //listPieceCircle.get(0).getCircle().setRadius(20);
        //listPieceCircle.get(0).gewicht = 30;

        startPositionBlaueMurmel = new PieceCircle(blaue_murmel.getLayoutX() / xPixelFürEinMeter, blaue_murmel.getLayoutY() / xPixelFürEinMeter, blaue_murmel.getRadius(), blaue_murmel, new Vektor());
        startPositionGrüneMurmel = new PieceCircle(gruene_murmel.getLayoutX() / xPixelFürEinMeter, gruene_murmel.getLayoutY() / xPixelFürEinMeter, gruene_murmel.getRadius(), gruene_murmel, new Vektor());

        for (int index = 0; index < listPieceCircle.size(); index++) {
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
        pieceRectangle = new PieceRectangle(bahn5.getLayoutX(), bahn5.getLayoutY(), ((int) bahn5.getHeight()), ((int) bahn5.getWidth()), bahn5);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn6.getLayoutX(), bahn6.getLayoutY(), ((int) bahn6.getHeight()), ((int) bahn6.getWidth()), bahn6);
        listPieceRectangle.add(pieceRectangle);

        pieceRectangle = new PieceRectangle(bahn7.getLayoutX(), bahn7.getLayoutY(), ((int) bahn7.getHeight()), ((int) bahn7.getWidth()), bahn7);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn8.getLayoutX(), bahn8.getLayoutY(), ((int) bahn8.getHeight()), ((int) bahn8.getWidth()), bahn8);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn9.getLayoutX(), bahn9.getLayoutY(), ((int) bahn9.getHeight()), ((int) bahn9.getWidth()), bahn9);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn10.getLayoutX(), bahn10.getLayoutY(), ((int) bahn10.getHeight()), ((int) bahn10.getWidth()), bahn10);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn11.getLayoutX(), bahn11.getLayoutY(), ((int) bahn11.getHeight()), ((int) bahn11.getWidth()), bahn11);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn12.getLayoutX(), bahn12.getLayoutY(), ((int) bahn12.getHeight()), ((int) bahn12.getWidth()), bahn12);
        listPieceRectangle.add(pieceRectangle);

        pieceRectangle = new PieceRectangle(bahn13.getLayoutX(), bahn13.getLayoutY(), ((int) bahn13.getHeight()), ((int) bahn13.getWidth()), bahn13);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn14.getLayoutX(), bahn14.getLayoutY(), ((int) bahn14.getHeight()), ((int) bahn14.getWidth()), bahn14);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn15.getLayoutX(), bahn15.getLayoutY(), ((int) bahn15.getHeight()), ((int) bahn15.getWidth()), bahn15);
        listPieceRectangle.add(pieceRectangle);

        pieceRectangle = new PieceRectangle(bahn16.getLayoutX(), bahn16.getLayoutY(), ((int) bahn16.getHeight()), ((int) bahn16.getWidth()), bahn16);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn17.getLayoutX(), bahn17.getLayoutY(), ((int) bahn17.getHeight()), ((int) bahn17.getWidth()), bahn17);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn18.getLayoutX(), bahn18.getLayoutY(), ((int) bahn18.getHeight()), ((int) bahn18.getWidth()), bahn18);
        listPieceRectangle.add(pieceRectangle);

        pieceRectangle = new PieceRectangle(bahn19.getLayoutX(), bahn19.getLayoutY(), ((int) bahn19.getHeight()), ((int) bahn19.getWidth()), bahn19);
        listPieceRectangle.add(pieceRectangle);
        pieceRectangle = new PieceRectangle(bahn20.getLayoutX(), bahn20.getLayoutY(), ((int) bahn20.getHeight()), ((int) bahn20.getWidth()), bahn20);
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

        listPieceCircle.get(chosed).gewicht = slider_gewicht.getValue();
        zuruecksetzen(chosed);
    }

    @FXML
    public void resetten(){
        Vektor nullVektor = new Vektor(0, 0);
        for(int index = 0; index < listPieceCircle.size(); index++){
            listPieceCircle.get(index).setGeschwindigkeitV(nullVektor);
            listPieceCircle.get(index).setBeschleunigungA(nullVektor);
            listPieceCircle.get(index).vorherigeDeltaXY = new Vektor(nullVektor.x, nullVektor.y);
            listPieceCircle.get(index).gewicht = 0.3;
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


        for(int index = 0; index < listPieceRectangle.size(); index++){
            if(!listPieceRectangle.get(index).getRectangle().getId().equals(bahn2.getId())){
                listPieceRectangle.get(index).setB(100);
                listPieceRectangle.get(index).getRectangle().setWidth(100);
                listPieceRectangle.get(index).getRectangle().setRotate(0);
                listPieceRectangle.get(index).setX(0);
                listPieceRectangle.get(index).setY(375);
                listPieceRectangle.get(index).getRectangle().setLayoutX(0);
                listPieceRectangle.get(index).getRectangle().setLayoutY(375);
            }
        }
        addedRectange = 0;

        slider_rechteckLaenge.setDisable(true);
        slider_rechteckWinkel1.setDisable(true);

        showRichtungInLinie(line, 0);

        if (buttonPlay){
            startAndPlay();
        }

        slider_startgeschwindigkeit.setValue(0);
        slider_gewicht.setValue(0.3);

        label_gewicht.setText(0.3 + " kg");
        label_startgeschwindigkeit.setText(0 + " m/s");

        uebernehmen();
    }

    @FXML
    private void displayPosition(MouseEvent event){ status_text.setText("Maus Position: X = " + event.getSceneX() + ", Y = " + event.getSceneY()); }

    @FXML
    public void setRectanglePlace(MouseEvent event){
        Rectangle rectangle = (Rectangle) event.getSource();
        for(int index = 0; index < listPieceRectangle.size(); index++){
            if(rectangle.getId().equals(listPieceRectangle.get(index).getRectangle().getId())){
                listPieceRectangle.get(index).setX(event.getSceneX() - rectangle.getWidth()/2.0);
                listPieceRectangle.get(index).setY(event.getSceneY() - rectangle.getHeight()/2.0);
                listPieceRectangle.get(index).getRectangle().setLayoutX(event.getSceneX() - rectangle.getWidth()/2.0);
                listPieceRectangle.get(index).getRectangle().setLayoutY(event.getSceneY() - rectangle.getHeight()/2.0);
                listPieceRectangle.get(index).getRectangle().setFill(Color.web("#383838"));
                label_startwinkel.setText(((int) listPieceRectangle.get(index).getWinkel() == 0.0 ? "" : "-") + (int) listPieceRectangle.get(index).getWinkel() + "°");
            }else if(index != 2){
                listPieceRectangle.get(index).getRectangle().setFill(Color.web("#2f2f2f"));
            }
        }
    }

    @FXML
    public void setRectangleChosed(MouseEvent event){
        Rectangle rectangle = (Rectangle) event.getSource();
        for(int index = 0; index < listPieceRectangle.size(); index++){
            if(rectangle.getId().equals(listPieceRectangle.get(index).getRectangle().getId())){
                chosedRectange = index;
                slider_rechteckWinkel1.setValue(listPieceRectangle.get(index).getWinkel());
                slider_rechteckWinkel1.setDisable(false);
                slider_rechteckLaenge.setDisable(false);
                listPieceRectangle.get(index).getRectangle().setFill(Color.web("#383838"));
                label_startwinkel.setText(((int) listPieceRectangle.get(index).getWinkel() == 0.0 ? "" : "-") + (int) listPieceRectangle.get(index).getWinkel() + "°");
            }else if(index != 2){
                listPieceRectangle.get(index).getRectangle().setFill(Color.web("#2f2f2f"));
            }
        }
    }

    @FXML
    public void setRectangleLength(MouseEvent event){
        double placeX = listPieceRectangle.get(chosedRectange).getRectangle().getLayoutX();
        double placeY = listPieceRectangle.get(chosedRectange).getRectangle().getLayoutY();
        listPieceRectangle.get(chosedRectange).getRectangle().setWidth((int) slider_rechteckLaenge.getValue());
        listPieceRectangle.get(chosedRectange).setB((int) slider_rechteckLaenge.getValue());
        listPieceRectangle.get(chosedRectange).setX(placeX);
        listPieceRectangle.get(chosedRectange).setY(placeY);
        listPieceRectangle.get(chosedRectange).getRectangle().setLayoutX(placeX);
        listPieceRectangle.get(chosedRectange).getRectangle().setLayoutY(placeY);
        System.out.println(placeX + ", y: " + placeY);
    }

    @FXML
    public void setRectangleRotate(MouseEvent event){
        listPieceRectangle.get(chosedRectange).getRectangle().setRotate(slider_rechteckWinkel1.getValue());
        label_startwinkel.setText(((int) listPieceRectangle.get(chosedRectange).getWinkel() == 0.0 ? "" : "-") + (int) listPieceRectangle.get(chosedRectange).getWinkel() + "°");
    }

    @FXML
    public void addRectangle(){
        int placeX = 600;
        int placeY = 500;
        boolean isAvailable = false;
        if(addedRectange == 2){
            addedRectange++;
        }
        for(int index = 0; index < listPieceRectangle.size(); index++){
            if((listPieceRectangle.get(index).getX() == placeX && listPieceRectangle.get(index).getY() == placeY) || addedRectange > 20){
                isAvailable = true;
            }
        }
        if(!isAvailable){
            if(addedRectange == 0){
                chosedRectange = 0;
            }
            listPieceRectangle.get(addedRectange).getRectangle().setLayoutX(placeX);
            listPieceRectangle.get(addedRectange).getRectangle().setLayoutY(placeY);
            listPieceRectangle.get(addedRectange).setX(placeX);
            listPieceRectangle.get(addedRectange).setY(placeY);
            listPieceRectangle.get(addedRectange).getRectangle().setVisible(true);
            listPieceRectangle.get(addedRectange++).getRectangle().setDisable(false);
            slider_rechteckLaenge.setDisable(false);
            slider_rechteckWinkel1.setDisable(false);
        }

    }

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
            //System.out.println("\n\n\nrectangleWinkel: " + rectangleWinkel);
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


            /**
             * Berücksichtigung von Wind:
             * a_wind_parallel = a * cos(alpha)
             * a_wind_senkrecht = a * sin(alpha)
             * a_wind_reibung = a * sin(alpha) * µ
             * aGesUP = a_wind_parallel - a_wind_reibung
             * aGesDOWN = -a_wind_parallel - a_wind_reibung
             */

            // a_wind_parallel = a * cos(alpha)
            double aWP = windbeschleunigungA * cos(alpha);
            // a_wind_senkrecht = a * sin(alpha)
            double aWS = windbeschleunigungA * sin(alpha);
            // a_wind_reibung = a * sin(alpha) * µ
            double aWR = aWS * listPieceCircle.get(index).reibungskoeffizient;
            // Wenn Kugel nach oben rollt: aGes = a_wind_parallel - a_wind_reibung
            double aWGesUP = aWP - aWR;
            // Wenn Kugel nach unten rollt: aGes = -a_wind_parallel - a_wind_reibung
            double aWGesDown = -aWP - aWR;
            // Vektor in entsprechende Richtung nach oben
            Vektor a_W_UP_Vektor = new Vektor(aWGesUP * cos(alpha_Entgegen), aWGesUP * sin(alpha_Entgegen));
            // Vektor in entsprechende Richtung nach unten
            Vektor a_W_DOWN_Vektor = new Vektor(aWGesDown * cos(alpha), aWGesDown * sin(alpha));


            if(((rectangleWinkel > 10 && rectangleWinkel < 90) || (rectangleWinkel > 190 && rectangleWinkel < 270)) && windbeschleunigungA > 0 && geschwindigkeitV.länge() > 0.2){
                //System.out.print("rollt links - ");
                aWP = windbeschleunigungA * cos(alpha_Entgegen);
                aWS = windbeschleunigungA * sin(alpha_Entgegen);
                aWR = aWS * listPieceCircle.get(index).reibungskoeffizient;
                aWGesUP = aWP - aWR;
                aWGesDown = -aWP - aWR;
                a_W_UP_Vektor = new Vektor(aWGesUP * cos(alpha), aWGesUP * sin(alpha));
                a_W_DOWN_Vektor = new Vektor(aWGesDown * cos(alpha_Entgegen), aWGesDown * sin(alpha_Entgegen));

                if(geschwindigkeitV.y < 0){
                    beschleunigungA = a_W_DOWN_Vektor;
                    beschleunigungA.scalareMultiplikationProdukt(-1);
                    //System.out.println("besch DOWN: x: " + beschleunigungA.x + ", y: " + beschleunigungA.y);
                }else {
                    beschleunigungA = a_W_UP_Vektor;
                    beschleunigungA.scalareMultiplikationProdukt(-1);
                    //System.out.println("besch UP: x: " + beschleunigungA.x + ", y: " + beschleunigungA.y);
                }
            }else if(((rectangleWinkel > 90 && rectangleWinkel < 170) || (rectangleWinkel > 270 && rectangleWinkel < 350)) && windbeschleunigungA < 0 && geschwindigkeitV.länge() > 0.2){
                //System.out.print("rollt rechts - ");
                if(geschwindigkeitV.y < 0){
                    beschleunigungA = a_W_DOWN_Vektor;
                    beschleunigungA.scalareMultiplikationProdukt(-1);
                    //System.out.println("besch DOWN: x: " + beschleunigungA.x + ", y: " + beschleunigungA.y);
                }else {
                    beschleunigungA = a_W_UP_Vektor;
                    beschleunigungA.scalareMultiplikationProdukt(-1);
                    //System.out.println("besch UP: x: " + beschleunigungA.x + ", y: " + beschleunigungA.y);
                }
            }else if((rectangleWinkel == 0 || rectangleWinkel == 180 || rectangleWinkel == 360) && windbeschleunigungA != 0.0){
                //System.out.println("rollt flach");
                beschleunigungA = a_W_DOWN_Vektor;
                beschleunigungA.scalareMultiplikationProdukt(-1);
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
            if(rectangleWinkel < 270 && rectangleWinkel > 90){
                a_N_Vektor = new Vektor(a_N * cos(alpha_N_entgegen), a_N * sin(alpha_N_entgegen));
                //System.out.println("n entgegen");
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

            beschleunigungA.addition(a_Ges);
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

        // Überlappung feststellen
        if (distance < (murmel1Radius + murmel2Radius) && distance < deltaDistance) {
            for (int index = 0; index < listPieceCircle.size(); index++){
                listPieceCircle.get(index).isRolling = false;
                listPieceCircle.get(index).isCollision = false;
            }

            // Kollisionswinkel berechnen
            double collisionAngle = -Math.atan2(distanceY, distanceX);
            Vektor normalVector = new Vektor(-Math.cos(collisionAngle), Math.sin(collisionAngle));

            Vektor v_parallel_1 = Vektor.projektion(murmel1.getGeschwindigkeitV(), normalVector);
            normalVector.scalareMultiplikationProdukt(-1);
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
             * v1' = v1'_parallel + v1_senkrecht
             * v2' = v2'_parallel + v2_senkrecht
             *
             * Formel (mit Teilelastischer Stoß), die angewendet wurde für die Rechnungen:
             * v'1 = m1 * v1 + m2 * v2 - m2 * (v1-v2) * k) / (m1 + m2)
             * v'2 = m1 * v1 + m2 * v2 - m1 * (v2-v1) * k) / (m1 + m2)
             */

            // m1v1 = m1 * v1
            Vektor m1v1 = Vektor.scalareMultiplikationProdukt(v_parallel_1, m1);
            // m2v2 = m2 * v2
            Vektor m2v2 = Vektor.scalareMultiplikationProdukt(v_parallel_2, m2);
            // m1 * v1 + m2 * v2
            Vektor v_strich_parallel = Vektor.addition(m1v1, m2v2);

            // v1'_parallel = ((m1 * v1 + m2 * v2 - m2 * (v1 - v2) * k)
            Vektor v1_stirch_parallel = Vektor.subtraktion(v_strich_parallel, Vektor.scalareMultiplikationProdukt(Vektor.subtraktion(v_parallel_1, v_parallel_2), m2 * k));
            // v2'_parallel = ((m1 * v1 + m2 * v2 - m1 * (v2 - v1) * k)
            Vektor v2_strich_parallel = Vektor.subtraktion(v_strich_parallel, Vektor.scalareMultiplikationProdukt(Vektor.subtraktion(v_parallel_2, v_parallel_1), m1 * k));

            // v1'_parallel = ((m1 * v1 + m2 * v2 - m2 * (v1 - v2) * k) / (m1 + m2)
            v1_stirch_parallel.scalareMultiplikationProdukt(1 / massSum);
            // v2'_parallel = ((m1 * v1 + m2 * v2 - m1 * (v2 - v1) * k) / (m1 + m2)
            v2_strich_parallel.scalareMultiplikationProdukt(1 / massSum);

            //  v1'= v1'_parallel + v1_senkrecht
            Vektor v1_strich = Vektor.addition(v1_stirch_parallel, v_senkrecht_1);
            //  v2'= v2'_parallel + v2_senkrecht
            Vektor v2_strich = Vektor.addition(v2_strich_parallel, v_senkrecht_2);

            listPieceCircle.get(0).setGeschwindigkeitV(v1_strich);
            listPieceCircle.get(1).setGeschwindigkeitV(v2_strich);
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

                        // v_senkrecht = v - v_parallel
                        Vektor v_senkrecht = Vektor.subtraktion(listPieceCircle.get(index).getGeschwindigkeitV(), v_parallel);

                        // Energieverlust
                        v_strich_parallel.scalareMultiplikationProdukt(k);

                        // v'_senkrecht bleibt unverändert: v'_senkrecht = v_senkrecht
                        Vektor v_strich_senkrecht = v_senkrecht;
                        // Vektoren zusammensetzen: v' = v'_parallel + v'_senkrecht
                        Vektor v_strich = new Vektor(v_strich_parallel.x, v_strich_parallel.y);
                        v_strich.addition(v_strich_senkrecht);
                        Vektor new_v = new Vektor(v_strich.x, v_strich.y);

                        double tatsächlicherAbstandHöheOhneAbs = abstandKugelZumMittelLinieOhneAbs(vektorRechteckMittelpunkt, rechteckRotation, listPieceCircle.get(index));
                        double tatsächlicherAbstandBreiteOhneAbs = abstandKugelZumMittelLinieOhneAbs(vektorRechteckMittelpunkt, rechteckRotation +
                                90, listPieceCircle.get(index));

                        // Normalen zum Rechteck berechnen
                        Vektor normaleNachOben = calculateNormalVector(rechteckRotation);
                        Vektor normaleNachRechts = calculateNormalVector(rechteckRotation + 270);
                        Vektor normaleNachUnten = calculateNormalVector(rechteckRotation + 180);
                        Vektor normaleNachLinks = calculateNormalVector(rechteckRotation + 90);
                        Point2D normalePO = new Point2D(normaleNachOben.x, -normaleNachOben.y);
                        Point2D normalePU = new Point2D(normaleNachUnten.x, -normaleNachUnten.y);
                        Point2D normalePR = new Point2D(normaleNachRechts.x, -normaleNachRechts.y);
                        Point2D normalePL = new Point2D(normaleNachLinks.x, -normaleNachLinks.y);
                        double angle = 0.0;

                        // Winkel in Richtung Rechteck oder weg vom Rechteck berechnen (Wenn Winkel > 90, dann bewegt es sich weg vom Rechteck)
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
                            // Wenn Kugel rollt und sich auch nicht weg von dem Rechteck bewegt
                            if((rechteckRotation < 90 && tatsächlicherAbstandHöheOhneAbs > 0.0) || (rechteckRotation > 90 && tatsächlicherAbstandHöheOhneAbs < 0.0)){
                                listPieceCircle.get(index).isRolling = true;
                                // Der Code ist erstmal nicht wichtig, sagt nur das die Kugel rollt (Doppelt gemoppelt, aber eventuell später brauchbar)
                                listPieceCircle.get(index).rollingDetails[0] = 1;
                                // Auf welchem Rechteck die Kugel rollt speichern
                                listPieceCircle.get(index).rollingDetails[1] = indexRectangle;
                                listPieceCircle.get(index).setGeschwindigkeitV(v_senkrecht);
                            }
                        } else if(angle <= 90 || Double.isNaN(angle)) /* Wenn Kugel abprallt */ {
                            // Wenn von einer Seite des Rechtecks abprallt
                            if(Math.abs(Math.abs(abstandBreiteBerührung) - Math.abs(tatsächlicherAbstandBreite)) <= 5){
                                new_v.scalareMultiplikationProdukt(-1);
                            }

                            // Aktualisierung der Geschwindigkeit des Kugels
                            listPieceCircle.get(index).setGeschwindigkeitV(new_v);
                        }
                    }else{
                        if(listPieceCircle.get(index).rollingDetails[1] != indexRectangle){
                            zuruecksetzen(index);
                            indexRectangle--;
                        }
                    }
                }else /* Wenn die Kugel aufhört zu rollen und eine Eckenkollision stattfindet */ {
                    zuruecksetzen(index);

                    double tatsächlicherAbstandHöheOhneAbs = abstandKugelZumMittelLinieOhneAbs(vektorRechteckMittelpunkt, rechteckRotation, listPieceCircle.get(index));
                    double tatsächlicherAbstandBreiteOhneAbs = abstandKugelZumMittelLinieOhneAbs(vektorRechteckMittelpunkt, rechteckRotation +
                            90, listPieceCircle.get(index));

                    // oben true, unten false
                    boolean oben_unten = false;
                    // links true, rechts false
                    boolean links_rechts = false;

                    // Gucken, von wo die Kugel kollidiert (oben, unten, rechts oder links)
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

                    // Eckpunkt wird erstmal mit Rechteckmittelpunkt gleichgesetzt
                    Vektor eckpunkt = new Vektor(vektorRechteckMittelpunkt[0], vektorRechteckMittelpunkt[1]);

                    // Oben linke Ecke
                    if(oben_unten && links_rechts){
                        // in dem Fall wird der Vektor von Rechteckmittelpunkt zur Mitte der linken Seite berechnet.
                        Vektor r1 = new Vektor(cos(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0,
                                sin(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0);
                        // Hier wird der Vektor von Rechteckmittelpunkt zur oberen Mitte des Rechtecks berechnet
                        Vektor r2 = new Vektor(cos(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0,
                                sin(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0);
                        // r1 * -1 weil es links ist und der im Koordinatensystem nach links minus ist.
                        r1.scalareMultiplikationProdukt(-1);
                        // r2 * -1 weil es oben ist und der im Koordinatensystem nach oben minus ist.
                        r2.scalareMultiplikationProdukt(-1);
                        // beide punkte miteinander addieren, um den Vektor in Richtung und Abstand zur Ecke von Mittelpunkt des Rechtecks zu erhalten.
                        r1.addition(r2);
                        // Mit Rechteckmittelpunkt zusammenrechnen, um den Eckpunkt zu erhalten.
                        eckpunkt.addition(r1);
                        // Der Code wird für die anderen Ecken genauso berechnet
                    }else if(!oben_unten && links_rechts) /* Unten linke Ecke */ {
                        Vektor r1 = new Vektor(cos(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0,
                                sin(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0);
                        Vektor r2 = new Vektor(cos(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0,
                                sin(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0);
                        r1.scalareMultiplikationProdukt(-1);
                        r1.addition(r2);
                        eckpunkt.addition(r1);
                    }else if(oben_unten && !links_rechts) /* Oben rechte Ecke */ {
                        Vektor r1 = new Vektor(cos(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0,
                                sin(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0);
                        Vektor r2 = new Vektor(cos(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0,
                                sin(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0);
                        r1.scalareMultiplikationProdukt(-1);
                        r1.addition(r2);
                        eckpunkt.subtraktion(r1);
                    }else if(!oben_unten && !links_rechts) /* Unten rechte Ecke */ {
                        Vektor r1 = new Vektor(cos(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0,
                                sin(Math.toRadians(rechteckRotation)) * listPieceRectangle.get(indexRectangle).getB()/2.0);
                        Vektor r2 = new Vektor(cos(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0,
                                sin(Math.toRadians(rechteckRotation + 90)) * listPieceRectangle.get(indexRectangle).getA()/2.0);
                        r1.scalareMultiplikationProdukt(-1);
                        r2.scalareMultiplikationProdukt(-1);
                        r1.addition(r2);
                        eckpunkt.subtraktion(r1);
                    }

                    // Kugel position in Vektor circle
                    Vektor circle = new Vektor(listPieceCircle.get(index).getCircle().getLayoutX(), listPieceCircle.get(index).getCircle().getLayoutY());
                    // Normale der Ecke berechnen mit eckpunkt - circleposition
                    Vektor normale = Vektor.subtraktion(eckpunkt, circle);
                    // Weil Koordinatensystem andersrum und es ist ein festes Element
                    normale.y *= -1;
                    // Normalisierung der berechneten Normale
                    normale.normalisierung();
                    // Normale und Geschwindigkeit in Point2D umwandeln aufgrund der angle-Methode
                    Point2D normaleP = new Point2D(normale.x, normale.y);
                    Point2D velocity = new Point2D(listPieceCircle.get(index).getGeschwindigkeitV().x, listPieceCircle.get(index).getGeschwindigkeitV().y);
                    // Wenn man nun die angle-Methode zwischen der Normale und der Geschwindigkeit der Kugel errechnet, erhält man, ob die Kugel in
                    // Richtung Ecke bewegt oder sich fern von der Ecke
                    double angle = normaleP.angle(velocity);

                    // Normalenvektor berechnen (-rechteckRotation, weil Winkel in Uhrzeigersinn ist).
                    Vektor normalVector = normale;

                    // Projektion von v auf Normalenvektor
                    // v_parallel = v
                    Vektor v_parallel = Vektor.projektion(listPieceCircle.get(index).getGeschwindigkeitV(), normalVector);

                    // Formel anwenden: v'_parallel = -v_parallel
                    Vektor v_strich_parallel = Vektor.scalareMultiplikationProdukt(v_parallel, -1);

                    // v_senkrecht = v - v_parallel
                    Vektor v_senkrecht = Vektor.subtraktion(listPieceCircle.get(index).getGeschwindigkeitV(), v_parallel);

                    // Energieverlust an der Ecke
                    v_strich_parallel.scalareMultiplikationProdukt(k);

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
        // Berechnung der Normalenvektor mit Rotation des Rechtecks
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
