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
 * Service d'optimisation haute performance pour le réseau électrique.
 * 
 * Cette classe combine une construction gloutonne initiale et un recuit simulé
 * avec calcul de coût local via un cache.
 * 
 * Objectifs principaux :
 * - Obtenir rapidement une bonne solution initiale grâce à un algorithme glouton.
 * - Améliorer cette solution par des mouvements aléatoires (déplacement ou échange de maisons)
 *   en utilisant le critère de Metropolis.
 * - Réduire au maximum les recalculs coûteux en maintenant un cache des charges par générateur.
 */
public class OptimiseurReseau {

    private final CalculateurCouts calculateur;
    /** Durée maximale d'optimisation en millisecondes pour éviter les temps de calcul trop longs. */
    private static final long MAX_DURATION_MS = 3000; 

    public OptimiseurReseau(CalculateurCouts calculateur) {
        this.calculateur = calculateur;
    }

    /**
     * Lance l'optimisation du réseau.
     * 
     * Stratégie :
     * - Étape 1 : construction d'une solution initiale gloutonne où chaque maison
     *   est affectée au générateur qui minimise le coût global.
     * - Étape 2 : recuit simulé avec deux types de mouvements (déplacement et échange)
     *   pour échapper aux optima locaux.
     * - Le coût est évalué de manière locale grâce à un cache des charges,
     *   ce qui évite de recalculer toutes les sommes à chaque mouvement.
     * 
     * @param reseau le réseau électrique à optimiser (modifié en place)
     * @return le même réseau, dans une configuration de coût amélioré
     */
    public ReseauElectrique optimiser(ReseauElectrique reseau) {
        if (reseau.getMaisons().isEmpty() || reseau.getGenerateurs().isEmpty()) {
        	return reseau;
        }

        long startTime = System.currentTimeMillis();

        // 1. Construction gloutonne de la solution initiale
        for (Maison m : reseau.getMaisons()) {
        	reseau.supprimerConnexion(m);
        }
        
        List<Maison> maisonsTriees = new ArrayList<>(reseau.getMaisons());
        maisonsTriees.sort((m1, m2) -> Double.compare(m2.getConsommation(), m1.getConsommation()));
        construireSolutionGloutonne(reseau, maisonsTriees);

        // 2. Initialisation du cache des charges pour chaque générateur
        Map<Generateur, Double> chargesCache = new HashMap<>();
        List<Generateur> generateurs = reseau.getGenerateurs();
        
        for (Generateur g : generateurs) {
            chargesCache.put(g, calculateur.getSommeDesDemandesElectriques(g, reseau));
        }

        // Coût initial évalué via le cache
        double meilleurCoutGlobal = evaluerCoutAvecCache(generateurs, chargesCache);
        double coutActuel = meilleurCoutGlobal;

        int[] meilleureConfiguration = new int[reseau.getMaisons().size()];
        sauvegarderConfiguration(reseau, meilleureConfiguration);

        // 3. Boucle de recuit simulé
        Random rand = new Random();
        List<Maison> maisons = reseau.getMaisons();
        
        double temperature = 100.0;
        double tauxRefroidissement = 0.999; 

        while (System.currentTimeMillis() - startTime < MAX_DURATION_MS) {
            
            // Réchauffage léger si la température devient trop faible
            if (temperature < 0.001) temperature = 10.0;

            int typeMouvement = rand.nextInt(10);
            boolean mouvementParEchange = (typeMouvement < 4); // Environ 40% de swaps

            if (mouvementParEchange) {
                // --- ÉCHANGE (SWAP) ---
                int idx1 = rand.nextInt(maisons.size());
                int idx2 = rand.nextInt(maisons.size());
                Maison m1 = maisons.get(idx1);
                Maison m2 = maisons.get(idx2);
                
                Generateur g1 = reseau.trouverGenerateur(m1);
                Generateur g2 = reseau.trouverGenerateur(m2);

                if (g1 != g2) {
                    // Mise à jour temporaire du cache pour simuler l'échange
                    double conso1 = m1.getConsommation();
                    double conso2 = m2.getConsommation();
                    
                    chargesCache.put(g1, chargesCache.get(g1) - conso1 + conso2);
                    chargesCache.put(g2, chargesCache.get(g2) - conso2 + conso1);

                    double nouveauCout = evaluerCoutAvecCache(generateurs, chargesCache);
                    double delta = nouveauCout - coutActuel;

                    // Critère de Metropolis : accepter ou non le mouvement
                    if (delta < 0 || rand.nextDouble() < Math.exp(-delta / temperature)) {
                        echangerConnexionsMaisons(reseau, m1, g1, m2, g2);
                        coutActuel = nouveauCout;
                        if (coutActuel < meilleurCoutGlobal) {
                            meilleurCoutGlobal = coutActuel;
                            sauvegarderConfiguration(reseau, meilleureConfiguration);
                        }
                    } else {
                        // Mouvement refusé : annuler la modification du cache
                        chargesCache.put(g1, chargesCache.get(g1) + conso1 - conso2);
                        chargesCache.put(g2, chargesCache.get(g2) + conso2 - conso1);
                    }
                }
            } else {
                // --- DÉPLACEMENT (MOVE) ---
                Maison m = maisons.get(rand.nextInt(maisons.size()));
                Generateur gAncien = reseau.trouverGenerateur(m);
                Generateur gNouveau = generateurs.get(rand.nextInt(generateurs.size()));

                if (gAncien != gNouveau) {
                    double conso = m.getConsommation();
                    // Mise à jour temporaire du cache pour simuler le déplacement
                    chargesCache.put(gAncien, chargesCache.get(gAncien) - conso);
                    chargesCache.put(gNouveau, chargesCache.get(gNouveau) + conso);

                    double nouveauCout = evaluerCoutAvecCache(generateurs, chargesCache);
                    double delta = nouveauCout - coutActuel;

                    if (delta < 0 || rand.nextDouble() < Math.exp(-delta / temperature)) {
                        deplacerMaison(reseau, m, gAncien, gNouveau);
                        coutActuel = nouveauCout;
                        if (coutActuel < meilleurCoutGlobal) {
                            meilleurCoutGlobal = coutActuel;
                            sauvegarderConfiguration(reseau, meilleureConfiguration);
                        }
                    } else {
                        // Mouvement refusé : annuler la modification du cache
                        chargesCache.put(gAncien, chargesCache.get(gAncien) + conso);
                        chargesCache.put(gNouveau, chargesCache.get(gNouveau) - conso);
                    }
                }
            }
            temperature *= tauxRefroidissement;
        }

        appliquerConfiguration(reseau, meilleureConfiguration);
        System.out.println("Meilleur coût : " + meilleurCoutGlobal);
        return reseau;
    }

