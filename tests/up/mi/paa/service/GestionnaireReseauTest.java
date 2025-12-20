package up.mi.paa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import up.mi.paa.exception.*;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.ReseauElectrique;
import up.mi.paa.model.TypeConsommation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link GestionnaireReseau}.
 * 
 * <p>Couvre les opérations suivantes :
 * <ul>
 *   <li>Ajout et modification des générateurs</li>
 *   <li>Ajout et modification des maisons</li>
 *   <li>Création, modification et suppression des connexions</li>
 *   <li>Validation de l'état du réseau</li>
 * </ul>
 * 
 * @author Groupe 10
 * @version 1.0
 */
@DisplayName("Tests de GestionnaireReseau")
class GestionnaireReseauTest {

    private GestionnaireReseau gestionnaire;
    private ReseauElectrique reseau;

    @BeforeEach
    void beforeEach() {
        gestionnaire = new GestionnaireReseau();
        reseau = gestionnaire.getReseauElectrique();
    }

    @Nested
    @DisplayName("Gestion des générateurs")
    class GenerateursTests {

        @Test
        @DisplayName("Ajout d'un nouveau générateur retourne false")
        void ajouterNouveauGenerateur() {
            boolean existait = gestionnaire.ajouterOuModifierGenerateur("G1", 100.0);

            assertFalse(existait);
            assertEquals(1, reseau.getGenerateurs().size());
            assertEquals(100.0, reseau.trouverGenerateur("G1").getCapaciteMaximale());
        }

        @Test
        @DisplayName("Modification d'un générateur existant retourne true")
        void modifierGenerateurExistant() {
            gestionnaire.ajouterOuModifierGenerateur("G1", 100.0);
            boolean existait = gestionnaire.ajouterOuModifierGenerateur("G1", 200.0);

            assertTrue(existait);
            assertEquals(1, reseau.getGenerateurs().size());
            assertEquals(200.0, reseau.trouverGenerateur("G1").getCapaciteMaximale());
        }

        @Test
        @DisplayName("Plusieurs générateurs avec noms différents")
        void plusieursGenerateurs() {
            gestionnaire.ajouterOuModifierGenerateur("G1", 100.0);
            gestionnaire.ajouterOuModifierGenerateur("G2", 200.0);
            gestionnaire.ajouterOuModifierGenerateur("G3", 300.0);

            assertEquals(3, reseau.getGenerateurs().size());
        }
    }

    @Nested
    @DisplayName("Gestion des maisons")
    class MaisonsTests {

        @Test
        @DisplayName("Ajout d'une nouvelle maison retourne false")
        void ajouterNouvelleMaison() {
            boolean existait = gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);

            assertFalse(existait);
            assertEquals(1, reseau.getMaisons().size());
            assertEquals(TypeConsommation.NORMAL, reseau.trouverMaison("M1").getTypeConsommation());
        }

