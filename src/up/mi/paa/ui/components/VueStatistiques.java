package up.mi.paa.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import up.mi.paa.model.Couts;
import up.mi.paa.ui.StyleUI;

/**
 * Panneau droit : Affiche les coûts, gère le slider Lambda et le bouton d'optimisation.
 */
public class VueStatistiques extends VBox implements StyleUI {

    private final Label labelDispersion = new Label("0.000");
    private final Label labelSurcharge = new Label("0.000");
    private final Label labelCout = new Label("0.000");
    private final Slider selecteurLambda;
    
    // Callbacks
    private Runnable actionOptimisation;
    private java.util.function.Consumer<Integer> actionChangementLambda;

    public VueStatistiques(int defaultLambda) {
        this.setPrefWidth(320);
        this.setPadding(new Insets(20));
        this.setStyle("-fx-background-color: " + FOND_PANNEAU + "; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 0 0 0 1;");

        // En-tête
        Label labelEntete = new Label("ANALYSE & OPTIMISATION");
        labelEntete.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        // Carte Coût Global
        VBox carteCout = new VBox(5);
        carteCout.setPadding(new Insets(20));
        carteCout.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-background-radius: 8; -fx-border-color: " + COULEUR_BORDURE + ";");
        Label titreCout = new Label("COÛT GLOBAL");
        titreCout.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        labelCout.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");
        carteCout.getChildren().addAll(titreCout, labelCout, new Label("Coût (Calculé)"));

        // Petites stats (Dispersion / Surcharge)
        HBox ligneStats = new HBox(10);
        ligneStats.getChildren().addAll(
            creerBoiteStat("Dispersion", labelDispersion, ACCENT_BLEU),
            creerBoiteStat("Surcharge", labelSurcharge, ACCENT_VERT)
        );

        // Contrôles
        VBox controlesOpt = new VBox(10);
        Label labelLambda = new Label("Lambda: " + defaultLambda); 
        labelLambda.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + ";");
        
        selecteurLambda = new Slider(0, 100, defaultLambda);
        selecteurLambda.valueProperty().addListener((obs, o, n) -> {
            labelLambda.setText("Lambda: " + n.intValue());
            if (actionChangementLambda != null) actionChangementLambda.accept(n.intValue());
        });

        Button btnOpt = new Button("⚡ Lancer l'Optimisation");
        btnOpt.setMaxWidth(Double.MAX_VALUE);
        btnOpt.setPrefHeight(45);
        btnOpt.setStyle("-fx-background-color: " + ACCENT_BLEU + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        btnOpt.setOnAction(e -> {
            if (actionOptimisation != null) actionOptimisation.run();
        });

        controlesOpt.getChildren().addAll(labelLambda, selecteurLambda, btnOpt);
        
        this.getChildren().addAll(labelEntete, carteCout, ligneStats, new Separator(), controlesOpt);
    }

    public void mettreAJourStats(Couts couts) {
        if (couts == null) {
            // Remise à zéro propre si pas de réseau
            labelCout.setText("0.000");
            labelDispersion.setText("0.000");
            labelSurcharge.setText("0.000");
            return;
        }
        labelCout.setText(String.format("%.3f", couts.getCoutGlobale()));
        labelDispersion.setText(String.format("%.3f", couts.getDispersion()));
        labelSurcharge.setText(String.format("%.3f", couts.getSurcharge()));
    }

    public void setActionOptimisation(Runnable action) { this.actionOptimisation = action; }
    public void setActionChangementLambda(java.util.function.Consumer<Integer> action) { this.actionChangementLambda = action; }

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
}