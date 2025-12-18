package up.mi.paa.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.ReseauElectrique;
import up.mi.paa.model.TypeConsommation;
import up.mi.paa.service.GestionnaireReseau;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe GestionnaireFichier.
 * Vérifie :
 * - Lecture d'un fichier valide
 * - Détection d'erreurs (syntaxe, ordre, données invalides)
 * - Écriture et relecture (persistance)
 */
class GestionnaireFichierTest {

    @TempDir
    Path tempDir; // Crée un dossier temporaire automatique pour les tests

    @Test
    void testLectureFichier_Valide() throws IOException {
        Path cheminFichier = tempDir.resolve("valide.txt");
        List<String> lignes = List.of(
            "generateur(G1,100).",
            "maison(M1,NORMAL).",
            "connexion(G1,M1)."
        );
        Files.write(cheminFichier, lignes);

        GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

        assertNotNull(res, "Le gestionnaire ne doit pas être null pour un fichier valide");
        assertEquals(1, res.getReseauElectrique().getGenerateurs().size());
        assertEquals(1, res.getReseauElectrique().getMaisons().size());
        
        Generateur g = res.getReseauElectrique().trouverGenerateur("G1");
        assertEquals(100.0, g.getCapaciteMaximale());
    }

    @Test
    void testLectureFichier_ErreurSyntaxe_PointManquant() throws IOException {
        Path cheminFichier = tempDir.resolve("erreur_point.txt");
        Files.write(cheminFichier, List.of("generateur(G1,100)")); // Pas de point

        GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

        assertNull(res, "L'import doit retourner null si la syntaxe est incorrecte");
    }

    @Test
    void testLectureFichier_ErreurOrdre_MaisonAvantGenerateur() throws IOException {
        Path cheminFichier = tempDir.resolve("erreur_ordre.txt");
        // Maison avant générateur = Interdit
        Files.write(cheminFichier, List.of(
            "maison(M1,NORMAL).",
            "generateur(G1,100)."
        ));

        GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

        assertNull(res, "L'import doit retourner null si l'ordre n'est pas respecté");
    }

    @Test
    void testLectureFichier_ErreurDonnee_CapaciteNegative() throws IOException {
        Path cheminFichier = tempDir.resolve("erreur_negatif.txt");
        Files.write(cheminFichier, List.of("generateur(G1,-50)."));

        GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

        assertNull(res, "L'import doit retourner null si la capacité est négative");
    }

    @Test
    void testEcritureEtRelecture_Persistance() throws IOException {
        ReseauElectrique reseauOriginal = new ReseauElectrique();
        Generateur g1 = new Generateur("GEN_TEST", 200);
        Maison m1 = new Maison("MAISON_TEST", TypeConsommation.FORTE);
        
        reseauOriginal.ajouterGenerateur(g1);
        reseauOriginal.ajouterMaison(m1);
        reseauOriginal.ajouterConnexion(m1, g1);

        File fichierExport = tempDir.resolve("export.txt").toFile();
        GestionnaireFichier.ecrireFichierReseau(fichierExport, reseauOriginal);

        GestionnaireReseau gestionnaireRelu = GestionnaireFichier.lireFichierReseau(fichierExport);

        assertNotNull(gestionnaireRelu);
        ReseauElectrique reseauRelu = gestionnaireRelu.getReseauElectrique();

        assertEquals(1, reseauRelu.getGenerateurs().size());
        assertEquals("GEN_TEST", reseauRelu.getGenerateurs().get(0).getNom());
        assertEquals(200.0, reseauRelu.getGenerateurs().get(0).getCapaciteMaximale());
        
        assertEquals(1, reseauRelu.getMaisons().size());
        assertEquals("MAISON_TEST", reseauRelu.getMaisons().get(0).getNom());
        assertEquals(TypeConsommation.FORTE, reseauRelu.getMaisons().get(0).getTypeConsommation());
    }
}