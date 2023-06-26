package sample;

import javafx.scene.shape.Rectangle;

public class PieceRectangle {

    private double x, y, winkel;
    private int a, b;
    private final int gewicht = 300;
    private Rectangle rectangle;

    public PieceRectangle(double x, double y, int a, int b, Rectangle rectangle) {
        this.x = x;
        this.y = y;
        this.a = a;
        this.b = b;
        this.rectangle = rectangle;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWinkel() {
        return rectangle.getRotate();
    }

    public void setWinkel(double winkel) {
        this.winkel = winkel;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getGewicht() {
        return gewicht;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void draw(){
        rectangle.setLayoutX(x);
        rectangle.setLayoutY(y);
    }

    public double rectangleRichtungInMurmelRichtung(double limit){
        double newRichtung = 360.0 - getWinkel() + limit;
        if(newRichtung > 360.0){
            newRichtung -= 360.0;
        }else if(newRichtung < 0.0){
            newRichtung += 360.0;
        }
        return newRichtung;
    }
}
