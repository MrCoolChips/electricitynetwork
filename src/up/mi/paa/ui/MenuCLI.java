package up.mi.paa.ui;

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
 * Classe gerant l'interface utilisateur en ligne de commande (CLI).
 * Affiche les menus, lit les entrees utilisateur et coordonne avec GestionnaireReseau.
 */
public class MenuCLI {

    // Codes couleur
    private static final String RESET = "\033[0m";
    private static final String VERT = "\033[32m";
    private static final String ROUGE = "\033[31m";
    private static final String JAUNE = "\033[33m";
    private static final String CYAN = "\033[36m";

    private Scanner sc;
    private GestionnaireReseau gestionnaire;
    private CalculateurCouts calculateur;
    private OptimiseurReseau optimiseur;
    
    /**
     * Constructeur du menu CLI pour la partie 1 (mode manuel).
     * 
     * @param sc Le scanner pour lire les entrees utilisateur
     * @param lambda Le coefficient de penalisation pour le calcul des couts
     */
    public MenuCLI(Scanner sc, int lambda) {
        this.sc = sc;
        this.gestionnaire = new GestionnaireReseau();
        this.calculateur = new CalculateurCouts(lambda);
        this.optimiseur = new OptimiseurReseau(calculateur);
    }
    
    /**
     * Constructeur du menu CLI pour la partie 2 (mode fichier).
     * Permet de charger un reseau existant depuis un fichier.
     * 
     * @param sc Le scanner pour lire les entrees utilisateur
     * @param lambda Le coefficient de penalisation pour le calcul des couts
     * @param gestionnaire Le gestionnaire de reseau pre-charge depuis le fichier
     */
    public MenuCLI(Scanner sc, int lambda, GestionnaireReseau gestionnaire) {
        this.sc = sc;
        this.gestionnaire = gestionnaire;
        this.calculateur = new CalculateurCouts(lambda);
        this.optimiseur = new OptimiseurReseau(calculateur);
    }
    
    /**
     * Demarre l'application et affiche le menu principal.
     * Gere la boucle principale du programme jusqu'a validation du reseau.
     */
    public void demarrer() {
        int choix;
        afficherBanniere();

        while (true) {
            afficherMenuPrincipal();
            choix = lireChoix();
            if (choix == 5) {
                if (verifierReseau()) break;
            } else {
                traiterChoixPrincipal(choix);
            }
        }
        menuEvaluation();
    }
    
