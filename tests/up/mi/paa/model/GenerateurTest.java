package up.mi.paa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link Generateur}.
 * 
 * <p>Couvre les fonctionnalités suivantes :
 * <ul>
 *   <li>Construction et validation des paramètres</li>
 *   <li>Normalisation du nom en majuscules</li>
 *   <li>Accesseurs et mutateurs</li>
 *   <li>Contrat equals/hashCode pour les collections</li>
 * </ul>
 * 
 * @author Groupe 10
 * @version 1.0
 */
@DisplayName("Tests de Generateur")
class GenerateurTest {

    @Nested
    @DisplayName("Construction et validation")
    class ConstructionTests {

        @Test
        @DisplayName("Le nom est normalisé en majuscules")
        void testNomEstNormaliseEnMajuscules() {
            Generateur g = new Generateur("gen1", 100);
            assertEquals("GEN1", g.getNom());
        }

        @Test
        @DisplayName("Les espaces sont supprimés du nom")
        void testNomSansEspaces() {
            Generateur g = new Generateur("  gen1  ", 100);
            assertEquals("GEN1", g.getNom());
        }

        @Test
        @DisplayName("Exception si le nom est null")
        void testNomNullLeveException() {
            assertThrows(IllegalArgumentException.class, 
                () -> new Generateur(null, 100));
        }

        @Test
        @DisplayName("Exception si le nom est vide")
        void testNomVideLeveException() {
            assertThrows(IllegalArgumentException.class, 
                () -> new Generateur("", 100));
        }

        @Test
        @DisplayName("Exception si le nom contient uniquement des espaces")
        void testNomEspacesLeveException() {
            assertThrows(IllegalArgumentException.class, 
                () -> new Generateur("   ", 100));
        }
    }

    @Nested
    @DisplayName("Accesseurs et mutateurs")
    class AccesseursTests {

        @Test
        @DisplayName("getCapaciteMaximale retourne la valeur initiale")
        void testGetCapacite() {
            Generateur g = new Generateur("G1", 100);
            assertEquals(100.0, g.getCapaciteMaximale(), 1e-9);
        }

        @Test
        @DisplayName("setCapaciteMaximale modifie la capacité")
        void testSetCapacite() {
            Generateur g = new Generateur("G1", 100);
            g.setCapaciteMaximale(250.5);
            assertEquals(250.5, g.getCapaciteMaximale(), 1e-9);
        }

        @Test
        @DisplayName("toString retourne le format attendu")
        void testToString() {
            Generateur g = new Generateur("G1", 100);
            assertEquals("G1 (100 kW)", g.toString());
        }
    }

    @Nested
    @DisplayName("Contrat equals/hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Deux générateurs avec le même nom sont égaux")
        void testEquals_MemeNom() {
            Generateur g1 = new Generateur("gen1", 50);
            Generateur g2 = new Generateur("GEN1", 999);
            assertEquals(g1, g2);
        }

        @Test
        @DisplayName("Un générateur n'est pas égal à null")
        void testEquals_Null() {
            Generateur g = new Generateur("G1", 100);
            assertNotEquals(null, g);
        }

        @Test
        @DisplayName("Un générateur n'est pas égal à un objet d'un autre type")
        void testEquals_AutreType() {
            Generateur g = new Generateur("G1", 100);
            assertNotEquals("G1", g);
        }

        @Test
        @DisplayName("hashCode est cohérent avec equals dans un HashSet")
        void testHashCodeCoherence() {
            Generateur g1 = new Generateur("gen1", 100);
            Generateur g2 = new Generateur("GEN1", 200);

            HashSet<Generateur> set = new HashSet<>();
            set.add(g1);
            assertTrue(set.contains(g2));
        }

        @Test
        @DisplayName("Deux générateurs égaux ont le même hashCode")
        void testHashCodeEgaux() {
            Generateur g1 = new Generateur("gen1", 100);
            Generateur g2 = new Generateur("GEN1", 200);
            assertEquals(g1.hashCode(), g2.hashCode());
        }
    }
}
