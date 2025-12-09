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

/**
 * Composant responsable de l'affichage graphique du réseau (Noeuds et Connexions).
 * Gère aussi le drag & drop des éléments.
 */
public class VueReseau extends Pane implements StyleUI {
    
    // On garde une trace des éléments visuels pour dessiner les lignes
    private final Map<Object, StackPane> visuelsNoeuds = new HashMap<>();
    private final Pane coucheConnexions = new Pane();
    private final Pane coucheNoeuds = new Pane();

    public VueReseau() {
        // Configuration de base du panneau
        this.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        this.getChildren().addAll(coucheConnexions, coucheNoeuds);
        
        // On dessine une grille pour faire "pro"
        dessinerGrille(3000, 3000);
        
        // Empêche les éléments de dépasser du cadre (clipping)
        Rectangle masque = new Rectangle();
        masque.widthProperty().bind(this.widthProperty());
        masque.heightProperty().bind(this.heightProperty());
        this.setClip(masque);
    }

    /**
     * Méthode principale pour tout redessiner.
     * Appelée à chaque changement dans le modèle.
     */
    public void rafraichir(GestionnaireReseau gestionnaire, CalculateurCouts calculateur) {
        coucheNoeuds.getChildren().clear();
        coucheConnexions.getChildren().clear();
        visuelsNoeuds.clear();

        if (gestionnaire == null || gestionnaire.getReseauElectrique() == null) return;

        // Dimensions de la zone de dessin
        double largeur = this.getWidth() > 0 ? this.getWidth() : 1000;
        double hauteur = this.getHeight() > 0 ? this.getHeight() : 800;
        double marge = 100;

        // 1. On place d'abord les générateurs
        for (Generateur g : gestionnaire.getReseauElectrique().getGenerateurs()) {
            
            // On récupère la charge via le service pour savoir si c'est rouge ou bleu
            double demandeTotale = 0.0;
            try {
                demandeTotale = calculateur.getSommeDesDemandesElectriques(g, gestionnaire.getReseauElectrique());
            } catch (Exception e) { demandeTotale = 0.0; }

            boolean surcharge = demandeTotale > g.getCapaciteMaximale();
            
            // Position aléatoire (pour l'instant)
            double gx = marge + Math.random() * (largeur - 2 * marge);
            double gy = marge + Math.random() * (hauteur - 2 * marge);
            
            StackPane noeudGen = creerNoeudDeplacable(g, gx, gy, 
                    surcharge ? ACCENT_ROUGE : ACCENT_BLEU, 
                    "⚡", g.getNom(), g.getCapaciteMaximale() + "kW", true, gestionnaire);
            
            visuelsNoeuds.put(g, noeudGen);
            coucheNoeuds.getChildren().add(noeudGen);
            
            // 2. On place les maisons en cercle autour du générateur
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
                            "🏠", m.getNom(), m.getConsommation() + "kW", false, gestionnaire);
                    visuelsNoeuds.put(m, noeudMaison);
                    coucheNoeuds.getChildren().add(noeudMaison);
                }
            }
        }

        // 3. On place les maisons orphelines (non connectées)
        for (Maison m : gestionnaire.getReseauElectrique().getMaisons()) {
            if (!visuelsNoeuds.containsKey(m)) {
                StackPane n = creerNoeudDeplacable(m, largeur/2, hauteur/2, ACCENT_VERT, "🏠", m.getNom(), m.getConsommation() + "kW", false, gestionnaire);
                coucheNoeuds.getChildren().add(n);
                visuelsNoeuds.put(m, n);
            }
        }

        // 4. Enfin, on trace les lignes (connexions)
        dessinerConnexions(gestionnaire);
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

                // Liaison dynamique : si le noeud bouge, la ligne suit
                ligne.startXProperty().bind(noeudGen.layoutXProperty().add(25)); 
                ligne.startYProperty().bind(noeudGen.layoutYProperty().add(25)); 
                ligne.endXProperty().bind(noeudMaison.layoutXProperty().add(18));    
                ligne.endYProperty().bind(noeudMaison.layoutYProperty().add(18));

                coucheConnexions.getChildren().add(ligne);
            }
        }
    }

    // Petite classe interne pour gérer le décalage lors du drag & drop
    private static class Delta { 
    	double x, y;
    }

    /**
     * Crée un élément graphique (cercle) qui peut être déplacé à la souris.
     * Inclut la gestion des limites (ne sort pas du cadre) et le déplacement synchronisé.
     */
    private StackPane creerNoeudDeplacable(Object donnee, double initX, double initY, String couleurHex, String iconeTxt, String nom, String sousTexte, boolean estGen, GestionnaireReseau gestionnaire) {
        // Création Graphique
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
        
        // LOGIQUE DE DÉPLACEMENT ET LIMITES
        final Delta delta = new Delta();

        conteneurNoeud.setOnMousePressed(e -> {
            // On mémorise l'écart entre la souris et le coin du composant
            delta.x = conteneurNoeud.getLayoutX() - e.getSceneX();
            delta.y = conteneurNoeud.getLayoutY() - e.getSceneY();
            conteneurNoeud.setCursor(javafx.scene.Cursor.MOVE);
            conteneurNoeud.toFront();
        });

        conteneurNoeud.setOnMouseDragged(e -> {
            // 1. Calcul de la position théorique (où la souris veut aller)
            double tentativeX = e.getSceneX() + delta.x;
            double tentativeY = e.getSceneY() + delta.y;

            // 2. Définition des limites (La taille du panneau actuel)
            double marge = 20.0; // Marge pour ne pas coller au bord
            double largeurPanneau = this.getWidth();
            double hauteurPanneau = this.getHeight();
            
            // Si le panneau n'est pas encore affiché, on prend des valeurs par défaut
            if(largeurPanneau == 0) largeurPanneau = 1000;
            if(hauteurPanneau == 0) hauteurPanneau = 800;

            // 3. Application des contraintes (Clamping)
            // On s'assure que X est entre [marge] et [largeur - marge]
            double nouvelleX = Math.max(marge, Math.min(largeurPanneau - marge - 50, tentativeX));
            double nouvelleY = Math.max(marge, Math.min(hauteurPanneau - marge - 50, tentativeY));

            // 4. Calcul du déplacement EFFECTIF (Différence entre nouvelle position valide et ancienne)
            double deplacementX = nouvelleX - conteneurNoeud.getLayoutX();
            double deplacementY = nouvelleY - conteneurNoeud.getLayoutY();

            // 5. Mise à jour du noeud principal
            conteneurNoeud.setLayoutX(nouvelleX);
            conteneurNoeud.setLayoutY(nouvelleY);
            
            // 6. Si c'est un générateur, on déplace ses maisons de la même quantité exacte
            if (estGen && donnee instanceof Generateur) {
                Generateur g = (Generateur) donnee;
                for (Maison m : gestionnaire.getReseauElectrique().trouverLesMaisonsDeGenerateur(g)) {
                    StackPane noeudMaison = visuelsNoeuds.get(m);
                    if (noeudMaison != null) {
                         // On applique le même vecteur de déplacement
                         noeudMaison.setLayoutX(noeudMaison.getLayoutX() + deplacementX);
                         noeudMaison.setLayoutY(noeudMaison.getLayoutY() + deplacementY);
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