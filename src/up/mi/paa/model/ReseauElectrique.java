package up.mi.paa.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represente un reseau electrique compose de generateurs, de maisons
 * et de connexions entre eux.
 */
public class ReseauElectrique {

    private List<Generateur> generateurs;
    private List<Maison> maisons;
    private Map<Maison, Generateur> connexions;

    /**
     * Constructeur d'un reseau electrique vide.
     */
    public ReseauElectrique() {
        generateurs = new ArrayList<>();
        maisons = new ArrayList<>();
        connexions = new HashMap<>();
    }

    /**
     * Retourne la map des connexions entre maisons et generateurs.
     * 
     * @return La map associant chaque maison a son generateur
     */
    public Map<Maison, Generateur> getConnexions() {
        return connexions;
    }

    /**
     * Retourne la liste de tous les generateurs du reseau.
     * 
     * @return La liste des generateurs
     */
    public List<Generateur> getGenerateurs() {
        return generateurs;
    }

    /**
     * Retourne la liste de toutes les maisons du reseau.
     * 
     * @return La liste des maisons
     */
    public List<Maison> getMaisons() {
        return maisons;
    }

    /**
     * Ajoute un generateur au reseau s'il n'existe pas deja.
     * 
     * @param generateur Le generateur a ajouter
     */
    public void ajouterGenerateur(Generateur generateur) {
        if (generateur != null && !generateurs.contains(generateur)) {
            generateurs.add(generateur);
        }
    }

    /**
     * Ajoute une maison au reseau si elle n'existe pas deja.
     * 
     * @param maison La maison a ajouter
     */
    public void ajouterMaison(Maison maison) {
        if (maison != null && !maisons.contains(maison)) {
            maisons.add(maison);
        }
    }

    /**
     * Cree une connexion entre une maison et un generateur.
     * La maison et le generateur doivent deja exister dans le reseau.
     * 
     * @param maison La maison a connecter
     * @param generateur Le generateur a connecter
     * @throws IllegalArgumentException si maison ou generateur est null
     * @throws IllegalStateException si maison ou generateur n'existe pas dans le reseau
     */
    public void ajouterConnexion(Maison maison, Generateur generateur) {
    	
        if (maison == null || generateur == null) {
            throw new IllegalArgumentException("maison/generateur ne peut pas etre null");
        }

        if (!maisons.contains(maison) || !generateurs.contains(generateur)) {
            throw new IllegalStateException("Tout d'abord, la maison/générateur doit être ajouté à la liste");
        }
        
        connexions.put(maison, generateur);
    }

    /**
     * Recherche un generateur dans le reseau par objet.
     * 
     * @param g1 Le generateur a rechercher
     * @return Le generateur trouve ou null s'il n'existe pas
     */
    public Generateur trouverGenerateur(Generateur g1) {
        for (Generateur g : generateurs) {
            if (g.equals(g1)) {
                return g;
            }
        }
        return null;
    }

    /**
     * Recherche une maison dans le reseau par objet.
     * 
     * @param m1 La maison a rechercher
     * @return La maison trouvee ou null si elle n'existe pas
     */
    public Maison trouverMaison(Maison m1) {
        for (Maison m : maisons) {
            if (m.equals(m1)) {
                return m;
            }
        }
        return null;
    }
    
    /**
     * Recherche un generateur dans le reseau par nom.
     * 
     * @param nom Le nom du generateur a rechercher
     * @return Le generateur trouve ou null s'il n'existe pas
     */
    public Generateur trouverGenerateur(String nom) {
        for (Generateur g : generateurs) {
        	if (g.getNom().equals(nom)) {
        		return g;
        	}
        }
        return null;
    }
    
    /**
     * Recherche une maison dans le reseau par nom.
     * 
     * @param nom Le nom de la maison a rechercher
     * @return La maison trouvee ou null si elle n'existe pas
     */
    public Maison trouverMaison(String nom) {
        for (Maison m : maisons) {
        	if (m.getNom().equals(nom)) {
        		return m;
        	}
        }
        return null;
    }
    
    /**
     * Retourne la liste des maisons connectees a un generateur donne.
     * 
     * @param g Le generateur
     * @return La liste des maisons connectees a ce generateur
     */
    public List<Maison> trouverLesMaisonsDesGenerateurs(Generateur g) {
    	List<Maison> m = new ArrayList<Maison>();
    	for (Map.Entry<Maison, Generateur> connexion : connexions.entrySet()) {
    		if (g.equals(connexion.getValue())) {
    			m.add(connexion.getKey());
    		}
    	}
    	return m;
    }
    
    /**
     * Affiche tous les generateurs du reseau dans la console.
     */
    public void affichageGenerateurs() {
    	System.out.println("\nGENERATEURS :");
    	System.out.println("─────────────────────────────────");
    	for (Generateur g: generateurs) {
    		System.out.println("  - " + g.toString());
    	}
    }
    
    /**
     * Affiche toutes les maisons du reseau dans la console.
     */
    public void affichageMaisons() {
    	System.out.println("\nMAISONS :");
    	System.out.println("─────────────────────────────────");
    	for (Maison m: maisons) {
    		System.out.println("  - " + m.toString());
    	}
    }
    
}
