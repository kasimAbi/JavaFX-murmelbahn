package sample.utilities;

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

    public void normalisierung(){
        double länge = this.länge();
        double normalisierungsFaktor = 1.0 / länge;
        this.scalareMultiplikationProdukt(normalisierungsFaktor);
    }

    public void scalareMultiplikationProdukt(double scalar){
        this.x *= scalar;
        this.y *= scalar;
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

    public void rotate(double winkel){
        // eulersche rotation // https://www.brainm.com/software/pubs/math/Rotation_matrix.pdf
        double altX = this.x;
        double altY = this.y;

        this.x = altX * Math.cos(Math.toRadians(winkel)) - altY * Math.sin(Math.toRadians(winkel));
        this.y = altX * Math.sin(Math.toRadians(winkel)) + altY * Math.cos(Math.toRadians(winkel));
    }
}
