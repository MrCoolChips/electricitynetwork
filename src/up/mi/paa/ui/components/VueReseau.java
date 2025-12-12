package up.mi.paa.ui.components;

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.service.CalculateurCouts;
import up.mi.paa.service.GestionnaireReseau;
import up.mi.paa.ui.StyleUI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VueReseau extends Pane implements StyleUI {
    
    private final Map<Object, StackPane> visuelsNoeuds = new HashMap<>();
    private final Map<Object, double[]> positionsSauvegardees = new HashMap<>();
    private final Pane coucheConnexions = new Pane();
    private final Pane coucheNoeuds = new Pane();

    public VueReseau() {
        this.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        this.getChildren().addAll(coucheConnexions, coucheNoeuds);
        dessinerGrille(3000, 3000);
        
        Rectangle masque = new Rectangle();
        masque.widthProperty().bind(this.widthProperty());
        masque.heightProperty().bind(this.heightProperty());
        this.setClip(masque);
    }

    public void rafraichir(GestionnaireReseau gestionnaire, CalculateurCouts calculateur) {
        coucheNoeuds.getChildren().clear();
        coucheConnexions.getChildren().clear();
        visuelsNoeuds.clear();

        if (gestionnaire == null || gestionnaire.getReseauElectrique() == null) return;

        double largeur = this.getWidth() > 0 ? this.getWidth() : 1000;
        double hauteur = this.getHeight() > 0 ? this.getHeight() : 800;
        double marge = 100;

        for (Generateur g : gestionnaire.getReseauElectrique().getGenerateurs()) {
            double demandeTotale = 0.0;
            try {
                demandeTotale = calculateur.getSommeDesDemandesElectriques(g, gestionnaire.getReseauElectrique());
            } catch (Exception e) { demandeTotale = 0.0; }

            boolean surcharge = demandeTotale > g.getCapaciteMaximale();
            
            double[] pos = obtenirOuCreerPosition(g, largeur, hauteur, marge);
            
            StackPane noeudGen = creerNoeudDeplacable(g, pos[0], pos[1], 
                    surcharge ? ACCENT_ROUGE : ACCENT_BLEU, 
                    "⚡", g.getNom(), g.getCapaciteMaximale() + "kW", true, gestionnaire);
            
            visuelsNoeuds.put(g, noeudGen);
            coucheNoeuds.getChildren().add(noeudGen);
            
            List<Maison> maisonsConnectees = gestionnaire.getReseauElectrique().trouverLesMaisonsDeGenerateur(g);
            int nbMaisons = maisonsConnectees.size();
            double rayon = 120;
            
            for (int i = 0; i < nbMaisons; i++) {
                Maison m = maisonsConnectees.get(i);
                double[] posM;
                
                if (positionsSauvegardees.containsKey(m)) {
                    posM = positionsSauvegardees.get(m);
                } else {
                    double angle = (2 * Math.PI / nbMaisons) * i;
                    double mx = pos[0] + rayon * Math.cos(angle);
                    double my = pos[1] + rayon * Math.sin(angle);
                    posM = new double[]{mx, my};
                    positionsSauvegardees.put(m, posM);
                }
                
                if (!visuelsNoeuds.containsKey(m)) {
                    StackPane noeudMaison = creerNoeudDeplacable(m, posM[0], posM[1], 
                            ACCENT_VERT, 
                            "🏠", m.getNom(), m.getConsommation() + "kW", false, gestionnaire);
                    visuelsNoeuds.put(m, noeudMaison);
                    coucheNoeuds.getChildren().add(noeudMaison);
                }
            }
        }

        for (Maison m : gestionnaire.getReseauElectrique().getMaisons()) {
            if (!visuelsNoeuds.containsKey(m)) {
                double[] pos = obtenirOuCreerPosition(m, largeur, hauteur, marge);
                StackPane n = creerNoeudDeplacable(m, pos[0], pos[1], ACCENT_VERT, "🏠", m.getNom(), m.getConsommation() + "kW", false, gestionnaire);
                coucheNoeuds.getChildren().add(n);
                visuelsNoeuds.put(m, n);
            }
        }

        dessinerConnexions(gestionnaire);
    }

    public void reorganiserLayout() {
        positionsSauvegardees.entrySet().removeIf(entry -> entry.getKey() instanceof Maison);
    }

    private double[] obtenirOuCreerPosition(Object obj, double w, double h, double marge) {
        if (positionsSauvegardees.containsKey(obj)) {
            return positionsSauvegardees.get(obj);
        } else {
            double x = marge + Math.random() * (w - 2 * marge);
            double y = marge + Math.random() * (h - 2 * marge);
            double[] pos = new double[]{x, y};
            positionsSauvegardees.put(obj, pos);
            return pos;
        }
    }

    private void dessinerConnexions(GestionnaireReseau gestionnaire) {
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

    private static class Delta { double x, y; }

    private StackPane creerNoeudDeplacable(Object donnee, double initX, double initY, String couleurHex, String iconeTxt, String nom, String sousTexte, boolean estGen, GestionnaireReseau gestionnaire) {
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
        
        final Delta delta = new Delta();

        conteneurNoeud.setOnMousePressed(e -> {
            delta.x = conteneurNoeud.getLayoutX() - e.getSceneX();
            delta.y = conteneurNoeud.getLayoutY() - e.getSceneY();
            conteneurNoeud.setCursor(javafx.scene.Cursor.MOVE);
            conteneurNoeud.toFront();
        });

        conteneurNoeud.setOnMouseDragged(e -> {
            double tentativeX = e.getSceneX() + delta.x;
            double tentativeY = e.getSceneY() + delta.y;

            double marge = 20.0;
            double largeurPanneau = this.getWidth() > 0 ? this.getWidth() : 1000;
            double hauteurPanneau = this.getHeight() > 0 ? this.getHeight() : 800;
            
            double nouvelleX = Math.max(marge, Math.min(largeurPanneau - marge - 50, tentativeX));
            double nouvelleY = Math.max(marge, Math.min(hauteurPanneau - marge - 50, tentativeY));

            double oldX = conteneurNoeud.getLayoutX();
            double oldY = conteneurNoeud.getLayoutY();

            conteneurNoeud.setLayoutX(nouvelleX);
            conteneurNoeud.setLayoutY(nouvelleY);
            
            positionsSauvegardees.put(donnee, new double[]{nouvelleX, nouvelleY});
            
            if (estGen && donnee instanceof Generateur) {
                double deplacementX = nouvelleX - oldX;
                double deplacementY = nouvelleY - oldY;

                Generateur g = (Generateur) donnee;
                for (Maison m : gestionnaire.getReseauElectrique().trouverLesMaisonsDeGenerateur(g)) {
                    StackPane noeudMaison = visuelsNoeuds.get(m);
                    if (noeudMaison != null) {
                         double newMaisonX = noeudMaison.getLayoutX() + deplacementX;
                         double newMaisonY = noeudMaison.getLayoutY() + deplacementY;
                         
                         noeudMaison.setLayoutX(newMaisonX);
                         noeudMaison.setLayoutY(newMaisonY);
                         
                         positionsSauvegardees.put(m, new double[]{newMaisonX, newMaisonY});
                    }
                }
            }
        });

        conteneurNoeud.setOnMouseReleased(e -> conteneurNoeud.setCursor(javafx.scene.Cursor.HAND));

        return conteneurNoeud;
    }

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
        this.getChildren().add(0, grille);
    }
}