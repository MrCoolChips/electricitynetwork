package cli;

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
    	
        if (maison == null || generateur == null) {
            throw new IllegalArgumentException("maison/generateur ne peut pas etre null");
        }

        if (!maisons.contains(maison) || !generateurs.contains(generateur)) {
            throw new IllegalStateException("Tout d'abord, la maison/générateur doit être ajouté à la liste");
        }
        
        connexions.put(maison, generateur);
    }

    public Generateur trouverGenerateur(Generateur g1) {
    	Generateur g2 = null;
    	boolean trouve = false;
    	
    	for (int i = 0; i < generateurs.size() && !trouve; i++) {
    		if (generateurs.get(i).equals(g1)) {
    			g2 = generateurs.get(i);
    			trouve = true;
    		}
    	}

        return g2;
    }

    public Maison trouverMaison(Maison m1) {
    	Maison m2 = null;
    	boolean trouve = false;
    	
    	for (int i = 0; i < maisons.size() && !trouve; i++) {
    		if (maisons.get(i).equals(m1)) {
    			m2 = maisons.get(i);
    			trouve = true;
    		}
    	}

        return m2;
    }
    
    public Generateur trouverGenerateur(String nom) {
        for (Generateur g : generateurs) {
        	if (g.getNom().equals(nom)) {
        		return g;
        	}
        }
        return null;
    }
    
    public Maison trouverMaison(String nom) {
        for (Maison m : maisons) {
        	if (m.getNom().equals(nom)) {
        		return m;
        	}
        }
        return null;
    }
    
    public List<Maison> trouverLesMaisonsDesGenerateurs(Generateur g) {
    	List<Maison> m = new ArrayList<Maison>();
    	for (Map.Entry<Maison, Generateur> connexion : connexions.entrySet()) {
    		if (g.equals(connexion.getValue())) {
    			m.add(connexion.getKey());
    		}
    	}
    	return m;
    }
    
    public void affichageGenerateurs() {
    	System.out.println("\nGENERATEURS :");
    	for (Generateur g: generateurs) {
    		System.out.println(g.toString());
    	}
    }
    
    public void affichageMaisons() {
    	System.out.println("\nMAISONS :");
    	for (Maison m: maisons) {
    		System.out.println(m.toString());
    	}
    }
    
}
