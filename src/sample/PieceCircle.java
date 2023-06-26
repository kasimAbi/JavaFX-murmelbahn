package sample;

import javafx.scene.shape.Circle;
import sample.Main;
import sample.Vektor;

public class PieceCircle {

    private double radius;
    public Vektor position = new Vektor(0, 0);
    public Vektor vorherigePosition = new Vektor(0, 0);
    public Vektor startPosition = new Vektor(0, 0);
    public Vektor deltaXY = new Vektor(0, 0);
    public Vektor vorherigeDeltaXY = new Vektor(0, 0);
    private Vektor geschwindigkeitV = new Vektor(0, 0);
    private Vektor beschleunigungA = new Vektor(0, 0);
    private Circle circle;
    public boolean isRolling = false;
    public boolean isCollision = false;

    public int[] rollingDetails = new int[]{0, -1};     // 0 (es rollt), -1 (auf welchem rechteck es rollt)

    double strecke = 0.0;
    double vorherigeStrecke = 0.0;

    /**
     * Reibungskoeffizienten:
     * Autoreifen auf glatter Asphaltstraße: 0.1
     * Quelle: https://www.schweizer-fn.de/stoff/reibwerte/reibwerte_sonstige.php
     */
    public double reibungskoeffizient = 0.01; // geeigneter Wert für den Reibungskoeffizienten

    // Gewicht in Kilogramm
    public double gewicht = 0.5;

    public PieceCircle(double x, double y, double radius, Circle circle, Vektor geschwindigkeit){
        this.position = new Vektor(x, y);
        this.vorherigePosition = new Vektor(x, y);
        this.startPosition = new Vektor(x, y);
        this.deltaXY = new Vektor();
        this.radius = radius;
        this.circle = circle;
        this.geschwindigkeitV = geschwindigkeit;
    }

    public double getRadius() {
        return this.radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public Vektor getGeschwindigkeitV() {
        return this.geschwindigkeitV;
    }

    public void setGeschwindigkeitV (Vektor geschwindigkeitV) {
        this.geschwindigkeitV = geschwindigkeitV;
    }

    public Vektor getBeschleunigungA() {
        return this.beschleunigungA;
    }

    public void setBeschleunigungA(Vektor beschleunigungA) {
        this.beschleunigungA = beschleunigungA;
    }

    public Circle getCircle() {
        return circle;
    }

    public void draw(double screenHeight, double xPixelFürEinMeter){
        /**
         * Position is in Metern und wird bei der Zeichnung mit Pixel gezeichnet.
         * : Meter * Pixel Faktor
          */
        this.circle.setLayoutX(this.position.x * xPixelFürEinMeter);

        /**
         * Koordinatensystem Richtung y wird umgedreht und in Meter umgewandelt, da Position der Kugel in Metern ist.
         * Aschließend wird gezeichnet
         * : Bildschirmhöhe - Position der Kugel in Richtung y
         * Zum Schluss wird wieder in Pixeln gezeichnet
         * : Meter * Pixel Faktor
          */
        this.circle.setLayoutY((screenHeight / xPixelFürEinMeter - position.y) * xPixelFürEinMeter);
    }

    public void drawJoystick(){
        this.circle.setRadius(this.radius);
        this.circle.setTranslateX(this.position.x);
        this.circle.setTranslateY(this.position.y);
    }

    public int getRichtung() {
        if(deltaXY.y == 0.0 && deltaXY.x == 0.0){
            return 270;
        }
        return (int) Math.toDegrees(Math.atan2(this.deltaXY.y, this.deltaXY.x)) % 360;
    }

    public int getRichtungPositiv(){
        return Math.abs(getRichtung() - 360) % 360;
    }

    public void updateVorherigePosition(){
        this.vorherigePosition.x = this.position.x;
        this.vorherigePosition.y = this.position.y;
    }

    public void updateDeltaXY(){
        this.deltaXY.x = this.position.x - this.vorherigePosition.x;
        this.deltaXY.y = this.position.y - this.vorherigePosition.y;
    }



    public void applyImpulse(Vektor impulse) {
        geschwindigkeitV.x += impulse.x / gewicht;
        geschwindigkeitV.y += impulse.y / gewicht;
    }
}
