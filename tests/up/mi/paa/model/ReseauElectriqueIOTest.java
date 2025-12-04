package up.mi.paa.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import up.mi.paa.io.GestionnaireFichier;
import up.mi.paa.service.GestionnaireReseau;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de test validant la persistance des données (Entrées/Sorties).
 * Vérifie la lecture, l'écriture et la gestion des erreurs de format.
 */
class ReseauElectriqueIOTest {

    private GestionnaireReseau gestionnaire;

    @BeforeEach
    void setUp() {
        gestionnaire = new GestionnaireReseau();
    }

    /**
     * Test Nominal : Lecture d'un fichier valide respectant l'ordre standard.
     */
    @Test
    void testLireFichierReseau_CasNominal(@TempDir Path dossierTemporaire) throws IOException {
        // 1. Préparation
        Path cheminFichier = dossierTemporaire.resolve("reseau_ok.txt");
        String contenu = "generateur(GEN1, 100.0).\nmaison(M1, NORMAL).\nconnexion(GEN1, M1).";
        Files.writeString(cheminFichier, contenu);

        // 2. Exécution
        gestionnaire = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

        // 3. Vérifications
        assertNotNull(gestionnaire, "La lecture a échoué (retour null) pour un fichier valide.");
        
        ReseauElectrique reseau = gestionnaire.getReseauElectrique();
        Generateur gen = reseau.trouverGenerateur("GEN1");
        Maison maison = reseau.trouverMaison("M1");

        assertNotNull(gen, "Le générateur GEN1 n'a pas été créé.");
        assertNotNull(maison, "La maison M1 n'a pas été créée.");
        assertTrue(reseau.getConnexions().get(gen).contains(maison), "La connexion GEN1-M1 est manquante.");
    }

    /**
     * Test de Flexibilité : Vérification de la lecture avec paramètres de connexion inversés.
     * Format accepté : connexion(Maison, Generateur).
     */
    @Test
    void testLireFichierReseau_ConnexionInverse(@TempDir Path dossierTemporaire) throws IOException {
        Path cheminFichier = dossierTemporaire.resolve("reseau_inverse.txt");
        String contenu = "generateur(G1, 50).\nmaison(M1, BASSE).\nconnexion(M1, G1).";
        Files.writeString(cheminFichier, contenu);

        gestionnaire = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

        assertNotNull(gestionnaire, "Le fichier valide (inversé) n'a pas été lu.");
        ReseauElectrique reseau = gestionnaire.getReseauElectrique();
        
        Generateur gen = reseau.trouverGenerateur("G1");
        Maison maison = reseau.trouverMaison("M1");
        
        assertTrue(reseau.getConnexions().get(gen).contains(maison), "L'inversion des paramètres n'a pas été gérée.");
    }

    /**
     * Test d'Écriture : Vérifie que le fichier généré respecte strictement le format imposé.
     */
    @Test
    void testEcrireFichierReseau(@TempDir Path dossierTemporaire) throws IOException {
        // Préparation des données
        ReseauElectrique reseau = gestionnaire.getReseauElectrique();
        Generateur g1 = new Generateur("GEN_A", 200);
        Maison m1 = new Maison("MAISON_A", TypeConsommation.FORTE);
        
        reseau.ajouterGenerateur(g1);
        reseau.ajouterMaison(m1);
        reseau.ajouterConnexion(m1, g1);

        // Écriture
        File fichierSortie = dossierTemporaire.resolve("sortie_test.txt").toFile();
        GestionnaireFichier.ecrireFichierReseau(fichierSortie, reseau);

        // Vérification du contenu ligne par ligne
        List<String> lignes = Files.readAllLines(fichierSortie.toPath());
        assertEquals(3, lignes.size(), "Le fichier de sortie doit contenir 3 lignes.");
        assertEquals("generateur(GEN_A,200).", lignes.get(0).trim());
        assertEquals("maison(MAISON_A,FORTE).", lignes.get(1).trim());
        assertEquals("connexion(GEN_A,MAISON_A).", lignes.get(2).trim());
    }

    /**
     * Test de Robustesse : Vérifie le rejet d'un fichier mal formé (Fail-Fast).
     * Doit retourner null et afficher une erreur console.
     */
    @Test
    void testLireFichierReseau_FichierInvalide(@TempDir Path dossierTemporaire) throws IOException {
        // Capture de la sortie standard (console)
        PrintStream ancienOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            Path cheminFichier = dossierTemporaire.resolve("reseau_ko.txt");
            String contenu = "generateur(GEN1, 100."; // Erreur : parenthèse/point manquant
            Files.writeString(cheminFichier, contenu);

            GestionnaireReseau resultat = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

            // 1. Le résultat doit être NULL pour garantir l'intégrité du système
            assertNull(resultat, "Un fichier invalide doit retourner null.");

            // 2. Un message d'erreur explicite doit être affiché
            String log = outContent.toString();
            boolean messageErreurPresent = log.contains("invalide") || log.contains("Erreur") || log.contains("attendus");
            assertTrue(messageErreurPresent, "Aucun message d'erreur n'a été affiché sur la console.");

        } finally {
            System.setOut(ancienOut);
        }
    }
}