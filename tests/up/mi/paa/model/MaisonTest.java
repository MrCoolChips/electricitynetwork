package up.mi.paa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link Maison}.
 * 
 * <p>Couvre les fonctionnalités suivantes :
 * <ul>
 *   <li>Construction et validation des paramètres</li>
 *   <li>Normalisation du nom en majuscules</li>
 *   <li>Accesseurs et mutateurs</li>
 *   <li>Calcul de la consommation via le type</li>
 *   <li>Contrat equals/hashCode pour les collections</li>
 * </ul>
 * 
 * @author Groupe 10
 * @version 1.0
 */
@DisplayName("Tests de Maison")
class MaisonTest {

    @Nested
    @DisplayName("Construction et validation")
    class ConstructionTests {

        @Test
        @DisplayName("Le nom est normalisé en majuscules")
        void testNomEstNormaliseEnMajuscules() {
            Maison m = new Maison("m1", TypeConsommation.NORMAL);
            assertEquals("M1", m.getNom());
        }

        @Test
        @DisplayName("Les espaces sont supprimés du nom")
        void testNomSansEspaces() {
            Maison m = new Maison("  m1  ", TypeConsommation.NORMAL);
            assertEquals("M1", m.getNom());
        }

        @Test
        @DisplayName("Exception si le nom est null")
        void testNomNullLeveException() {
            assertThrows(IllegalArgumentException.class, 
                () -> new Maison(null, TypeConsommation.NORMAL));
        }

        @Test
        @DisplayName("Exception si le nom est vide")
        void testNomVideLeveException() {
            assertThrows(IllegalArgumentException.class, 
                () -> new Maison("", TypeConsommation.NORMAL));
        }

        @Test
        @DisplayName("Exception si le nom contient uniquement des espaces")
        void testNomEspacesLeveException() {
            assertThrows(IllegalArgumentException.class, 
                () -> new Maison("   ", TypeConsommation.NORMAL));
        }
    }

    @Nested
    @DisplayName("Accesseurs et mutateurs")
    class AccesseursTests {

        @Test
        @DisplayName("getTypeConsommation retourne le type initial")
        void testGetTypeConsommation() {
            Maison m = new Maison("M1", TypeConsommation.FORTE);
            assertEquals(TypeConsommation.FORTE, m.getTypeConsommation());
        }

        @Test
        @DisplayName("setTypeConsommation modifie le type")
        void testSetTypeConsommation() {
            Maison m = new Maison("M1", TypeConsommation.BASSE);
            m.setTypeConsommation(TypeConsommation.FORTE);
            assertEquals(TypeConsommation.FORTE, m.getTypeConsommation());
        }

        @Test
        @DisplayName("getConsommation retourne la valeur du type")
        void testGetConsommation() {
            Maison m = new Maison("M1", TypeConsommation.NORMAL);
            assertEquals(20, m.getConsommation());
        }

        @Test
        @DisplayName("toString retourne le format attendu")
        void testToString() {
            Maison m = new Maison("M1", TypeConsommation.FORTE);
            assertEquals("M1 (FORTE, 40 kW)", m.toString());
        }
    }

    @Nested
    @DisplayName("Consommation selon le type")
    class ConsommationTests {

        @Test
        @DisplayName("Consommation BASSE = 10 kW")
        void testConsommationBasse() {
            Maison m = new Maison("M1", TypeConsommation.BASSE);
            assertEquals(10, m.getConsommation());
        }

        @Test
        @DisplayName("Consommation NORMAL = 20 kW")
        void testConsommationNormal() {
            Maison m = new Maison("M1", TypeConsommation.NORMAL);
            assertEquals(20, m.getConsommation());
        }

        @Test
        @DisplayName("Consommation FORTE = 40 kW")
        void testConsommationForte() {
            Maison m = new Maison("M1", TypeConsommation.FORTE);
            assertEquals(40, m.getConsommation());
        }
    }

    @Nested
    @DisplayName("Contrat equals/hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Deux maisons avec le même nom sont égales")
        void testEquals_MemeNom() {
            Maison m1 = new Maison("m1", TypeConsommation.BASSE);
            Maison m2 = new Maison("M1", TypeConsommation.FORTE);
            assertEquals(m1, m2);
        }

        @Test
        @DisplayName("Une maison n'est pas égale à null")
        void testEquals_Null() {
            Maison m = new Maison("M1", TypeConsommation.NORMAL);
            assertNotEquals(null, m);
        }

        @Test
        @DisplayName("Une maison n'est pas égale à un objet d'un autre type")
        void testEquals_AutreType() {
            Maison m = new Maison("M1", TypeConsommation.NORMAL);
            assertNotEquals("M1", m);
        }

        @Test
        @DisplayName("hashCode est cohérent avec equals dans un HashSet")
        void testHashCodeCoherence() {
            Maison m1 = new Maison("m1", TypeConsommation.BASSE);
            Maison m2 = new Maison("M1", TypeConsommation.FORTE);

            HashSet<Maison> set = new HashSet<>();
            set.add(m1);
            assertTrue(set.contains(m2));
        }

        @Test
        @DisplayName("Deux maisons égales ont le même hashCode")
        void testHashCodeEgaux() {
            Maison m1 = new Maison("m1", TypeConsommation.BASSE);
            Maison m2 = new Maison("M1", TypeConsommation.FORTE);
            assertEquals(m1.hashCode(), m2.hashCode());
        }
    }
}
