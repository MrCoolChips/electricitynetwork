package up.mi.paa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.ReseauElectrique;

/**
 * Service d'optimisation du réseau électrique par métaheuristique.
 * 
 * <p>L'algorithme combine deux phases :
 * <ol>
 *   <li><b>Construction gloutonne (Best-Fit Decreasing)</b> : affectation initiale</li>
 *   <li><b>Recuit simulé</b> : amélioration par exploration stochastique</li>
 * </ol>
 * 
 * <p>Le recuit simulé utilise deux types de mouvements :
 * <ul>
 *   <li><b>Échange</b> : permutation de deux maisons entre générateurs</li>
 *   <li><b>Déplacement</b> : migration d'une maison vers un autre générateur</li>
 * </ul>
 * 
 * @author Groupe 10
 * @version 1.0
 * @see CalculateurCouts
 */
public class OptimiseurReseau {

    private static final long DUREE_MAX_MS = 3000;
    private static final double TEMPERATURE_INITIALE = 100.0;
    private static final double FACTEUR_REFROIDISSEMENT = 0.999;
    private static final double SEUIL_RECHAUFFAGE = 0.001;
    private static final double TEMPERATURE_RECHAUFFAGE = 10.0;
    private static final double RATIO_ECHANGE = 0.4;

    private final CalculateurCouts calculateur;
    private final Random random;

    /**
     * Construit un optimiseur avec le calculateur de coûts spécifié.
     *
     * @param calculateur le calculateur de coûts à utiliser
     */
    public OptimiseurReseau(CalculateurCouts calculateur) {
        this.calculateur = calculateur;
        this.random = new Random();
    }

    /**
     * Optimise le réseau en minimisant la fonction de coût.
     * 
     * <p>Le réseau est modifié en place avec la meilleure configuration trouvée.
     *
     * @param reseau le réseau à optimiser
     * @return le réseau optimisé
     */
    public ReseauElectrique optimiser(ReseauElectrique reseau) {
        if (reseau.getMaisons().isEmpty() || reseau.getGenerateurs().isEmpty()) {
            return reseau;
        }

        construireSolutionGloutonne(reseau);
        EtatOptimisation etat = new EtatOptimisation(reseau);
        executerRecuitSimule(reseau, etat);
        appliquerConfiguration(reseau, etat.meilleureConfig);

        System.out.println("\n  \033[32m[OK] Meilleur coût trouvé : " + 
                          String.format("%.4f", etat.meilleurCout) + "\033[0m\n");
        return reseau;
    }

    /**
     * Construit une solution initiale par algorithme glouton Best-Fit Decreasing.
     * 
     * <p>Les maisons sont triées par consommation décroissante, puis chaque maison
     * est affectée au générateur qui minimise le coût global.
     *
     * @param reseau le réseau à initialiser
     */
    private void construireSolutionGloutonne(ReseauElectrique reseau) {
        reseau.getMaisons().forEach(reseau::supprimerConnexion);

        List<Maison> maisonsTries = new ArrayList<>(reseau.getMaisons());
        maisonsTries.sort((a, b) -> Double.compare(b.getConsommation(), a.getConsommation()));

        for (Maison maison : maisonsTries) {
            Generateur meilleur = trouverMeilleurGenerateur(reseau, maison);
            if (meilleur != null) {
                reseau.ajouterConnexion(maison, meilleur);
            }
        }
    }

    /**
     * Trouve le générateur optimal pour une maison donnée.
     * 
     * <p>Teste tous les générateurs et retourne celui qui minimise le coût global.
     *
     * @param reseau le réseau électrique
     * @param maison la maison à connecter
     * @return le générateur optimal, ou {@code null} si aucun générateur disponible
     */
    private Generateur trouverMeilleurGenerateur(ReseauElectrique reseau, Maison maison) {
        Generateur meilleur = null;
        double coutMin = Double.MAX_VALUE;

        for (Generateur g : reseau.getGenerateurs()) {
            reseau.ajouterConnexion(maison, g);
            double cout = calculateur.calculerCout(reseau).getCoutGlobale();
            if (cout < coutMin) {
                coutMin = cout;
                meilleur = g;
            }
            reseau.supprimerConnexion(maison);
        }
        return meilleur;
    }

    /**
     * Exécute la phase de recuit simulé.
     * 
     * <p>Alterne entre échanges et déplacements de maisons selon le ratio défini.
     * La température décroît exponentiellement avec réchauffage périodique.
     *
     * @param reseau le réseau à optimiser
     * @param etat l'état courant de l'optimisation
     */
    private void executerRecuitSimule(ReseauElectrique reseau, EtatOptimisation etat) {
        long tempsDebut = System.currentTimeMillis();
        double temperature = TEMPERATURE_INITIALE;

        List<Maison> maisons = reseau.getMaisons();
        List<Generateur> generateurs = reseau.getGenerateurs();

        while (System.currentTimeMillis() - tempsDebut < DUREE_MAX_MS) {
            if (temperature < SEUIL_RECHAUFFAGE) {
                temperature = TEMPERATURE_RECHAUFFAGE;
            }

            if (random.nextDouble() < RATIO_ECHANGE) {
                tenterEchange(reseau, etat, maisons, generateurs, temperature);
            } else {
                tenterDeplacement(reseau, etat, maisons, generateurs, temperature);
            }

            temperature *= FACTEUR_REFROIDISSEMENT;
        }
    }

