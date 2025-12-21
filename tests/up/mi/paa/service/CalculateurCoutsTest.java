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
 * Tests unitaires pour la classe {@link CalculateurCouts}.
 * 
 * <p>Vérifie le calcul des coûts selon la formule :
 * <pre>Coût = Dispersion + (Lambda × Surcharge)</pre>
 * 
 * <p>Tests organisés par fonctionnalité :
 * <ul>
 *   <li>Gestion du paramètre Lambda</li>
 *   <li>Calcul de la demande électrique</li>
 *   <li>Calcul du taux d'utilisation</li>
 *   <li>Calcul du coût global</li>
 *   <li>Détection des surcharges</li>
 * </ul>
 * 
 * @author Groupe 10
 * @version 1.0
 */
@DisplayName("Tests de CalculateurCouts")
class CalculateurCoutsTest {

    private CalculateurCouts calculateur;
    private ReseauElectrique reseau;

    @BeforeEach
    void beforeEach() {
        calculateur = new CalculateurCouts(10);
        reseau = new ReseauElectrique();
    }

    @Nested
    @DisplayName("Gestion du paramètre Lambda")
    class LambdaTests {

        @Test
        @DisplayName("Getter retourne la valeur initiale")
        void getLambdaInitial() {
            assertEquals(10, calculateur.getLambda());
        }

        @Test
        @DisplayName("Setter modifie la valeur de Lambda")
        void setLambda() {
            calculateur.setLambda(50);
            assertEquals(50, calculateur.getLambda());
        }

        @Test
        @DisplayName("Lambda à zéro est accepté")
        void lambdaZero() {
            calculateur.setLambda(0);
            assertEquals(0, calculateur.getLambda());
        }
    }

    @Nested
    @DisplayName("Calcul de la demande électrique")
    class DemandeElectriqueTests {

        @Test
        @DisplayName("Somme correcte pour plusieurs maisons")
        void sommeDemandesPlusieurs() {
            Generateur g1 = new Generateur("G1", 100);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            Maison m2 = new Maison("M2", TypeConsommation.FORTE);

            reseau.ajouterGenerateur(g1);
            reseau.ajouterMaison(m1);
            reseau.ajouterMaison(m2);
            reseau.ajouterConnexion(m1, g1);
            reseau.ajouterConnexion(m2, g1);

            double sommeAttendue = m1.getConsommation() + m2.getConsommation();
            assertEquals(sommeAttendue, 
                calculateur.getSommeDesDemandesElectriques(g1, reseau), 1e-9);
        }

        @Test
        @DisplayName("Retourne zéro pour générateur sans maisons")
        void sommeDemandesVide() {
            Generateur g1 = new Generateur("G1", 100);
            reseau.ajouterGenerateur(g1);

            assertEquals(0.0, 
                calculateur.getSommeDesDemandesElectriques(g1, reseau), 1e-9);
        }
    }

    @Nested
    @DisplayName("Calcul du taux d'utilisation")
    class TauxUtilisationTests {

        @Test
        @DisplayName("Taux = Demande / Capacité")
        void tauxUtilisationNormal() {
            Generateur g1 = new Generateur("G1", 100);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);

            reseau.ajouterGenerateur(g1);
            reseau.ajouterMaison(m1);
            reseau.ajouterConnexion(m1, g1);

            double tauxAttendu = m1.getConsommation() / 100.0;
            assertEquals(tauxAttendu, 
                calculateur.calculerLeTauxDUtilisation(g1, reseau), 1e-9);
        }

        @Test
        @DisplayName("Exception si capacité = 0 (division par zéro)")
        void tauxUtilisationCapaciteZero() {
            Generateur g1 = new Generateur("G_Zero", 0);

            assertThrows(ArithmeticException.class, 
                () -> calculateur.calculerLeTauxDUtilisation(g1, reseau));
        }

