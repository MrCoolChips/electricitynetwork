package up.mi.paa.ui;

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
import up.mi.paa.ui.components.*;

import java.io.File;

public class ReseauElectriqueUI extends Application implements StyleUI {

    private GestionnaireReseau gestionnaire = new GestionnaireReseau();
    private CalculateurCouts calculateur = new CalculateurCouts(10);
    private OptimiseurReseau optimiseur = new OptimiseurReseau(calculateur);
    
    private VueReseau vueReseau;
    private VueStatistiques vueStats;
    private VueInventaire vueInventaire;
    private VueTopBar vueTopBar;
    
    private BorderPane root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setStyle("-fx-font-family: " + FONT_MAIN + ";");
        
        vueReseau = new VueReseau();
        vueStats = new VueStatistiques(10);
        vueInventaire = new VueInventaire();
        vueTopBar = new VueTopBar();
        
        vueInventaire.setGestionnaire(gestionnaire);
        
        vueStats.setActionChangementLambda(nouveauLambda -> {
            calculateur.setLambda(nouveauLambda);
            rafraichirTout();
        });
        
        vueStats.setActionOptimisation(this::lancerOptimisation);
        
        vueInventaire.setOnUpdateRequired(() -> {
            vueReseau.reorganiserLayout();
            rafraichirTout();             
        });

        vueTopBar.setActionImport(() -> gererImportFichier(stage));
        vueTopBar.setActionExport(() -> gererExportFichier(stage));

        root.setTop(vueTopBar);
        root.setCenter(vueReseau);
        root.setRight(vueStats);
        root.setLeft(vueInventaire);

        Scene scene = new Scene(root, 1400, 850);
        stage.setTitle("Gestionnaire Réseau");
        stage.setScene(scene);
        stage.show();
        
        rafraichirTout();
    }

    private void rafraichirTout() {
        if (gestionnaire == null) return;

        Couts couts = null;
        if (!gestionnaire.getReseauElectrique().getGenerateurs().isEmpty()) {
             try {
                 couts = calculateur.calculerCout(gestionnaire.getReseauElectrique());
             } catch (Exception e) {
             }
        }
        
        vueStats.mettreAJourStats(couts);
        vueReseau.rafraichir(gestionnaire, calculateur);
        vueInventaire.rafraichirListe(calculateur);
    }
    
    private void lancerOptimisation() {
        if (gestionnaire == null || gestionnaire.getReseauElectrique().getMaisons().isEmpty()) {
            afficherAlerte("Info", "Le réseau est vide, rien à optimiser !");
            return;
        }

        root.getScene().setCursor(Cursor.WAIT);
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
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
            Throwable error = task.getException();
            error.printStackTrace();
            afficherAlerte("Erreur", "Erreur d'optimisation : " + error.getMessage());
        });

        new Thread(task).start();
    }

    private void gererImportFichier(Stage stage) {
         FileChooser fc = new FileChooser();
         fc.setTitle("Importer Réseau");
         File f = fc.showOpenDialog(stage);
         
         if (f != null) {
             try {
                 GestionnaireReseau nouveauGestionnaire = GestionnaireFichier.lireFichierReseau(f);
                 
                 if (nouveauGestionnaire == null) {
                     afficherAlerte("Erreur Import", "Le fichier contient des erreurs (voir console). Import annulé.");
                     return; 
                 }

                 this.gestionnaire = nouveauGestionnaire;
                 
                 vueInventaire.setGestionnaire(this.gestionnaire);
                 vueReseau.reorganiserLayout();
                 rafraichirTout();
                 
                 afficherAlerte("Succès", "Réseau importé avec succès !");

             } catch (Exception e) { 
                 e.printStackTrace(); 
                 afficherAlerte("Erreur Import", e.getMessage());
             }
         }
    }
    
    private void gererExportFichier(Stage stage) {
        if(gestionnaire == null) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Sauvegarder");
        File f = fc.showSaveDialog(stage);
        if(f != null) {
            try { 
                GestionnaireFichier.ecrireFichierReseau(f, gestionnaire.getReseauElectrique()); 
            } catch (Exception e) { 
                e.printStackTrace(); 
                afficherAlerte("Erreur Export", e.getMessage());
            }
        }
    }
    
    private void afficherAlerte(String titre, String msg) {
        Alert alerte = new Alert(Alert.AlertType.INFORMATION);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.setContentText(msg);
        alerte.showAndWait();
    }
}