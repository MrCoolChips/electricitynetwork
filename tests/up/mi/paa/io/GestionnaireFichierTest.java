package up.mi.paa.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import up.mi.paa.io.GestionnaireFichier;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.ReseauElectrique;
import up.mi.paa.model.TypeConsommation;
import up.mi.paa.service.GestionnaireReseau;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'Entrées/Sorties (I/O) pour la persistance du réseau électrique.
 *
 * Objectifs :
 * - Valider la lecture d'un fichier conforme (cas nominal).
 * - Valider une tolérance (connexion inversée) si le code le supporte.
 * - Valider l'écriture : format produit + cohérence des lignes.
 * - Valider la robustesse : fichiers invalides, ordre invalide, valeurs non numériques,
 *   types inconnus, fichier absent.
 *
 * Remarque :
 * La méthode lireFichierReseau(...) ne relance pas d'exception : elle renvoie null et écrit sur System.err.
 * On teste donc "null + message d'erreur"
 */
class GestionnaireFichierTest {

    private GestionnaireReseau gestionnaire;

    @BeforeEach
    void beforeEach() {
        gestionnaire = new GestionnaireReseau();
    }

    // -------------------------------------------------------------------------
    // Utilitaires : gestion temporaire + capture de System.err
    // -------------------------------------------------------------------------

    /** Action pouvant lever une exception (pour capture System.err). */
    @FunctionalInterface
    private interface ActionThrowable {
        void run() throws Exception;
    }

    /**
     * Capture System.err pendant l'exécution de l'action, puis restaure System.err.
     * @param action Action à exécuter
     * @return le texte capturé sur System.err
     */
    private String capturerSystemErr(ActionThrowable action) throws Exception {
        PrintStream ancienErr = System.err;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        try {
            action.run();
            return errContent.toString();
        } finally {
            System.setErr(ancienErr);
        }
    }

    /** Crée un dossier temporaire pour un test. */
    private Path creerDossierTemp() throws IOException {
        return Files.createTempDirectory("paa-io-test-");
    }