        @Test
        @DisplayName("Taux > 1 indique une surcharge")
        void tauxUtilisationSurcharge() {
            Generateur g1 = new Generateur("G1", 1);
            Maison m1 = new Maison("M1", TypeConsommation.FORTE);

            reseau.ajouterGenerateur(g1);
            reseau.ajouterMaison(m1);
            reseau.ajouterConnexion(m1, g1);

            assertTrue(calculateur.calculerLeTauxDUtilisation(g1, reseau) > 1.0);
        }
    }

    @Nested
    @DisplayName("Calcul du coût global")
    class CoutGlobalTests {

        @Test
        @DisplayName("Réseau vide retourne un coût nul")
        void coutReseauVide() {
            Couts couts = calculateur.calculerCout(reseau);
            assertNotNull(couts);
            assertEquals(0.0, couts.getCoutGlobale(), 0.001);
        }

        @Test
        @DisplayName("Coût valide avec générateurs et maisons")
        void coutScenarioComplexe() {
            Generateur g1 = new Generateur("G1", 100);
            Generateur g2 = new Generateur("G2", 100);
            reseau.ajouterGenerateur(g1);
            reseau.ajouterGenerateur(g2);

            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            reseau.ajouterMaison(m1);
            reseau.ajouterConnexion(m1, g1);

            Couts resultat = calculateur.calculerCout(reseau);

            assertNotNull(resultat);
            assertTrue(resultat.getCoutGlobale() >= 0);
            assertTrue(resultat.getDispersion() >= 0);
            assertTrue(resultat.getSurcharge() >= 0);
        }

        @Test
        @DisplayName("Formule respectée : Coût = Dispersion + (Lambda × Surcharge)")
        void coutFormuleVerifiee() {
            Generateur g1 = new Generateur("G1", 100);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);

            reseau.ajouterGenerateur(g1);
            reseau.ajouterMaison(m1);
            reseau.ajouterConnexion(m1, g1);

            Couts couts = calculateur.calculerCout(reseau);

            double coutCalcule = couts.getDispersion() + (10 * couts.getSurcharge());
            assertEquals(coutCalcule, couts.getCoutGlobale(), 1e-9);
        }
    }

    @Nested
    @DisplayName("Détection des surcharges")
    class SurchargeTests {

        @Test
        @DisplayName("Surcharge détectée quand demande > capacité")
        void surchargeDetectee() {
            Generateur g1 = new Generateur("G1", 10);
            reseau.ajouterGenerateur(g1);

            for (int i = 0; i < 5; i++) {
                Maison m = new Maison("M" + i, TypeConsommation.FORTE);
                reseau.ajouterMaison(m);
                reseau.ajouterConnexion(m, g1);
            }

            Couts couts = calculateur.calculerCout(reseau);
            assertTrue(couts.getSurcharge() > 0);
        }

        @Test
        @DisplayName("Pas de surcharge si capacité suffisante")
        void pasDeSurcharge() {
            Generateur g1 = new Generateur("G1", 10000);
            Maison m1 = new Maison("M1", TypeConsommation.BASSE);

            reseau.ajouterGenerateur(g1);
            reseau.ajouterMaison(m1);
            reseau.ajouterConnexion(m1, g1);

            Couts couts = calculateur.calculerCout(reseau);
            assertEquals(0.0, couts.getSurcharge(), 1e-9);
        }

        @Test
        @DisplayName("Impact de Lambda sur le coût en cas de surcharge")
        void impactLambdaSurCout() {
            Generateur g1 = new Generateur("G1", 10);
            Maison m1 = new Maison("M1", TypeConsommation.FORTE);

            reseau.ajouterGenerateur(g1);
            reseau.ajouterMaison(m1);
            reseau.ajouterConnexion(m1, g1);

            calculateur.setLambda(1);
            Couts coutsLambda1 = calculateur.calculerCout(reseau);

            calculateur.setLambda(100);
            Couts coutsLambda100 = calculateur.calculerCout(reseau);

            assertTrue(coutsLambda100.getCoutGlobale() > coutsLambda1.getCoutGlobale(),
                "Un Lambda plus élevé doit augmenter le coût en cas de surcharge");
        }
    }
}