    /**
     * Tente un échange de deux maisons entre leurs générateurs respectifs.
     * 
     * <p>L'échange est accepté si le coût diminue ou selon le critère de Metropolis.
     *
     * @param reseau le réseau électrique
     * @param etat l'état de l'optimisation
     * @param maisons liste des maisons
     * @param generateurs liste des générateurs
     * @param temperature température courante du recuit
     */
    private void tenterEchange(ReseauElectrique reseau, EtatOptimisation etat,
                               List<Maison> maisons, List<Generateur> generateurs, double temperature) {
        Maison m1 = maisons.get(random.nextInt(maisons.size()));
        Maison m2 = maisons.get(random.nextInt(maisons.size()));
        Generateur g1 = reseau.trouverGenerateur(m1);
        Generateur g2 = reseau.trouverGenerateur(m2);

        if (g1 == null || g2 == null || g1 == g2) return;

        double delta1 = m2.getConsommation() - m1.getConsommation();
        double delta2 = m1.getConsommation() - m2.getConsommation();

        modifierCache(etat.cache, g1, delta1);
        modifierCache(etat.cache, g2, delta2);

        double nouveauCout = evaluerCout(generateurs, etat.cache);
        if (accepter(nouveauCout - etat.coutActuel, temperature)) {
            effectuerEchange(reseau, m1, m2, g1, g2);
            etat.coutActuel = nouveauCout;
            mettreAJourMeilleur(reseau, etat);
        } else {
            modifierCache(etat.cache, g1, -delta1);
            modifierCache(etat.cache, g2, -delta2);
        }
    }

    /**
     * Tente le déplacement d'une maison vers un autre générateur.
     * 
     * <p>Le déplacement est accepté si le coût diminue ou selon le critère de Metropolis.
     *
     * @param reseau le réseau électrique
     * @param etat l'état de l'optimisation
     * @param maisons liste des maisons
     * @param generateurs liste des générateurs
     * @param temperature température courante du recuit
     */
    private void tenterDeplacement(ReseauElectrique reseau, EtatOptimisation etat,
                                   List<Maison> maisons, List<Generateur> generateurs, double temperature) {
        Maison maison = maisons.get(random.nextInt(maisons.size()));
        Generateur ancien = reseau.trouverGenerateur(maison);
        Generateur nouveau = generateurs.get(random.nextInt(generateurs.size()));

        if (ancien == null || ancien == nouveau) return;

        double conso = maison.getConsommation();
        modifierCache(etat.cache, ancien, -conso);
        modifierCache(etat.cache, nouveau, conso);

        double nouveauCout = evaluerCout(generateurs, etat.cache);
        if (accepter(nouveauCout - etat.coutActuel, temperature)) {
            effectuerDeplacement(reseau, maison, nouveau);
            etat.coutActuel = nouveauCout;
            mettreAJourMeilleur(reseau, etat);
        } else {
            modifierCache(etat.cache, ancien, conso);
            modifierCache(etat.cache, nouveau, -conso);
        }
    }

    /**
     * Détermine si un mouvement doit être accepté selon le critère de Metropolis.
     * 
     * <p>Un mouvement améliorant est toujours accepté. Un mouvement dégradant
     * est accepté avec probabilité {@code exp(-deltaCout / temperature)}.
     *
     * @param deltaCout variation du coût (négatif = amélioration)
     * @param temperature température courante
     * @return {@code true} si le mouvement est accepté
     */
    private boolean accepter(double deltaCout, double temperature) {
        return deltaCout < 0 || random.nextDouble() < Math.exp(-deltaCout / temperature);
    }

    /**
     * Modifie la charge d'un générateur dans le cache.
     *
     * @param cache le cache des charges
     * @param g le générateur concerné
     * @param delta variation de charge à appliquer
     */
    private void modifierCache(Map<Generateur, Double> cache, Generateur g, double delta) {
        cache.put(g, cache.get(g) + delta);
    }

