package cli;

/**
 * Represente une maison consommatrice d'electricite dans le reseau.
 * Chaque maison possede un nom unique et un type de consommation.
 */
public class Maison {

    private String nom;
    private TypeConsommation typeConsommation;

    /**
     * Constructeur d'une maison.
     * 
     * @param nom Le nom unique de la maison
     * @param typeConsommation Le type de consommation electrique (BASSE, NORMAL, FORTE)
     */
    public Maison(String nom, TypeConsommation typeConsommation) {
        this.nom = nom;
        this.typeConsommation = typeConsommation;
    }

    /**
     * Retourne le nom de la maison.
     * 
     * @return Le nom de la maison
     */
    public String getNom() {
        return nom;
    }

    /**
     * Retourne le type de consommation de la maison.
     * 
     * @return Le type de consommation
     */
    public TypeConsommation getTypeConsommation() {
        return typeConsommation;
    }

    /**
     * Modifie le type de consommation de la maison.
     * 
     * @param typeConsommation Le nouveau type de consommation
     */
    public void setTypeConsommation(TypeConsommation typeConsommation) {
        this.typeConsommation = typeConsommation;
    }

    /**
     * Retourne la consommation electrique de la maison en kW.
     * 
     * @return La consommation en kW selon le type de consommation
     */
    public int getConsommation() {
        return typeConsommation.demand();
    }
    
    /**
     * Retourne une representation textuelle de la maison.
     * 
     * @return Une chaine decrivant la maison et sa consommation
     */
    public String toString() {
    	return nom + ": le type de consommation de la maison " + typeConsommation.name() + " et elle consomme " + typeConsommation.demand() + "kW";
    }
    
    /**
     * Compare cette maison avec un autre objet.
     * Deux maisons sont egales si elles ont le meme nom (insensible a la casse).
     * 
     * @param o L'objet a comparer
     * @return true si les maisons ont le meme nom, false sinon
     */
    public boolean equals(Object o) {
        if (this == o) { 
        	return true;
        }
        
        if (!(o instanceof Maison)) { 
        	return false;
        }
        
        return nom.equals(((Maison) o).nom.toUpperCase());
    }
    
}
