package up.mi.paa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link Couts}.
 * 
 * <p>Vérifie le comportement du conteneur de résultats d'évaluation
 * du réseau électrique.
 * 
 * @author Groupe 10
 * @version 1.0
 */
@DisplayName("Tests de Couts")
class CoutsTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("Les valeurs sont correctement stockées")
        void testConstruction() {
            Couts couts = new Couts(15.5, 5.0, 1.05);
            
            assertEquals(15.5, couts.getCoutGlobale(), 1e-9);
            assertEquals(5.0, couts.getDispersion(), 1e-9);
            assertEquals(1.05, couts.getSurcharge(), 1e-9);
        }

        @Test
        @DisplayName("Les valeurs nulles sont acceptées")
        void testValeursNulles() {
            Couts couts = new Couts(0.0, 0.0, 0.0);
            
            assertEquals(0.0, couts.getCoutGlobale(), 1e-9);
            assertEquals(0.0, couts.getDispersion(), 1e-9);
            assertEquals(0.0, couts.getSurcharge(), 1e-9);
        }

        @Test
        @DisplayName("Les valeurs négatives sont acceptées")
        void testValeursNegatives() {
            // Bien que non réalistes, le conteneur doit les accepter
            Couts couts = new Couts(-1.0, -2.0, -3.0);
            
            assertEquals(-1.0, couts.getCoutGlobale(), 1e-9);
            assertEquals(-2.0, couts.getDispersion(), 1e-9);
            assertEquals(-3.0, couts.getSurcharge(), 1e-9);
        }
    }

    @Nested
    @DisplayName("Accesseurs")
    class AccesseursTests {

        @Test
        @DisplayName("getCoutGlobale retourne la valeur correcte")
        void testGetCoutGlobale() {
            Couts couts = new Couts(10.5, 3.0, 0.75);
            assertEquals(10.5, couts.getCoutGlobale(), 1e-9);
        }

        @Test
        @DisplayName("getDispersion retourne la valeur correcte")
        void testGetDispersion() {
            Couts couts = new Couts(10.5, 3.0, 0.75);
            assertEquals(3.0, couts.getDispersion(), 1e-9);
        }

        @Test
        @DisplayName("getSurcharge retourne la valeur correcte")
        void testGetSurcharge() {
            Couts couts = new Couts(10.5, 3.0, 0.75);
            assertEquals(0.75, couts.getSurcharge(), 1e-9);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString retourne le format attendu")
        void testToString() {
            Couts couts = new Couts(15.55, 5.25, 1.03);
            // Le format dépend de la locale (virgule en français, point en anglais)
            String result = couts.toString();
            assertTrue(result.contains("15") && result.contains("55"));
            assertTrue(result.contains("dispersion"));
            assertTrue(result.contains("surcharge"));
        }

        @Test
        @DisplayName("toString arrondit à 2 décimales")
        void testToStringArrondi() {
            Couts couts = new Couts(15.556789, 5.254321, 1.037890);
            String result = couts.toString();
            assertTrue(result.contains("15") && (result.contains("56") || result.contains(",56")));
            assertTrue(result.contains("5") && (result.contains("25") || result.contains(",25")));
            assertTrue(result.contains("1") && (result.contains("04") || result.contains(",04")));
        }

        @Test
        @DisplayName("toString avec valeurs nulles")
        void testToStringValeursNulles() {
            Couts couts = new Couts(0.0, 0.0, 0.0);
            String result = couts.toString();
            assertTrue(result.contains("0") && result.contains("00"));
            assertTrue(result.contains("dispersion"));
            assertTrue(result.contains("surcharge"));
        }
    }

    @Nested
    @DisplayName("Cohérence mathématique")
    class CoherenceMathematiqueTests {

        @Test
        @DisplayName("Le coût global égale dispersion + lambda * surcharge")
        void testFormuleCout() {
            int lambda = 10;
            double dispersion = 2.5;
            double surcharge = 0.5;
            double coutGlobal = dispersion + lambda * surcharge;
            
            Couts couts = new Couts(coutGlobal, dispersion, surcharge);
            
            assertEquals(coutGlobal, couts.getDispersion() + lambda * couts.getSurcharge(), 1e-9);
        }

        @Test
        @DisplayName("Le coût global est toujours >= dispersion si surcharge >= 0")
        void testCoutMinimal() {
            Couts couts = new Couts(10.0, 5.0, 0.5);
            assertTrue(couts.getCoutGlobale() >= couts.getDispersion());
        }
    }
}
