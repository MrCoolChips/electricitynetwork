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

    public int getConsommation(TypeConsommation typeConsommation) {
        if(typeConsommation == TypeConsommation.BASSE) {
            return 10;
        }
        else if(typeConsommation == TypeConsommation.NORMAL) {
            return 20;
        }
        else {
            return 40;
        }
    }
    
}
