package up.mi.paa.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import up.mi.paa.io.GestionnaireFichier;
import up.mi.paa.model.Couts;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.ReseauElectrique;
import up.mi.paa.service.CalculateurCouts;
import up.mi.paa.service.GestionnaireReseau;
import up.mi.paa.ui.components.*;

import java.io.File;
import java.util.List;
import java.util.Random;

/**
 * Point d'entrée de l'application (Main Controller).
 * Orchestre les vues (gauche, centre, droite) et les services.
 */
public class ReseauElectriqueUI extends Application implements StyleUI {

    // Modèle et Services
    private GestionnaireReseau gestionnaire;
    private CalculateurCouts calculateur;
    
    // Vues (Composants UI)
    private VueReseau vueReseau;
    private VueStatistiques vueStats;
    private VueInventaire vueInventaire;
    private VueTopBar vueTopBar;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        this.gestionnaire = new GestionnaireReseau();
        // Lambda défaut = 10 (modifiable via l'interface)
        this.calculateur = new CalculateurCouts(10);
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-font-family: " + FONT_MAIN + ";");
        
        // 1. Initialisation des composants
        vueReseau = new VueReseau();
        vueStats = new VueStatistiques(10);
        vueInventaire = new VueInventaire();
        vueTopBar = new VueTopBar();
        
        // 2. Injection des dépendances (Le contrôleur donne les références aux vues)
        vueInventaire.setGestionnaire(gestionnaire);
        
        // 3. Configuration des événements (Wiring)
        
        // Quand le slider change -> On met à jour le calculateur et on rafraîchit
        vueStats.setActionChangementLambda(nouveauLambda -> {
            calculateur.setLambda(nouveauLambda);
            rafraichirTout();
        });
        
        // Quand on clique sur Optimiser
        vueStats.setActionOptimisation(this::lancerOptimisation);
        
        // Quand l'inventaire change (ajout/suppression) -> On rafraîchit tout
        vueInventaire.setOnUpdateRequired(this::rafraichirTout);

        // Import / Export
        vueTopBar.setActionImport(() -> gererImportFichier(stage));
        vueTopBar.setActionExport(() -> gererExportFichier(stage));

        // 4. Assemblage final
        root.setTop(vueTopBar);
        root.setCenter(vueReseau);
        root.setRight(vueStats);
        root.setLeft(vueInventaire);

        Scene scene = new Scene(root, 1400, 850);
        stage.setTitle("Gestionnaire Réseau");
        stage.setScene(scene);
        stage.show();
        
        // Premier affichage
        rafraichirTout();
    }

    /**
     * Méthode centrale de mise à jour.
     * Elle recalcule les coûts et demande à chaque vue de se redessiner.
     */
    private void rafraichirTout() {
        // 1. Calcul des stats
        Couts couts = null;
        if (!gestionnaire.getReseauElectrique().getGenerateurs().isEmpty()) {
             try {
                 couts = calculateur.calculerCout(gestionnaire.getReseauElectrique());
             } catch (Exception e) {
                 // Si erreur (ex: division par zéro temporaire), on laisse null
             }
        }
        
        // 2. Mise à jour des vues
        vueStats.mettreAJourStats(couts);
        vueReseau.rafraichir(gestionnaire, calculateur);
        vueInventaire.rafraichirListe(calculateur);
    }
    
    /**
     * Logique de l'algorithme d'optimisation (Partie 2).
     */
    private void lancerOptimisation() {
        if (gestionnaire == null || gestionnaire.getReseauElectrique().getMaisons().isEmpty()) {
            afficherAlerte("Info", "Le réseau est vide, rien à optimiser !");
            return;
        }

        ReseauElectrique reseau = gestionnaire.getReseauElectrique();
        
        // Coût initial
        double meilleurCout = Double.MAX_VALUE;
        try {
            meilleurCout = calculateur.calculerCout(reseau).getCoutGlobale();
        } catch (Exception e) {}
        
        int iterations = 1000;
        int ameliorations = 0;
        Random rand = new Random();
        List<Maison> maisons = reseau.getMaisons();
        List<Generateur> generateurs = reseau.getGenerateurs();
        
        // Boucle d'optimisation (Algorithme naïf / Hill Climbing)
        for (int i = 0; i < iterations; i++) {
            Maison m = maisons.get(rand.nextInt(maisons.size()));
            Generateur gNouveau = generateurs.get(rand.nextInt(generateurs.size()));
            Generateur gAncien = reseau.trouverGenerateur(m);
            
            if (gNouveau == gAncien) continue;
            
            try {
                // On tente le changement (S -> S')
                if(gAncien != null) gestionnaire.modifierConnexion(m.getNom(), gAncien.getNom(), m.getNom(), gNouveau.getNom());
                else gestionnaire.creerConnexion(gNouveau.getNom(), m.getNom());
                
                double nouveauCout = calculateur.calculerCout(reseau).getCoutGlobale();
                
                if (nouveauCout < meilleurCout) {
                    meilleurCout = nouveauCout;
                    ameliorations++;
                } else {
                    // Annulation (Rollback)
                    gestionnaire.modifierConnexion(m.getNom(), gNouveau.getNom(), m.getNom(), gAncien.getNom());
                }
            } catch (Exception ex) {}
        }
        
        rafraichirTout();
        afficherAlerte("Optimisation terminée", ameliorations + " améliorations trouvées.");
    }

    private void gererImportFichier(Stage stage) {
         FileChooser fc = new FileChooser();
         fc.setTitle("Importer Réseau");
         File f = fc.showOpenDialog(stage);
         if (f != null) {
             try {
                 this.gestionnaire = GestionnaireFichier.lireFichierReseau(f);
                 // On doit redonner la nouvelle référence au panneau gauche
                 vueInventaire.setGestionnaire(this.gestionnaire);
                 rafraichirTout(); 
             } catch (Exception e) { e.printStackTrace(); afficherAlerte("Erreur Import", e.getMessage());}
         }
    }
    
    private void gererExportFichier(Stage stage) {
        if(gestionnaire == null) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Sauvegarder");
        File f = fc.showSaveDialog(stage);
        if(f != null) {
            try { GestionnaireFichier.ecrireFichierReseau(f, gestionnaire.getReseauElectrique()); } 
            catch (Exception e) { e.printStackTrace(); afficherAlerte("Erreur Export", e.getMessage());}
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