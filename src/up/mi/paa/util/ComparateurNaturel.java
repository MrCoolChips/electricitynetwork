package up.mi.paa.util;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Comparateur générique pour le tri naturel des chaînes alphanumériques.
 * Permet de trier "M1, M2, M10" correctement (et non "M1, M10, M2").
 * 
 * <p>Exemple d'utilisation :
 * <pre>
 *     list.sort(ComparateurNaturel.de(Maison::getNom));
 *     list.sort(ComparateurNaturel.de(Generateur::getNom));
 * </pre>
 * 
 * @param <T> Le type des objets à comparer
 * @author Groupe 10
 */
public class ComparateurNaturel<T> implements Comparator<T> {

    private final Function<T, String> extracteur;

    /**
     * Constructeur privé - utiliser la méthode factory {@link #de(Function)}.
     * @param extracteur Fonction pour extraire la chaîne à comparer
     */
    private ComparateurNaturel(Function<T, String> extracteur) {
        this.extracteur = extracteur;
    }

    /**
     * Crée un comparateur naturel pour le type donné.
     * 
     * @param <T> Le type des objets à comparer
     * @param extracteur Fonction pour extraire la chaîne à comparer
     * @return Un nouveau comparateur naturel
     */
    public static <T> ComparateurNaturel<T> de(Function<T, String> extracteur) {
        return new ComparateurNaturel<>(extracteur);
    }

    /**
     * Crée un comparateur naturel pour des chaînes directement.
     * 
     * @return Un comparateur naturel pour String
     */
    public static ComparateurNaturel<String> pourChaines() {
        return new ComparateurNaturel<>(s -> s);
    }

    @Override
    public int compare(T a, T b) {
        String nomA = extracteur.apply(a);
        String nomB = extracteur.apply(b);
        
        if (nomA == null && nomB == null) return 0;
        if (nomA == null) return -1;
        if (nomB == null) return 1;
        
        String prefixA = nomA.replaceAll("[0-9]+$", "");
        String prefixB = nomB.replaceAll("[0-9]+$", "");
        String numStrA = nomA.substring(prefixA.length());
        String numStrB = nomB.substring(prefixB.length());
        
        int cmpPrefix = prefixA.compareToIgnoreCase(prefixB);
        if (cmpPrefix != 0) {
            return cmpPrefix;
        }
        
        if (numStrA.isEmpty() && numStrB.isEmpty()) return 0;
        if (numStrA.isEmpty()) return -1;
        if (numStrB.isEmpty()) return 1;
        
        try {
            return Integer.compare(Integer.parseInt(numStrA), Integer.parseInt(numStrB));
        } catch (NumberFormatException e) {
            return numStrA.compareTo(numStrB);
        }
    }
}
