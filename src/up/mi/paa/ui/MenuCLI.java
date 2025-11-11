package up.mi.paa.ui;

import up.mi.paa.service.GestionnaireReseau;
import up.mi.paa.model.*;
import up.mi.paa.exception.*;

import java.util.Scanner;

/**
 * Classe gerant l'interface utilisateur en ligne de commande (CLI).
 * Affiche les menus, lit les entrees utilisateur et coordonne avec GestionnaireReseau.
 */
public class MenuCLI {

    private Scanner sc;
    private GestionnaireReseau gestionnaire;
    
    /**
     * Constructeur du menu CLI.
     * 
     * @param sc Le scanner pour lire les entrees utilisateur
     */
    public MenuCLI(Scanner sc) {
        this.sc = sc;
        this.gestionnaire = new GestionnaireReseau();
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
            System.out.println("\n[ERREUR] Entree invalide ! Veuillez saisir un nombre.\n");
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
                System.out.println("\n[ERREUR] Choix invalide ! Veuillez choisir entre 1 et 5.\n");
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
            String[] parts = sc.nextLine().split(" ");
            
            if(parts.length != 2) {
                throw new FormatInvalideException("Le format doit etre: NOM CAPACITE");
            }

            String nom = parts[0].toUpperCase();
            double capacite;
            
            try {
                capacite = Double.parseDouble(parts[1]);
                if(capacite <= 0) {
                    throw new NumberFormatException("La capacite doit etre positive");
                }
            } catch(NumberFormatException e) {
                throw new FormatInvalideException("La capacite doit etre un nombre positif valide");
            }

            boolean existe = gestionnaire.ajouterOuModifierGenerateur(nom, capacite);
            
            if(existe) {
                System.out.println("[OK] Generateur " + nom + " mis a jour !\n");
            } else {
                System.out.println("[OK] Generateur " + nom + " cree !\n");
            }
            
        } catch(FormatInvalideException e) {
            System.out.println("[ERREUR] " + e.getMessage() + "\n");
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
            String[] parts = sc.nextLine().split(" ");

            if(parts.length != 2) {
                throw new FormatInvalideException("Le format doit etre: NOM TYPE_CONSOMMATION");
            }

            String nom = parts[0].toUpperCase();
            String consommation = parts[1].toUpperCase();
            TypeConsommation type;
            
            try {
                type = TypeConsommation.valueOf(consommation);
            } catch (IllegalArgumentException e) {
                throw new FormatInvalideException("Type invalide ! Utilisez: BASSE, NORMAL ou FORTE");
            }

            boolean existe = gestionnaire.ajouterOuModifierMaison(nom, type);
            
            if(existe) {
                System.out.println("[OK] Maison " + nom + " mise a jour !\n");
            } else {
                System.out.println("[OK] Maison " + nom + " creee !\n");
            }
            
        } catch(FormatInvalideException e) {
            System.out.println("[ERREUR] " + e.getMessage() + "\n");
        }
    }

    /**
     * Dialogue pour creer une connexion entre une maison et un generateur.
     */
    private void ajouterConnexion() {
        System.out.println("\n--- AJOUTER UNE CONNEXION ---");
        System.out.print("> Generateur et maison (ex: G1 M1 ou M1 G1) : ");
        
        try {
            String[] parts = sc.nextLine().split(" ");

            if(parts.length != 2) {
                throw new FormatInvalideException("Le format doit etre: ELEMENT1 ELEMENT2");
            }

            String element1 = parts[0].toUpperCase();
            String element2 = parts[1].toUpperCase();

            gestionnaire.creerConnexion(element1, element2);
            System.out.println("[OK] Connexion creee !\n");
            
        } catch(FormatInvalideException | GenerateurIntrouvableException | 
                MaisonIntrouvableException | ConnexionExistanteException e) {
            System.out.println("[ERREUR] " + e.getMessage() + "\n");
        }
    }

    /**
     * Dialogue pour supprimer une connexion existante.
     */
    private void supprimerConnexion() {
        System.out.println("\n--- SUPPRIMER UNE CONNEXION ---");
        System.out.print("> Generateur et maison (ex: G1 M1 ou M1 G1) : ");
        
        try {
            String[] parts = sc.nextLine().split(" ");

            if(parts.length != 2) {
                throw new FormatInvalideException("Le format doit etre: ELEMENT1 ELEMENT2");
            }

            String element1 = parts[0].toUpperCase();
            String element2 = parts[1].toUpperCase();

            gestionnaire.supprimerConnexion(element1, element2);
            System.out.println("[OK] Connexion supprimee !\n");
            
        } catch(FormatInvalideException | GenerateurIntrouvableException | 
                MaisonIntrouvableException | ConnexionIntrouvableException e) {
            System.out.println("[ERREUR] " + e.getMessage() + "\n");
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
    
        String[] problemes = gestionnaire.verifierValiditeReseau();
        
        if (problemes.length == 0) {
            System.out.println("[OK] Reseau valide ! Chaque maison est connectee a exactement un generateur.");
            return true;
        } else {
            System.out.println("[ATTENTION] Problemes detectes :");
            for (String probleme : problemes) {
                System.out.println("  - " + probleme);
            }
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
                    System.out.println("\n[ERREUR] Choix invalide ! Veuillez choisir entre 1 et 4.\n");
            }
        }
    }
    
    /**
     * Calcule et affiche le cout du reseau.
     */
    private void calculerCout() {
        try {
            double cout = gestionnaire.calculerCout();
            System.out.println("\nLe cout du reseau electrique actuel est : " + String.format("%.2f", cout) + "\n");
        } catch(ArithmeticException e) {
            System.out.println("[ERREUR] Impossible de calculer le cout : " + e.getMessage() + "\n");
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
                ancienneConnexion[0].toUpperCase(), 
                ancienneConnexion[1].toUpperCase(),
                nouvelleConnexion[0].toUpperCase(), 
                nouvelleConnexion[1].toUpperCase()
            );
            
            System.out.println("[OK] Modification reussie, voici les connexions :\n");
            afficherReseau();
            
        } catch(FormatInvalideException | GenerateurIntrouvableException | 
                MaisonIntrouvableException | ConnexionIntrouvableException e) {
            System.out.println("[ERREUR] " + e.getMessage() + "\n");
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
        for (var connexion : reseau.getConnexions().entrySet()) {
            System.out.println("   " + connexion.getKey().getNom() + " <-> " + connexion.getValue().getNom());
        }
        System.out.println();
    }

}
