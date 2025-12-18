package up.mi.paa.ui.gui.components;

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
import up.mi.paa.ui.gui.StyleUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Panneau latéral d'inventaire du réseau.
 * Permet de visualiser et gérer les générateurs et maisons.
 */
public class VueInventaire extends VBox implements StyleUI {

    private final VBox conteneurListe = new VBox(10);
    private String ongletActif = "Generateurs";

    private TextField champNom;
    private TextField champCapacite;
    private ComboBox<TypeConsommation> comboType;
    private StackPane conteneurDynamique;
    private Button boutonAjouter;

    private GestionnaireReseau gestionnaire;
    private Runnable onUpdateRequired;

    /**
     * Constructeur de la vue inventaire.
     * Initialise le style et les composants graphiques.
     */
    public VueInventaire() {
        setPrefWidth(320);
        setStyle("-fx-background-color: " + FOND_PANNEAU + "; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 0 1 0 0;");
        construireInterface();
    }

    /**
     * Définit le gestionnaire de réseau pour interagir avec le modèle.
     * @param gestionnaire L'instance du gestionnaire.
     */
    public void setGestionnaire(GestionnaireReseau gestionnaire) {
        this.gestionnaire = gestionnaire;
    }

    /**
     * Définit l'action à exécuter lorsqu'une mise à jour de l'interface est requise.
     * @param callback L'action à exécuter.
     */
    public void setOnUpdateRequired(Runnable callback) {
        this.onUpdateRequired = callback;
    }

    /**
     * Construit l'interface utilisateur complète du panneau latéral.
     * Assemble l'entête, les onglets, la liste défilante et le formulaire.
     */
    private void construireInterface() {
        Label entete = new Label("EXPLORATEUR RÉSEAU");
        entete.setPadding(new Insets(15));
        entete.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        HBox onglets = creerOnglets();
        ScrollPane scroll = creerScrollPane();
        VBox formulaire = creerFormulaire();

        getChildren().addAll(entete, onglets, scroll, formulaire);
        mettreAJourFormulaire();
    }

    /**
     * Crée la barre d'onglets pour basculer entre Générateurs et Maisons.
     * @return HBox contenant les boutons d'onglets.
     */
    private HBox creerOnglets() {
        HBox onglets = new HBox();
        Button btnGen = creerBoutonOnglet("Générateurs", true);
        Button btnMaison = creerBoutonOnglet("Maisons", false);

        btnGen.setOnAction(e -> changerOnglet("Generateurs", btnGen, btnMaison));
        btnMaison.setOnAction(e -> changerOnglet("Maisons", btnMaison, btnGen));

        onglets.getChildren().addAll(btnGen, btnMaison);
        return onglets;
    }

    /**
     * Crée la zone de défilement pour la liste des éléments.
     * @return ScrollPane configuré.
     */
    private ScrollPane creerScrollPane() {
        ScrollPane scroll = new ScrollPane(conteneurListe);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + FOND_PANNEAU + "; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        conteneurListe.setPadding(new Insets(10));
        return scroll;
    }

    /**
     * Crée le formulaire d'ajout en bas du panneau.
     * @return VBox contenant les champs de saisie.
     */
    private VBox creerFormulaire() {
        VBox formulaire = new VBox(10);
        formulaire.setPadding(new Insets(15));
        formulaire.setStyle("-fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 1 0 0 0;");

        HBox ligne = new HBox(5);
        champNom = creerChamp("Nom");
        champCapacite = creerChamp("Cap (kW)");
        champCapacite.setPrefWidth(80);

        comboType = new ComboBox<>();
        comboType.getItems().addAll(TypeConsommation.values());
        comboType.getSelectionModel().select(TypeConsommation.NORMAL);
        comboType.setPrefWidth(100);
        comboType.setStyle("-fx-background-color: " + FOND_PRINCIPAL + "; -fx-text-base-color: white; -fx-border-color: " + COULEUR_BORDURE + ";");

        conteneurDynamique = new StackPane();
        conteneurDynamique.getChildren().add(champCapacite);

        boutonAjouter = new Button("+");
        boutonAjouter.setPrefWidth(30);
        boutonAjouter.setStyle("-fx-background-color: " + ACCENT_BLEU + "; -fx-text-fill: white; -fx-font-weight: bold;");
        boutonAjouter.setOnAction(e -> ajouterElement());

        ligne.getChildren().addAll(champNom, conteneurDynamique, boutonAjouter);
        formulaire.getChildren().add(ligne);
        return formulaire;
    }

