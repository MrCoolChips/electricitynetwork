package electricitynetwork;

public class Generateur {

    private String nom;
    private double capaciteMaximale;

    public Generateur(String nom, double capaciteMaximale) {
        this.nom = nom;
        this.capaciteMaximale = capaciteMaximale;
    }

    public String getNom() {
        return nom;
    }

    public double getCapaciteMaximale() {
        return capaciteMaximale;
    }

    public void setCapaciteMaximale(Double capacite) {
        this.capaciteMaximale = capacite;
    }
    
}
