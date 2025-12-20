package up.mi.paa.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour la classe {@link ReseauElectrique}.
 * 
 * <p>Couvre les fonctionnalités suivantes :
 * <ul>
 *   <li>Construction du réseau</li>
 *   <li>Ajout et recherche de générateurs</li>
 *   <li>Ajout et recherche de maisons</li>
 *   <li>Gestion des connexions</li>
 *   <li>Vérification des états de connexion</li>
 * </ul>
 * 
 * @author Groupe 10
 * @version 1.0
 */
@DisplayName("Tests de ReseauElectrique")
public class ReseauElectriqueTest {

    private ReseauElectrique re;

    @BeforeEach
    void beforeEach() {
        re = new ReseauElectrique();
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("Le réseau est créé avec des collections vides")
        void creationDeReseauTest() {
            assertNotNull(re.getConnexions());
            assertNotNull(re.getMaisons());
            assertNotNull(re.getGenerateurs());
            assertTrue(re.getGenerateurs().isEmpty());
            assertTrue(re.getMaisons().isEmpty());
        }
    }

    @Nested
    @DisplayName("Gestion des générateurs")
    class GenerateursTests {

        @Test
        @DisplayName("Ajout d'un générateur valide")
        void ajouterGenerateurValide() {
            Generateur g1 = new Generateur("G1", 40.0);
            re.ajouterGenerateur(g1);
            assertTrue(re.getGenerateurs().contains(g1));
            assertEquals(1, re.getGenerateurs().size());
        }

        @Test
        @DisplayName("Les doublons sont ignorés")
        void ajouterGenerateurDoublon() {
            Generateur g1 = new Generateur("G1", 40.0);
            Generateur g2 = new Generateur("G1", 60.0);
            re.ajouterGenerateur(g1);
            re.ajouterGenerateur(g2);
            assertEquals(1, re.getGenerateurs().size());
        }

        @Test
        @DisplayName("Exception si le générateur est null")
        void ajouterGenerateurNull() {
            assertThrows(IllegalArgumentException.class, 
                () -> re.ajouterGenerateur(null));
        }

        @Test
        @DisplayName("Suppression d'un générateur existant")
        void supprimerGenerateurExistant() {
            Generateur g1 = new Generateur("G1", 40.0);
            re.ajouterGenerateur(g1);
            assertTrue(re.supprimerGenerateur(g1));
            assertFalse(re.getGenerateurs().contains(g1));
        }

        @Test
        @DisplayName("Suppression d'un générateur inexistant retourne false")
        void supprimerGenerateurInexistant() {
            Generateur g1 = new Generateur("G1", 40.0);
            assertFalse(re.supprimerGenerateur(g1));
        }

        @Test
        @DisplayName("Suppression d'un générateur null retourne false")
        void supprimerGenerateurNull() {
            assertFalse(re.supprimerGenerateur(null));
        }

        @Test
        @DisplayName("Recherche d'un générateur par nom")
        void trouverGenerateurParNom() {
            Generateur g1 = new Generateur("G1", 40.0);
            re.ajouterGenerateur(g1);
            assertEquals(g1, re.trouverGenerateur("G1"));
            assertEquals(g1, re.trouverGenerateur("g1"));
        }

        @Test
        @DisplayName("Recherche d'un générateur inexistant retourne null")
        void trouverGenerateurInexistant() {
            assertNull(re.trouverGenerateur("G_INCONNU"));
            assertNull(re.trouverGenerateur((String) null));
        }
    }

    @Nested
    @DisplayName("Gestion des maisons")
    class MaisonsTests {

        @Test
        @DisplayName("Ajout d'une maison valide")
        void ajouterMaisonValide() {
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            re.ajouterMaison(m1);
            assertTrue(re.getMaisons().contains(m1));
            assertEquals(1, re.getMaisons().size());
        }

        @Test
        @DisplayName("Les doublons sont ignorés")
        void ajouterMaisonDoublon() {
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            Maison m2 = new Maison("M1", TypeConsommation.FORTE);
            re.ajouterMaison(m1);
            re.ajouterMaison(m2);
            assertEquals(1, re.getMaisons().size());
        }

        @Test
        @DisplayName("Exception si la maison est null")
        void ajouterMaisonNull() {
            assertThrows(IllegalArgumentException.class, 
                () -> re.ajouterMaison(null));
        }