    // =========================================================================
    //  MOTEUR DE CALCUL LOCAL
    // =========================================================================

    /**
     * Évalue le coût global du réseau à partir du cache des charges.
     * 
     * Le coût est défini comme :
     * - une mesure de dispersion des taux de charge autour du taux moyen
     * - plus une pénalité de surcharge multipliée par le paramètre lambda
     * 
     * On ne relit pas le réseau : on utilise uniquement les charges stockées
     * pour chaque générateur, ce qui rend cette évaluation très rapide.
     * 
     * @param generateurs liste des générateurs du réseau
     * @param charges map associant à chaque générateur sa charge totale actuelle
     * @return valeur du coût global pour ces charges
     */
    private double evaluerCoutAvecCache(List<Generateur> generateurs, Map<Generateur, Double> charges) {
        double sommeTaux = 0.0;
        int n = generateurs.size();
        
        // Calcul du taux moyen d'utilisation des générateurs
        for (Generateur g : generateurs) {
            double cap = g.getCapaciteMaximale();
            if (cap > 0) sommeTaux += charges.get(g) / cap;
        }
        double tauxMoyen = sommeTaux / n;

        double dispersion = 0.0;
        double surcharge = 0.0;

        // Calcul de la dispersion et de la surcharge
        for (Generateur g : generateurs) {
            double cap = g.getCapaciteMaximale();
            if (cap > 0) {
                double charge = charges.get(g);
                double taux = charge / cap;
                
                dispersion += Math.abs(taux - tauxMoyen);
                surcharge += Math.max(0.0, (charge - cap) / cap);
            }
        }

        // Lambda vient du calculateur de coûts pour garder une définition cohérente
        return dispersion + (calculateur.getLambda() * surcharge);
    }

    // =========================================================================
    //  CONSTRUCTION INITIALE GLOUTONNE
    // =========================================================================

