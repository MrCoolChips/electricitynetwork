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
 * Service d'optimisation du réseau électrique.
 * Combine construction gloutonne et recuit simulé.
 * 
 * @author Groupe 10
 */
public class OptimiseurReseau {

    private static final long DUREE_MAX_MS = 3000;
    private static final double TEMPERATURE_INITIALE = 100.0;
    private static final double FACTEUR_REFROIDISSEMENT = 0.999;
    private static final double SEUIL_RECHAUFFAGE = 0.001;
    private static final double TEMPERATURE_RECHAUFFAGE = 10.0;
    private static final double RATIO_ECHANGE = 0.4;

    private final CalculateurCouts calculateur;
    private final Random random = new Random();

    public OptimiseurReseau(CalculateurCouts calculateur) {
        this.calculateur = calculateur;
    }

    /**
     * Optimise le réseau électrique en minimisant la fonction de coût.
     * 
     * @param reseau Le réseau à optimiser (modifié en place)
     * @return Le réseau optimisé
     */
    public ReseauElectrique optimiser(ReseauElectrique reseau) {
        if (reseau.getMaisons().isEmpty() || reseau.getGenerateurs().isEmpty()) {
            return reseau;
        }

        construireSolutionGloutonne(reseau);
        
        Map<Generateur, Double> cache = initialiserCache(reseau);
        EtatOptimisation etat = new EtatOptimisation(reseau, cache);
        
        executerRecuitSimule(reseau, etat);
        
        appliquerConfiguration(reseau, etat.meilleureConfig);
        System.out.println("Meilleur coût : " + etat.meilleurCout);
        
        return reseau;
    }

    /**
     * Construit une solution initiale par algorithme glouton Best-Fit Decreasing.
     */
    private void construireSolutionGloutonne(ReseauElectrique reseau) {
        reseau.getMaisons().forEach(reseau::supprimerConnexion);
        
        List<Maison> maisonsTries = new ArrayList<>(reseau.getMaisons());
        maisonsTries.sort((a, b) -> Double.compare(b.getConsommation(), a.getConsommation()));
        
        for (Maison m : maisonsTries) {
            Generateur meilleur = trouverMeilleurGenerateur(reseau, m);
            if (meilleur != null) {
                reseau.ajouterConnexion(m, meilleur);
            }
        }
    }

    private Generateur trouverMeilleurGenerateur(ReseauElectrique reseau, Maison m) {
        Generateur meilleur = null;
        double coutMin = Double.MAX_VALUE;
        
        for (Generateur g : reseau.getGenerateurs()) {
            reseau.ajouterConnexion(m, g);
            double cout = calculateur.calculerCout(reseau).getCoutGlobale();
            if (cout < coutMin) {
                coutMin = cout;
                meilleur = g;
            }
            reseau.supprimerConnexion(m);
        }
        return meilleur;
    }