    /**
     * Affiche la banniere de l'application.
     */
    private void afficherBanniere() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║     GESTIONNAIRE DE RESEAU ELECTRIQUE          ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
    }
    
    /**
     * Lit et valide un choix numerique saisi par l'utilisateur.
     * 
     * @return Le choix de l'utilisateur
     */
    private int lireChoix() {
        while (!sc.hasNextInt()) { 
            sc.nextLine(); 
            System.out.println("\n" + ROUGE + "[ERREUR]" + RESET + " Entree invalide ! Veuillez saisir un nombre.\n");
            System.out.print("> Votre choix : ");
        }
        int choix = sc.nextInt(); 
        sc.nextLine();
        return choix;
    }

    /**
     * Affiche le menu principal avec les options disponibles.
     */
    private void afficherMenuPrincipal() {
        System.out.println("┌────────────────────────────────────────────────┐");
        System.out.println("│              MENU PRINCIPAL                    │");
        System.out.println("├────────────────────────────────────────────────┤");
        System.out.println("│  1 | Ajouter un generateur                     │");
        System.out.println("│  2 | Ajouter une maison                        │");
        System.out.println("│  3 | Ajouter une connexion                     │");
        System.out.println("│  4 | Supprimer une connexion                   │");
        System.out.println("│  5 | Fin                                       │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.print("\n> Votre choix : ");
    }

    /**
     * Traite le choix de l'utilisateur dans le menu principal.
     * 
     * @param choix Le numero de l'option choisie
     */
    private void traiterChoixPrincipal(int choix) {
        switch (choix) {
            case 1:
                ajouterGenerateur();
                break;
            case 2:
                ajouterMaison();
                break;
            case 3:
                ajouterConnexion();
                break;
            case 4:
                supprimerConnexion();
                break;
            default:
                System.out.println("\n" + ROUGE + "[ERREUR]" + RESET + " Choix invalide ! Veuillez choisir entre 1 et 5.\n");
                break;
        }
    }

    /**
     * Dialogue pour ajouter ou mettre a jour un generateur.
     */
    private void ajouterGenerateur() {
        System.out.println("\n--- AJOUTER UN GENERATEUR ---");
        System.out.print("> Nom et capacite (ex: G1 60) : ");
        
        try {
            String[] parts = sc.nextLine().trim().split("\\s+");
            
            if(parts.length != 2) {
                throw new FormatInvalideException("Le format doit etre: NOM CAPACITE");
            }

            String nom = parts[0].trim().toUpperCase();
            double capacite;
            
            try {
                capacite = Double.parseDouble(parts[1].trim());
                if(capacite <= 0) {
                    throw new NumberFormatException("La capacite doit etre positive");
                }
            } catch(NumberFormatException e) {
                throw new FormatInvalideException("La capacite doit etre un nombre positif valide");
            }

            boolean existe = gestionnaire.ajouterOuModifierGenerateur(nom, capacite);
            
            if(existe) {
                System.out.println(VERT + "[OK]" + RESET + " Generateur " + nom + " mis a jour !\n");
            } else {
                System.out.println(VERT + "[OK]" + RESET + " Generateur " + nom + " cree !\n");
            }
            
        } catch(FormatInvalideException e) {
            System.out.println(ROUGE + "[ERREUR]" + RESET + " " + e.getMessage() + "\n");
        }
    }

    /**
     * Dialogue pour ajouter ou mettre a jour une maison.
     */
    private void ajouterMaison() {
        System.out.println("\n--- AJOUTER UNE MAISON ---");
        System.out.println("Types de consommation: BASSE, NORMAL, FORTE");
        System.out.print("> Nom et Consommation (ex: M1 FORTE) : ");
        
        try {
            String[] parts = sc.nextLine().trim().split("\\s+");

            if(parts.length != 2) {
                throw new FormatInvalideException("Le format doit etre: NOM TYPE_CONSOMMATION");
            }

            String nom = parts[0].trim().toUpperCase();
            String consommation = parts[1].trim().toUpperCase();
            TypeConsommation type;
            
            try {
                type = TypeConsommation.valueOf(consommation);
            } catch (IllegalArgumentException e) {
                throw new FormatInvalideException("Type invalide ! Utilisez: BASSE, NORMAL ou FORTE");
            }

            boolean existe = gestionnaire.ajouterOuModifierMaison(nom, type);
            
            if(existe) {
                System.out.println(VERT + "[OK]" + RESET + " Maison " + nom + " mise a jour !\n");
            } else {
                System.out.println(VERT + "[OK]" + RESET + " Maison " + nom + " creee !\n");
            }
            
        } catch(FormatInvalideException e) {
            System.out.println(ROUGE + "[ERREUR]" + RESET + " " + e.getMessage() + "\n");
        }
    }

    /**
     * Dialogue pour creer une connexion entre une maison et un generateur.
     */
    private void ajouterConnexion() {
        System.out.println("\n--- AJOUTER UNE CONNEXION ---");
        System.out.print("> Generateur et maison (ex: G1 M1 ou M1 G1) : ");
        
        try {
            String[] parts = sc.nextLine().trim().split("\\s+");

            if(parts.length != 2) {
                throw new FormatInvalideException("Le format doit etre: ELEMENT1 ELEMENT2");
            }

            String element1 = parts[0].trim().toUpperCase();
            String element2 = parts[1].trim().toUpperCase();

            gestionnaire.creerConnexion(element1, element2);
            System.out.println(VERT + "[OK]" + RESET + " Connexion creee !\n");
            
        } catch(FormatInvalideException | GenerateurIntrouvableException | 
                MaisonIntrouvableException | ConnexionExistanteException e) {
            System.out.println(ROUGE + "[ERREUR]" + RESET + " " + e.getMessage() + "\n");
        }
    }

    /**
     * Dialogue pour supprimer une connexion existante.
     */
    private void supprimerConnexion() {
        System.out.println("\n--- SUPPRIMER UNE CONNEXION ---");
        System.out.print("> Generateur et maison (ex: G1 M1 ou M1 G1) : ");
        
        try {
            String[] parts = sc.nextLine().trim().split("\\s+");

            if(parts.length != 2) {
                throw new FormatInvalideException("Le format doit etre: ELEMENT1 ELEMENT2");
            }

            String element1 = parts[0].trim().toUpperCase();
            String element2 = parts[1].trim().toUpperCase();

            gestionnaire.supprimerConnexion(element1, element2);
            System.out.println(VERT + "[OK]" + RESET + " Connexion supprimee !\n");
            
        } catch(FormatInvalideException | GenerateurIntrouvableException | 
                MaisonIntrouvableException | ConnexionIntrouvableException e) {
            System.out.println(ROUGE + "[ERREUR]" + RESET + " " + e.getMessage() + "\n");
        }
    }

    /**
     * Verifie que le reseau est valide avant de terminer.
     * 
     * @return true si le reseau est valide, false sinon
     */
    private boolean verifierReseau() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│         VERIFICATION DU RESEAU                 │");
        System.out.println("└────────────────────────────────────────────────┘");
    
        String problemes = gestionnaire.verifierValiditeReseau();
        
        if (problemes.length() == 0) {
            System.out.println(VERT + "[OK]" + RESET + " Reseau valide ! Chaque maison est connectee a exactement un generateur.");
            return true;
        } else {
            System.out.println(JAUNE + "[ATTENTION]" + RESET + " Problemes detectes :");
            System.out.println(problemes);
            
            System.out.println("\nCorrigez ces problemes avant de terminer !\n");
            return false;
        }
    }
    
    /**
     * Affiche le menu d'evaluation du reseau.
     */
    private void afficherMenuEvaluation() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│           EVALUATION DU RESEAU                 │");
        System.out.println("├────────────────────────────────────────────────┤");
        System.out.println("│  1 | Calculer le cout du reseau                │");
        System.out.println("│  2 | Modifier une connexion                    │");
        System.out.println("│  3 | Afficher le reseau                        │");
        System.out.println("│  4 | Fin                                       │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.print("\n> Votre choix : ");
    }
    
    /**
     * Gere le menu d'evaluation et les operations avancees.
     */
    private void menuEvaluation() {
        while (true) {
            afficherMenuEvaluation();
            int reponse = lireChoix();
            switch (reponse) {
                case 1: 
                    calculerCout();
                    break;
                case 2: 
                    modifierConnexion();
                    break;
                case 3: 
                    afficherReseau();
                    break;
                case 4: 
                    verifierReseau();
                    return;
                default: 
                    System.out.println("\n" + ROUGE + "[ERREUR]" + RESET + " Choix invalide ! Veuillez choisir entre 1 et 4.\n");
            }
        }
    }
    
    /**
     * Calcule et affiche le cout du reseau.
     */
    private void calculerCout() {
        try {
            Couts cout = calculateur.calculerCout(gestionnaire.getReseauElectrique());
            System.out.println("\n" + CYAN + "Le cout du reseau electrique actuel est : " + cout.toString() + RESET + "\n");
        } catch(ArithmeticException e) {
            System.out.println(ROUGE + "[ERREUR]" + RESET + " Impossible de calculer le cout : " + e.getMessage() + "\n");
        }
    }
    
    /**
     * Dialogue pour modifier une connexion existante.
     */
    private void modifierConnexion() {
        System.out.println("\n--- MODIFIER UNE CONNEXION ---");
        System.out.print("> Ancienne connexion (ex: M1 G1 ou G1 M1) : ");
        String[] ancienneConnexion = sc.nextLine().trim().split("\\s+");
        
        try {
            if (ancienneConnexion.length != 2) { 
                throw new FormatInvalideException("Le format doit etre: ELEMENT1 ELEMENT2");
            }

            System.out.print("> Nouvelle connexion (ex: M1 G2 ou G2 M1) : ");
            String[] nouvelleConnexion = sc.nextLine().trim().split("\\s+");
            
            if (nouvelleConnexion.length != 2) { 
                throw new FormatInvalideException("Le format doit etre: ELEMENT1 ELEMENT2");
            }

            gestionnaire.modifierConnexion(
                ancienneConnexion[0].trim().toUpperCase(), 
                ancienneConnexion[1].trim().toUpperCase(),
                nouvelleConnexion[0].trim().toUpperCase(), 
                nouvelleConnexion[1].trim().toUpperCase()
            );
            
            System.out.println(VERT + "[OK]" + RESET + " Modification reussie, voici les connexions :\n");
            afficherReseau();
            
        } catch(FormatInvalideException | GenerateurIntrouvableException | 
                MaisonIntrouvableException | ConnexionIntrouvableException e) {
            System.out.println(ROUGE + "[ERREUR]" + RESET + " " + e.getMessage() + "\n");
        }
    }
    
    /**
     * Affiche l'etat complet du reseau electrique.
     */
    private void afficherReseau() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║              ETAT DU RESEAU                    ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        
        ReseauElectrique reseau = gestionnaire.getReseauElectrique();
        
        System.out.println("\nMAISONS :");
        System.out.println("─────────────────────────────────");
        for (Maison m : reseau.getMaisons()) {
            System.out.println("  - " + m.toString());
        }
        
        System.out.println("\nGENERATEURS :");
        System.out.println("─────────────────────────────────");
        for (Generateur g : reseau.getGenerateurs()) {
            System.out.println("  - " + g.toString());
        }
        
        System.out.println("\nCONNEXIONS :");
        System.out.println("─────────────────────────────────");
        reseau.affichageConnexions();
        System.out.println();
    }

    // =========================================================================
    //  PARTIE 2 - MODE FICHIER ET OPTIMISATION
    // =========================================================================

    /**
     * Demarre le menu de la partie 2 (mode fichier).
     * Le reseau a ete charge depuis un fichier et on peut l'optimiser automatiquement.
     */
    public void demarrerPartie2() {
        // Afficher le reseau charge
        afficherReseau();
        
        int choix;
        while (true) {
            afficherMenuPartie2();
            choix = lireChoix();
            
            switch (choix) {
                case 1:
                    resolutionAutomatique();
                    break;
                case 2:
                    sauvegarderSolution();
                    break;
                case 3:
                    System.out.println("\n" + CYAN + "[INFO]" + RESET + " Au revoir !\n");
                    return;
                default:
                    System.out.println("\n" + ROUGE + "[ERREUR]" + RESET + " Choix invalide ! Veuillez choisir entre 1 et 3.\n");
            }
        }
    }

    /**
     * Affiche le menu de la partie 2.
     */
    private void afficherMenuPartie2() {
        System.out.println("┌────────────────────────────────────────────────┐");
        System.out.println("│              MENU PARTIE 2                     │");
        System.out.println("├────────────────────────────────────────────────┤");
        System.out.println("│  1 | Resolution automatique                    │");
        System.out.println("│  2 | Sauvegarder la solution                   │");
        System.out.println("│  3 | Fin                                       │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.print("\n> Votre choix : ");
    }

    /**
     * Execute la resolution automatique du reseau.
     * Affiche l'ancien cout, optimise le reseau, affiche le nouveau cout
     * et les connexions modifiees.
     */
    private void resolutionAutomatique() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║         RESOLUTION AUTOMATIQUE                 ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        ReseauElectrique reseau = gestionnaire.getReseauElectrique();
        
        // Sauvegarder les connexions avant optimisation
        Map<Maison, Generateur> connexionsAvant = new HashMap<>();
        for (Generateur g : reseau.getGenerateurs()) {
            List<Maison> maisons = reseau.trouverLesMaisonsDeGenerateur(g);
            if (maisons != null) {
                for (Maison m : maisons) {
                    connexionsAvant.put(m, g);
                }
            }
        }

        // Calculer et afficher l'ancien cout
        Couts ancienCout = calculateur.calculerCout(reseau);
        System.out.println(ROUGE + "[AVANT] Cout : " + ancienCout.toString() + RESET);

        // Lancer l'optimisation
        System.out.println("\n" + JAUNE + "[...] Optimisation en cours (max 3 secondes)..." + RESET + "\n");
        optimiseur.optimiser(reseau);

        // Calculer et afficher le nouveau cout
        Couts nouveauCout = calculateur.calculerCout(reseau);
        System.out.println(VERT + "[APRES] Cout : " + nouveauCout.toString() + RESET);

        // Calculer l'amelioration
        double amelioration = ancienCout.getCoutGlobale() - nouveauCout.getCoutGlobale();
        if (amelioration > 0) {
            System.out.println(VERT + "[GAIN]  Amelioration : -" + String.format("%.2f", amelioration) + RESET);
        } else if (amelioration == 0) {
            System.out.println(JAUNE + "[INFO]  Le reseau etait deja optimal !" + RESET);
        }

        // Afficher les connexions modifiees
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│         CONNEXIONS MODIFIEES                   │");
        System.out.println("└────────────────────────────────────────────────┘");

        List<String> modifications = new ArrayList<>();
        for (Generateur g : reseau.getGenerateurs()) {
            List<Maison> maisonsApres = reseau.trouverLesMaisonsDeGenerateur(g);
            if (maisonsApres != null) {
                for (Maison m : maisonsApres) {
                    Generateur ancienGen = connexionsAvant.get(m);
                    if (ancienGen != null && !ancienGen.equals(g)) {
                        modifications.add("  " + m.getNom() + " : " + ROUGE + ancienGen.getNom() + RESET 
                                        + " --> " + VERT + g.getNom() + RESET);
                    }
                }
            }
        }

        if (modifications.isEmpty()) {
            System.out.println(JAUNE + "  Aucune connexion modifiee." + RESET);
        } else {
            System.out.println("  " + modifications.size() + " connexion(s) modifiee(s) :\n");
            for (String mod : modifications) {
                System.out.println(mod);
            }
        }
        System.out.println();
    }

    /**
     * Sauvegarde la solution actuelle dans un fichier.
     * Verifie si le fichier existe deja et demande confirmation.
     */
    private void sauvegarderSolution() {
        System.out.println("\n--- SAUVEGARDER LA SOLUTION ---");
        System.out.print("> Nom du fichier de sauvegarde : ");
        String nomFichier = sc.nextLine().trim();

        if (nomFichier.isEmpty()) {
            System.out.println(ROUGE + "[ERREUR]" + RESET + " Le nom du fichier ne peut pas etre vide.\n");
            return;
        }

        // Ajouter l'extension .txt si absente
        if (!nomFichier.endsWith(".txt")) {
            nomFichier += ".txt";
        }

        File fichier = new File(nomFichier);

        // Verifier si le fichier existe deja
        if (fichier.exists()) {
            System.out.println(JAUNE + "[ATTENTION]" + RESET + " Le fichier '" + nomFichier + "' existe deja.");
            System.out.print("> Voulez-vous l'ecraser ? (oui/non) : ");
            String reponse = sc.nextLine().trim().toLowerCase();
            
            if (!reponse.equals("oui") && !reponse.equals("o")) {
                System.out.println(CYAN + "[INFO]" + RESET + " Sauvegarde annulee.\n");
                return;
            }
        }

        // Sauvegarder le reseau
        GestionnaireFichier.ecrireFichierReseau(fichier, gestionnaire.getReseauElectrique());
        System.out.println(VERT + "[OK]" + RESET + " Solution sauvegardee dans '" + nomFichier + "' !\n");
    }

}
