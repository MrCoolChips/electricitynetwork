package up.mi.paa.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.StringConverter;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.TypeConsommation;
import up.mi.paa.service.CalculateurCouts;
import up.mi.paa.service.GestionnaireReseau;
import up.mi.paa.ui.StyleUI;

/**
 * Panneau latéral gauche : Gère l'affichage des listes et le formulaire d'ajout.
 */
public class VueInventaire extends VBox implements StyleUI {

    private final VBox conteneurListe = new VBox(10);
    private String ongletActif = "Generateurs";
    
    // Champs du formulaire
    private TextField champSaisieNom;
    private TextField champSaisieCapacite;
    private ComboBox<TypeConsommation> comboTypeMaison;
    private StackPane conteneurSaisieDynamique;
    private Button boutonAjouter;
    
    // Références et Callbacks
    private GestionnaireReseau gestionnaire;
    private Runnable onUpdateRequired; // Pour prévenir l'app principale qu'il faut rafraîchir

    public VueInventaire() {
        this.setPrefWidth(320);
        this.setStyle("-fx-background-color: " + FOND_PANNEAU + "; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 0 1 0 0;");
        construireInterface();
    }

    public void setGestionnaire(GestionnaireReseau gestionnaire) {
        this.gestionnaire = gestionnaire;
    }

    public void setOnUpdateRequired(Runnable onUpdateRequired) {
        this.onUpdateRequired = onUpdateRequired;
    }

    private void construireInterface() {
        // En-tête
        Label labelEntete = new Label("EXPLORATEUR RÉSEAU");
        labelEntete.setPadding(new Insets(15));
        labelEntete.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        // Onglets
        HBox onglets = new HBox();
        Button btnGen = creerBoutonOnglet("Générateurs", true);
        Button btnMaison = creerBoutonOnglet("Maisons", false);
        
        btnGen.setOnAction(e -> changerOnglet("Generateurs", btnGen, btnMaison));
        btnMaison.setOnAction(e -> changerOnglet("Maisons", btnMaison, btnGen));
        onglets.getChildren().addAll(btnGen, btnMaison);

        // Zone de liste avec scroll
        ScrollPane defilement = new ScrollPane(conteneurListe);
        defilement.setFitToWidth(true);
        defilement.setStyle("-fx-background: " + FOND_PANNEAU + "; -fx-background-color: transparent;");
        VBox.setVgrow(defilement, Priority.ALWAYS);
        conteneurListe.setPadding(new Insets(10));

        // Formulaire bas
        this.getChildren().addAll(labelEntete, onglets, defilement, construireFormulaire());
        mettreAJourEtatFormulaire();
    }

    private VBox construireFormulaire() {
        VBox formulaireBas = new VBox(10);
        formulaireBas.setPadding(new Insets(15));
        formulaireBas.setStyle("-fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 1 0 0 0;");
        
        HBox ligneFormulaire = new HBox(5);
        champSaisieNom = creerChampTexte("Nom");
        
        conteneurSaisieDynamique = new StackPane();
        champSaisieCapacite = creerChampTexte("Cap (kW)");
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
        return formulaireBas;
    }

    /**
     * Met à jour la liste des éléments affichés.
     */
    public void rafraichirListe(CalculateurCouts calculateur) {
        conteneurListe.getChildren().clear();
        if (gestionnaire == null) return;

        if (ongletActif.equals("Generateurs")) {
            for (Generateur g : gestionnaire.getReseauElectrique().getGenerateurs()) {
                double usage = 0.0;
                try { usage = calculateur.getSommeDesDemandesElectriques(g, gestionnaire.getReseauElectrique()); } catch (Exception e) {}
                boolean surcharge = usage > g.getCapaciteMaximale();
                
                creerCarte(g.getNom(), g.getCapaciteMaximale() + " kW • Utilisé: " + usage + "kW", 
                           surcharge ? ACCENT_ROUGE : ACCENT_BLEU, null, e -> {
                    gestionnaire.getReseauElectrique().getConnexions().remove(g);
                    declencherUpdate();
                });
            }
        } else {
            for (Maison m : gestionnaire.getReseauElectrique().getMaisons()) {
                creerCarteMaison(m);
            }
        }
    }

