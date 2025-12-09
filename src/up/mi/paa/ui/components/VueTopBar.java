package up.mi.paa.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import up.mi.paa.ui.StyleUI;

/**
 * Barre supérieure simple avec le titre et les boutons d'import/export.
 */
public class VueTopBar extends HBox implements StyleUI {

    private Runnable actionImport;
    private Runnable actionExport;

    public VueTopBar() {
        this.setSpacing(20);
        this.setPadding(new Insets(10, 20, 10, 20));
        this.setAlignment(Pos.CENTER_LEFT);
        this.setStyle("-fx-background-color: " + FOND_PANNEAU + "; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 0 0 1 0;");

        Label icone = new Label("⚡");
        icone.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-background-color: #2563eb; -fx-padding: 5 10; -fx-background-radius: 5;");
        Label titre = new Label("PAA PROJET");
        titre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");

        Button btnImport = creerBoutonTexte("📂 Importer");
        btnImport.setOnAction(e -> { if(actionImport != null) actionImport.run(); });
        
        Button btnExport = creerBoutonTexte("💾 Exporter");
        btnExport.setOnAction(e -> { if(actionExport != null) actionExport.run(); });

        Region espaceur = new Region();
        HBox.setHgrow(espaceur, Priority.ALWAYS);

        Label univ = new Label("Univ. Paris Cité");
        univ.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 12px;");

        this.getChildren().addAll(icone, titre, new Separator(javafx.geometry.Orientation.VERTICAL), btnImport, btnExport, espaceur, univ);
    }

    public void setActionImport(Runnable action) { this.actionImport = action; }
    public void setActionExport(Runnable action) { this.actionExport = action; }

    private Button creerBoutonTexte(String texte) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 11px; -fx-cursor: hand;");
        return btn;
    }
}