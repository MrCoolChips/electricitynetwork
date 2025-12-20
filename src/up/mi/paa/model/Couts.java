package up.mi.paa.model;

/**
 * Conteneur immutable pour les résultats d'évaluation d'un réseau électrique.
 * 
 * <p>Encapsule les trois composantes du coût :
 * <ul>
 *   <li><b>Coût global</b> : score total combinant dispersion et surcharge</li>
 *   <li><b>Dispersion</b> : mesure du déséquilibre entre générateurs</li>
 *   <li><b>Surcharge</b> : pénalité pour les dépassements de capacité</li>
 * </ul>
 * 
 * @author Groupe 10
 * @version 1.0
 * @see up.mi.paa.service.CalculateurCouts
 */
public class Couts {

    private final double coutGlobal;
    private final double dispersion;
    private final double surcharge;

    /**
     * Construit un objet Couts avec les valeurs spécifiées.
     *
     * @param coutGlobal le coût total calculé
     * @param dispersion la composante de dispersion
     * @param surcharge  la composante de surcharge
     */
    public Couts(double coutGlobal, double dispersion, double surcharge) {
        this.coutGlobal = coutGlobal;
        this.dispersion = dispersion;
        this.surcharge = surcharge;
    }

    /**
     * Retourne le coût global.
     *
     * @return le coût total
     */
    public double getCoutGlobale() {
        return coutGlobal;
    }

    /**
     * Retourne la composante de dispersion.
     *
     * @return la valeur de dispersion
     */
    public double getDispersion() {
        return dispersion;
    }

    /**
     * Retourne la composante de surcharge.
     *
     * @return la valeur de surcharge
     */
    public double getSurcharge() {
        return surcharge;
    }

    @Override
    public String toString() {
        return String.format("%.2f (dispersion = %.2f, surcharge = %.2f)", 
                            coutGlobal, dispersion, surcharge);
    }
}
