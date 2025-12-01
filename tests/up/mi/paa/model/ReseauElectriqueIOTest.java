package up.mi.paa.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de test pour valider les entrées-sorties (I/O) de la classe ReseauElectrique.
 */
class ReseauElectriqueIOTest {

    private ReseauElectrique reseau;

    /**
     * Initialisation avant chaque test pour avoir un réseau vide.
     */
    @BeforeEach
    void setUp() {
        reseau = new ReseauElectrique();
    }

    /**
     * Test du cas nominal : Lecture d'un fichier correctement formaté.
     * Vérifie que les générateurs, maisons et connexions sont bien créés.
     */
    @Test
    void testLireFichierReseau_CasNominal(@TempDir Path dossierTemporaire) throws IOException {
        // 1. Création d'un fichier temporaire
        Path cheminFichier = dossierTemporaire.resolve("reseau_ok.txt");
        String contenu =
                "generateur(gen1, 100.0).\n" +
                "maison(m1, NORMAL).\n" +
                "connexion(gen1, m1).";
        Files.writeString(cheminFichier, contenu);

        // 2. Exécution de la méthode
        reseau.lireFichierReseau(cheminFichier.toFile());

        // 3. Vérifications
        Generateur gen = reseau.trouverGenerateur("gen1");
        Maison maison = reseau.trouverMaison("m1");

        assertNotNull(gen, "Le générateur 'gen1' aurait dû être créé.");
        assertNotNull(maison, "La maison 'm1' aurait dû être créée.");
        assertEquals(100.0, gen.getCapaciteMaximale(), 1e-9,
                "La capacité du générateur est incorrecte.");

        // Vérification de la connexion
        List<Maison> maisonsConnectees = reseau.trouverLesMaisonsDesGenerateurs(gen);
        assertTrue(maisonsConnectees.contains(maison),
                "La maison 'm1' devrait être connectée à 'gen1'.");
    }

    /**
     * Test de la flexibilité : Vérifie que connexion(maison, generateur) fonctionne
     * aussi bien que connexion(generateur, maison).
     */
    @Test
    void testLireFichierReseau_ConnexionInverse(@TempDir Path dossierTemporaire) throws IOException {
        Path cheminFichier = dossierTemporaire.resolve("reseau_inverse.txt");
        // Ordre inversé dans le paramètre connexion
        String contenu =
                "generateur(g1, 50).\n" +
                "maison(m1, BASSE).\n" +
                "connexion(m1, g1).";
        Files.writeString(cheminFichier, contenu);

        reseau.lireFichierReseau(cheminFichier.toFile());

        Generateur gen = reseau.trouverGenerateur("g1");
        Maison maison = reseau.trouverMaison("m1");

        assertNotNull(gen, "Le générateur 'g1' devrait exister.");
        assertNotNull(maison, "La maison 'm1' devrait exister.");

        // La connexion doit exister malgré l'inversion
        assertTrue(reseau.trouverLesMaisonsDesGenerateurs(gen).contains(maison),
                "La connexion inverse (maison, gen) n'a pas été reconnue.");
    }

    /**
     * Test de l'écriture : Vérifie que le fichier généré respecte strictement le format
     * demandé (Générateurs, puis Maisons, puis Connexions, avec les points finaux).
     */
    @Test
    void testEcrireFichierReseau(@TempDir Path dossierTemporaire) throws IOException {
        // 1. Préparation des données en mémoire
        Generateur g1 = new Generateur("genA", 200);
        Maison m1 = new Maison("maisonA", TypeConsommation.FORTE);

        reseau.ajouterGenerateur(g1);
        reseau.ajouterMaison(m1);
        reseau.ajouterConnexion(m1, g1);

        // 2. Écriture dans un fichier
        File fichierSortie = dossierTemporaire.resolve("sortie_test.txt").toFile();
        reseau.ecrireFichierReseau(fichierSortie);

        // 3. Lecture et analyse du fichier produit
        List<String> lignes = Files.readAllLines(fichierSortie.toPath());

        // On vérifie la taille et l'ordre exact : Générateurs → Maisons → Connexions
        assertEquals(3, lignes.size(),
                "Le fichier devrait contenir exactement 3 lignes (générateur, maison, connexion).");

        assertEquals("generateur(genA,200.0).", lignes.get(0).trim(),
                "La première ligne doit définir le générateur au bon format.");
        assertEquals("maison(maisonA,FORTE).", lignes.get(1).trim(),
                "La deuxième ligne doit définir la maison au bon format.");
        assertEquals("connexion(genA,maisonA).", lignes.get(2).trim(),
                "La troisième ligne doit définir la connexion au bon format.");
    }

    /**
     * Test de robustesse : Vérifie que le programme ne plante pas (exception)
     * face à une ligne mal formée, mais signale l'erreur (via System.out).
     */
    @Test
    void testLireFichierReseau_LigneInvalide(@TempDir Path dossierTemporaire) throws IOException {
        // On sauvegarde la sortie standard avant de la rediriger
        PrintStream ancienOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(outContent));

            Path cheminFichier = dossierTemporaire.resolve("reseau_erreur.txt");
            // Manque la parenthèse fermante
            String contenu = "generateur(gen1, 100.";
            Files.writeString(cheminFichier, contenu);

            reseau.lireFichierReseau(cheminFichier.toFile());

            // On vérifie que le message d'erreur a été imprimé
            assertTrue(outContent.toString().contains("Ligne invalide"),
                    "Une erreur de format aurait dû être signalée dans la console.");
        } finally {
            // Restauration de la sortie standard (important pour les autres tests)
            System.setOut(ancienOut);
        }
    }
}