    /**
     * Construit une solution initiale gloutonne.
     * 
     * Stratégie :
     * - Les maisons sont triées par consommation décroissante.
     * - Pour chaque maison, on teste tous les générateurs et on conserve celui
     *   qui minimise le coût global calculé par le CalculateurCouts.
     * - L'idée est de commencer le recuit simulé à partir d'une solution déjà raisonnablement bonne.
     * 
     * @param reseau réseau à initialiser
     * @param maisons liste de maisons déjà triée par consommation décroissante
     */
    private void construireSolutionGloutonne(ReseauElectrique reseau, List<Maison> maisons) {
        List<Generateur> generateurs = reseau.getGenerateurs();
        for (Maison m : maisons) {
            Generateur meilleurGenerateur = null;
            double meilleurCout = Double.MAX_VALUE;
            for (Generateur g : generateurs) {
                reseau.ajouterConnexion(m, g);
                double c = calculateur.calculerCout(reseau).getCoutGlobale();
                if (c < meilleurCout) { 
                    meilleurCout = c; 
                    meilleurGenerateur = g; 
                }
                reseau.supprimerConnexion(m);
            }
            if (meilleurGenerateur != null) reseau.ajouterConnexion(m, meilleurGenerateur);
        }
    }

    // =========================================================================
    //  GESTION DES CONFIGURATIONS (SAUVEGARDE / RESTAURATION)
    // =========================================================================

    /**
     * Sauvegarde la configuration actuelle du réseau dans un tableau d'indices.
     * 
     * Pour chaque maison, on mémorise l'indice du générateur auquel elle est connectée.
     * Cela permet de restaurer rapidement la meilleure solution trouvée à la fin du recuit.
     * 
     * @param reseau réseau dont on veut mémoriser la configuration
     * @param config tableau dans lequel on stocke l'indice du générateur par maison
     */
    private void sauvegarderConfiguration(ReseauElectrique reseau, int[] config) {
        List<Generateur> generateurs = reseau.getGenerateurs();
        List<Maison> maisons = reseau.getMaisons();
        int i = 0;
        for (Maison m : maisons) {
            Generateur g = reseau.trouverGenerateur(m);
            config[i++] = (g != null) ? generateurs.indexOf(g) : -1;
        }
    }

    /**
     * Applique une configuration mémorisée au réseau.
     * 
     * Pour chaque maison, on supprime l'ancienne connexion et on la reconnecte
     * au générateur dont l'indice a été sauvegardé dans le tableau.
     * Cela permet de revenir exactement à la meilleure solution trouvée
     * après la phase de recuit simulé.
     * 
     * @param reseau réseau sur lequel appliquer la configuration
     * @param config tableau d'indices de générateurs par maison
     */
    private void appliquerConfiguration(ReseauElectrique reseau, int[] config) {
        List<Generateur> generateurs = reseau.getGenerateurs();
        List<Maison> maisons = reseau.getMaisons();
        int i = 0;
        for (Maison m : maisons) {
            reseau.supprimerConnexion(m);
            if (config[i] != -1) {
                reseau.ajouterConnexion(m, generateurs.get(config[i]));
            }
            i++;
        }
    }

    // =========================================================================
    //  MOUVEMENTS DE BASE SUR LE RÉSEAU
    // =========================================================================

    /**
     * Échange les générateurs de deux maisons.
     * 
     * Utilisé dans les mouvements de type SWAP du recuit simulé.
     * Permet de modifier la répartition des charges sans modifier le nombre
     * de maisons affectées à chaque générateur.
     * 
     * @param reseau réseau dans lequel effectuer l'échange
     * @param m1 première maison
     * @param g1 générateur actuel de la première maison
     * @param m2 seconde maison
     * @param g2 générateur actuel de la seconde maison
     */
    private void echangerConnexionsMaisons(ReseauElectrique reseau, Maison m1, Generateur g1, Maison m2, Generateur g2) {
        reseau.supprimerConnexion(m1); 
        reseau.supprimerConnexion(m2);
        reseau.ajouterConnexion(m1, g2); 
        reseau.ajouterConnexion(m2, g1);
    }

    /**
     * Déplace une maison d'un générateur vers un autre.
     * 
     * Utilisé dans les mouvements de type MOVE du recuit simulé.
     * Ce mouvement permet de charger légèrement plus un générateur
     * et de soulager un autre, afin de réduire la dispersion ou la surcharge.
     * 
     * @param reseau réseau dans lequel effectuer le déplacement
     * @param maison maison à déplacer
     * @param ancienGenerateur générateur source
     * @param nouveauGenerateur générateur destination
     */
    private void deplacerMaison(ReseauElectrique reseau, Maison maison, Generateur ancienGenerateur, Generateur nouveauGenerateur) {
        reseau.supprimerConnexion(maison);
        reseau.ajouterConnexion(maison, nouveauGenerateur);
    }
}
