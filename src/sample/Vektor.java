package sample;

public class Vektor {
    public double x, y;

    public Vektor(){
        this(0.0, 0.0);
    }

    public Vektor(double x, double y){
        this.x = x;
        this.y = y;
    }

    public void addition(Vektor vek_2){
        this.x += vek_2.x;
        this.y += vek_2.y;
    }

    public static Vektor addition(Vektor v1, Vektor v2) {
        Vektor v = new Vektor();
        v.x = v1.x + v2.x;
        v.y = v1.y + v2.y;
        return v;
    }

    public void normalisierung(){
        double länge = this.länge();
        double normalisierungsFaktor = 0.0;
        if(this.länge() != 0.0){
            normalisierungsFaktor = 1.0 / länge;
        }
        this.scalareMultiplikationProdukt(normalisierungsFaktor);
    }

    public void scalareMultiplikationProdukt(double scalar){
        this.x *= scalar;
        this.y *= scalar;
    }

    public static double skalarprodukt(Vektor vektor1, Vektor vektor2){
        return vektor1.x * vektor2.x + vektor1.y * vektor2.y;
    }

    public static Vektor projektion(Vektor v1, Vektor v2) {
        double skalaresProdukt = v1.x * v2.x + v1.y * v2.y;
        double betragV2Quadrat = v2.x * v2.x + v2.y * v2.y;
        double skalar = skalaresProdukt / betragV2Quadrat;
        Vektor projizierterVektor = scalareMultiplikationProdukt(v2, skalar);
        return projizierterVektor;
    }

    public static Vektor normalenVektor(Vektor v) {
        Vektor normal = new Vektor();
        normal.x = -v.y;
        normal.y = v.x;
        return normal;
    }

    public void subtraktion(Vektor vek_2){
        this.x -= vek_2.x;
        this.y -= vek_2.y;
    }

    public static Vektor subtraktion(Vektor v1, Vektor v2) {
        Vektor v = new Vektor();
        v.x = v1.x - v2.x;
        v.y = v1.y - v2.y;
        return v;
    }

    public double länge(){
        return Math.sqrt(Math.pow(x, 2.0) + Math.pow(y, 2.0));
    }

    public static Vektor scalareMultiplikationProdukt(Vektor vektor, double scalar){
        Vektor v = new Vektor();
        v.x = vektor.x * scalar;
        v.y = vektor.y * scalar;
        return v;
    }

    public Vektor skalarDivision(double skalar) {
        double resultX = this.x / skalar;
        double resultY = this.y / skalar;
        return new Vektor(resultX, resultY);
    }

    public Vektor divideVektor(){
        return (new Vektor(-this.x / this.länge(), this.y / this.länge()));
    }

    public void rotate(double winkel){
        // eulersche rotation // https://www.brainm.com/software/pubs/math/Rotation_matrix.pdf
        double altX = this.x;
        double altY = this.y;

        this.x = altX * Math.cos(Math.toRadians(winkel)) - altY * Math.sin(Math.toRadians(winkel));
        this.y = altX * Math.sin(Math.toRadians(winkel)) + altY * Math.cos(Math.toRadians(winkel));
    }
}