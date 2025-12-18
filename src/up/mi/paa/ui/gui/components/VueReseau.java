package up.mi.paa.ui.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.Cursor;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.service.CalculateurCouts;
import up.mi.paa.service.GestionnaireReseau;
import up.mi.paa.ui.gui.StyleUI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vue graphique du réseau électrique.
 * Affiche les générateurs, maisons et leurs connexions de manière interactive.
 * Les noeuds peuvent être déplacés par glisser-déposer (drag-and-drop).
 * @author Groupe 10
 */
public class VueReseau extends Pane implements StyleUI {

    private static final double MARGE = 100;
    private static final double RAYON_ORBITE = 120;

    private final Map<Object, StackPane> noeudsVisuels = new HashMap<>();
    private final Map<Object, double[]> positions = new HashMap<>();
    private final Pane coucheConnexions = new Pane();
    private final Pane coucheNoeuds = new Pane();

    /**
     * Constructeur de la vue réseau.
     * Initialise le fond, les couches graphiques et la grille d'arrière-plan.
     */
    public VueReseau() {
        setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        getChildren().addAll(coucheConnexions, coucheNoeuds);
        dessinerGrille(3000, 3000);
        appliquerMasque();
    }

    /**
     * Applique un masque de découpage pour empêcher les éléments de dépasser des bords du panneau.
     */
    private void appliquerMasque() {
        Rectangle masque = new Rectangle();
        masque.widthProperty().bind(widthProperty());
        masque.heightProperty().bind(heightProperty());
        setClip(masque);
    }

    /**
     * Rafraîchit l'affichage complet du réseau.
     * Redessine les générateurs, les maisons et les câbles.
     * @param gestionnaire Le gestionnaire de réseau contenant les données.
     * @param calculateur Le calculateur pour déterminer les surcharges.
     */
    public void rafraichir(GestionnaireReseau gestionnaire, CalculateurCouts calculateur) {
        coucheNoeuds.getChildren().clear();
        coucheConnexions.getChildren().clear();
        noeudsVisuels.clear();

        if (gestionnaire == null || gestionnaire.getReseauElectrique() == null) return;

        double largeur = getWidth() > 0 ? getWidth() : 1000;
        double hauteur = getHeight() > 0 ? getHeight() : 800;

        dessinerGenerateurs(gestionnaire, calculateur, largeur, hauteur);
        dessinerMaisonsOrphelines(gestionnaire, largeur, hauteur);
        dessinerConnexions(gestionnaire);
    }

    /**
     * Dessine tous les générateurs sur le panneau.
     * Applique une couleur rouge en cas de surcharge.
     */
    private void dessinerGenerateurs(GestionnaireReseau gestionnaire, CalculateurCouts calculateur, double largeur, double hauteur) {
        for (Generateur g : gestionnaire.getReseauElectrique().getGenerateurs()) {
            double demande = obtenirDemande(calculateur, g, gestionnaire);
            boolean surcharge = demande > g.getCapaciteMaximale();
            String couleur = surcharge ? ACCENT_ROUGE : ACCENT_BLEU;

            double[] pos = obtenirPosition(g, largeur, hauteur);
            StackPane noeud = creerNoeud(g, pos, couleur, "⚡", g.getNom(), g.getCapaciteMaximale() + "kW", true, gestionnaire);

            noeudsVisuels.put(g, noeud);
            coucheNoeuds.getChildren().add(noeud);

            dessinerMaisonsConnectees(gestionnaire, g, pos);
        }
    }

    /**
     * Dessine les maisons connectées autour d'un générateur spécifique (en orbite).
     */
    private void dessinerMaisonsConnectees(GestionnaireReseau gestionnaire, Generateur g, double[] posGen) {
        List<Maison> maisons = gestionnaire.getReseauElectrique().trouverLesMaisonsDeGenerateur(g);
        int total = maisons.size();

        for (int i = 0; i < total; i++) {
            Maison m = maisons.get(i);
            if (noeudsVisuels.containsKey(m)) continue;

            double[] posM = obtenirPositionMaison(m, posGen, i, total);
            StackPane noeud = creerNoeud(m, posM, ACCENT_VERT, "🏠", m.getNom(), m.getConsommation() + "kW", false, gestionnaire);

            noeudsVisuels.put(m, noeud);
            coucheNoeuds.getChildren().add(noeud);
        }
    }

