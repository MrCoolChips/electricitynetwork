package up.mi.paa.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link ComparateurNaturel}.
 * 
 * <p>Vérifie le tri naturel des chaînes alphanumériques :
 * <ul>
 *   <li>Tri correct de "M1, M2, M10" (et non "M1, M10, M2")</li>
 *   <li>Gestion des préfixes différents</li>
 *   <li>Gestion des valeurs null</li>
 * </ul>
 * 
 * @author Groupe 10
 * @version 1.0
 */
@DisplayName("Tests de ComparateurNaturel")
class ComparateurNaturelTest {

    @Nested
    @DisplayName("Tri naturel des chaînes")
    class TriNaturelTests {

        @Test
        @DisplayName("Tri correct : M1, M2, M10 (pas M1, M10, M2)")
        void triNaturelNumerique() {
            List<String> liste = Arrays.asList("M10", "M2", "M1", "M20", "M3");
            liste.sort(ComparateurNaturel.pourChaines());

            assertEquals(Arrays.asList("M1", "M2", "M3", "M10", "M20"), liste);
        }

        @Test
        @DisplayName("Tri avec préfixe G (générateurs)")
        void triGenerateurs() {
            List<String> liste = Arrays.asList("G100", "G2", "G10", "G1");
            liste.sort(ComparateurNaturel.pourChaines());

            assertEquals(Arrays.asList("G1", "G2", "G10", "G100"), liste);
        }

        @Test
        @DisplayName("Tri mixte de préfixes différents")
        void triPrefixesDifferents() {
            List<String> liste = Arrays.asList("M1", "G1", "M2", "G2");
            liste.sort(ComparateurNaturel.pourChaines());

            assertEquals(Arrays.asList("G1", "G2", "M1", "M2"), liste);
        }

        @Test
        @DisplayName("Tri insensible à la casse pour les préfixes")
        void triInsensibleCasse() {
            List<String> liste = Arrays.asList("m2", "M1", "m10");
            liste.sort(ComparateurNaturel.pourChaines());

            assertEquals(Arrays.asList("M1", "m2", "m10"), liste);
        }
    }

    @Nested
    @DisplayName("Cas limites")
    class CasLimitesTests {

        @Test
        @DisplayName("Chaînes sans numéro")
        void chainesSansNumero() {
            List<String> liste = Arrays.asList("Maison", "Generateur", "Centrale");
            liste.sort(ComparateurNaturel.pourChaines());

            assertEquals(Arrays.asList("Centrale", "Generateur", "Maison"), liste);
        }

        @Test
        @DisplayName("Chaînes uniquement numériques")
        void chainesNumeriques() {
            List<String> liste = Arrays.asList("100", "2", "10", "1");
            liste.sort(ComparateurNaturel.pourChaines());

            assertEquals(Arrays.asList("1", "2", "10", "100"), liste);
        }

        @Test
        @DisplayName("Gestion des valeurs null")
        void valeursNull() {
            List<String> liste = Arrays.asList("M2", null, "M1", null);
            liste.sort(ComparateurNaturel.pourChaines());

            assertNull(liste.get(0));
            assertNull(liste.get(1));
            assertEquals("M1", liste.get(2));
            assertEquals("M2", liste.get(3));
        }

        @Test
        @DisplayName("Liste vide")
        void listeVide() {
            List<String> liste = Arrays.asList();
            assertDoesNotThrow(() -> liste.sort(ComparateurNaturel.pourChaines()));
            assertTrue(liste.isEmpty());
        }

        @Test
        @DisplayName("Un seul élément")
        void unSeulElement() {
            List<String> liste = Arrays.asList("M1");
            liste.sort(ComparateurNaturel.pourChaines());
            assertEquals(Arrays.asList("M1"), liste);
        }
    }

    @Nested
    @DisplayName("Factory method de()")
    class FactoryMethodTests {

        @Test
        @DisplayName("Comparateur avec extracteur personnalisé")
        void extracteurPersonnalise() {
            record Element(String nom) {}
            
            List<Element> liste = Arrays.asList(
                new Element("E10"),
                new Element("E2"),
                new Element("E1")
            );
            
            liste.sort(ComparateurNaturel.de(Element::nom));

            assertEquals("E1", liste.get(0).nom());
            assertEquals("E2", liste.get(1).nom());
            assertEquals("E10", liste.get(2).nom());
        }
    }

    @Nested
    @DisplayName("Comparaisons directes")
    class ComparaisonsDirectesTests {

        @ParameterizedTest
        @DisplayName("Comparaison de paires")
        @CsvSource({
            "M1, M2, -1",
            "M10, M2, 1",
            "M1, M1, 0",
            "G1, M1, -1",
            "A1, B1, -1"
        })
        void comparaisonPaires(String a, String b, int signeAttendu) {
            ComparateurNaturel<String> comp = ComparateurNaturel.pourChaines();
            int resultat = comp.compare(a, b);

            if (signeAttendu < 0) {
                assertTrue(resultat < 0, a + " devrait être avant " + b);
            } else if (signeAttendu > 0) {
                assertTrue(resultat > 0, a + " devrait être après " + b);
            } else {
                assertEquals(0, resultat, a + " devrait être égal à " + b);
            }
        }

        @Test
        @DisplayName("Comparaison avec null")
        void comparaisonAvecNull() {
            ComparateurNaturel<String> comp = ComparateurNaturel.pourChaines();

            assertTrue(comp.compare(null, "M1") < 0);
            assertTrue(comp.compare("M1", null) > 0);
            assertEquals(0, comp.compare(null, null));
        }
    }
}