        @Test
        @DisplayName("Recherche d'une maison par nom")
        void trouverMaisonParNom() {
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            re.ajouterMaison(m1);
            assertEquals(m1, re.trouverMaison("M1"));
            assertEquals(m1, re.trouverMaison("m1"));
        }

        @Test
        @DisplayName("Recherche d'une maison inexistante retourne null")
        void trouverMaisonInexistante() {
            assertNull(re.trouverMaison("M_INCONNUE"));
            assertNull(re.trouverMaison((String) null));
        }
    }

    @Nested
    @DisplayName("Gestion des connexions")
    class ConnexionsTests {

        @Test
        @DisplayName("Ajout d'une connexion valide")
        void ajouterConnexionValide() {
            Generateur g1 = new Generateur("G1", 40.0);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            re.ajouterGenerateur(g1);
            re.ajouterMaison(m1);
            
            assertDoesNotThrow(() -> re.ajouterConnexion(m1, g1));
            assertTrue(re.getConnexions().get(g1).contains(m1));
        }

        @Test
        @DisplayName("Exception si la maison est null")
        void ajouterConnexionMaisonNull() {
            Generateur g1 = new Generateur("G1", 40.0);
            re.ajouterGenerateur(g1);
            
            assertThrows(IllegalArgumentException.class, 
                () -> re.ajouterConnexion(null, g1));
        }

        @Test
        @DisplayName("Exception si le générateur est null")
        void ajouterConnexionGenerateurNull() {
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            re.ajouterMaison(m1);
            
            assertThrows(IllegalArgumentException.class, 
                () -> re.ajouterConnexion(m1, null));
        }

        @Test
        @DisplayName("Exception si la maison n'existe pas dans le réseau")
        void ajouterConnexionMaisonInexistante() {
            Generateur g1 = new Generateur("G1", 40.0);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            re.ajouterGenerateur(g1);
            
            assertThrows(IllegalStateException.class, 
                () -> re.ajouterConnexion(m1, g1));
        }

        @Test
        @DisplayName("Exception si le générateur n'existe pas dans le réseau")
        void ajouterConnexionGenerateurInexistant() {
            Generateur g1 = new Generateur("G1", 40.0);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            re.ajouterMaison(m1);
            
            assertThrows(IllegalStateException.class, 
                () -> re.ajouterConnexion(m1, g1));
        }

        @Test
        @DisplayName("Suppression d'une connexion existante")
        void supprimerConnexionExistante() {
            Generateur g1 = new Generateur("G1", 40.0);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            re.ajouterGenerateur(g1);
            re.ajouterMaison(m1);
            re.ajouterConnexion(m1, g1);
            
            re.supprimerConnexion(m1);
            
            assertFalse(re.getConnexions().get(g1).contains(m1));
        }

        @Test
        @DisplayName("Suppression d'une connexion null ne lève pas d'exception")
        void supprimerConnexionNull() {
            assertDoesNotThrow(() -> re.supprimerConnexion(null));
        }

        @Test
        @DisplayName("Suppression d'une maison non connectée ne lève pas d'exception")
        void supprimerConnexionInexistante() {
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            assertDoesNotThrow(() -> re.supprimerConnexion(m1));
        }
    }

    @Nested
    @DisplayName("Recherche de générateur pour une maison")
    class TrouverGenerateurMaisonTests {

        @Test
        @DisplayName("Trouver le générateur d'une maison connectée")
        void trouverGenerateurMaisonConnectee() {
            Generateur g1 = new Generateur("G1", 40.0);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            re.ajouterGenerateur(g1);
            re.ajouterMaison(m1);
            re.ajouterConnexion(m1, g1);
            
            assertEquals(g1, re.trouverGenerateur(m1));
        }

        @Test
        @DisplayName("Retourne null pour une maison non connectée")
        void trouverGenerateurMaisonNonConnectee() {
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            re.ajouterMaison(m1);
            
            assertNull(re.trouverGenerateur(m1));
        }

        @Test
        @DisplayName("Retourne null pour une maison null")
        void trouverGenerateurMaisonNull() {
            assertNull(re.trouverGenerateur((Maison) null));
        }
    }

    @Nested
    @DisplayName("Liste des maisons d'un générateur")
    class MaisonsDeGenerateurTests {

