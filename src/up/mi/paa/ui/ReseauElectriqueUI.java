package up.mi.paa.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import up.mi.paa.io.GestionnaireFichier;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.TypeConsommation;
import up.mi.paa.service.GestionnaireReseau;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface graphique principale pour l'application PAA.
 * Permet la visualisation, l'édition et l'analyse du réseau électrique.
 */
public class ReseauElectriqueUI extends Application {

    // --- Palette de Couleurs ---
    private static final String FOND_PRINCIPAL = "#020617";
    private static final String FOND_PANNEAU = "#0f172a";
    private static final String FOND_CARTE = "#1e293b";
    private static final String COULEUR_BORDURE = "#334155";
    private static final String ACCENT_BLEU = "#3b82f6";
    private static final String ACCENT_VERT = "#10b981";
    private static final String ACCENT_ROUGE = "#ef4444";
    private static final String TEXTE_SECONDAIRE = "#94a3b8";

    // --- État de l'application ---
    private GestionnaireReseau gestionnaire = new GestionnaireReseau();
    
    // Map pour suivre les éléments visuels (Noeuds graphiques)
    private final Map<Object, StackPane> visuelsNoeuds = new HashMap<>();

    // Onglet actif ("Generateurs" ou "Maisons")
    private String ongletActif = "Generateurs"; 

    // --- Composants UI ---
    private Label labelDispersion = new Label("0.000");
    private Label labelSurcharge = new Label("0.000");
    private Label labelCout = new Label("0.000");
    private Slider selecteurLambda;
    private VBox conteneurListe = new VBox(10);
    private Pane panneauReseau = new Pane(); 
    private Pane coucheConnexions = new Pane(); 
    private Pane coucheNoeuds = new Pane();       

    // --- Formulaire d'ajout ---
    private TextField champSaisieNom;
    private TextField champSaisieCapacite; 
    private ComboBox<TypeConsommation> comboTypeMaison; 
    private StackPane conteneurSaisieDynamique; 
    private Button boutonAjouter;