    private void creerCarteMaison(Maison m) {
        VBox boiteConnexion = new VBox(5);
        Label labelConnecter = new Label("CONNECTER:");
        labelConnecter.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 9px; -fx-font-weight: bold;");
        
        ComboBox<Generateur> comboConnect = new ComboBox<>();
        comboConnect.getItems().addAll(gestionnaire.getReseauElectrique().getGenerateurs());
        comboConnect.setValue(gestionnaire.getReseauElectrique().trouverGenerateur(m));
        comboConnect.setMaxWidth(Double.MAX_VALUE);
        comboConnect.setStyle("-fx-background-color: " + FOND_PRINCIPAL + "; -fx-text-base-color: white; -fx-border-color: " + COULEUR_BORDURE + "; -fx-font-size: 10px;");
        
        // Affichage propre dans la combobox
        comboConnect.setConverter(new StringConverter<Generateur>() {
            @Override public String toString(Generateur g) { return g == null ? "Aucun" : g.getNom() + " (" + g.getCapaciteMaximale() + ")"; }
            @Override public Generateur fromString(String s) { return null; }
        });

        comboConnect.setOnAction(e -> {
            Generateur nouv = comboConnect.getValue();
            Generateur ancien = gestionnaire.getReseauElectrique().trouverGenerateur(m);
            if (nouv != null && nouv != ancien) {
                try {
                    if (ancien != null) gestionnaire.modifierConnexion(m.getNom(), ancien.getNom(), m.getNom(), nouv.getNom());
                    else gestionnaire.creerConnexion(nouv.getNom(), m.getNom());
                    declencherUpdate();
                } catch (Exception ex) { afficherAlerte("Erreur", ex.getMessage()); }
            }
        });

        boiteConnexion.getChildren().addAll(labelConnecter, comboConnect);
        creerCarte(m.getNom(), m.getConsommation() + " kW • " + m.getTypeConsommation(), 
                   ACCENT_VERT, boiteConnexion, e -> {
            gestionnaire.getReseauElectrique().getMaisons().remove(m);
            gestionnaire.getReseauElectrique().supprimerConnexion(m);
            declencherUpdate();
        });
    }

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

    private void gererAjout() {
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
                
                // Connexion auto au premier générateur (UX)
                if (!gestionnaire.getReseauElectrique().getGenerateurs().isEmpty()) {
                     Generateur premierGen = gestionnaire.getReseauElectrique().getGenerateurs().get(0);
                     gestionnaire.creerConnexion(premierGen.getNom(), nom);
                }
            }
            champSaisieNom.clear();
            champSaisieCapacite.clear();
            declencherUpdate();
        } catch (Exception e) { 
            afficherAlerte("Erreur", e.getMessage());
        }
    }

    private void changerOnglet(String onglet, Button actif, Button inactif) {
        this.ongletActif = onglet;
        actif.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-text-fill: " + ACCENT_BLEU + "; -fx-border-color: " + ACCENT_BLEU + "; -fx-border-width: 0 0 2 0;");
        inactif.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-border-width: 0;");
        mettreAJourEtatFormulaire();
        declencherUpdate();
    }

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

    private TextField creerChampTexte(String invite) {
        TextField tf = new TextField();
        tf.setPromptText(invite);
        tf.setStyle("-fx-background-color: " + FOND_PRINCIPAL + "; -fx-text-fill: white; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-radius: 4;");
        return tf;
    }

    private void declencherUpdate() {
        if (onUpdateRequired != null) onUpdateRequired.run();
    }

    private void afficherAlerte(String titre, String msg) {
        Alert alerte = new Alert(Alert.AlertType.INFORMATION);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.setContentText(msg);
        alerte.showAndWait();
    }
}