    /** Supprime un dossier temporaire (et son contenu) de manière récursive. */
    private void supprimerRecursivement(Path dir) {
        if (dir == null) return;
        try {
            if (!Files.exists(dir)) return;
            Files.walk(dir)
                    // suppression des fichiers d'abord, puis des dossiers
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                            // Sur Windows, un verrou peut parfois empêcher la suppression immédiate.
                            // On ignore ici pour éviter de faire échouer le test uniquement à cause du nettoyage.
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // 1) Lecture - Cas nominal
    // -------------------------------------------------------------------------

    /**
     * Test Nominal :
     * Lecture d'un fichier valide respectant l'ordre standard :
     * générateurs -> maisons -> connexions.
     */
    @Test
    void testLireFichierReseau_CasNominal() throws Exception {
        Path dir = creerDossierTemp();
        try {
            // 1. Préparation
            Path cheminFichier = dir.resolve("reseau_ok.txt");
            String contenu =
                    "generateur(GEN1, 100.0).\n" +
                    "maison(M1, NORMAL).\n" +
                    "connexion(GEN1, M1).";
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

            assertNotNull(reseau.getConnexions().get(gen),
                    "Aucune liste de connexions n'est associée au générateur GEN1.");
            assertTrue(reseau.getConnexions().get(gen).contains(maison),
                    "La connexion GEN1-M1 est manquante dans la structure de données.");
        } finally {
            supprimerRecursivement(dir);
        }
    }

    // -------------------------------------------------------------------------
    // 2) Lecture - Tolérance (connexion inversée) si supportée
    // -------------------------------------------------------------------------

    /**
     * Test de Flexibilité :
     * Vérifie la lecture avec paramètres de connexion inversés.
     * Format accepté si le code le gère : connexion(Maison, Generateur).
     *
     * Si votre implémentation ne supporte pas cette tolérance, ce test doit être retiré.
     */
    @Test
    void testLireFichierReseau_ConnexionInverse() throws Exception {
        Path dir = creerDossierTemp();
        try {
            Path cheminFichier = dir.resolve("reseau_inverse.txt");
            String contenu =
                    "generateur(G1, 50).\n" +
                    "maison(M1, BASSE).\n" +
                    "connexion(M1, G1).";
            Files.writeString(cheminFichier, contenu);

            gestionnaire = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());

            assertNotNull(gestionnaire, "Le fichier valide (inversé) n'a pas été lu (retour null).");

            ReseauElectrique reseau = gestionnaire.getReseauElectrique();
            Generateur gen = reseau.trouverGenerateur("G1");
            Maison maison = reseau.trouverMaison("M1");

            assertNotNull(gen, "Le générateur G1 n'a pas été créé.");
            assertNotNull(maison, "La maison M1 n'a pas été créée.");

            assertNotNull(reseau.getConnexions().get(gen),
                    "Aucune liste de connexions n'est associée au générateur G1.");
            assertTrue(reseau.getConnexions().get(gen).contains(maison),
                    "L'inversion des paramètres n'a pas été prise en charge (connexion attendue entre G1 et M1).");
        } finally {
            supprimerRecursivement(dir);
        }
    }

    // -------------------------------------------------------------------------
    // 3) Écriture - Vérification du format produit
    // -------------------------------------------------------------------------

    /**
     * Test d'Écriture :
     * Vérifie que le fichier généré respecte le format imposé.
     *
     * Attention :
     * On évite de dépendre de l'ordre des lignes si les collections ne garantissent pas l'ordre (Set/Map).
     * On vérifie donc la présence des lignes attendues, indépendamment de leur position.
     */
    @Test
    void testEcrireFichierReseau_FormatEtContenu() throws Exception {
        Path dir = creerDossierTemp();
        try {
            // Préparation des données en mémoire
            ReseauElectrique reseau = gestionnaire.getReseauElectrique();

            Generateur g1 = new Generateur("GEN_A", 200);
            Maison m1 = new Maison("MAISON_A", TypeConsommation.FORTE);

            reseau.ajouterGenerateur(g1);
            reseau.ajouterMaison(m1);
            reseau.ajouterConnexion(m1, g1);

            // Écriture dans un fichier temporaire
            File fichierSortie = dir.resolve("sortie_test.txt").toFile();
            GestionnaireFichier.ecrireFichierReseau(fichierSortie, reseau);

            // Lecture des lignes produites
            List<String> lignes = Files.readAllLines(fichierSortie.toPath());
            assertEquals(3, lignes.size(), "Le fichier de sortie doit contenir exactement 3 lignes.");

            // Vérification indépendante de l'ordre
            assertTrue(lignes.stream().anyMatch(l -> l.trim().equals("generateur(GEN_A,200).")),
                    "Ligne générateur manquante ou mal formée.");
            assertTrue(lignes.stream().anyMatch(l -> l.trim().equals("maison(MAISON_A,FORTE).")),
                    "Ligne maison manquante ou mal formée.");
            assertTrue(lignes.stream().anyMatch(l -> l.trim().equals("connexion(GEN_A,MAISON_A).")),
                    "Ligne connexion manquante ou mal formée.");
        } finally {
            supprimerRecursivement(dir);
        }
    }

    // -------------------------------------------------------------------------
    // 4) Robustesse - Erreurs de format / données invalides
    // -------------------------------------------------------------------------

    /**
     * Test de Robustesse :
     * Fichier mal formé => doit retourner null et afficher une erreur sur System.err.
     */
    @Test
    void testLireFichierReseau_FichierInvalide_RetourneNull_EtLogSurErr() throws Exception {
        Path dir = creerDossierTemp();
        try {
            Path cheminFichier = dir.resolve("reseau_ko.txt");

            // Erreur : pas de '.' final + parenthèse fermante manquante
            String contenu = "generateur(GEN1, 100";
            Files.writeString(cheminFichier, contenu);

            final GestionnaireReseau[] resultat = new GestionnaireReseau[1];

            String log = capturerSystemErr(() -> {
                resultat[0] = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());
            });

            assertNull(resultat[0], "Un fichier invalide doit retourner null.");

            assertTrue(
                    log.contains("[Erreur Format]") || log.contains("Problème") || log.contains("Format incorrect"),
                    "Aucun message d'erreur explicite n'a été affiché sur System.err.\nLog capturé:\n" + log
            );
            assertTrue(
                    log.contains("ligne 1") || log.contains("Ligne 1") || log.contains("ligne 1"),
                    "Le message d'erreur devrait indiquer la ligne concernée.\nLog capturé:\n" + log
            );
        } finally {
            supprimerRecursivement(dir);
        }
    }