    /**
     * Dessine les maisons qui ne sont connectées à aucun générateur.
     */
    private void dessinerMaisonsOrphelines(GestionnaireReseau gestionnaire, double largeur, double hauteur) {
        for (Maison m : gestionnaire.getReseauElectrique().getMaisons()) {
            if (!noeudsVisuels.containsKey(m)) {
                double[] pos = obtenirPosition(m, largeur, hauteur);
                StackPane noeud = creerNoeud(m, pos, ACCENT_VERT, "🏠", m.getNom(), m.getConsommation() + "kW", false, gestionnaire);
                noeudsVisuels.put(m, noeud);
                coucheNoeuds.getChildren().add(noeud);
            }
        }
    }

    /**
     * Trace les lignes de connexion entre les générateurs et leurs maisons.
     */
    private void dessinerConnexions(GestionnaireReseau gestionnaire) {
        for (Generateur g : gestionnaire.getReseauElectrique().getGenerateurs()) {
            StackPane noeudGen = noeudsVisuels.get(g);
            if (noeudGen == null) continue;

            for (Maison m : gestionnaire.getReseauElectrique().trouverLesMaisonsDeGenerateur(g)) {
                StackPane noeudMaison = noeudsVisuels.get(m);
                if (noeudMaison == null) continue;

                Line ligne = creerLigneConnexion(noeudGen, noeudMaison);
                coucheConnexions.getChildren().add(ligne);
            }
        }
    }

    /**
     * Crée une ligne pointillée stylisée représentant un câble électrique.
     */
    private Line creerLigneConnexion(StackPane source, StackPane cible) {
        Line ligne = new Line();
        ligne.setStroke(Color.web(TEXTE_SECONDAIRE));
        ligne.setStrokeWidth(2);
        ligne.setOpacity(0.3);
        ligne.getStrokeDashArray().addAll(10d, 5d);

        // Liaison dynamique des coordonnées pour suivre le mouvement des noeuds
        ligne.startXProperty().bind(source.layoutXProperty().add(25));
        ligne.startYProperty().bind(source.layoutYProperty().add(25));
        ligne.endXProperty().bind(cible.layoutXProperty().add(18));
        ligne.endYProperty().bind(cible.layoutYProperty().add(18));

        return ligne;
    }

    /**
     * Réinitialise les positions des maisons pour forcer un recalcul (utile après optimisation).
     */
    public void reorganiserLayout() {
        positions.entrySet().removeIf(e -> e.getKey() instanceof Maison);
    }

    /**
     * Calcule la demande électrique totale sur un générateur.
     */
    private double obtenirDemande(CalculateurCouts calc, Generateur g, GestionnaireReseau gest) {
        try {
            return calc.getSommeDesDemandesElectriques(g, gest.getReseauElectrique());
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Obtient ou génère une position aléatoire pour un élément.
     */
    private double[] obtenirPosition(Object obj, double largeur, double hauteur) {
        if (positions.containsKey(obj)) {
            return positions.get(obj);
        }
        double x = MARGE + Math.random() * (largeur - 2 * MARGE);
        double y = MARGE + Math.random() * (hauteur - 2 * MARGE);
        double[] pos = {x, y};
        positions.put(obj, pos);
        return pos;
    }

    /**
     * Calcule la position d'une maison en orbite autour de son générateur.
     */
    private double[] obtenirPositionMaison(Maison m, double[] posGen, int index, int total) {
        if (positions.containsKey(m)) {
            return positions.get(m);
        }
        double angle = (2 * Math.PI / total) * index;
        double x = posGen[0] + RAYON_ORBITE * Math.cos(angle);
        double y = posGen[1] + RAYON_ORBITE * Math.sin(angle);
        double[] pos = {x, y};
        positions.put(m, pos);
        return pos;
    }

    /**
     * Crée un noeud visuel (cercle + icône + étiquette) pour un élément du réseau.
     */
    private StackPane creerNoeud(Object donnee, double[] pos, String couleur, String icone, String nom, String sousTexte, boolean estGenerateur, GestionnaireReseau gestionnaire) {
        int rayon = estGenerateur ? 25 : 18;

        Circle cercle = new Circle(rayon);
        cercle.setFill(Color.web(couleur));
        cercle.setStroke(Color.web(FOND_PRINCIPAL));
        cercle.setStrokeWidth(3);
        cercle.setEffect(new DropShadow(10, Color.BLACK));

        Text txtIcone = new Text(icone);
        txtIcone.setFill(Color.WHITE);
        txtIcone.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, estGenerateur ? 20 : 14));

        StackPane groupeCercle = new StackPane(cercle, txtIcone);
        VBox etiquette = creerEtiquette(nom, sousTexte, estGenerateur);

        StackPane conteneur = new StackPane(groupeCercle, etiquette);
        conteneur.setLayoutX(pos[0]);
        conteneur.setLayoutY(pos[1]);

        configurerDragAndDrop(conteneur, donnee, estGenerateur, gestionnaire);

        return conteneur;
    }