    // TODO: Service d'optimisation à intégrer
    // private ReseauOptimiseur optimiseur; 

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage scenePrincipale) {
        BorderPane racine = new BorderPane();
        racine.setStyle("-fx-background-color: " + FOND_PRINCIPAL + "; -fx-font-family: 'Segoe UI', sans-serif;");

        racine.setTop(creerBarreSuperieure(scenePrincipale));
        racine.setLeft(creerPanneauGauche());

        // Configuration du panneau central (Réseau)
        panneauReseau.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        panneauReseau.getChildren().addAll(coucheConnexions, coucheNoeuds);
        dessinerGrille(3000, 3000); 

        // Masquage des débordements (Clipping)
        Rectangle masque = new Rectangle();
        masque.widthProperty().bind(panneauReseau.widthProperty());
        masque.heightProperty().bind(panneauReseau.heightProperty());
        panneauReseau.setClip(masque);

        racine.setCenter(panneauReseau);
        racine.setRight(creerPanneauDroit());

        Scene scene = new Scene(racine, 1400, 850);
        scenePrincipale.setTitle("PAA - Gestionnaire Réseau");
        scenePrincipale.setScene(scene);
        scenePrincipale.show();
        
        mettreAJourInterface();
    }

    // ==========================================
    // CONSTRUCTION DE L'INTERFACE (UI)
    // ==========================================

    /**
     * Crée la barre supérieure contenant le titre et les boutons d'import/export.
     */
    private HBox creerBarreSuperieure(Stage stage) {
        HBox barre = new HBox(20);
        barre.setPadding(new Insets(10, 20, 10, 20));
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.setStyle("-fx-background-color: " + FOND_PANNEAU + "; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 0 0 1 0;");

        Label icone = new Label("⚡");
        icone.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-background-color: #2563eb; -fx-padding: 5 10; -fx-background-radius: 5;");
        Label titre = new Label("PAA");
        titre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");

        Button btnImport = creerBoutonTexte("📂 Importer");
        btnImport.setOnAction(e -> gererImportFichier(stage));
        
        Button btnExport = creerBoutonTexte("💾 Exporter");
        btnExport.setOnAction(e -> gererExportFichier(stage));

        Region espaceur = new Region();
        HBox.setHgrow(espaceur, Priority.ALWAYS);

        Label univ = new Label("Univ. Paris Cité");
        univ.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 12px;");

        barre.getChildren().addAll(icone, titre, new Separator(javafx.geometry.Orientation.VERTICAL), btnImport, btnExport, espaceur, univ);
        return barre;
    }

    /**
     * Crée le panneau gauche (Explorateur et Formulaire d'ajout).
     */
    private VBox creerPanneauGauche() {
        VBox panneau = new VBox();
        panneau.setPrefWidth(320);
        panneau.setStyle("-fx-background-color: " + FOND_PANNEAU + "; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 0 1 0 0;");

        Label labelEntete = new Label("EXPLORATEUR RÉSEAU");
        labelEntete.setPadding(new Insets(15));
        labelEntete.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        HBox onglets = new HBox();
        Button btnGen = creerBoutonOnglet("Générateurs", true);
        Button btnMaison = creerBoutonOnglet("Maisons", false);
        
        btnGen.setOnAction(e -> {
            ongletActif = "Generateurs";
            mettreAJourStylesOnglets(btnGen, btnMaison);
            mettreAJourEtatFormulaire();
            mettreAJourInventaire();
        });
        btnMaison.setOnAction(e -> {
            ongletActif = "Maisons";
            mettreAJourStylesOnglets(btnMaison, btnGen);
            mettreAJourEtatFormulaire();
            mettreAJourInventaire();
        });
        onglets.getChildren().addAll(btnGen, btnMaison);

        ScrollPane defilement = new ScrollPane(conteneurListe);
        defilement.setFitToWidth(true);
        defilement.setStyle("-fx-background: " + FOND_PANNEAU + "; -fx-background-color: transparent;");
        VBox.setVgrow(defilement, Priority.ALWAYS);
        conteneurListe.setPadding(new Insets(10));

        VBox formulaireBas = new VBox(10);
        formulaireBas.setPadding(new Insets(15));
        formulaireBas.setStyle("-fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 1 0 0 0;");
        
        HBox ligneFormulaire = new HBox(5);
        champSaisieNom = creerChampTexteStyle("Nom");
        
        conteneurSaisieDynamique = new StackPane();
        champSaisieCapacite = creerChampTexteStyle("Cap (kW)");
        champSaisieCapacite.setPrefWidth(80);
        
        comboTypeMaison = new ComboBox<>();
        comboTypeMaison.getItems().addAll(TypeConsommation.values());
        comboTypeMaison.getSelectionModel().select(TypeConsommation.NORMAL);
        comboTypeMaison.setPrefWidth(100);
        comboTypeMaison.setStyle("-fx-background-color: " + FOND_PRINCIPAL + "; -fx-text-base-color: white; -fx-border-color: " + COULEUR_BORDURE + ";");
        
        conteneurSaisieDynamique.getChildren().add(champSaisieCapacite); 
        
        boutonAjouter = new Button("+");
        boutonAjouter.setPrefWidth(30);
        boutonAjouter.setStyle("-fx-background-color: " + ACCENT_BLEU + "; -fx-text-fill: white; -fx-font-weight: bold;");
        boutonAjouter.setOnAction(e -> gererAjout());
        
        ligneFormulaire.getChildren().addAll(champSaisieNom, conteneurSaisieDynamique, boutonAjouter);
        formulaireBas.getChildren().add(ligneFormulaire);

        panneau.getChildren().addAll(labelEntete, onglets, defilement, formulaireBas);
        mettreAJourEtatFormulaire(); 
        return panneau;
    }

    /**
     * Crée le panneau droit (Statistiques et Contrôles).
     */
    private VBox creerPanneauDroit() {
        VBox panneau = new VBox(20);
        panneau.setPrefWidth(320);
        panneau.setPadding(new Insets(20));
        panneau.setStyle("-fx-background-color: " + FOND_PANNEAU + "; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 0 0 0 1;");

        Label labelEntete = new Label("ANALYSE & OPTIMISATION");
        labelEntete.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        VBox carteCout = new VBox(5);
        carteCout.setPadding(new Insets(20));
        carteCout.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-background-radius: 8; -fx-border-color: " + COULEUR_BORDURE + ";");
        Label titreCout = new Label("COÛT GLOBAL");
        titreCout.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        labelCout.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");
        carteCout.getChildren().addAll(titreCout, labelCout, new Label("Coût (Calculé)"));

        HBox ligneStats = new HBox(10);
        ligneStats.getChildren().addAll(
            creerBoiteStat("Dispersion", labelDispersion, ACCENT_BLEU),
            creerBoiteStat("Surcharge", labelSurcharge, ACCENT_VERT)
        );

        VBox controlesOpt = new VBox(10);
        Label labelLambda = new Label("Lambda: " + GestionnaireReseau.LAMBDA); 
        labelLambda.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + ";");
        
        selecteurLambda = new Slider(0, 100, GestionnaireReseau.LAMBDA);
        selecteurLambda.valueProperty().addListener((obs, o, n) -> {
            labelLambda.setText("Lambda: " + n.intValue());
            recalculerStatistiques();
        });

        Button btnOpt = new Button("⚡ Lancer l'Optimisation");
        btnOpt.setMaxWidth(Double.MAX_VALUE);
        btnOpt.setPrefHeight(45);
        btnOpt.setStyle("-fx-background-color: " + ACCENT_BLEU + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        btnOpt.setOnAction(e -> lancerOptimisation());

        controlesOpt.getChildren().addAll(labelLambda, selecteurLambda, btnOpt);
        panneau.getChildren().addAll(labelEntete, carteCout, ligneStats, new Separator(), controlesOpt);
        return panneau;
    }

    // ==========================================
    // DESSIN ET INTERACTION DU RÉSEAU
    // ==========================================

    /**
     * Reconstruit entièrement la visualisation graphique du réseau.
     */
    private void reconstruireGraphe() {
        coucheNoeuds.getChildren().clear();
        coucheConnexions.getChildren().clear();
        visuelsNoeuds.clear();

        if (gestionnaire == null || gestionnaire.getReseauElectrique() == null) return;

        double largeur = panneauReseau.getWidth() > 0 ? panneauReseau.getWidth() : 1000;
        double hauteur = panneauReseau.getHeight() > 0 ? panneauReseau.getHeight() : 800;
        
        double marge = 100;
        double largeurUtile = largeur - 2 * marge;
        double hauteurUtile = hauteur - 2 * marge;

        // 1. Placement des noeuds (Générateurs et Maisons)
        for (Generateur g : gestionnaire.getReseauElectrique().getGenerateurs()) {
            
            double demandeTotale = gestionnaire.getSommeDesDemandesElectriques(g);
            boolean surcharge = demandeTotale > g.getCapaciteMaximale();
            
            double gx = marge + Math.random() * largeurUtile;
            double gy = marge + Math.random() * hauteurUtile;
            
            StackPane noeudGen = creerNoeudDeplacable(g, gx, gy, 
                                                 surcharge ? ACCENT_ROUGE : ACCENT_BLEU, 
                                                 "⚡", g.getNom(), g.getCapaciteMaximale() + "kW", true);
            visuelsNoeuds.put(g, noeudGen);
            coucheNoeuds.getChildren().add(noeudGen);
            
            List<Maison> maisonsConnectees = gestionnaire.getReseauElectrique().trouverLesMaisonsDeGenerateur(g);
            int nbMaisons = maisonsConnectees.size();
            double rayon = 120;
            
            for (int i = 0; i < nbMaisons; i++) {
                Maison m = maisonsConnectees.get(i);
                double angle = (2 * Math.PI / nbMaisons) * i;
                double mx = gx + rayon * Math.cos(angle);
                double my = gy + rayon * Math.sin(angle);
                
                if (!visuelsNoeuds.containsKey(m)) {
                    StackPane noeudMaison = creerNoeudDeplacable(m, mx, my, 
                                                         ACCENT_VERT, 
                                                         "🏠", m.getNom(), m.getConsommation() + "kW", false);
                    visuelsNoeuds.put(m, noeudMaison);
                    coucheNoeuds.getChildren().add(noeudMaison);
                }
            }
        }

        // Placement des maisons non connectées
        for (Maison m : gestionnaire.getReseauElectrique().getMaisons()) {
            if (!visuelsNoeuds.containsKey(m)) {
                creerNoeudDeplacable(m, largeur/2, hauteur/2, ACCENT_VERT, "🏠", m.getNom(), m.getConsommation() + "kW", false);
            }
        }

        // 2. Dessin des connexions
        for (Generateur g : gestionnaire.getReseauElectrique().getGenerateurs()) {
            StackPane noeudGen = visuelsNoeuds.get(g);
            if (noeudGen == null) continue;

            for (Maison m : gestionnaire.getReseauElectrique().trouverLesMaisonsDeGenerateur(g)) {
                StackPane noeudMaison = visuelsNoeuds.get(m);
                if (noeudMaison == null) continue;

                Line ligne = new Line();
                ligne.setStroke(Color.web(TEXTE_SECONDAIRE));
                ligne.setStrokeWidth(2);
                ligne.setOpacity(0.3);
                ligne.getStrokeDashArray().addAll(10d, 5d); 

                ligne.startXProperty().bind(noeudGen.layoutXProperty().add(25)); 
                ligne.startYProperty().bind(noeudGen.layoutYProperty().add(25)); 
                ligne.endXProperty().bind(noeudMaison.layoutXProperty().add(18));   
                ligne.endYProperty().bind(noeudMaison.layoutYProperty().add(18));

                coucheConnexions.getChildren().add(ligne);
            }
        }
    }

    /**
     * Crée un élément visuel interactif (draggable) représentant un noeud du réseau.
     */
    private StackPane creerNoeudDeplacable(Object donnee, double initX, double initY, String couleurHex, String iconeTxt, String nom, String sousTexte, boolean estGen) {
        
        Circle cercle = new Circle(estGen ? 25 : 18);
        cercle.setFill(Color.web(couleurHex));
        cercle.setStroke(Color.web(FOND_PRINCIPAL));
        cercle.setStrokeWidth(3);
        cercle.setEffect(new javafx.scene.effect.DropShadow(10, Color.BLACK));

        Text icone = new Text(iconeTxt);
        icone.setFill(Color.WHITE);
        icone.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, estGen ? 20 : 14));

        StackPane groupeCercle = new StackPane(cercle, icone);

        VBox boiteEtiquette = new VBox(0);
        boiteEtiquette.setAlignment(Pos.CENTER);
        boiteEtiquette.setPadding(new Insets(2, 6, 2, 6));
        boiteEtiquette.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-background-radius: 4; -fx-border-color: " + COULEUR_BORDURE + ";");
        
        Label lNom = new Label(nom); lNom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lSous = new Label(sousTexte); lSous.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 9px;");
        boiteEtiquette.getChildren().addAll(lNom, lSous);
        boiteEtiquette.setTranslateY(estGen ? 40 : 32);

        StackPane conteneurNoeud = new StackPane(groupeCercle, boiteEtiquette);
        conteneurNoeud.setLayoutX(initX);
        conteneurNoeud.setLayoutY(initY);
        
        // --- Logique de Déplacement et Collision ---
        final Delta deltaDeplacement = new Delta();

        conteneurNoeud.setOnMousePressed(e -> {
            deltaDeplacement.x = conteneurNoeud.getLayoutX() - e.getSceneX();
            deltaDeplacement.y = conteneurNoeud.getLayoutY() - e.getSceneY();
            conteneurNoeud.setCursor(javafx.scene.Cursor.MOVE);
            conteneurNoeud.toFront();
            e.consume();
        });

        conteneurNoeud.setOnMouseDragged(e -> {
            double cibleX = e.getSceneX() + deltaDeplacement.x;
            double cibleY = e.getSceneY() + deltaDeplacement.y;
            
            double deltaProposeX = cibleX - conteneurNoeud.getLayoutX();
            double deltaProposeY = cibleY - conteneurNoeud.getLayoutY();
            
            double marge = 40;
            double l = panneauReseau.getWidth();
            double h = panneauReseau.getHeight();
            
            double maxDxPos = (l - marge) - conteneurNoeud.getLayoutX();
            double maxDxNeg = marge - conteneurNoeud.getLayoutX();
            double maxDyPos = (h - marge) - conteneurNoeud.getLayoutY();
            double maxDyNeg = marge - conteneurNoeud.getLayoutY();

            // Gestion collision groupe (Si Générateur, on vérifie ses maisons)
            if (estGen && donnee instanceof Generateur) {
                Generateur g = (Generateur) donnee;
                for (Maison m : gestionnaire.getReseauElectrique().trouverLesMaisonsDeGenerateur(g)) {
                    StackPane noeudMaison = visuelsNoeuds.get(m);
                    if (noeudMaison != null) {
                        double hMaxDx = (l - marge) - noeudMaison.getLayoutX();
                        double hMinDx = marge - noeudMaison.getLayoutX();
                        double hMaxDy = (h - marge) - noeudMaison.getLayoutY();
                        double hMinDy = marge - noeudMaison.getLayoutY();
                        
                        maxDxPos = Math.min(maxDxPos, hMaxDx);
                        maxDxNeg = Math.max(maxDxNeg, hMinDx);
                        maxDyPos = Math.min(maxDyPos, hMaxDy);
                        maxDyNeg = Math.max(maxDyNeg, hMinDy);
                    }
                }
            }

            double deltaSurX = Math.max(maxDxNeg, Math.min(maxDxPos, deltaProposeX));
            double deltaSurY = Math.max(maxDyNeg, Math.min(maxDyPos, deltaProposeY));

            conteneurNoeud.setLayoutX(conteneurNoeud.getLayoutX() + deltaSurX);
            conteneurNoeud.setLayoutY(conteneurNoeud.getLayoutY() + deltaSurY);
            
            // Déplacement solidaire des maisons connectées
            if (estGen && donnee instanceof Generateur) {
                Generateur g = (Generateur) donnee;
                for (Maison m : gestionnaire.getReseauElectrique().trouverLesMaisonsDeGenerateur(g)) {
                    StackPane noeudMaison = visuelsNoeuds.get(m);
                    if (noeudMaison != null) {
                        noeudMaison.setLayoutX(noeudMaison.getLayoutX() + deltaSurX);
                        noeudMaison.setLayoutY(noeudMaison.getLayoutY() + deltaSurY);
                    }
                }
            }
            e.consume();
        });

        conteneurNoeud.setOnMouseReleased(e -> conteneurNoeud.setCursor(javafx.scene.Cursor.HAND));

        return conteneurNoeud;
    }
    
    // Classe interne pour le calcul de déplacement
    private static class Delta { double x, y; }

    /**
     * Dessine la grille de fond.
     */
    private void dessinerGrille(double l, double h) {
        Pane grille = new Pane();
        for (int i = 0; i < l; i += 40) {
            Line v = new Line(i, 0, i, h);
            v.setStroke(Color.web("#1e293b"));
            grille.getChildren().add(v);
        }
        for (int j = 0; j < h; j += 40) {
            Line hr = new Line(0, j, l, j);
            hr.setStroke(Color.web("#1e293b"));
            grille.getChildren().add(hr);
        }
        grille.toBack();
        panneauReseau.getChildren().add(0, grille);
    }

    // ==========================================
    // GESTION INVENTAIRE ET ACTIONS
    // ==========================================

    /**
     * Met à jour la liste latérale (Générateurs ou Maisons).
     */
    private void mettreAJourInventaire() {
        conteneurListe.getChildren().clear();
        if (gestionnaire == null) return;

        if (ongletActif.equals("Generateurs")) {
            for (Generateur g : gestionnaire.getReseauElectrique().getGenerateurs()) {
                double usage = gestionnaire.getSommeDesDemandesElectriques(g);
                boolean surcharge = usage > g.getCapaciteMaximale();
                
                creerCarte(g.getNom(), g.getCapaciteMaximale() + " kW • Utilisé: " + usage + "kW", 
                           surcharge ? ACCENT_ROUGE : ACCENT_BLEU, null, e -> {
                    gestionnaire.getReseauElectrique().getConnexions().remove(g);
                    mettreAJourInterface();
                });
            }
        } else {
            for (Maison m : gestionnaire.getReseauElectrique().getMaisons()) {
                VBox boiteConnexion = new VBox(5);
                Label labelConnecter = new Label("CONNECTER:");
                labelConnecter.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 9px; -fx-font-weight: bold;");
                
                ComboBox<Generateur> comboConnect = new ComboBox<>();
                comboConnect.getItems().addAll(gestionnaire.getReseauElectrique().getGenerateurs());
                comboConnect.setValue(gestionnaire.getReseauElectrique().trouverGenerateur(m));
                comboConnect.setMaxWidth(Double.MAX_VALUE);
                comboConnect.setStyle("-fx-background-color: " + FOND_PRINCIPAL + "; -fx-text-base-color: white; -fx-border-color: " + COULEUR_BORDURE + "; -fx-font-size: 10px;");
                
                comboConnect.setConverter(new StringConverter<Generateur>() {
                    @Override public String toString(Generateur g) { return g == null ? "Aucun" : g.getNom() + " (Cap: " + g.getCapaciteMaximale() + ")"; }
                    @Override public Generateur fromString(String s) { return null; }
                });

                comboConnect.setOnAction(e -> {
                    Generateur genSelectionne = comboConnect.getValue();
                    Generateur genActuel = gestionnaire.getReseauElectrique().trouverGenerateur(m);
                    
                    if (genSelectionne != null && genSelectionne != genActuel) {
                        try {
                            if (genActuel != null) {
                                gestionnaire.modifierConnexion(m.getNom(), genActuel.getNom(), m.getNom(), genSelectionne.getNom());
                            } else {
                                gestionnaire.creerConnexion(genSelectionne.getNom(), m.getNom());
                            }
                            mettreAJourInterface();
                        } catch (Exception ex) {
                            afficherAlerte("Erreur", ex.getMessage());
                        }
                    }
                });

                boiteConnexion.getChildren().addAll(labelConnecter, comboConnect);

                creerCarte(m.getNom(), m.getConsommation() + " kW • " + m.getTypeConsommation(), 
                           ACCENT_VERT, boiteConnexion, e -> {
                    gestionnaire.getReseauElectrique().getMaisons().remove(m);
                    gestionnaire.getReseauElectrique().supprimerConnexion(m);
                    mettreAJourInterface();
                });
            }
        }
    }

    /**
     * Crée une carte d'information pour la liste latérale.
     */
    private void creerCarte(String titre, String sousTitre, String couleurPoint, javafx.scene.Node contenuExtra, javafx.event.EventHandler<javafx.event.ActionEvent> suppression) {
        VBox carte = new VBox(8);
        carte.setPadding(new Insets(10));
        carte.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-background-radius: 6; -fx-border-color: " + COULEUR_BORDURE + ";");
        
        HBox ligneHaut = new HBox(10);
        ligneHaut.setAlignment(Pos.CENTER_LEFT);
        
        Circle point = new Circle(4, Color.web(couleurPoint));
        
        VBox textes = new VBox(2);
        Label lTitre = new Label(titre); lTitre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label lSous = new Label(sousTitre); lSous.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 10px;");
        textes.getChildren().addAll(lTitre, lSous);
        HBox.setHgrow(textes, Priority.ALWAYS);

        Button btnSuppr = new Button("✕");
        btnSuppr.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-cursor: hand;");
        btnSuppr.setOnMouseEntered(e -> btnSuppr.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT_ROUGE + ";"));
        btnSuppr.setOnMouseExited(e -> btnSuppr.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXTE_SECONDAIRE + ";"));
        btnSuppr.setOnAction(suppression);

        ligneHaut.getChildren().addAll(point, textes, btnSuppr);
        carte.getChildren().add(ligneHaut);

        if (contenuExtra != null) {
            Separator sep = new Separator();
            sep.setOpacity(0.1);
            carte.getChildren().addAll(sep, contenuExtra);
        }

        conteneurListe.getChildren().add(carte);
    }

    /**
     * Gère l'ajout d'un nouvel élément depuis le formulaire.
     */
    private void gererAjout() {
    	// Important : Conversion majuscule pour la cohérence
    	String nom = champSaisieNom.getText().trim().toUpperCase();
        if (nom.isEmpty()) return;

        try {
            if (ongletActif.equals("Generateurs")) {
                String capStr = champSaisieCapacite.getText().trim();
                double cap = capStr.isEmpty() ? 60.0 : Double.parseDouble(capStr);
                gestionnaire.ajouterOuModifierGenerateur(nom, cap);
            } else {
                TypeConsommation type = comboTypeMaison.getValue();
                if (type == null) type = TypeConsommation.NORMAL;
                
                gestionnaire.ajouterOuModifierMaison(nom, type);
                
                // Connexion auto au premier générateur (Optionnel)
                if (!gestionnaire.getReseauElectrique().getGenerateurs().isEmpty()) {
                     Generateur premierGen = gestionnaire.getReseauElectrique().getGenerateurs().get(0);
                     gestionnaire.creerConnexion(premierGen.getNom(), nom);
                }
            }
            champSaisieNom.clear();
            champSaisieCapacite.clear();
            mettreAJourInterface(); 
        } catch (Exception e) { 
            afficherAlerte("Erreur", e.getMessage());
        }
    }

    /**
     * Met à jour l'état du formulaire selon l'onglet actif.
     */
    private void mettreAJourEtatFormulaire() {
        conteneurSaisieDynamique.getChildren().clear();
        if (ongletActif.equals("Generateurs")) {
            champSaisieNom.setPromptText("Nom (G1)");
            conteneurSaisieDynamique.getChildren().add(champSaisieCapacite);
            boutonAjouter.setStyle("-fx-background-color: " + ACCENT_BLEU + "; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            champSaisieNom.setPromptText("Nom (M1)");
            conteneurSaisieDynamique.getChildren().add(comboTypeMaison);
            boutonAjouter.setStyle("-fx-background-color: " + ACCENT_VERT + "; -fx-text-fill: white; -fx-font-weight: bold;");
        }
    }

    private Button creerBoutonOnglet(String texte, boolean estActif) {
        Button btn = new Button(texte);
        btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS);
        btn.setPrefHeight(35);
        if (estActif) btn.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-text-fill: " + ACCENT_BLEU + "; -fx-border-color: " + ACCENT_BLEU + "; -fx-border-width: 0 0 2 0;");
        else btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-border-width: 0;");
        return btn;
    }

    private void mettreAJourStylesOnglets(Button actif, Button inactif) {
        actif.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-text-fill: " + ACCENT_BLEU + "; -fx-border-color: " + ACCENT_BLEU + "; -fx-border-width: 0 0 2 0;");
        inactif.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-border-width: 0;");
    }

    private VBox creerBoiteStat(String titre, Label etiquetteValeur, String couleur) {
        VBox boite = new VBox(5);
        HBox.setHgrow(boite, Priority.ALWAYS);
        boite.setPadding(new Insets(10));
        boite.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-background-radius: 6; -fx-border-color: " + COULEUR_BORDURE + ";");
        Label lTitre = new Label(titre); lTitre.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 10px;");
        etiquetteValeur.setStyle("-fx-text-fill: " + couleur + "; -fx-font-weight: bold; -fx-font-size: 16px;");
        boite.getChildren().addAll(lTitre, etiquetteValeur);
        return boite;
    }

    private TextField creerChampTexteStyle(String invite) {
        TextField tf = new TextField();
        tf.setPromptText(invite);
        tf.setStyle("-fx-background-color: " + FOND_PRINCIPAL + "; -fx-text-fill: white; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-radius: 4;");
        return tf;
    }

    private Button creerBoutonTexte(String texte) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 11px; -fx-cursor: hand;");
        return btn;
    }

    // TODO: Intégrer la logique d'optimisation ici
    private void lancerOptimisation() {
        if (gestionnaire != null) {
            afficherAlerte("Info", "Optimisation non implémentée (TODO)");
            mettreAJourInterface(); 
        }
    }

    /**
     * Rafraîchit l'interface utilisateur (Graphe, Stats, Liste).
     */
    private void mettreAJourInterface() {
        recalculerStatistiques();
        reconstruireGraphe();
        mettreAJourInventaire();
    }

    private void recalculerStatistiques() {
        if (gestionnaire == null) return;
        
        try {
            double cout = gestionnaire.calculerCout();
            double surcharge = gestionnaire.calculerSurcharge(gestionnaire.getReseauElectrique().getGenerateurs());
            double dispersion = gestionnaire.calculerDisps(gestionnaire.getReseauElectrique().getGenerateurs());

            labelCout.setText(String.format("%.3f", cout));
            labelDispersion.setText(String.format("%.3f", dispersion));
            labelSurcharge.setText(String.format("%.3f", surcharge));
        } catch (Exception e) {
            labelCout.setText("Err");
        }
    }
    
    private void gererImportFichier(Stage stage) {
         FileChooser selecteurFichier = new FileChooser();
         selecteurFichier.setTitle("Importer Réseau");
         File fichier = selecteurFichier.showOpenDialog(stage);
         if (fichier != null) {
             try {
                 gestionnaire = GestionnaireFichier.lireFichierReseau(fichier);
                 mettreAJourInterface(); 
             } catch (Exception e) { e.printStackTrace(); }
         }
    }
    
    private void gererExportFichier(Stage stage) {
        if(gestionnaire == null) return;
        FileChooser selecteurFichier = new FileChooser();
        selecteurFichier.setTitle("Sauvegarder");
        File fichier = selecteurFichier.showSaveDialog(stage);
        if(fichier != null) {
            try { 
                GestionnaireFichier.ecrireFichierReseau(fichier, gestionnaire.getReseauElectrique()); 
            } 
            catch (Exception e) { e.printStackTrace(); }
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