    /**
     * Évalue le coût total à partir du cache des charges.
     * 
     * <p>Calcule la dispersion et la surcharge selon la formule :
     * <pre>Coût = Dispersion + Lambda × Surcharge</pre>
     *
     * @param generateurs liste des générateurs
     * @param cache cache des charges par générateur
     * @return le coût global calculé
     */
    private double evaluerCout(List<Generateur> generateurs, Map<Generateur, Double> cache) {
        int n = generateurs.size();
        double sommeTaux = 0.0;

        for (Generateur g : generateurs) {
            double cap = g.getCapaciteMaximale();
            if (cap > 0) sommeTaux += cache.get(g) / cap;
        }

        double tauxMoyen = sommeTaux / n;
        double dispersion = 0.0;
        double surcharge = 0.0;

        for (Generateur g : generateurs) {
            double cap = g.getCapaciteMaximale();
            if (cap > 0) {
                double charge = cache.get(g);
                dispersion += Math.abs(charge / cap - tauxMoyen);
                surcharge += Math.max(0.0, (charge - cap) / cap);
            }
        }

        return dispersion + calculateur.getLambda() * surcharge;
    }

    /**
     * Met à jour la meilleure solution si le coût actuel est inférieur.
     *
     * @param reseau le réseau électrique
     * @param etat l'état de l'optimisation contenant le meilleur coût
     */
    private void mettreAJourMeilleur(ReseauElectrique reseau, EtatOptimisation etat) {
        if (etat.coutActuel < etat.meilleurCout) {
            etat.meilleurCout = etat.coutActuel;
            sauvegarderConfiguration(reseau, etat.meilleureConfig);
        }
    }

    /**
     * Sauvegarde la configuration actuelle des connexions.
     * 
     * <p>Chaque indice du tableau correspond à une maison et contient
     * l'indice du générateur associé (-1 si non connectée).
     *
     * @param reseau le réseau électrique
     * @param config tableau de configuration à remplir
     */
    private void sauvegarderConfiguration(ReseauElectrique reseau, int[] config) {
        List<Generateur> generateurs = reseau.getGenerateurs();
        List<Maison> maisons = reseau.getMaisons();
        for (int i = 0; i < maisons.size(); i++) {
            Generateur g = reseau.trouverGenerateur(maisons.get(i));
            config[i] = (g != null) ? generateurs.indexOf(g) : -1;
        }
    }

    /**
     * Applique une configuration sauvegardée au réseau.
     * 
     * <p>Restaure les connexions maison-générateur selon le tableau.
     *
     * @param reseau le réseau électrique à modifier
     * @param config tableau de configuration à appliquer
     */
    private void appliquerConfiguration(ReseauElectrique reseau, int[] config) {
        List<Generateur> generateurs = reseau.getGenerateurs();
        List<Maison> maisons = reseau.getMaisons();
        for (int i = 0; i < maisons.size(); i++) {
            reseau.supprimerConnexion(maisons.get(i));
            if (config[i] >= 0 && config[i] < generateurs.size()) {
                reseau.ajouterConnexion(maisons.get(i), generateurs.get(config[i]));
            }
        }
    }

    /**
     * Effectue l'échange de deux maisons entre leurs générateurs.
     * 
     * <p>Après l'opération : m1 → g2 et m2 → g1.
     *
     * @param reseau le réseau électrique
     * @param m1 première maison
     * @param m2 deuxième maison
     * @param g1 générateur initial de m1
     * @param g2 générateur initial de m2
     */
    private void effectuerEchange(ReseauElectrique reseau, Maison m1, Maison m2,
                                  Generateur g1, Generateur g2) {
        reseau.supprimerConnexion(m1);
        reseau.supprimerConnexion(m2);
        reseau.ajouterConnexion(m1, g2);
        reseau.ajouterConnexion(m2, g1);
    }

    /**
     * Déplace une maison vers un nouveau générateur.
     *
     * @param reseau le réseau électrique
     * @param m la maison à déplacer
     * @param g le générateur de destination
     */
    private void effectuerDeplacement(ReseauElectrique reseau, Maison m, Generateur g) {
        reseau.supprimerConnexion(m);
        reseau.ajouterConnexion(m, g);
    }

    /**
     * État interne de l'optimisation.
     * 
     * <p>Encapsule :
     * <ul>
     *   <li>Le cache des charges par générateur (évite les recalculs)</li>
     *   <li>La meilleure configuration trouvée</li>
     *   <li>Les coûts actuel et minimal</li>
     * </ul>
     */
    private class EtatOptimisation {
        final Map<Generateur, Double> cache;
        final int[] meilleureConfig;
        double coutActuel;
        double meilleurCout;

        /**
         * Initialise l'état d'optimisation à partir du réseau.
         *
         * @param reseau le réseau électrique initial
         */
        EtatOptimisation(ReseauElectrique reseau) {
            this.cache = new HashMap<>();
            for (Generateur g : reseau.getGenerateurs()) {
                cache.put(g, calculateur.getSommeDesDemandesElectriques(g, reseau));
            }
            this.meilleureConfig = new int[reseau.getMaisons().size()];
            this.coutActuel = evaluerCout(reseau.getGenerateurs(), cache);
            this.meilleurCout = coutActuel;
            sauvegarderConfiguration(reseau, meilleureConfig);
        }
    }
}
