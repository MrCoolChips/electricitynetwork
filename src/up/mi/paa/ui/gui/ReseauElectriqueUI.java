package up.mi.paa.ui.gui;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import up.mi.paa.io.GestionnaireFichier;
import up.mi.paa.model.Couts;
import up.mi.paa.service.CalculateurCouts;
import up.mi.paa.service.GestionnaireReseau;
import up.mi.paa.service.OptimiseurReseau;
import up.mi.paa.ui.gui.components.*;

import java.io.File;

/**
 * Interface graphique principale de l'application.
 * Utilise JavaFX pour afficher le réseau électrique et ses statistiques.
 * 
 * @author Groupe 10
 */
public class ReseauElectriqueUI extends Application implements StyleUI {

    private GestionnaireReseau gestionnaire = new GestionnaireReseau();
    private CalculateurCouts calculateur = new CalculateurCouts(10);
    private OptimiseurReseau optimiseur = new OptimiseurReseau(calculateur);

    private VueReseau vueReseau;
    private VueStatistiques vueStats;
    private VueInventaire vueInventaire;
    private VueTopBar vueTopBar;
    private BorderPane root;

    /**
     * Point d'entrée standard de l'application.
     * @param args Arguments de la ligne de commande.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initialise et affiche la fenêtre principale de l'application.
     * @param stage La fenêtre principale (Stage).
     */
    @Override
    public void start(Stage stage) {
        initialiserComposants();
        configurerActions(stage);
        assemblerInterface();

        Scene scene = new Scene(root, 1400, 850);
        stage.setTitle("Gestionnaire Réseau Électrique");
        stage.setScene(scene);
        stage.show();

        rafraichirTout();
    }

    /**
     * Instancie les composants graphiques et applique le style global.
     */
    private void initialiserComposants() {
        root = new BorderPane();
        root.setStyle("-fx-font-family: " + FONT_MAIN + ";");

        vueReseau = new VueReseau();
        vueStats = new VueStatistiques(10);
        vueInventaire = new VueInventaire();
        vueTopBar = new VueTopBar();

        vueInventaire.setGestionnaire(gestionnaire);
    }

    /**
     * Configure les événements et les actions des boutons.
     * @param stage La fenêtre principale pour les dialogues de fichiers.
     */
    private void configurerActions(Stage stage) {
        vueStats.setActionChangementLambda(lambda -> {
            calculateur.setLambda(lambda);
            rafraichirTout();
        });

        vueStats.setActionOptimisation(this::lancerOptimisation);

        vueInventaire.setOnUpdateRequired(() -> {
            vueReseau.reorganiserLayout();
            rafraichirTout();
        });

        vueTopBar.setActionImport(() -> importerFichier(stage));
        vueTopBar.setActionExport(() -> exporterFichier(stage));
    }

    /**
     * Organise la disposition des vues dans le conteneur principal.
     */
    private void assemblerInterface() {
        root.setTop(vueTopBar);
        root.setCenter(vueReseau);
        root.setRight(vueStats);
        root.setLeft(vueInventaire);
    }

    /**
     * Met à jour toutes les vues avec les données actuelles du réseau.
     */
    private void rafraichirTout() {
        if (gestionnaire == null) return;

        Couts couts = null;
        if (!gestionnaire.getReseauElectrique().getGenerateurs().isEmpty()) {
            try {
                couts = calculateur.calculerCout(gestionnaire.getReseauElectrique());
            } catch (Exception ignored) {}
        }

        vueStats.mettreAJourStats(couts);
        vueReseau.rafraichir(gestionnaire, calculateur);
        vueInventaire.rafraichirListe(calculateur);
    }

    /**
     * Exécute l'algorithme d'optimisation dans un thread d'arrière-plan.
     */
    private void lancerOptimisation() {
        if (gestionnaire == null || gestionnaire.getReseauElectrique().getMaisons().isEmpty()) {
            afficherAlerte("Info", "Le réseau est vide, rien à optimiser !");
            return;
        }

        root.getScene().setCursor(Cursor.WAIT);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                optimiseur.optimiser(gestionnaire.getReseauElectrique());
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            root.getScene().setCursor(Cursor.DEFAULT);
            vueReseau.reorganiserLayout();
            rafraichirTout();
            afficherAlerte("Succès", "Optimisation terminée !");
        });

        task.setOnFailed(e -> {
            root.getScene().setCursor(Cursor.DEFAULT);
            afficherAlerte("Erreur", "Erreur d'optimisation : " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    /**
     * Ouvre une boîte de dialogue pour importer un réseau depuis un fichier.
     * @param stage La fenêtre parente.
     */
    private void importerFichier(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importer Réseau");
        File fichier = fc.showOpenDialog(stage);

        if (fichier == null) return;

        try {
            GestionnaireReseau nouveau = GestionnaireFichier.lireFichierReseau(fichier);

            if (nouveau == null) {
                afficherAlerte("Erreur", "Le fichier contient des erreurs. Import annulé.");
                return;
            }

            this.gestionnaire = nouveau;
            vueInventaire.setGestionnaire(gestionnaire);
            vueReseau.reorganiserLayout();
            rafraichirTout();
            afficherAlerte("Succès", "Réseau importé avec succès !");

        } catch (Exception e) {
            afficherAlerte("Erreur", e.getMessage());
        }
    }

    /**
     * Ouvre une boîte de dialogue pour sauvegarder le réseau au format .txt.
     * @param stage La fenêtre parente.
     */
    private void exporterFichier(Stage stage) {
        if (gestionnaire == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Sauvegarder le Réseau");
        
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Fichiers Texte (*.txt)", "*.txt");
        fc.getExtensionFilters().add(extFilter);
        fc.setInitialFileName("reseau.txt");

        File fichier = fc.showSaveDialog(stage);

        if (fichier != null) {
            if (!fichier.getName().toLowerCase().endsWith(".txt")) {
                fichier = new File(fichier.getAbsolutePath() + ".txt");
            }

            try {
                GestionnaireFichier.ecrireFichierReseau(fichier, gestionnaire.getReseauElectrique());
                afficherAlerte("Succès", "Réseau sauvegardé avec succès !");
            } catch (Exception e) {
                afficherAlerte("Erreur", "Erreur lors de la sauvegarde : " + e.getMessage());
            }
        }
    }

    /**
     * Affiche une boîte de dialogue d'information à l'utilisateur.
     * @param titre Le titre de la fenêtre.
     * @param message Le message à afficher.
     */
    private void afficherAlerte(String titre, String message) {
        Alert alerte = new Alert(Alert.AlertType.INFORMATION);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.showAndWait();
    }
}