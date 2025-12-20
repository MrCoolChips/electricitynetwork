package up.mi.paa.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
 * Tests unitaires pour la classe {@link GestionnaireFichier}.
 * 
 * <p>Vérifie les fonctionnalités suivantes :
 * <ul>
 *   <li>Lecture de fichiers valides</li>
 *   <li>Détection des erreurs de syntaxe</li>
 *   <li>Validation de l'ordre des déclarations</li>
 *   <li>Écriture et persistance des données</li>
 * </ul>
 * 
 * @author Groupe 10
 * @version 1.0
 */
@DisplayName("Tests de GestionnaireFichier")
class GestionnaireFichierTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("Lecture de fichiers valides")
    class LectureValideTests {

        @Test
        @DisplayName("Fichier avec générateur, maison et connexion")
        void fichierComplet() throws IOException {
            Path cheminFichier = tempDir.resolve("valide.txt");
            List<String> lignes = List.of(
                "generateur(G1,100).",
                "maison(M1,NORMAL).",
                "connexion(G1,M1)."
            );
            Files.write(cheminFichier, lignes);

            GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

            assertNotNull(res);
            assertEquals(1, res.getReseauElectrique().getGenerateurs().size());
            assertEquals(1, res.getReseauElectrique().getMaisons().size());

            Generateur g = res.getReseauElectrique().trouverGenerateur("G1");
            assertEquals(100.0, g.getCapaciteMaximale());
        }

        @Test
        @DisplayName("Fichier avec plusieurs générateurs et maisons")
        void fichierPlusieursElements() throws IOException {
            Path cheminFichier = tempDir.resolve("multiple.txt");
            List<String> lignes = List.of(
                "generateur(G1,100).",
                "generateur(G2,200).",
                "maison(M1,NORMAL).",
                "maison(M2,FORTE).",
                "maison(M3,BASSE).",
                "connexion(G1,M1).",
                "connexion(G2,M2).",
                "connexion(G2,M3)."
            );
            Files.write(cheminFichier, lignes);

            GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

            assertNotNull(res);
            assertEquals(2, res.getReseauElectrique().getGenerateurs().size());
            assertEquals(3, res.getReseauElectrique().getMaisons().size());
        }

        @Test
        @DisplayName("Fichier avec lignes vides ignorées")
        void fichierAvecLignesVides() throws IOException {
            Path cheminFichier = tempDir.resolve("lignes_vides.txt");
            List<String> lignes = List.of(
                "generateur(G1,100).",
                "",
                "maison(M1,NORMAL).",
                "",
                "connexion(G1,M1)."
            );
            Files.write(cheminFichier, lignes);

            GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

            assertNotNull(res);
        }
    }

    @Nested
    @DisplayName("Détection des erreurs de syntaxe")
    class ErreursSyntaxeTests {

        @Test
        @DisplayName("Point manquant en fin de ligne")
        void pointManquant() throws IOException {
            Path cheminFichier = tempDir.resolve("erreur_point.txt");
            Files.write(cheminFichier, List.of("generateur(G1,100)"));

            GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

            assertNull(res);
        }

        @Test
        @DisplayName("Parenthèse manquante")
        void parentheseManquante() throws IOException {
            Path cheminFichier = tempDir.resolve("erreur_parenthese.txt");
            Files.write(cheminFichier, List.of("generateur(G1,100."));

            GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

            assertNull(res);
        }

        @Test
        @DisplayName("Type de consommation invalide")
        void typeConsommationInvalide() throws IOException {
            Path cheminFichier = tempDir.resolve("erreur_type.txt");
            Files.write(cheminFichier, List.of(
                "generateur(G1,100).",
                "maison(M1,INVALIDE)."
            ));

            GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

            assertNull(res);
        }
    }

    @Nested
    @DisplayName("Validation de l'ordre des déclarations")
    class OrdreDeclarationsTests {

        @Test
        @DisplayName("Maison avant générateur = erreur")
        void maisonAvantGenerateur() throws IOException {
            Path cheminFichier = tempDir.resolve("erreur_ordre.txt");
            Files.write(cheminFichier, List.of(
                "maison(M1,NORMAL).",
                "generateur(G1,100)."
            ));

            GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

            assertNull(res);
        }

        @Test
        @DisplayName("Connexion avant maison = erreur")
        void connexionAvantMaison() throws IOException {
            Path cheminFichier = tempDir.resolve("erreur_ordre_connexion.txt");
            Files.write(cheminFichier, List.of(
                "generateur(G1,100).",
                "connexion(G1,M1).",
                "maison(M1,NORMAL)."
            ));

            GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

            assertNull(res);
        }
    }

    @Nested
    @DisplayName("Validation des données")
    class ValidationDonneesTests {

        @Test
        @DisplayName("Capacité négative = erreur")
        void capaciteNegative() throws IOException {
            Path cheminFichier = tempDir.resolve("erreur_negatif.txt");
            Files.write(cheminFichier, List.of("generateur(G1,-50)."));

            GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

            assertNull(res);
        }

        @Test
        @DisplayName("Capacité nulle rejetée")
        void capaciteNulle() throws IOException {
            Path cheminFichier = tempDir.resolve("capacite_nulle.txt");
            Files.write(cheminFichier, List.of(
                "generateur(G1,0).",
                "maison(M1,BASSE)."
            ));

            // La capacité nulle est considérée comme invalide par le parseur
            GestionnaireReseau res = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());
            assertNull(res);
        }
    }

    @Nested
    @DisplayName("Écriture et persistance")
    class EcriturePersistanceTests {

        @Test
        @DisplayName("Export puis import préserve les données")
        void exportImportPersistance() throws IOException {
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

        @Test
        @DisplayName("Export d'un réseau vide")
        void exportReseauVide() throws IOException {
            ReseauElectrique reseauVide = new ReseauElectrique();
            File fichierExport = tempDir.resolve("vide.txt").toFile();

            assertDoesNotThrow(() -> 
                GestionnaireFichier.ecrireFichierReseau(fichierExport, reseauVide));

            assertTrue(fichierExport.exists());
        }

        @Test
        @DisplayName("Tous les types de consommation exportés correctement")
        void exportTousTypesConsommation() throws IOException {
            ReseauElectrique reseau = new ReseauElectrique();
            reseau.ajouterGenerateur(new Generateur("G1", 1000));
            reseau.ajouterMaison(new Maison("M1", TypeConsommation.BASSE));
            reseau.ajouterMaison(new Maison("M2", TypeConsommation.NORMAL));
            reseau.ajouterMaison(new Maison("M3", TypeConsommation.FORTE));

            File fichierExport = tempDir.resolve("types.txt").toFile();
            GestionnaireFichier.ecrireFichierReseau(fichierExport, reseau);

            // Vérifie que le fichier a été créé et contient les données
            assertTrue(fichierExport.exists());
            String contenu = Files.readString(fichierExport.toPath());
            assertTrue(contenu.contains("G1"));
            assertTrue(contenu.contains("M1"));
            assertTrue(contenu.contains("M2"));
            assertTrue(contenu.contains("M3"));
        }
    }
}