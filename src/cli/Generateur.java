package cli;

/**
 * Represente un generateur electrique dans le reseau.
 * Un generateur possede un nom unique et une capacite maximale de production.
 */
public class Generateur {

    private String nom;
    private double capaciteMaximale;

    /**
     * Constructeur d'un generateur.
     * 
     * @param nom Le nom unique du generateur
     * @param capaciteMaximale La capacite maximale de production en kW
     */
    public Generateur(String nom, double capaciteMaximale) {
        this.nom = nom;
        this.capaciteMaximale = capaciteMaximale;
    }

    /**
     * Retourne le nom du generateur.
     * 
     * @return Le nom du generateur
     */
    public String getNom() {
        return nom;
    }

    /**
     * Retourne la capacite maximale du generateur.
     * 
     * @return La capacite maximale en kW
     */
    public double getCapaciteMaximale() {
        return capaciteMaximale;
    }

    /**
     * Modifie la capacite maximale du generateur.
     * 
     * @param capacite La nouvelle capacite maximale en kW
     */
    public void setCapaciteMaximale(double capacite) {
        this.capaciteMaximale = capacite;
    }
    
    /**
     * Retourne une representation textuelle du generateur.
     * 
     * @return Une chaine decrivant le generateur et sa capacite
     */
    public String toString() {
    	return nom + ": la capacite maximale du générateur est de " + capaciteMaximale + "kW";
    }
    
    /**
     * Compare ce generateur avec un autre objet.
     * Deux generateurs sont egaux s'ils ont le meme nom (insensible a la casse).
     * 
     * @param o L'objet a comparer
     * @return true si les generateurs ont le meme nom, false sinon
     */
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
