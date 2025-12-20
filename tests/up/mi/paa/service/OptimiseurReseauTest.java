package up.mi.paa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import up.mi.paa.model.Couts;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.ReseauElectrique;
import up.mi.paa.model.TypeConsommation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link OptimiseurReseau}.
 * 
 * <p>Vérifie le bon fonctionnement de l'algorithme d'optimisation
 * hybride (Glouton + Recuit Simulé) :
 * <ul>
 *   <li>Connexion de toutes les maisons</li>
 *   <li>Réduction du coût global</li>
 *   <li>Répartition équilibrée des charges</li>
 * </ul>
 * 
 * @author Groupe 10
 * @version 1.0
 */
@DisplayName("Tests de OptimiseurReseau")
class OptimiseurReseauTest {

    private CalculateurCouts calculateur;
    private OptimiseurReseau optimiseur;
    private ReseauElectrique reseau;

    @BeforeEach
    void beforeEach() {
        calculateur = new CalculateurCouts(10);
        optimiseur = new OptimiseurReseau(calculateur);
        reseau = new ReseauElectrique();
    }

    @Nested
    @DisplayName("Cas limites")
    class CasLimitesTests {

        @Test
        @DisplayName("Réseau vide retourne un réseau vide")
        void reseauVide() {
            ReseauElectrique resultat = optimiseur.optimiser(reseau);

            assertNotNull(resultat);
            assertTrue(resultat.getGenerateurs().isEmpty());
            assertTrue(resultat.getMaisons().isEmpty());
        }

        @Test
        @DisplayName("Un seul générateur et une seule maison")
        void unGenerateurUneMaison() {
            Generateur g1 = new Generateur("G1", 100);
            Maison m1 = new Maison("M1", TypeConsommation.BASSE);

            reseau.ajouterGenerateur(g1);
            reseau.ajouterMaison(m1);

            optimiseur.optimiser(reseau);

            assertTrue(reseau.maisonEstConnectee(m1));
            assertEquals(g1, reseau.trouverGenerateur(m1));
        }

        @Test
        @DisplayName("Générateurs sans maisons")
        void generateursSansMaisons() {
            reseau.ajouterGenerateur(new Generateur("G1", 100));
            reseau.ajouterGenerateur(new Generateur("G2", 100));

            ReseauElectrique resultat = optimiseur.optimiser(reseau);

            assertNotNull(resultat);
            assertEquals(2, resultat.getGenerateurs().size());
        }
    }

    @Nested
    @DisplayName("Connexion des maisons")
    class ConnexionMaisonsTests {

        @Test
        @DisplayName("Toutes les maisons sont connectées après optimisation")
        void toutesLesMaisonsConnectees() {
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
                    "La maison " + m.getNom() + " devrait être connectée");
            }
        }

        @Test
        @DisplayName("Maisons de différents types toutes connectées")
        void maisonsTypesVariesConnectees() {
            reseau.ajouterGenerateur(new Generateur("G1", 200));

            reseau.ajouterMaison(new Maison("M1", TypeConsommation.BASSE));
            reseau.ajouterMaison(new Maison("M2", TypeConsommation.NORMAL));
            reseau.ajouterMaison(new Maison("M3", TypeConsommation.FORTE));

            optimiseur.optimiser(reseau);

            assertTrue(reseau.toutesLesMaisonsConnectees());
        }
    }

    @Nested
    @DisplayName("Optimisation du coût")
    class OptimisationCoutTests {

        @Test
        @DisplayName("Réduction du coût après optimisation")
        void reductionCout() {
            Generateur g1 = new Generateur("G1", 50);
            Generateur g2 = new Generateur("G2", 50);
            reseau.ajouterGenerateur(g1);
            reseau.ajouterGenerateur(g2);

            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            Maison m2 = new Maison("M2", TypeConsommation.NORMAL);
            reseau.ajouterMaison(m1);
            reseau.ajouterMaison(m2);

            reseau.ajouterConnexion(m1, g1);
            reseau.ajouterConnexion(m2, g1);

            Couts coutAvant = calculateur.calculerCout(reseau);

            optimiseur.optimiser(reseau);

            Couts coutApres = calculateur.calculerCout(reseau);

            assertTrue(coutApres.getCoutGlobale() <= coutAvant.getCoutGlobale(),
                "L'optimisation ne devrait pas augmenter le coût");
        }

        @Test
        @DisplayName("Répartition équilibrée entre générateurs")
        void repartitionEquilibree() {
            Generateur g1 = new Generateur("G1", 50);
            Generateur g2 = new Generateur("G2", 50);
            reseau.ajouterGenerateur(g1);
            reseau.ajouterGenerateur(g2);

            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            Maison m2 = new Maison("M2", TypeConsommation.NORMAL);
            reseau.ajouterMaison(m1);
            reseau.ajouterMaison(m2);

            reseau.ajouterConnexion(m1, g1);
            reseau.ajouterConnexion(m2, g1);

            optimiseur.optimiser(reseau);

            Generateur genM1 = reseau.trouverGenerateur(m1);
            Generateur genM2 = reseau.trouverGenerateur(m2);

            assertNotEquals(genM1, genM2,
                "Pour minimiser la dispersion, les maisons devraient être réparties");
        }
    }

    @Nested
    @DisplayName("Stabilité de l'algorithme")
    class StabiliteTests {

        @Test
        @DisplayName("Plusieurs exécutions produisent un résultat valide")
        void plusieursExecutions() {
            Generateur g1 = new Generateur("G1", 100);
            Generateur g2 = new Generateur("G2", 100);
            reseau.ajouterGenerateur(g1);
            reseau.ajouterGenerateur(g2);

            for (int i = 0; i < 5; i++) {
                reseau.ajouterMaison(new Maison("M" + i, TypeConsommation.NORMAL));
            }

            for (int run = 0; run < 3; run++) {
                ReseauElectrique resultat = optimiseur.optimiser(reseau);
                
                assertNotNull(resultat);
                assertTrue(reseau.toutesLesMaisonsConnectees());
            }
        }

        @Test
        @DisplayName("Coût toujours positif ou nul")
        void coutPositif() {
            reseau.ajouterGenerateur(new Generateur("G1", 100));
            reseau.ajouterMaison(new Maison("M1", TypeConsommation.NORMAL));

            optimiseur.optimiser(reseau);

            Couts couts = calculateur.calculerCout(reseau);
            assertTrue(couts.getCoutGlobale() >= 0);
            assertTrue(couts.getDispersion() >= 0);
            assertTrue(couts.getSurcharge() >= 0);
        }
    }
}