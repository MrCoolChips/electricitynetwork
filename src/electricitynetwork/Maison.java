package electricitynetwork;

public class Maison {

    private String nom;
    private TypeConsommation typeConsommation;

    public Maison(String nom, TypeConsommation typeConsommation) {
        this.nom = nom;
        this.typeConsommation = typeConsommation;
    }

    public String getNom() {
        return nom;
    }

    public TypeConsommation getTypeConsommation() {
        return typeConsommation;
    }

    public void setTypeConsommation(TypeConsommation typeConsommation) {
        this.typeConsommation = typeConsommation;
    }

    public int getConsommation() {
        return typeConsommation.demand();
    }
    
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
