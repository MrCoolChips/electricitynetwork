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

    /**
     * Map associant chaque generateur a la liste des maisons qu'il alimente.
     */
    private Map<Generateur, List<Maison>> connexions;

    /**
     * Liste de toutes les maisons du reseau (connectees ou non).
     */
    private List<Maison> maisons;

    /**
     * Constructeur d'un reseau electrique vide.
     */
    public ReseauElectrique() {
        connexions = new HashMap<>();
        maisons = new ArrayList<>();
    }

    /**
     * Retourne la map des connexions entre generateurs et maisons.
     *
     * @return La map associant chaque generateur a la liste de ses maisons
     */
    public Map<Generateur, List<Maison>> getConnexions() {
        return connexions;
    }

    /**
     * Retourne la liste de tous les generateurs du reseau.
     * (Chaque generateur correspond a une cle dans la map.)
     *
     * @return La liste des generateurs
     */
    public List<Generateur> getGenerateurs() {
        return new ArrayList<>(connexions.keySet());
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
        if (generateur == null) {
            throw new IllegalArgumentException("Le generateur ne peut pas etre null");
        }
        // si le generateur n'est pas encore connu, on l'ajoute avec une liste vide de maisons
        connexions.putIfAbsent(generateur, new ArrayList<>());
    }

    /**
     * Ajoute une maison au reseau si elle n'existe pas deja.
     *
     * @param maison La maison a ajouter
     */
    public void ajouterMaison(Maison maison) {
        if (maison == null) {
            throw new IllegalArgumentException("La maison ne peut pas etre null");
        }
        if (!maisons.contains(maison)) {
            maisons.add(maison);
        }
    }

    /**
     * Cree une connexion entre une maison et un generateur.
     * La maison et le generateur doivent deja exister dans le reseau.
     * Une maison ne peut etre connectee qu'a un seul generateur.
     *
     * @param maison      La maison a connecter
     * @param generateur  Le generateur a connecter
     * @throws IllegalArgumentException si maison ou generateur est null
     * @throws IllegalStateException si maison ou generateur n'existe pas dans le reseau
     */
    public void ajouterConnexion(Maison maison, Generateur generateur) {

        if (maison == null || generateur == null) {
            throw new IllegalArgumentException("maison/generateur ne peut pas etre null");
        }

        if (!maisons.contains(maison)) {
            throw new IllegalStateException("La maison doit d'abord etre ajoutee au reseau");
        }

        if (!connexions.containsKey(generateur)) {
            throw new IllegalStateException("Le generateur doit d'abord etre ajoute au reseau");
        }

        connexions.get(generateur).add(maison);
    }


    /**
     * Recherche le generateur qui alimente une maison donnee.
     *
     * @param m La maison dont on veut connaitre le generateur
     * @return Le generateur qui alimente cette maison, ou null si aucun
     *         generateur ne la contient (maison non connectee ou inconnue)
     */
    public Generateur trouverGenerateur(Maison m) {
        if (m == null) {
            return null;
        }
        for (Generateur g : connexions.keySet()) {
            if (connexions.get(g).contains(m)) {
                return g;
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
        if (nom == null) {
            return null;
        }
        for (Generateur g : connexions.keySet()) {
            if (nom.equals(g.getNom())) {
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
        if (nom == null) {
            return null;
        }
        for (Maison m : maisons) {
            if (nom.equals(m.getNom())) {
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
    public List<Maison> trouverLesMaisonsDeGenerateur(Generateur g) {
        List<Maison> liste = connexions.get(g);
        if (liste == null) {
            return new ArrayList<>();
        }
        // on renvoie une copie pour eviter les modifications externes
        return new ArrayList<>(liste);
    }

    /**
     * Indique si une maison est connectee a un generateur quelconque.
     *
     * @param maison La maison a tester
     * @return true si la maison est connectee, false sinon
     */
    public boolean maisonEstConnectee(Maison maison) {
        for (List<Maison> liste : connexions.values()) {
            if (liste.contains(maison)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retourne la liste des maisons qui ne sont connectees a aucun generateur.
     *
     * @return La liste des maisons non connectees
     */
    public List<Maison> maisonsNonConnectees() {
        List<Maison> res = new ArrayList<>();
        for (Maison m : maisons) {
            if (!maisonEstConnectee(m)) {
                res.add(m);
            }
        }
        return res;
    }

    /**
     * Verifie si toutes les maisons du reseau sont connectees a un generateur.
     *
     * @return true si toutes les maisons sont connectees, false sinon
     */
    public boolean toutesLesMaisonsConnectees() {
        return maisonsNonConnectees().isEmpty();
    }
    
    /**
     * Supprime la connexion entre une maison et son generateur.
     *
     * @param m La maison dont on veut supprimer la connexion
     *          (si la maison n'est pas connectee, la methode ne fait rien)
     */
    public void supprimerConnexion(Maison m) {

        Generateur g = trouverGenerateur(m);

        if (g == null || m == null) {
            return;
        }

        List<Maison> maisonsDuGenerateur = connexions.get(g);

        if (maisonsDuGenerateur != null) {
            maisonsDuGenerateur.remove(m);
        }
    }


    /**
     * Affiche tous les generateurs du reseau dans la console.
     */
    public void affichageGenerateurs() {
        System.out.println("\nGENERATEURS :");
        System.out.println("─────────────────────────────────");
        for (Generateur g : connexions.keySet()) {
            System.out.println("  - " + g.toString());
        }
    }

    /**
     * Affiche toutes les maisons du reseau dans la console.
     */
    public void affichageMaisons() {
        System.out.println("\nMAISONS :");
        System.out.println("─────────────────────────────────");
        for (Maison m : maisons) {
            System.out.println("  - " + m.toString());
        }
    }
    
    public void affichageConnexions() {
    	for (Generateur g: connexions.keySet()) {
    		if (connexions.get(g) == null) {
    			System.out.println("   " + g.getNom() + " <-> vide");
    		} else {
	    		for (Maison m: connexions.get(g)) {
	    			System.out.println("   " + g.getNom() + " <-> " + m.getNom());
	    		}
    		}
    	}
    }
}