        @Test
        @DisplayName("Modification d'une maison existante retourne true")
        void modifierMaisonExistante() {
            gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);
            boolean existait = gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.FORTE);

            assertTrue(existait);
            assertEquals(1, reseau.getMaisons().size());
            assertEquals(TypeConsommation.FORTE, reseau.trouverMaison("M1").getTypeConsommation());
        }

        @Test
        @DisplayName("Maisons avec tous les types de consommation")
        void maisonsAvecDifferentsTypes() {
            gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.BASSE);
            gestionnaire.ajouterOuModifierMaison("M2", TypeConsommation.NORMAL);
            gestionnaire.ajouterOuModifierMaison("M3", TypeConsommation.FORTE);

            assertEquals(TypeConsommation.BASSE, reseau.trouverMaison("M1").getTypeConsommation());
            assertEquals(TypeConsommation.NORMAL, reseau.trouverMaison("M2").getTypeConsommation());
            assertEquals(TypeConsommation.FORTE, reseau.trouverMaison("M3").getTypeConsommation());
        }
    }

    @Nested
    @DisplayName("Création de connexions")
    class CreationConnexionTests {

        @Test
        @DisplayName("Connexion valide entre générateur et maison")
        void connexionValide() throws Exception {
            gestionnaire.ajouterOuModifierGenerateur("G1", 100);
            gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);

            gestionnaire.creerConnexion("G1", "M1");

            assertTrue(reseau.maisonEstConnectee(reseau.trouverMaison("M1")));
            assertEquals(reseau.trouverGenerateur("G1"), 
                reseau.trouverGenerateur(reseau.trouverMaison("M1")));
        }

        @Test
        @DisplayName("Exception si générateur introuvable")
        void generateurIntrouvable() {
            assertThrows(GenerateurIntrouvableException.class, 
                () -> gestionnaire.creerConnexion("G_INCONNU", "M1"));
        }

        @Test
        @DisplayName("Exception si maison introuvable")
        void maisonIntrouvable() {
            gestionnaire.ajouterOuModifierGenerateur("G1", 100);
            assertThrows(MaisonIntrouvableException.class, 
                () -> gestionnaire.creerConnexion("G1", "M_INCONNU"));
        }

        @Test
        @DisplayName("Exception si maison déjà connectée")
        void connexionExistante() throws Exception {
            gestionnaire.ajouterOuModifierGenerateur("G1", 100);
            gestionnaire.ajouterOuModifierGenerateur("G2", 100);
            gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);

            gestionnaire.creerConnexion("G1", "M1");

            assertThrows(ConnexionExistanteException.class, 
                () -> gestionnaire.creerConnexion("G2", "M1"));
        }
    }

    @Nested
    @DisplayName("Suppression et modification de connexions")
    class ModificationConnexionTests {

        @Test
        @DisplayName("Suppression d'une connexion existante")
        void supprimerConnexion() throws Exception {
            gestionnaire.ajouterOuModifierGenerateur("G1", 100);
            gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);
            gestionnaire.creerConnexion("G1", "M1");

            gestionnaire.supprimerConnexion("G1", "M1");

            assertFalse(reseau.maisonEstConnectee(reseau.trouverMaison("M1")));
        }

        @Test
        @DisplayName("Modification d'une connexion (changement de générateur)")
        void modifierConnexion() throws Exception {
            gestionnaire.ajouterOuModifierGenerateur("G1", 100);
            gestionnaire.ajouterOuModifierGenerateur("G2", 100);
            gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);
            gestionnaire.creerConnexion("G1", "M1");

            gestionnaire.modifierConnexion("G1", "M1", "G2", "M1");

            Generateur nouveauGen = reseau.trouverGenerateur(reseau.trouverMaison("M1"));
            assertEquals("G2", nouveauGen.getNom());
        }

        @Test
        @DisplayName("Plusieurs connexions sur un même générateur")
        void plusieursConnexionsMemeGenerateur() throws Exception {
            gestionnaire.ajouterOuModifierGenerateur("G1", 100);
            gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);
            gestionnaire.ajouterOuModifierMaison("M2", TypeConsommation.NORMAL);

            gestionnaire.creerConnexion("G1", "M1");
            gestionnaire.creerConnexion("G1", "M2");

            assertEquals(2, reseau.trouverLesMaisonsDeGenerateur(
                reseau.trouverGenerateur("G1")).size());
        }
    }

    @Nested
    @DisplayName("Validation du réseau")
    class ValidationReseauTests {

        @Test
        @DisplayName("Réseau vide détecte l'absence de maisons et générateurs")
        void reseauVide() {
            String erreurs = gestionnaire.verifierValiditeReseau();
            
            assertTrue(erreurs.contains("maison"));
            assertTrue(erreurs.contains("generateur"));
        }

        @Test
        @DisplayName("Maison orpheline signalée")
        void maisonOrpheline() {
            gestionnaire.ajouterOuModifierGenerateur("G1", 100);
            gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);

            String erreurs = gestionnaire.verifierValiditeReseau();
            
            assertTrue(erreurs.contains("M1"));
        }

        @Test
        @DisplayName("Surcharge globale détectée")
        void surchargeGlobale() throws Exception {
            gestionnaire.ajouterOuModifierGenerateur("G1", 0.1);
            gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.FORTE);
            gestionnaire.creerConnexion("G1", "M1");

            String erreurs = gestionnaire.verifierValiditeReseau();
            
            assertTrue(erreurs.toLowerCase().contains("demande") 
                || erreurs.toLowerCase().contains("capacité")
                || erreurs.toLowerCase().contains("capacite"));
        }

        @Test
        @DisplayName("Réseau valide retourne chaîne vide")
        void reseauValide() throws Exception {
            gestionnaire.ajouterOuModifierGenerateur("G1", 100000.0);
            gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.BASSE);
            gestionnaire.creerConnexion("G1", "M1");

            String erreurs = gestionnaire.verifierValiditeReseau();
            
            assertEquals("", erreurs);
        }

        @Test
        @DisplayName("Plusieurs erreurs combinées")
        void plusieursErreurs() {
            gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);
            gestionnaire.ajouterOuModifierMaison("M2", TypeConsommation.FORTE);

            String erreurs = gestionnaire.verifierValiditeReseau();
            
            assertTrue(erreurs.contains("M1"));
            assertTrue(erreurs.contains("M2"));
            assertTrue(erreurs.contains("generateur"));
        }
    }
}