    /**
     * Crée l'étiquette textuelle affichée sous le noeud.
     */
    private VBox creerEtiquette(String nom, String sousTexte, boolean estGenerateur) {
        VBox boite = new VBox(0);
        boite.setAlignment(Pos.CENTER);
        boite.setPadding(new Insets(2, 6, 2, 6));
        boite.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-background-radius: 4; -fx-border-color: " + COULEUR_BORDURE + ";");

        Label lNom = new Label(nom);
        lNom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label lSous = new Label(sousTexte);
        lSous.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 9px;");

        boite.getChildren().addAll(lNom, lSous);
        boite.setTranslateY(estGenerateur ? 40 : 32);

        return boite;
    }

    /**
     * Configure les événements de souris pour permettre le déplacement des noeuds.
     */
    private void configurerDragAndDrop(StackPane conteneur, Object donnee, boolean estGenerateur, GestionnaireReseau gestionnaire) {
        final double[] delta = new double[2];

        conteneur.setOnMousePressed(e -> {
            delta[0] = conteneur.getLayoutX() - e.getSceneX();
            delta[1] = conteneur.getLayoutY() - e.getSceneY();
            conteneur.setCursor(Cursor.MOVE);
            conteneur.toFront();
        });

        conteneur.setOnMouseDragged(e -> {
            double largeur = getWidth() > 0 ? getWidth() : 1000;
            double hauteur = getHeight() > 0 ? getHeight() : 800;

            double oldX = conteneur.getLayoutX();
            double oldY = conteneur.getLayoutY();

            // Calcul de la nouvelle position avec limites (clamping)
            double newX = clamp(e.getSceneX() + delta[0], 20, largeur - 70);
            double newY = clamp(e.getSceneY() + delta[1], 20, hauteur - 70);

            conteneur.setLayoutX(newX);
            conteneur.setLayoutY(newY);
            positions.put(donnee, new double[]{newX, newY});

            // Si on déplace un générateur, les maisons connectées suivent
            if (estGenerateur && donnee instanceof Generateur) {
                deplacerMaisonsConnectees((Generateur) donnee, newX - oldX, newY - oldY, gestionnaire);
            }
        });

        conteneur.setOnMouseReleased(e -> conteneur.setCursor(Cursor.HAND));
    }

    /**
     * Déplace toutes les maisons connectées à un générateur lors du mouvement de celui-ci.
     */
    private void deplacerMaisonsConnectees(Generateur g, double dx, double dy, GestionnaireReseau gestionnaire) {
        for (Maison m : gestionnaire.getReseauElectrique().trouverLesMaisonsDeGenerateur(g)) {
            StackPane noeud = noeudsVisuels.get(m);
            if (noeud != null) {
                double newX = noeud.getLayoutX() + dx;
                double newY = noeud.getLayoutY() + dy;
                noeud.setLayoutX(newX);
                noeud.setLayoutY(newY);
                positions.put(m, new double[]{newX, newY});
            }
        }
    }

    /**
     * Restreint une valeur entre un minimum et un maximum.
     */
    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Dessine une grille en arrière-plan pour l'esthétique.
     */
    private void dessinerGrille(double largeur, double hauteur) {
        Pane grille = new Pane();
        for (int i = 0; i < largeur; i += 40) {
            Line v = new Line(i, 0, i, hauteur);
            v.setStroke(Color.web("#1e293b"));
            grille.getChildren().add(v);
        }
        for (int j = 0; j < hauteur; j += 40) {
            Line h = new Line(0, j, largeur, j);
            h.setStroke(Color.web("#1e293b"));
            grille.getChildren().add(h);
        }
        grille.toBack();
        getChildren().add(0, grille);
    }
}