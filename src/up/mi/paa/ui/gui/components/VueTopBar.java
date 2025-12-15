package up.mi.paa.ui.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import up.mi.paa.ui.gui.StyleUI;

/**
 * Barre supérieure de l'interface graphique.
 * Contient le titre de l'application et les boutons d'import/export.
 * 
 * @author Groupe 10
 */
public class VueTopBar extends HBox implements StyleUI {

    private Runnable actionImport;
    private Runnable actionExport;

    public VueTopBar() {
        configurerStyle();
        assemblerComposants();
    }

    private void configurerStyle() {
        setSpacing(20);
        setPadding(new Insets(10, 20, 10, 20));
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: " + FOND_PANNEAU + "; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 0 0 1 0;");
    }

    private void assemblerComposants() {
        Label icone = new Label("⚡");
        icone.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-background-color: #2563eb; -fx-padding: 5 10; -fx-background-radius: 5;");

        Label titre = new Label("PAA PROJET");
        titre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");

        Button btnImport = creerBouton("📂 Importer");
        btnImport.setOnAction(e -> { if (actionImport != null) actionImport.run(); });

        Button btnExport = creerBouton("💾 Exporter");
        btnExport.setOnAction(e -> { if (actionExport != null) actionExport.run(); });

        Region espaceur = new Region();
        HBox.setHgrow(espaceur, Priority.ALWAYS);

        Label universite = new Label("Univ. Paris Cité");
        universite.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 12px;");

        getChildren().addAll(icone, titre, new Separator(javafx.geometry.Orientation.VERTICAL), btnImport, btnExport, espaceur, universite);
    }

    private Button creerBouton(String texte) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 11px; -fx-cursor: hand;");
        return btn;
    }

    public void setActionImport(Runnable action) { this.actionImport = action; }
    public void setActionExport(Runnable action) { this.actionExport = action; }
}