    /**
     * Test d'erreur :
     * Fichier introuvable => null + log [Erreur IO].
     */
    @Test
    void testLireFichierReseau_FichierIntrouvable() throws Exception {
        Path dir = creerDossierTemp();
        try {
            // On pointe vers un fichier qui n'existe pas (on ne l'écrit pas)
            File inexistant = dir.resolve("fichier_absent.txt").toFile();

            final GestionnaireReseau[] resultat = new GestionnaireReseau[1];

            String log = capturerSystemErr(() -> {
                resultat[0] = GestionnaireFichier.lireFichierReseau(inexistant);
            });

            assertNull(resultat[0], "Un fichier introuvable doit retourner null.");
            assertTrue(
                    log.contains("[Erreur IO]") || log.toLowerCase().contains("introuvable"),
                    "Le log doit indiquer une erreur IO (fichier introuvable).\nLog capturé:\n" + log
            );
        } finally {
            supprimerRecursivement(dir);
        }
    }

    /**
     * Test d'erreur :
     * Capacité non numérique => null + log mentionnant 'Capacité non numérique'.
     */
    @Test
    void testLireFichierReseau_CapaciteNonNumerique() throws Exception {
        Path dir = creerDossierTemp();
        try {
            Path cheminFichier = dir.resolve("reseau_capacite_ko.txt");
            String contenu =
                    "generateur(G1, abc).\n" +
                    "maison(M1, BASSE).\n" +
                    "connexion(G1, M1).";
            Files.writeString(cheminFichier, contenu);

            final GestionnaireReseau[] resultat = new GestionnaireReseau[1];

            String log = capturerSystemErr(() -> {
                resultat[0] = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());
            });

            assertNull(resultat[0], "Une capacité non numérique doit provoquer un retour null.");
            assertTrue(
                    log.contains("[Erreur Format]") &&
                            (log.contains("Capacité non numérique") || log.toLowerCase().contains("capacité")),
                    "Le log doit indiquer une erreur de capacité non numérique.\nLog capturé:\n" + log
            );
        } finally {
            supprimerRecursivement(dir);
        }
    }

    /**
     * Test d'erreur :
     * Type de consommation inconnu => null + log mentionnant 'Type de consommation inconnu'.
     */
    @Test
    void testLireFichierReseau_TypeConsommationInconnu() throws Exception {
        Path dir = creerDossierTemp();
        try {
            Path cheminFichier = dir.resolve("reseau_type_ko.txt");
            String contenu =
                    "generateur(G1, 50).\n" +
                    "maison(M1, INEXISTANT).\n" +  // TypeConsommation.valueOf(...) doit échouer
                    "connexion(G1, M1).";
            Files.writeString(cheminFichier, contenu);

            final GestionnaireReseau[] resultat = new GestionnaireReseau[1];

            String log = capturerSystemErr(() -> {
                resultat[0] = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());
            });

            assertNull(resultat[0], "Un type inconnu doit provoquer un retour null.");
            assertTrue(
                    log.contains("[Erreur Format]") &&
                            (log.contains("Type de consommation inconnu") || log.toLowerCase().contains("type")),
                    "Le log doit indiquer une erreur de type de consommation.\nLog capturé:\n" + log
            );
        } finally {
            supprimerRecursivement(dir);
        }
    }

    /**
     * Test d'erreur :
     * Ordre invalide (générateur défini après maison) => null + log mentionnant 'Ordre invalide'.
     */
    @Test
    void testLireFichierReseau_OrdreInvalide() throws Exception {
        Path dir = creerDossierTemp();
        try {
            Path cheminFichier = dir.resolve("reseau_ordre_ko.txt");
            String contenu =
                    "maison(M1, BASSE).\n" +
                    "generateur(G1, 50).\n" +   // Interdit par la règle "générateurs -> maisons -> connexions"
                    "connexion(G1, M1).";
            Files.writeString(cheminFichier, contenu);

            final GestionnaireReseau[] resultat = new GestionnaireReseau[1];

            String log = capturerSystemErr(() -> {
                resultat[0] = GestionnaireFichier.lireFichierReseau(cheminFichier.toFile());
            });

            assertNull(resultat[0], "Un ordre invalide doit provoquer un retour null.");
            assertTrue(
                    log.contains("[Erreur Format]") && log.toLowerCase().contains("ordre invalide"),
                    "Le log doit indiquer une erreur d'ordre invalide.\nLog capturé:\n" + log
            );
        } finally {
            supprimerRecursivement(dir);
        }
    }
}