        @Test
        @DisplayName("Retourne les maisons connectées")
        void trouverMaisonsConnectees() {
            Generateur g1 = new Generateur("G1", 100.0);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            Maison m2 = new Maison("M2", TypeConsommation.FORTE);
            
            re.ajouterGenerateur(g1);
            re.ajouterMaison(m1);
            re.ajouterMaison(m2);
            re.ajouterConnexion(m1, g1);
            re.ajouterConnexion(m2, g1);
            
            List<Maison> maisons = re.trouverLesMaisonsDeGenerateur(g1);
            assertEquals(2, maisons.size());
            assertTrue(maisons.contains(m1));
            assertTrue(maisons.contains(m2));
        }

        @Test
        @DisplayName("Retourne une liste vide pour un générateur sans maisons")
        void trouverMaisonsGenerateurVide() {
            Generateur g1 = new Generateur("G1", 100.0);
            re.ajouterGenerateur(g1);
            
            List<Maison> maisons = re.trouverLesMaisonsDeGenerateur(g1);
            assertTrue(maisons.isEmpty());
        }

        @Test
        @DisplayName("Retourne une liste vide pour un générateur null")
        void trouverMaisonsGenerateurNull() {
            List<Maison> maisons = re.trouverLesMaisonsDeGenerateur(null);
            assertTrue(maisons.isEmpty());
        }

        @Test
        @DisplayName("La liste retournée est une copie défensive")
        void trouverMaisonsCopieDefensive() {
            Generateur g1 = new Generateur("G1", 100.0);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            
            re.ajouterGenerateur(g1);
            re.ajouterMaison(m1);
            re.ajouterConnexion(m1, g1);
            
            List<Maison> maisons = re.trouverLesMaisonsDeGenerateur(g1);
            maisons.clear();
            
            assertEquals(1, re.trouverLesMaisonsDeGenerateur(g1).size());
        }
    }

    @Nested
    @DisplayName("État de connexion des maisons")
    class EtatConnexionTests {

        @Test
        @DisplayName("maisonEstConnectee retourne true pour une maison connectée")
        void maisonConnectee() {
            Generateur g1 = new Generateur("G1", 40.0);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            re.ajouterGenerateur(g1);
            re.ajouterMaison(m1);
            re.ajouterConnexion(m1, g1);
            
            assertTrue(re.maisonEstConnectee(m1));
        }

        @Test
        @DisplayName("maisonEstConnectee retourne false pour une maison non connectée")
        void maisonNonConnectee() {
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            re.ajouterMaison(m1);
            
            assertFalse(re.maisonEstConnectee(m1));
        }

        @Test
        @DisplayName("maisonEstConnectee retourne false pour null")
        void maisonNull() {
            assertFalse(re.maisonEstConnectee(null));
        }

        @Test
        @DisplayName("maisonsNonConnectees retourne les maisons orphelines")
        void maisonsOrphelines() {
            Generateur g1 = new Generateur("G1", 100.0);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            Maison m2 = new Maison("M2", TypeConsommation.FORTE);
            
            re.ajouterGenerateur(g1);
            re.ajouterMaison(m1);
            re.ajouterMaison(m2);
            re.ajouterConnexion(m1, g1);
            
            List<Maison> nonConnectees = re.maisonsNonConnectees();
            assertEquals(1, nonConnectees.size());
            assertTrue(nonConnectees.contains(m2));
        }

        @Test
        @DisplayName("maisonsNonConnectees retourne liste vide si toutes connectées")
        void toutesConnectees() {
            Generateur g1 = new Generateur("G1", 100.0);
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            
            re.ajouterGenerateur(g1);
            re.ajouterMaison(m1);
            re.ajouterConnexion(m1, g1);
            
            assertTrue(re.maisonsNonConnectees().isEmpty());
        }

        @Test
        @DisplayName("toutesLesMaisonsConnectees retourne true si réseau vide")
        void reseauVideToutesConnectees() {
            assertTrue(re.toutesLesMaisonsConnectees());
        }

        @Test
        @DisplayName("toutesLesMaisonsConnectees retourne false si maisons orphelines")
        void maisonsOrphelinesFalse() {
            Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
            re.ajouterMaison(m1);
            
            assertFalse(re.toutesLesMaisonsConnectees());
        }
    }
}