    /**
     * Rafraîchit la liste des éléments affichés en fonction de l'onglet actif.
     * @param calculateur Calculateur pour obtenir les statistiques de consommation.
     */
    public void rafraichirListe(CalculateurCouts calculateur) {
        conteneurListe.getChildren().clear();
        if (gestionnaire == null) return;

        if (ongletActif.equals("Generateurs")) {
            afficherGenerateurs(calculateur);
        } else {
            afficherMaisons();
        }
    }

    /**
     * Affiche la liste des générateurs sous forme de cartes.
     * Calcule l'utilisation actuelle pour afficher les surcharges.
     */
    private void afficherGenerateurs(CalculateurCouts calculateur) {
        for (Generateur g : new ArrayList<>(gestionnaire.getReseauElectrique().getGenerateurs())) {
            double usage = obtenirUsage(calculateur, g);
            boolean surcharge = usage > g.getCapaciteMaximale();

            creerCarte(
                g.getNom(),
                g.getCapaciteMaximale() + " kW • Utilisé : " + usage + "kW",
                surcharge ? ACCENT_ROUGE : ACCENT_BLEU,
                null,
                e -> supprimerGenerateur(g)
            );
        }
    }

    /**
     * Affiche la liste des maisons sous forme de cartes.
     * Ajoute un sélecteur pour changer le générateur connecté.
     */
    private void afficherMaisons() {
        for (Maison m : gestionnaire.getReseauElectrique().getMaisons()) {
            VBox selecteur = creerSelecteurGenerateur(m);
            creerCarte(
                m.getNom(),
                m.getConsommation() + " kW • " + m.getTypeConsommation(),
                ACCENT_VERT,
                selecteur,
                e -> supprimerMaison(m)
            );
        }
    }

