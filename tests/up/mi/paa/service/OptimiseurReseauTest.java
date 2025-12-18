package up.mi.paa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import up.mi.paa.model.Couts;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.ReseauElectrique;
import up.mi.paa.model.TypeConsommation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe OptimiseurReseau.
 * Vérifie le bon fonctionnement de l'algorithme hybride (Glouton + Recuit Simulé).
 */
class OptimiseurReseauTest {

    private CalculateurCouts calculateur;
    private OptimiseurReseau optimiseur;
    private ReseauElectrique reseau;

    @BeforeEach
    void beforeEach() {
        // Lambda = 10 pour pénaliser les surcharges
        calculateur = new CalculateurCouts(10);
        optimiseur = new OptimiseurReseau(calculateur);
        reseau = new ReseauElectrique();
    }

    @Test
    void testOptimiser_ReseauVide() {
        // Exécution sur un réseau vide
        ReseauElectrique resultat = optimiseur.optimiser(reseau);
        
        assertNotNull(resultat);
        assertTrue(resultat.getGenerateurs().isEmpty());
        assertTrue(resultat.getMaisons().isEmpty());
    }

    @Test
    void testOptimiser_ConnecteToutesLesMaisons() {
        Generateur g1 = new Generateur("G1", 100);
        Generateur g2 = new Generateur("G2", 100);
        reseau.ajouterGenerateur(g1);
        reseau.ajouterGenerateur(g2);

        for (int i = 0; i < 4; i++) {
            reseau.ajouterMaison(new Maison("M" + i, TypeConsommation.NORMAL));
        }

        optimiseur.optimiser(reseau);

        for (Maison m : reseau.getMaisons()) {
            assertTrue(reseau.maisonEstConnectee(m), 
                "La maison " + m.getNom() + " devrait être connectée après optimisation");
        }
    }

    @Test
    void testOptimiser_ReductionCout() {
        
        Generateur g1 = new Generateur("G1", 50);
        Generateur g2 = new Generateur("G2", 50);
        reseau.ajouterGenerateur(g1);
        reseau.ajouterGenerateur(g2);

        Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
        Maison m2 = new Maison("M2", TypeConsommation.NORMAL);
        
        reseau.ajouterMaison(m1);
        reseau.ajouterMaison(m2);

        // Force une mauvaise connexion initiale
        reseau.ajouterConnexion(m1, g1);
        reseau.ajouterConnexion(m2, g1);

        Couts coutAvant = calculateur.calculerCout(reseau);

        optimiseur.optimiser(reseau);

        Couts coutApres = calculateur.calculerCout(reseau);

        System.out.println("Coût Avant: " + coutAvant.getCoutGlobale());
        System.out.println("Coût Après: " + coutApres.getCoutGlobale());

        assertTrue(coutApres.getCoutGlobale() < coutAvant.getCoutGlobale(), 
            "L'optimisation devrait réduire le coût global (résoudre la surcharge)");
        
        // Vérification logique : Ils devraient être sur des générateurs différents
        Generateur genM1 = reseau.trouverGenerateur(m1);
        Generateur genM2 = reseau.trouverGenerateur(m2);
        
        assertNotEquals(genM1, genM2, 
            "Pour minimiser la surcharge et la dispersion, les maisons devraient être réparties");
    }

    @Test
    void testOptimiser_UnSeulGenerateurSuffisant() {
        // L'optimiseur doit simplement connecter la maison.
        Generateur g1 = new Generateur("G1", 100);
        Maison m1 = new Maison("M1", TypeConsommation.BASSE);
        
        reseau.ajouterGenerateur(g1);
        reseau.ajouterMaison(m1);

        optimiseur.optimiser(reseau);

        assertTrue(reseau.maisonEstConnectee(m1));
        assertEquals(g1, reseau.trouverGenerateur(m1));
    }
}