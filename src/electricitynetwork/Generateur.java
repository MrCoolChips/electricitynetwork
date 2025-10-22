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

    public void setCapaciteMaximale(double capacite) {
        this.capaciteMaximale = capacite;
    }
    
    public String toString() {
    	return nom + ": la capacite maximale du générateur est de " + capaciteMaximale + "kW";
    }
    
    public boolean equals(Object o) {
        if (this == o) { 
        	return true;
        }
        
        if (!(o instanceof Generateur)) { 
        	return false;
        }
        
        return nom.equals(((Generateur) o).nom.toUpperCase());
    }
    
}
