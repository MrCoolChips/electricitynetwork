package electricitynetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReseauElectrique {

    private List<Generateur> generateurs;
    private List<Maison> maisons;
    private Map<Maison, Generateur> connexions;

    public ReseauElectrique() {
        generateurs = new ArrayList<>();
        maisons = new ArrayList<>();
        connexions = new HashMap<>();
    }

    public Map<Maison, Generateur> getConnexions() {
        return connexions;
    }

    public List<Generateur> getGenerateurs() {
        return generateurs;
    }

    public List<Maison> getMaisons() {
        return maisons;
    }

    public void ajouterGenerateur(Generateur generateur) {
        if (generateur != null && !generateurs.contains(generateur)) {
            generateurs.add(generateur);
        }
    }

    public void ajouterMaison(Maison maison) {
        if (maison != null && !maisons.contains(maison)) {
            maisons.add(maison);
        }
    }

    public void ajouterConnexion(Maison maison, Generateur generateur) {
        connexions.put(maison, generateur);
    }

    public Generateur trouverGenerateur(String nom) {
    for(Generateur g : generateurs) {
        if (g.getNom().equals(nom)) {
            return g;
        }
    }
        return null;
    }

    public Maison trouverMaison(String nom) {

        for(Maison m : maisons) {
            if(m.getNom().equals(nom)) {
                return m;
            }
        }
        return null;
    }
    
}
