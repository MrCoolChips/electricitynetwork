package up.mi.paa.ui.cli;

import up.mi.paa.io.GestionnaireFichier;
import up.mi.paa.service.CalculateurCouts;
import up.mi.paa.service.GestionnaireReseau;
import up.mi.paa.service.OptimiseurReseau;
import up.mi.paa.model.*;
import up.mi.paa.exception.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Interface utilisateur en ligne de commande (CLI) pour le gestionnaire de réseau électrique.
 * Gère les menus, les entrées utilisateur et coordonne les opérations avec le GestionnaireReseau.
 * 
 * @author Groupe 10
 */
public class MenuCLI {

    private static final String RESET  = "\033[0m";
    private static final String VERT   = "\033[32m";
    private static final String ROUGE  = "\033[31m";
    private static final String JAUNE  = "\033[33m";
    private static final String CYAN   = "\033[36m";

    private final Scanner sc;
    private final GestionnaireReseau gestionnaire;
    private final CalculateurCouts calculateur;
    private final OptimiseurReseau optimiseur;

    /**
     * Constructeur pour le mode manuel (Partie 1).
     * 
     * @param sc     Scanner pour les entrées utilisateur
     * @param lambda Coefficient de pénalisation de la surcharge
     */
    public MenuCLI(Scanner sc, int lambda) {
        this.sc = sc;
        this.gestionnaire = new GestionnaireReseau();
        this.calculateur = new CalculateurCouts(lambda);
        this.optimiseur = new OptimiseurReseau(calculateur);
    }

    /**
     * Constructeur pour le mode fichier (Partie 2).
     * 
     * @param sc          Scanner pour les entrées utilisateur
     * @param lambda      Coefficient de pénalisation de la surcharge
     * @param gestionnaire Gestionnaire pré-chargé depuis un fichier
     */
    public MenuCLI(Scanner sc, int lambda, GestionnaireReseau gestionnaire) {
        this.sc = sc;
        this.gestionnaire = gestionnaire;
        this.calculateur = new CalculateurCouts(lambda);
        this.optimiseur = new OptimiseurReseau(calculateur);
    }

    /**
     * Démarre l'application en mode manuel (Partie 1).
     */
    public void demarrer() {
        afficherBanniere();
        
        while (true) {
            afficherMenu(MENU_PRINCIPAL);
            int choix = lireChoix();
            
            if (choix == 5) {
                if (verifierReseau()) break;
            } else {
                traiterChoixPrincipal(choix);
            }
        }
        menuEvaluation();
    }

    /**
     * Démarre l'application en mode fichier (Partie 2).
     */
    public void demarrerPartie2() {
        afficherReseau();
        
        while (true) {
            afficherMenu(MENU_PARTIE2);
            int choix = lireChoix();
            
            switch (choix) {
                case 1: resolutionAutomatique(); break;
                case 2: sauvegarderSolution();   break;
                case 3: 
                    afficherInfo("Au revoir !");
                    return;
                default: 
                    afficherErreur("Choix invalide ! Veuillez choisir entre 1 et 3.");
            }
        }
    }

    // =========================================================================
    //  MENUS ET AFFICHAGE
    // =========================================================================

    private static final String[] MENU_PRINCIPAL = {
        "MENU PRINCIPAL",
        "1 | Ajouter un generateur",
        "2 | Ajouter une maison",
        "3 | Ajouter une connexion",
        "4 | Supprimer une connexion",
        "5 | Fin"
    };

    private static final String[] MENU_EVALUATION = {
        "EVALUATION DU RESEAU",
        "1 | Calculer le cout du reseau",
        "2 | Modifier une connexion",
        "3 | Afficher le reseau",
        "4 | Fin"
    };

    private static final String[] MENU_PARTIE2 = {
        "MENU PARTIE 2",
        "1 | Resolution automatique",
        "2 | Sauvegarder la solution",
        "3 | Fin"
    };