    /**
     * Exécute la phase de recuit simulé.
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

    private void tenterEchange(ReseauElectrique reseau, EtatOptimisation etat, 
                               List<Maison> maisons, List<Generateur> generateurs, double temperature) {
        Maison maison1 = maisons.get(random.nextInt(maisons.size()));
        Maison maison2 = maisons.get(random.nextInt(maisons.size()));
        Generateur generateur1 = reseau.trouverGenerateur(maison1);
        Generateur generateur2 = reseau.trouverGenerateur(maison2);
        
        if (generateur1 == null || generateur2 == null || generateur1 == generateur2) return;

        double conso1 = maison1.getConsommation();
        double conso2 = maison2.getConsommation();

        modifierCache(etat.cache, generateur1, -conso1 + conso2);
        modifierCache(etat.cache, generateur2, -conso2 + conso1);

        double nouveauCout = evaluerCout(generateurs, etat.cache);
        double deltaCout = nouveauCout - etat.coutActuel;

        if (accepter(deltaCout, temperature)) {
            effectuerEchange(reseau, maison1, maison2, generateur1, generateur2);
            etat.coutActuel = nouveauCout;
            mettreAJourMeilleur(reseau, etat);
        } else {
            modifierCache(etat.cache, generateur1, conso1 - conso2);
            modifierCache(etat.cache, generateur2, conso2 - conso1);
        }
    }

    private void tenterDeplacement(ReseauElectrique reseau, EtatOptimisation etat,
                                   List<Maison> maisons, List<Generateur> generateurs, double temperature) {
        Maison maison = maisons.get(random.nextInt(maisons.size()));
        Generateur ancienGenerateur = reseau.trouverGenerateur(maison);
        Generateur nouveauGenerateur = generateurs.get(random.nextInt(generateurs.size()));
        
        if (ancienGenerateur == null || ancienGenerateur == nouveauGenerateur) return;

        double consommation = maison.getConsommation();

        modifierCache(etat.cache, ancienGenerateur, -consommation);
        modifierCache(etat.cache, nouveauGenerateur, consommation);

        double nouveauCout = evaluerCout(generateurs, etat.cache);
        double deltaCout = nouveauCout - etat.coutActuel;

        if (accepter(deltaCout, temperature)) {
            effectuerDeplacement(reseau, maison, nouveauGenerateur);
            etat.coutActuel = nouveauCout;
            mettreAJourMeilleur(reseau, etat);
        } else {
            modifierCache(etat.cache, ancienGenerateur, consommation);
            modifierCache(etat.cache, nouveauGenerateur, -consommation);
        }
    }

    private boolean accepter(double deltaCout, double temperature) {
        return deltaCout < 0 || random.nextDouble() < Math.exp(-deltaCout / temperature);
    }

    private Map<Generateur, Double> initialiserCache(ReseauElectrique reseau) {
        Map<Generateur, Double> cache = new HashMap<>();
        for (Generateur g : reseau.getGenerateurs()) {
            cache.put(g, calculateur.getSommeDesDemandesElectriques(g, reseau));
        }
        return cache;
    }

    private void modifierCache(Map<Generateur, Double> cache, Generateur generateur, double variation) {
        cache.put(generateur, cache.get(generateur) + variation);
    }

    /**
     * Évalue le coût à partir du cache (sans relire le réseau).
     */
    private double evaluerCout(List<Generateur> generateurs, Map<Generateur, Double> cache) {
        int nombreGenerateurs = generateurs.size();
        double sommeTauxUtilisation = 0.0;
        
        for (Generateur generateur : generateurs) {
            double capacite = generateur.getCapaciteMaximale();
            if (capacite > 0) {
                sommeTauxUtilisation += cache.get(generateur) / capacite;
            }
        }
        
        double tauxMoyen = sommeTauxUtilisation / nombreGenerateurs;
        double dispersion = 0.0;
        double surcharge = 0.0;

        for (Generateur generateur : generateurs) {
            double capacite = generateur.getCapaciteMaximale();
            if (capacite > 0) {
                double charge = cache.get(generateur);
                double tauxUtilisation = charge / capacite;
                dispersion += Math.abs(tauxUtilisation - tauxMoyen);
                surcharge += Math.max(0.0, (charge - capacite) / capacite);
            }
        }

        return dispersion + calculateur.getLambda() * surcharge;
    }

    private void mettreAJourMeilleur(ReseauElectrique reseau, EtatOptimisation etat) {
        if (etat.coutActuel < etat.meilleurCout) {
            etat.meilleurCout = etat.coutActuel;
            sauvegarderConfiguration(reseau, etat.meilleureConfig);
        }
    }

    private void sauvegarderConfiguration(ReseauElectrique reseau, int[] configuration) {
        List<Generateur> generateurs = reseau.getGenerateurs();
        List<Maison> maisons = reseau.getMaisons();
        for (int i = 0; i < maisons.size(); i++) {
            Generateur generateur = reseau.trouverGenerateur(maisons.get(i));
            configuration[i] = (generateur != null) ? generateurs.indexOf(generateur) : -1;
        }
    }

    private void appliquerConfiguration(ReseauElectrique reseau, int[] configuration) {
        List<Generateur> generateurs = reseau.getGenerateurs();
        List<Maison> maisons = reseau.getMaisons();
        for (int i = 0; i < maisons.size(); i++) {
            reseau.supprimerConnexion(maisons.get(i));
            if (configuration[i] >= 0 && configuration[i] < generateurs.size()) {
                reseau.ajouterConnexion(maisons.get(i), generateurs.get(configuration[i]));
            }
        }
    }

    private void effectuerEchange(ReseauElectrique reseau, Maison maison1, Maison maison2, 
                                   Generateur generateur1, Generateur generateur2) {
        reseau.supprimerConnexion(maison1);
        reseau.supprimerConnexion(maison2);
        reseau.ajouterConnexion(maison1, generateur2);
        reseau.ajouterConnexion(maison2, generateur1);
    }

    private void effectuerDeplacement(ReseauElectrique reseau, Maison maison, Generateur nouveauGenerateur) {
        reseau.supprimerConnexion(maison);
        reseau.ajouterConnexion(maison, nouveauGenerateur);
    }

    /**
     * État courant de l'optimisation.
     */
    private class EtatOptimisation {
        final Map<Generateur, Double> cache;
        final int[] meilleureConfig;
        double coutActuel;
        double meilleurCout;

        EtatOptimisation(ReseauElectrique reseau, Map<Generateur, Double> cache) {
            this.cache = cache;
            this.meilleureConfig = new int[reseau.getMaisons().size()];
            this.coutActuel = evaluerCout(reseau.getGenerateurs(), cache);
            this.meilleurCout = coutActuel;
            sauvegarderConfiguration(reseau, meilleureConfig);
        }
    }
}
