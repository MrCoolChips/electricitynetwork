package up.mi.paa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import up.mi.paa.exception.*;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.ReseauElectrique;
import up.mi.paa.model.TypeConsommation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe GestionnaireReseau
 * Couvre les opérations CRUD, la gestion des connexions et la validation du réseau
 */
class GestionnaireReseauTest {

    private GestionnaireReseau gestionnaire;
    private ReseauElectrique reseau;

    @BeforeEach
    void beforeEach() {
        gestionnaire = new GestionnaireReseau();
        reseau = gestionnaire.getReseauElectrique();
    }

    @Test
    void testAjouterGenerateur_Nouveau() {
        boolean existait = gestionnaire.ajouterOuModifierGenerateur("G1", 100.0);
        
        assertFalse(existait, "Le générateur ne devait pas exister");
        assertEquals(1, reseau.getGenerateurs().size());
        assertEquals(100.0, reseau.trouverGenerateur("G1").getCapaciteMaximale());
    }

    @Test
    void testAjouterGenerateur_Existant() {
        gestionnaire.ajouterOuModifierGenerateur("G1", 100.0);
        boolean existait = gestionnaire.ajouterOuModifierGenerateur("G1", 200.0);
        
        assertTrue(existait, "Le générateur devait déjà exister");
        assertEquals(1, reseau.getGenerateurs().size(), "Le nombre de générateurs ne doit pas changer");
        assertEquals(200.0, reseau.trouverGenerateur("G1").getCapaciteMaximale(), "La capacité doit être mise à jour");
    }

    @Test
    void testAjouterMaison_Nouvelle() {
        boolean existait = gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);
        
        assertFalse(existait);
        assertEquals(1, reseau.getMaisons().size());
        assertEquals(TypeConsommation.NORMAL, reseau.trouverMaison("M1").getTypeConsommation());
    }

    @Test
    void testAjouterMaison_Existante() {
        gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);
        boolean existait = gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.FORTE);
        
        assertTrue(existait);
        assertEquals(1, reseau.getMaisons().size());
        assertEquals(TypeConsommation.FORTE, reseau.trouverMaison("M1").getTypeConsommation());
    }

    @Test
    void testCreerConnexion_Valide() throws Exception {
        gestionnaire.ajouterOuModifierGenerateur("G1", 100);
        gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);

        gestionnaire.creerConnexion("G1", "M1");

        assertTrue(reseau.maisonEstConnectee(reseau.trouverMaison("M1")));
        assertEquals(reseau.trouverGenerateur("G1"), reseau.trouverGenerateur(reseau.trouverMaison("M1")));
    }

    @Test
    void testCreerConnexion_Inexistant() {
        assertThrows(GenerateurIntrouvableException.class, () -> 
            gestionnaire.creerConnexion("G_INCONNU", "M1")
        );
        
        gestionnaire.ajouterOuModifierGenerateur("G1", 100);
        assertThrows(Exception.class, () -> gestionnaire.creerConnexion("G1", "M_INCONNU")
        );
    }

    @Test
    void testCreerConnexion_DejaConnectee() throws Exception {
        gestionnaire.ajouterOuModifierGenerateur("G1", 100);
        gestionnaire.ajouterOuModifierGenerateur("G2", 100);
        gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);

        gestionnaire.creerConnexion("G1", "M1");

        assertThrows(ConnexionExistanteException.class, () -> 
            gestionnaire.creerConnexion("G2", "M1")
        , "Une maison ne peut pas être connectée à deux générateurs");
    }

    @Test
    void testSupprimerConnexion() throws Exception {
        gestionnaire.ajouterOuModifierGenerateur("G1", 100);
        gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);
        gestionnaire.creerConnexion("G1", "M1");

        gestionnaire.supprimerConnexion("G1", "M1");

        assertFalse(reseau.maisonEstConnectee(reseau.trouverMaison("M1")));
    }

    @Test
    void testModifierConnexion() throws Exception {
        gestionnaire.ajouterOuModifierGenerateur("G1", 100);
        gestionnaire.ajouterOuModifierGenerateur("G2", 100);
        gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL);
        
        // Connexion initiale : M1 -> G1
        gestionnaire.creerConnexion("G1", "M1");

        // Changement : M1 -> G2
        gestionnaire.modifierConnexion("G1", "M1", "G2", "M1");

        Generateur nouveauGen = reseau.trouverGenerateur(reseau.trouverMaison("M1"));
        assertEquals("G2", nouveauGen.getNom());
    }
    
    
    @Test
    void testVerifierValiditeReseau_Vide() {
        String erreurs = gestionnaire.verifierValiditeReseau();
        assertTrue(erreurs.contains("Le reseau doit contenir au moins une maison"));
        assertTrue(erreurs.contains("Le reseau doit contenir au moins un generateur"));
    }

    @Test
    void testVerifierValiditeReseau_MaisonOrpheline() {
        gestionnaire.ajouterOuModifierGenerateur("G1", 100);
        gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.NORMAL); // Pas de connexion

        String erreurs = gestionnaire.verifierValiditeReseau();
        assertTrue(erreurs.contains("M1 (aucune connexion)"));
    }

    @Test
    void testVerifierValiditeReseau_SurchargeGlobale() {

        gestionnaire.ajouterOuModifierGenerateur("G1", 0.1); 
        gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.FORTE);
        
        // Même si connectée, la capacité totale est insuffisante
        try {
            gestionnaire.creerConnexion("G1", "M1");
        } catch (Exception ignored) {}

        String erreurs = gestionnaire.verifierValiditeReseau();
        assertTrue(erreurs.contains("Demande totale"), "Doit détecter que la demande dépasse la capacité");
    }

    @Test
    void testVerifierValiditeReseau_Valide() throws Exception {
        gestionnaire.ajouterOuModifierGenerateur("G1", 100000.0); // Très grande capacité
        gestionnaire.ajouterOuModifierMaison("M1", TypeConsommation.BASSE);
        gestionnaire.creerConnexion("G1", "M1");

        String erreurs = gestionnaire.verifierValiditeReseau();
        assertEquals("", erreurs, "Un réseau valide ne doit retourner aucune erreur");
    }
}