    /**
     * Crée un menu déroulant pour choisir le générateur d'une maison.
     * @param m La maison concernée.
     * @return VBox contenant le label et le ComboBox.
     */
    private VBox creerSelecteurGenerateur(Maison m) {
        VBox boite = new VBox(5);

        Label label = new Label("CONNECTER À :");
        label.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 9px; -fx-font-weight: bold;");

        ComboBox<Generateur> combo = new ComboBox<>();
        combo.getItems().addAll(gestionnaire.getReseauElectrique().getGenerateurs());
        combo.setValue(gestionnaire.getReseauElectrique().trouverGenerateur(m));
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle("-fx-background-color: " + FOND_PRINCIPAL + "; -fx-text-base-color: white; -fx-border-color: " + COULEUR_BORDURE + "; -fx-font-size: 10px;");

        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Generateur g) { return g == null ? "Aucun" : g.getNom() + " (" + g.getCapaciteMaximale() + ")"; }
            @Override public Generateur fromString(String s) { return null; }
        });

        combo.setOnAction(e -> changerConnexion(m, combo.getValue()));

        boite.getChildren().addAll(label, combo);
        return boite;
    }

    /**
     * Crée une carte visuelle générique pour un élément (Maison ou Générateur).
     * @param titre Le titre principal (Nom).
     * @param sousTitre Le sous-titre (Infos).
     * @param couleur La couleur de l'indicateur.
     * @param extra Composant supplémentaire optionnel.
     * @param suppression Action de suppression.
     */
    private void creerCarte(String titre, String sousTitre, String couleur, javafx.scene.Node extra, javafx.event.EventHandler<javafx.event.ActionEvent> suppression) {
        VBox carte = new VBox(8);
        carte.setPadding(new Insets(10));
        carte.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-background-radius: 6; -fx-border-color: " + COULEUR_BORDURE + ";");

        HBox ligne = new HBox(10);
        ligne.setAlignment(Pos.CENTER_LEFT);

        Circle point = new Circle(4, Color.web(couleur));

        VBox textes = new VBox(2);
        Label lTitre = new Label(titre);
        lTitre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label lSous = new Label(sousTitre);
        lSous.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-font-size: 10px;");
        textes.getChildren().addAll(lTitre, lSous);
        HBox.setHgrow(textes, Priority.ALWAYS);

        Button btnSuppr = new Button("✕");
        btnSuppr.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-cursor: hand;");
        btnSuppr.setOnAction(suppression);

        ligne.getChildren().addAll(point, textes, btnSuppr);
        carte.getChildren().add(ligne);

        if (extra != null) {
            Separator sep = new Separator();
            sep.setOpacity(0.1);
            carte.getChildren().addAll(sep, extra);
        }

        conteneurListe.getChildren().add(carte);
    }

	/**
	 * Ajoute un nouvel élément ou met à jour un élément existant.
	 * Vérifie les valeurs négatives et gère la logique de mise à jour.
	 */
    private void ajouterElement() {
        String nom = champNom.getText().trim().toUpperCase();
        if (nom.isEmpty()) return;

        try {
            if (ongletActif.equals("Generateurs")) {
                String texteCapacite = champCapacite.getText();
                double capacite = texteCapacite.isEmpty() ? 60.0 : Double.parseDouble(texteCapacite);

                // Vérifier les valeurs négatives
                if (capacite < 0) {
                    afficherAlerte("Erreur de Saisie", "La capacité ne peut pas être négative !");
                    return;
                }

                gestionnaire.ajouterOuModifierGenerateur(nom, capacite);

            } else {
                
                TypeConsommation type = comboType.getValue() != null ? comboType.getValue() : TypeConsommation.NORMAL;
                Maison maisonExistante = gestionnaire.getReseauElectrique().trouverMaison(nom);
                
                gestionnaire.ajouterOuModifierMaison(nom, type);

                // Connecter au réseau SEULEMENT si c'est une nouvelle maison
                if (maisonExistante == null) {
                    connecterAuPremierGenerateur(nom);
                }
            }

            champNom.clear();
            champCapacite.clear();
            notifierMiseAJour();

        } catch (NumberFormatException e) {
            afficherAlerte("Erreur de Format", "Veuillez entrer un nombre valide pour la capacité.");
        } catch (Exception e) {
            afficherAlerte("Erreur", e.getMessage());
        }
    }

    /**
     * Connecte automatiquement une nouvelle maison au premier générateur disponible.
     * @param nomMaison Le nom de la maison à connecter.
     */
    private void connecterAuPremierGenerateur(String nomMaison) throws Exception {
        List<Generateur> generateurs = gestionnaire.getReseauElectrique().getGenerateurs();
        if (!generateurs.isEmpty()) {
            gestionnaire.creerConnexion(generateurs.get(0).getNom(), nomMaison);
        }
    }

    /**
     * Supprime un générateur du réseau.
     * Empêche la suppression du dernier générateur s'il reste des maisons.
     * Redistribue automatiquement les maisons orphelines vers d'autres générateurs.
     * @param g Le générateur à supprimer.
     */
    private void supprimerGenerateur(Generateur g) {
        try {
            // 1. VÉRIFICATION : Empêcher la suppression du dernier générateur s'il y a des maisons
            int numGen = gestionnaire.getReseauElectrique().getGenerateurs().size();
            boolean maisonExiste = !gestionnaire.getReseauElectrique().getMaisons().isEmpty();

            if (numGen <= 1 && maisonExiste) {
                afficherAlerte("Action Bloquée", 
                    "Impossible de supprimer le dernier générateur ! Chaque maison doit être connectée à une source d'énergie.\n" +
                    "Veuillez d'abord supprimer les maisons ou ajouter un nouveau générateur.");
                return;
            }

            // 2. SAUVEGARDE : Identifier les maisons qui vont devenir orphelines
            List<Maison> orphelines = new ArrayList<>(
                gestionnaire.getReseauElectrique().trouverLesMaisonsDeGenerateur(g)
            );

            // 3. SUPPRESSION : Supprimer le générateur du réseau
            boolean supprimer = gestionnaire.getReseauElectrique().supprimerGenerateur(g);

            if (supprimer) {
                // 4. REDISTRIBUTION : Connecter les maisons orphelines aux générateurs restants
                List<Generateur> genRestants = gestionnaire.getReseauElectrique().getGenerateurs();
                
                if (!genRestants.isEmpty() && !orphelines.isEmpty()) {
                    Random rand = new Random();
                    for (Maison m : orphelines) {
                        // Choisir un générateur aléatoire
                        Generateur nouvGen = genRestants.get(rand.nextInt(genRestants.size()));
                        
                        // Créer la connexion via le service
                        gestionnaire.creerConnexion(nouvGen.getNom(), m.getNom());
                    }
                }
                
                // Mettre à jour l'interface
                notifierMiseAJour();
            }

        } catch (Exception e) {
            afficherAlerte("Erreur", "Une erreur est survenue lors de la suppression : " + e.getMessage());
        }
    }

    /**
     * Supprime une maison du réseau et retire ses connexions.
     * @param m La maison à supprimer.
     */
    private void supprimerMaison(Maison m) {
        gestionnaire.getReseauElectrique().getMaisons().remove(m);
        gestionnaire.getReseauElectrique().supprimerConnexion(m);
        notifierMiseAJour();
    }

    /**
     * Change la connexion d'une maison vers un nouveau générateur.
     * @param m La maison concernée.
     * @param nouveau Le nouveau générateur cible.
     */
    private void changerConnexion(Maison m, Generateur nouveau) {
        Generateur ancien = gestionnaire.getReseauElectrique().trouverGenerateur(m);
        if (nouveau != null && nouveau != ancien) {
            try {
                if (ancien != null) {
                    gestionnaire.modifierConnexion(m.getNom(), ancien.getNom(), m.getNom(), nouveau.getNom());
                } else {
                    gestionnaire.creerConnexion(nouveau.getNom(), m.getNom());
                }
                notifierMiseAJour();
            } catch (Exception e) {
                afficherAlerte("Erreur", e.getMessage());
            }
        }
    }

    /**
     * Gère le changement d'onglet (Générateurs <-> Maisons).
     * Met à jour le style des boutons et le formulaire.
     */
    private void changerOnglet(String onglet, Button actif, Button inactif) {
        ongletActif = onglet;
        actif.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-text-fill: " + ACCENT_BLEU + "; -fx-border-color: " + ACCENT_BLEU + "; -fx-border-width: 0 0 2 0;");
        inactif.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-border-width: 0;");
        mettreAJourFormulaire();
        notifierMiseAJour();
    }

    /**
     * Met à jour les champs du formulaire en fonction de l'onglet actif.
     * Affiche soit la capacité (Générateur) soit le type (Maison).
     */
    private void mettreAJourFormulaire() {
        conteneurDynamique.getChildren().clear();
        if (ongletActif.equals("Generateurs")) {
            champNom.setPromptText("Nom (G1)");
            conteneurDynamique.getChildren().add(champCapacite);
            boutonAjouter.setStyle("-fx-background-color: " + ACCENT_BLEU + "; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            champNom.setPromptText("Nom (M1)");
            conteneurDynamique.getChildren().add(comboType);
            boutonAjouter.setStyle("-fx-background-color: " + ACCENT_VERT + "; -fx-text-fill: white; -fx-font-weight: bold;");
        }
    }

    /**
     * Obtient la demande électrique totale sur un générateur.
     * @param calc Le calculateur de coûts.
     * @param g Le générateur.
     * @return La demande en kW.
     */
    private double obtenirUsage(CalculateurCouts calc, Generateur g) {
        try {
            return calc.getSommeDesDemandesElectriques(g, gestionnaire.getReseauElectrique());
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Crée un bouton stylisé pour la barre d'onglets.
     */
    private Button creerBoutonOnglet(String texte, boolean actif) {
        Button btn = new Button(texte);
        btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS);
        btn.setPrefHeight(35);
        String style = actif
            ? "-fx-background-color: " + FOND_CARTE + "; -fx-text-fill: " + ACCENT_BLEU + "; -fx-border-color: " + ACCENT_BLEU + "; -fx-border-width: 0 0 2 0;"
            : "-fx-background-color: transparent; -fx-text-fill: " + TEXTE_SECONDAIRE + "; -fx-border-width: 0;";
        btn.setStyle(style);
        return btn;
    }

    /**
     * Crée un champ de texte stylisé.
     */
    private TextField creerChamp(String invite) {
        TextField tf = new TextField();
        tf.setPromptText(invite);
        tf.setStyle("-fx-background-color: " + FOND_PRINCIPAL + "; -fx-text-fill: white; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-radius: 4;");
        return tf;
    }

    /**
     * Notifie l'interface principale qu'une mise à jour est requise.
     */
    private void notifierMiseAJour() {
        if (onUpdateRequired != null) onUpdateRequired.run();
    }

    /**
     * Affiche une boîte de dialogue d'information ou d'erreur.
     * @param titre Le titre de la fenêtre.
     * @param msg Le message à afficher.
     */
    private void afficherAlerte(String titre, String msg) {
        Alert alerte = new Alert(Alert.AlertType.INFORMATION);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.setContentText(msg);
        alerte.showAndWait();
    }
}