package up.mi.paa.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Représente un réseau électrique composé de générateurs et de maisons.
 * 
 * <p>Cette classe gère :
 * <ul>
 *   <li>L'inventaire des générateurs et des maisons</li>
 *   <li>Les connexions entre générateurs et maisons (relation 1-N)</li>
 *   <li>Les opérations de recherche et de manipulation du réseau</li>
 * </ul>
 * 
 * <p>Chaque maison peut être connectée à au plus un générateur,
 * tandis qu'un générateur peut alimenter plusieurs maisons.
 * 
 * @author Groupe 10
 * @version 1.0
 * @see Generateur
 * @see Maison
 */
public class ReseauElectrique {

    private final Map<Generateur, List<Maison>> connexions;
    private final List<Maison> maisons;

    /**
     * Construit un réseau électrique vide.
     */
    public ReseauElectrique() {
        this.connexions = new HashMap<>();
        this.maisons = new ArrayList<>();
    }

    /**
     * Retourne la map des connexions générateur → maisons.
     *
     * @return la map des connexions (non modifiable recommandé)
     */
    public Map<Generateur, List<Maison>> getConnexions() {
        return connexions;
    }

    /**
     * Retourne la liste de tous les générateurs du réseau.
     *
     * @return une nouvelle liste contenant tous les générateurs
     */
    public List<Generateur> getGenerateurs() {
        return new ArrayList<>(connexions.keySet());
    }

    /**
     * Retourne la liste de toutes les maisons du réseau.
     *
     * @return la liste des maisons
     */
    public List<Maison> getMaisons() {
        return maisons;
    }

    /**
     * Ajoute un générateur au réseau s'il n'existe pas déjà.
     *
     * @param generateur le générateur à ajouter
     * @throws IllegalArgumentException si le générateur est null
     */
    public void ajouterGenerateur(Generateur generateur) {
        if (generateur == null) {
            throw new IllegalArgumentException("Le générateur ne peut pas être null");
        }
        connexions.putIfAbsent(generateur, new ArrayList<>());
    }

    /**
     * Ajoute une maison au réseau si elle n'existe pas déjà.
     *
     * @param maison la maison à ajouter
     * @throws IllegalArgumentException si la maison est null
     */
    public void ajouterMaison(Maison maison) {
        if (maison == null) {
            throw new IllegalArgumentException("La maison ne peut pas être null");
        }
        if (!maisons.contains(maison)) {
            maisons.add(maison);
        }
    }

    /**
     * Crée une connexion entre une maison et un générateur.
     *
     * @param maison     la maison à connecter
     * @param generateur le générateur à connecter
     * @throws IllegalArgumentException si maison ou générateur est null
     * @throws IllegalStateException    si la maison ou le générateur n'existe pas dans le réseau
     */
    public void ajouterConnexion(Maison maison, Generateur generateur) {
        if (maison == null || generateur == null) {
            throw new IllegalArgumentException("La maison et le générateur ne peuvent pas être null");
        }
        if (!maisons.contains(maison)) {
            throw new IllegalStateException("La maison doit d'abord être ajoutée au réseau");
        }
        if (!connexions.containsKey(generateur)) {
            throw new IllegalStateException("Le générateur doit d'abord être ajouté au réseau");
        }
        connexions.get(generateur).add(maison);
    }

    /**
     * Recherche le générateur qui alimente une maison donnée.
     *
     * @param maison la maison dont on veut connaître le générateur
     * @return le générateur alimentant cette maison, ou {@code null} si non connectée
     */
    public Generateur trouverGenerateur(Maison maison) {
        if (maison == null) {
            return null;
        }
        return connexions.entrySet().stream()
                .filter(e -> e.getValue().contains(maison))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Recherche un générateur par son nom.
     *
     * @param nom le nom du générateur
     * @return le générateur trouvé, ou {@code null} s'il n'existe pas
     */
    public Generateur trouverGenerateur(String nom) {
        if (nom == null) {
            return null;
        }
        String nomNormalise = nom.toUpperCase().trim();
        return connexions.keySet().stream()
                .filter(g -> g.getNom().equals(nomNormalise))
                .findFirst()
                .orElse(null);
    }

    /**
     * Recherche une maison par son nom.
     *
     * @param nom le nom de la maison
     * @return la maison trouvée, ou {@code null} si elle n'existe pas
     */
    public Maison trouverMaison(String nom) {
        if (nom == null) {
            return null;
        }
        String nomNormalise = nom.toUpperCase().trim();
        return maisons.stream()
                .filter(m -> m.getNom().equals(nomNormalise))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retourne la liste des maisons connectées à un générateur.
     *
     * @param generateur le générateur
     * @return une copie de la liste des maisons connectées (liste vide si aucune)
     */
    public List<Maison> trouverLesMaisonsDeGenerateur(Generateur generateur) {
        List<Maison> liste = connexions.get(generateur);
        return liste == null ? new ArrayList<>() : new ArrayList<>(liste);
    }

    /**
     * Vérifie si une maison est connectée à un générateur.
     *
     * @param maison la maison à tester
     * @return {@code true} si la maison est connectée
     */
    public boolean maisonEstConnectee(Maison maison) {
        return connexions.values().stream()
                .anyMatch(liste -> liste.contains(maison));
    }

    /**
     * Retourne la liste des maisons non connectées à un générateur.
     *
     * @return la liste des maisons non connectées
     */
    public List<Maison> maisonsNonConnectees() {
        return maisons.stream()
                .filter(m -> !maisonEstConnectee(m))
                .collect(Collectors.toList());
    }

    /**
     * Vérifie si toutes les maisons sont connectées.
     *
     * @return {@code true} si toutes les maisons ont un générateur
     */
    public boolean toutesLesMaisonsConnectees() {
        return maisonsNonConnectees().isEmpty();
    }

    /**
     * Supprime la connexion d'une maison avec son générateur.
     *
     * @param maison la maison à déconnecter (ignoré si null ou non connectée)
     */
    public void supprimerConnexion(Maison maison) {
        if (maison == null) {
            return;
        }
        Generateur g = trouverGenerateur(maison);
        if (g != null) {
            connexions.get(g).remove(maison);
        }
    }

    /**
     * Supprime un générateur du réseau.
     * 
     * <p>Attention : les maisons connectées à ce générateur deviennent orphelines.
     *
     * @param generateur le générateur à supprimer
     * @return {@code true} si le générateur a été supprimé
     */
    public boolean supprimerGenerateur(Generateur generateur) {
        if (generateur == null || !connexions.containsKey(generateur)) {
            return false;
        }
        connexions.remove(generateur);
        return true;
    }

    /**
     * Affiche tous les générateurs dans la console.
     */
    public void affichageGenerateurs() {
        System.out.println("\nGÉNÉRATEURS :");
        System.out.println("─────────────────────────────────");
        connexions.keySet().forEach(g -> System.out.println("  - " + g));
    }

    /**
     * Affiche toutes les maisons dans la console.
     */
    public void affichageMaisons() {
        System.out.println("\nMAISONS :");
        System.out.println("─────────────────────────────────");
        maisons.forEach(m -> System.out.println("  - " + m));
    }

    /**
     * Affiche toutes les connexions dans la console.
     */
    public void affichageConnexions() {
        connexions.forEach((g, listeMaisons) -> {
            if (listeMaisons == null || listeMaisons.isEmpty()) {
                System.out.println("   " + g.getNom() + " <-> (aucune maison)");
            } else {
                listeMaisons.forEach(m -> 
                    System.out.println("   " + g.getNom() + " <-> " + m.getNom()));
            }
        });
    }
}
