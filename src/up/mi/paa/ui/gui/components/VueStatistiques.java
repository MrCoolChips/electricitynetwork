package up.mi.paa.ui.gui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import up.mi.paa.model.Couts;
import up.mi.paa.ui.gui.StyleUI;

import java.util.function.Consumer;

/**
 * Panneau de statistiques et contrôles d'optimisation.
 * Affiche les coûts du réseau (Global, Dispersion, Surcharge) et permet de lancer l'algorithme d'optimisation.
 * @author Groupe 10
 */
public class VueStatistiques extends VBox implements StyleUI {

    private final Label labelDispersion = new Label("0.000");
    private final Label labelSurcharge = new Label("0.000");
    private final Label labelCout = new Label("0.000");
    private final Slider selecteurLambda;

    private Runnable actionOptimisation;
    private Consumer<Integer> actionChangementLambda;

    /**
     * Constructeur du panneau de statistiques.
     * @param lambdaDefaut La valeur initiale du paramètre Lambda.
     */
    public VueStatistiques(int lambdaDefaut) {
        configurerStyle();
        selecteurLambda = new Slider(0, 100, lambdaDefaut);
        assemblerComposants(lambdaDefaut);
    }

    /**
     * Configure le style visuel global du panneau (dimensions, fond, bordures).
     */
    private void configurerStyle() {
        setPrefWidth(320);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: " + FOND_PANNEAU + "; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 0 0 0 1;");
    }

    /**
     * Assemble les différents composants graphiques du panneau.
     * @param lambdaDefaut La valeur par défaut pour l'affichage du label Lambda.
     */
    private void assemblerComposants(int lambdaDefaut) {
        Label entete = creerEntete("ANALYSE & OPTIMISATION");
        VBox carteCout = creerCarteCout();
        HBox ligneStats = creerLigneStats();
        VBox controles = creerControles(lambdaDefaut);

        getChildren().addAll(entete, carteCout, ligneStats, new Separator(), controles);
    }

    /**
     * Crée un label d'en-tête stylisé.
     */
    private Label creerEntete(String texte) {
        Label label = new Label(texte);
        label.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        return label;
    }

    /**
     * Crée la carte principale affichant le coût global du réseau.
     */
    private VBox creerCarteCout() {
        VBox carte = new VBox(5);
        carte.setPadding(new Insets(20));
        carte.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-background-radius: 8; -fx-border-color: " + COULEUR_BORDURE + ";");

        Label titre = new Label("COÛT GLOBAL");
        titre.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 10px; -fx-font-weight: bold;");

        labelCout.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");

        carte.getChildren().addAll(titre, labelCout, new Label("Coût (Calculé)"));
        return carte;
    }

    /**
     * Crée la ligne contenant les statistiques détaillées (Dispersion et Surcharge).
     */
    private HBox creerLigneStats() {
        HBox ligne = new HBox(10);
        ligne.getChildren().addAll(
            creerBoiteStat("Dispersion", labelDispersion, ACCENT_BLEU),
            creerBoiteStat("Surcharge", labelSurcharge, ACCENT_VERT)
        );
        return ligne;
    }

    /**
     * Crée une petite boîte affichant une statistique spécifique.
     */
    private VBox creerBoiteStat(String titre, Label valeur, String couleur) {
        VBox boite = new VBox(5);
        HBox.setHgrow(boite, Priority.ALWAYS);
        boite.setPadding(new Insets(10));
        boite.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-background-radius: 6; -fx-border-color: " + COULEUR_BORDURE + ";");

        Label lTitre = new Label(titre);
        lTitre.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 10px;");

        valeur.setStyle("-fx-text-fill: " + couleur + "; -fx-font-weight: bold; -fx-font-size: 16px;");

        boite.getChildren().addAll(lTitre, valeur);
        return boite;
    }

    /**
     * Crée la section des contrôles (Slider Lambda et bouton d'optimisation).
     */
    private VBox creerControles(int lambdaDefaut) {
        VBox controles = new VBox(10);

        Label labelLambda = new Label("Lambda: " + lambdaDefaut);
        labelLambda.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + ";");

        selecteurLambda.valueProperty().addListener((obs, ancien, nouveau) -> {
            labelLambda.setText("Lambda: " + nouveau.intValue());
            if (actionChangementLambda != null) {
                actionChangementLambda.accept(nouveau.intValue());
            }
        });

        Button btnOptimiser = new Button("⚡ Lancer l'Optimisation");
        btnOptimiser.setMaxWidth(Double.MAX_VALUE);
        btnOptimiser.setPrefHeight(45);
        btnOptimiser.setStyle("-fx-background-color: " + ACCENT_BLEU + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        btnOptimiser.setOnAction(e -> { if (actionOptimisation != null) actionOptimisation.run(); });

        controles.getChildren().addAll(labelLambda, selecteurLambda, btnOptimiser);
        return controles;
    }

    /**
     * Met à jour les valeurs affichées (Coût, Dispersion, Surcharge).
     * @param couts L'objet contenant les résultats du calcul, ou null pour réinitialiser.
     */
    public void mettreAJourStats(Couts couts) {
        if (couts == null) {
            labelCout.setText("0.000");
            labelDispersion.setText("0.000");
            labelSurcharge.setText("0.000");
            return;
        }
        labelCout.setText(String.format("%.3f", couts.getCoutGlobale()));
        labelDispersion.setText(String.format("%.3f", couts.getDispersion()));
        labelSurcharge.setText(String.format("%.3f", couts.getSurcharge()));
    }

    /**
     * Définit l'action à exécuter lors du clic sur le bouton "Optimiser".
     */
    public void setActionOptimisation(Runnable action) {
        this.actionOptimisation = action;
    }

    /**
     * Définit l'action à exécuter lors du changement de la valeur Lambda via le slider.
     */
    public void setActionChangementLambda(Consumer<Integer> action) {
        this.actionChangementLambda = action;
    }
}