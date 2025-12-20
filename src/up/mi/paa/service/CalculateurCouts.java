package up.mi.paa.service;

import java.util.List;

import up.mi.paa.model.Couts;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.ReseauElectrique;

/**
 * Service de calcul des coûts d'un réseau électrique.
 * 
 * <p>La fonction de coût combine deux critères :
 * <ul>
 *   <li><b>Dispersion</b> : mesure l'équilibre des charges entre générateurs</li>
 *   <li><b>Surcharge</b> : pénalise les dépassements de capacité</li>
 * </ul>
 * 
 * <p>Formule : {@code coût = dispersion + λ × surcharge}
 * 
 * @author Groupe 10
 * @version 1.0
 * @see Couts
 */
public class CalculateurCouts {

    private int lambda;

    /**
     * Construit un calculateur de coûts avec le paramètre de pénalisation spécifié.
     *
     * @param lambda le coefficient de pénalisation des surcharges (λ ≥ 0)
     */
    public CalculateurCouts(int lambda) {
        this.lambda = lambda;
    }

    /**
     * Retourne le coefficient de pénalisation actuel.
     *
     * @return la valeur de lambda
     */
    public int getLambda() {
        return lambda;
    }

    /**
     * Modifie le coefficient de pénalisation.
     *
     * @param lambda la nouvelle valeur de lambda
     */
    public void setLambda(int lambda) {
        this.lambda = lambda;
    }

    /**
     * Calcule le coût global du réseau.
     *
     * @param reseau le réseau à évaluer
     * @return un objet {@link Couts} contenant le détail des coûts
     */
    public Couts calculerCout(ReseauElectrique reseau) {
        double dispersion = calculerDispersion(reseau);
        double surcharge = calculerSurcharge(reseau);
        double coutGlobal = dispersion + lambda * surcharge;
        return new Couts(coutGlobal, dispersion, surcharge);
    }

    /**
     * Calcule la charge totale des maisons connectées à un générateur.
     *
     * @param generateur le générateur concerné
     * @param reseau     le réseau contenant les connexions
     * @return la somme des consommations en kW
     */
    public double getSommeDesDemandesElectriques(Generateur generateur, ReseauElectrique reseau) {
        return reseau.trouverLesMaisonsDeGenerateur(generateur).stream()
                .mapToDouble(Maison::getConsommation)
                .sum();
    }

    /**
     * Calcule le taux d'utilisation d'un générateur.
     *
     * @param generateur le générateur à évaluer
     * @param reseau     le réseau contenant les connexions
     * @return le ratio charge/capacité (1.0 = 100%)
     * @throws ArithmeticException si la capacité est nulle
     */
    public double calculerLeTauxDUtilisation(Generateur generateur, ReseauElectrique reseau) {
        double capacite = generateur.getCapaciteMaximale();
        if (capacite == 0) {
            throw new ArithmeticException("La capacité du générateur " + generateur.getNom() + " ne peut pas être 0");
        }
        return getSommeDesDemandesElectriques(generateur, reseau) / capacite;
    }

    /**
     * Calcule la pénalité de surcharge totale.
     * 
     * <p>Pour chaque générateur, la surcharge est le dépassement normalisé :
     * {@code max(0, (charge - capacité) / capacité)}
     *
     * @param reseau le réseau à évaluer
     * @return la somme des surcharges normalisées
     */
    private double calculerSurcharge(ReseauElectrique reseau) {
        return reseau.getGenerateurs().stream()
                .mapToDouble(g -> {
                    double charge = getSommeDesDemandesElectriques(g, reseau);
                    double capacite = g.getCapaciteMaximale();
                    return Math.max(0.0, (charge - capacite) / capacite);
                })
                .sum();
    }

    /**
     * Calcule la dispersion des charges entre générateurs.
     * 
     * <p>La dispersion mesure l'écart absolu entre le taux d'utilisation
     * de chaque générateur et la moyenne globale.
     *
     * @param reseau le réseau à évaluer
     * @return la somme des écarts absolus
     */
    private double calculerDispersion(ReseauElectrique reseau) {
        List<Generateur> generateurs = reseau.getGenerateurs();
        if (generateurs.isEmpty()) {
            return 0.0;
        }

        double tauxMoyen = generateurs.stream()
                .mapToDouble(g -> calculerLeTauxDUtilisation(g, reseau))
                .average()
                .orElse(0.0);

        return generateurs.stream()
                .mapToDouble(g -> Math.abs(calculerLeTauxDUtilisation(g, reseau) - tauxMoyen))
                .sum();
    }
}