    private void afficherBanniere() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║     GESTIONNAIRE DE RESEAU ELECTRIQUE          ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
    }

    private void afficherMenu(String[] lignes) {
        System.out.println("┌────────────────────────────────────────────────┐");
        System.out.println("│              " + centrer(lignes[0], 34) + "│");
        System.out.println("├────────────────────────────────────────────────┤");
        for (int i = 1; i < lignes.length; i++) {
            System.out.println("│  " + completer(lignes[i], 44) + "│");
        }
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.print("\n> Votre choix : ");
    }

    private String centrer(String s, int largeur) {
        int padding = (largeur - s.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + s + " ".repeat(Math.max(0, largeur - s.length() - padding));
    }

    private String completer(String s, int largeur) {
        return s + " ".repeat(Math.max(0, largeur - s.length()));
    }

    private void afficherOK(String message)     { System.out.println(VERT + "[OK]" + RESET + " " + message + "\n"); }
    private void afficherErreur(String message) { System.out.println(ROUGE + "[ERREUR]" + RESET + " " + message + "\n"); }
    private void afficherInfo(String message)   { System.out.println(CYAN + "[INFO]" + RESET + " " + message + "\n"); }
    private void afficherAttention(String msg)  { System.out.println(JAUNE + "[ATTENTION]" + RESET + " " + msg); }

    // =========================================================================
    //  SAISIE UTILISATEUR
    // =========================================================================

    private int lireChoix() {
        while (!sc.hasNextInt()) {
            sc.nextLine();
            afficherErreur("Entrée invalide ! Veuillez saisir un nombre.");
            System.out.print("> Votre choix : ");
        }
        int choix = sc.nextInt();
        sc.nextLine();
        return choix;
    }

    private String[] lireDeuxElements(String invite) throws FormatInvalideException {
        System.out.print(invite);
        String[] parts = sc.nextLine().trim().split("\\s+");
        if (parts.length != 2) {
            throw new FormatInvalideException("Le format doit être: ELEMENT1 ELEMENT2");
        }
        return new String[] { parts[0].toUpperCase(), parts[1].toUpperCase() };
    }

    // =========================================================================
    //  TRAITEMENT DES CHOIX
    // =========================================================================

    private void traiterChoixPrincipal(int choix) {
        switch (choix) {
            case 1: ajouterGenerateur();   break;
            case 2: ajouterMaison();       break;
            case 3: ajouterConnexion();    break;
            case 4: supprimerConnexion();  break;
            default: afficherErreur("Choix invalide ! Veuillez choisir entre 1 et 5.");
        }
    }

    private void ajouterGenerateur() {
        System.out.println("\n--- AJOUTER UN GENERATEUR ---");
        try {
            String[] parts = lireDeuxElements("> Nom et capacité (ex: G1 60) : ");
            double capacite = Double.parseDouble(parts[1]);
            
            if (capacite <= 0) throw new FormatInvalideException("La capacité doit être positive");
            
            boolean existe = gestionnaire.ajouterOuModifierGenerateur(parts[0], capacite);
            afficherOK("Générateur " + parts[0] + (existe ? " mis à jour !" : " créé !"));
            
        } catch (NumberFormatException e) {
            afficherErreur("La capacité doit être un nombre valide");
        } catch (FormatInvalideException e) {
            afficherErreur(e.getMessage());
        }
    }

    private void ajouterMaison() {
        System.out.println("\n--- AJOUTER UNE MAISON ---");
        System.out.println("Types de consommation: BASSE, NORMAL, FORTE");
        try {
            String[] parts = lireDeuxElements("> Nom et consommation (ex: M1 FORTE) : ");
            TypeConsommation type = TypeConsommation.valueOf(parts[1]);
            
            boolean existe = gestionnaire.ajouterOuModifierMaison(parts[0], type);
            afficherOK("Maison " + parts[0] + (existe ? " mise à jour !" : " créée !"));
            
        } catch (IllegalArgumentException e) {
            afficherErreur("Type invalide ! Utilisez: BASSE, NORMAL ou FORTE");
        } catch (FormatInvalideException e) {
            afficherErreur(e.getMessage());
        }
    }

    private void ajouterConnexion() {
        System.out.println("\n--- AJOUTER UNE CONNEXION ---");
        try {
            String[] parts = lireDeuxElements("> Générateur et maison (ex: G1 M1) : ");
            gestionnaire.creerConnexion(parts[0], parts[1]);
            afficherOK("Connexion créée !");
        } catch (FormatInvalideException | GenerateurIntrouvableException |
                 MaisonIntrouvableException | ConnexionExistanteException e) {
            afficherErreur(e.getMessage());
        }
    }

    private void supprimerConnexion() {
        System.out.println("\n--- SUPPRIMER UNE CONNEXION ---");
        try {
            String[] parts = lireDeuxElements("> Générateur et maison (ex: G1 M1) : ");
            gestionnaire.supprimerConnexion(parts[0], parts[1]);
            afficherOK("Connexion supprimée !");
        } catch (FormatInvalideException | GenerateurIntrouvableException |
                 MaisonIntrouvableException | ConnexionIntrouvableException e) {
            afficherErreur(e.getMessage());
        }
    }

    // =========================================================================
    //  VERIFICATION ET EVALUATION
    // =========================================================================

    private boolean verifierReseau() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│         VERIFICATION DU RESEAU                 │");
        System.out.println("└────────────────────────────────────────────────┘");

        String problemes = gestionnaire.verifierValiditeReseau();
        
        if (problemes.isEmpty()) {
            afficherOK("Réseau valide ! Chaque maison est connectée à exactement un générateur.");
            return true;
        }
        
        afficherAttention("Problèmes détectés :");
        System.out.println(problemes);
        System.out.println("\nCorrigez ces problèmes avant de terminer !\n");
        return false;
    }

    private void menuEvaluation() {
        while (true) {
            afficherMenu(MENU_EVALUATION);
            int choix = lireChoix();
            
            switch (choix) {
                case 1: calculerCout();       break;
                case 2: modifierConnexion();  break;
                case 3: afficherReseau();     break;
                case 4: verifierReseau();     return;
                default: afficherErreur("Choix invalide ! Veuillez choisir entre 1 et 4.");
            }
        }
    }

    private void calculerCout() {
        try {
            Couts cout = calculateur.calculerCout(gestionnaire.getReseauElectrique());
            System.out.println("\n" + CYAN + "Coût du réseau : " + cout + RESET + "\n");
        } catch (ArithmeticException e) {
            afficherErreur("Impossible de calculer le coût : " + e.getMessage());
        }
    }

    private void modifierConnexion() {
        System.out.println("\n--- MODIFIER UNE CONNEXION ---");
        try {
            String[] ancienne = lireDeuxElements("> Ancienne connexion (ex: M1 G1) : ");
            String[] nouvelle = lireDeuxElements("> Nouvelle connexion (ex: M1 G2) : ");
            
            gestionnaire.modifierConnexion(ancienne[0], ancienne[1], nouvelle[0], nouvelle[1]);
            afficherOK("Modification réussie !");
            afficherReseau();
        } catch (FormatInvalideException | GenerateurIntrouvableException |
                 MaisonIntrouvableException | ConnexionIntrouvableException e) {
            afficherErreur(e.getMessage());
        }
    }

    private void afficherReseau() {
        ReseauElectrique reseau = gestionnaire.getReseauElectrique();
        
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║              ETAT DU RESEAU                    ║");
        System.out.println("╚════════════════════════════════════════════════╝");

        System.out.println("\nMAISONS :");
        System.out.println("─────────────────────────────────");
        for (Maison m : reseau.getMaisons()) {
            System.out.println("  - " + m);
        }

        System.out.println("\nGENERATEURS :");
        System.out.println("─────────────────────────────────");
        for (Generateur g : reseau.getGenerateurs()) {
            System.out.println("  - " + g);
        }

        System.out.println("\nCONNEXIONS :");
        System.out.println("─────────────────────────────────");
        reseau.affichageConnexions();
        System.out.println();
    }

    // =========================================================================
    //  PARTIE 2 - OPTIMISATION
    // =========================================================================

    private void resolutionAutomatique() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║         RESOLUTION AUTOMATIQUE                 ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        ReseauElectrique reseau = gestionnaire.getReseauElectrique();

        Map<Maison, Generateur> connexionsAvant = sauvegarderConnexions(reseau);
        Couts ancienCout = calculateur.calculerCout(reseau);
        
        System.out.println(ROUGE + "[AVANT] Coût : " + ancienCout + RESET);
        System.out.println("\n" + JAUNE + "[...] Optimisation en cours (max 3 secondes)..." + RESET + "\n");

        optimiseur.optimiser(reseau);

        Couts nouveauCout = calculateur.calculerCout(reseau);
        System.out.println(VERT + "[APRES] Coût : " + nouveauCout + RESET);

        double amelioration = ancienCout.getCoutGlobale() - nouveauCout.getCoutGlobale();
        if (amelioration > 0) {
            System.out.println(VERT + "[GAIN]  Amélioration : -" + String.format("%.2f", amelioration) + RESET);
        } else if (amelioration == 0) {
            System.out.println(JAUNE + "[INFO]  Le réseau était déjà optimal !" + RESET);
        }

        afficherModifications(reseau, connexionsAvant);
    }

    private Map<Maison, Generateur> sauvegarderConnexions(ReseauElectrique reseau) {
        Map<Maison, Generateur> map = new HashMap<>();
        for (Generateur g : reseau.getGenerateurs()) {
            List<Maison> maisons = reseau.trouverLesMaisonsDeGenerateur(g);
            if (maisons != null) {
                for (Maison m : maisons) {
                    map.put(m, g);
                }
            }
        }
        return map;
    }

    private void afficherModifications(ReseauElectrique reseau, Map<Maison, Generateur> avant) {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│         CONNEXIONS MODIFIEES                   │");
        System.out.println("└────────────────────────────────────────────────┘");

        List<String> modifications = new ArrayList<>();
        for (Generateur g : reseau.getGenerateurs()) {
            List<Maison> maisonsApres = reseau.trouverLesMaisonsDeGenerateur(g);
            if (maisonsApres != null) {
                for (Maison m : maisonsApres) {
                    Generateur ancienGen = avant.get(m);
                    if (ancienGen != null && !ancienGen.equals(g)) {
                        modifications.add("  " + m.getNom() + " : " + ROUGE + ancienGen.getNom() + RESET
                                        + " --> " + VERT + g.getNom() + RESET);
                    }
                }
            }
        }

        if (modifications.isEmpty()) {
            System.out.println(JAUNE + "  Aucune connexion modifiée." + RESET);
        } else {
            System.out.println("  " + modifications.size() + " connexion(s) modifiée(s) :\n");
            modifications.forEach(System.out::println);
        }
        System.out.println();
    }

    private void sauvegarderSolution() {
        System.out.println("\n--- SAUVEGARDER LA SOLUTION ---");
        System.out.print("> Nom du fichier : ");
        String nomFichier = sc.nextLine().trim();

        if (nomFichier.isEmpty()) {
            afficherErreur("Le nom du fichier ne peut pas être vide.");
            return;
        }

        if (!nomFichier.endsWith(".txt")) {
            nomFichier += ".txt";
        }

        File fichier = new File(nomFichier);

        if (fichier.exists()) {
            afficherAttention("Le fichier '" + nomFichier + "' existe déjà.");
            System.out.print("> Écraser ? (oui/non) : ");
            String reponse = sc.nextLine().trim().toLowerCase();
            if (!reponse.equals("oui") && !reponse.equals("o")) {
                afficherInfo("Sauvegarde annulée.");
                return;
            }
        }

        GestionnaireFichier.ecrireFichierReseau(fichier, gestionnaire.getReseauElectrique());
        afficherOK("Solution sauvegardée dans '" + nomFichier + "' !");
    }
}
