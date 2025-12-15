package up.mi.paa;

import up.mi.paa.io.GestionnaireFichier;
import up.mi.paa.service.GestionnaireReseau;
import up.mi.paa.ui.cli.MenuCLI;

import java.io.File;
import java.util.Scanner;

/**
 * Classe principale pour demarrer l'application de gestion du reseau electrique.
 * 
 * Modes d'execution :
 * - Sans arguments : mode manuel (partie 1) - construction du reseau a la main
 * - Avec arguments : mode fichier (partie 2) - lecture du reseau depuis un fichier
 * 
 * Usage : java Main [chemin_fichier] [lambda]
 */
public class Main {

    private static final String RESET = "\033[0m";
    private static final String ROUGE = "\033[31m";
    private static final String VERT = "\033[32m";
    private static final String CYAN = "\033[36m";

    /**
     * Point d'entree de l'application.
     * 
     * @param args Les arguments de la ligne de commande :
     *             args[0] : chemin vers le fichier du reseau (optionnel)
     *             args[1] : valeur de lambda (optionnel, defaut = 10)
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int lambda = 10;

        afficherBanniere();

        // Mode avec fichier (partie 2)
        if (args.length >= 1) {
            String cheminFichier = args[0];
            
            // Lecture du lambda si fourni
            if (args.length >= 2) {
                try {
                    lambda = Integer.parseInt(args[1]);
                    if (lambda <= 0) {
                        System.out.println(ROUGE + "[ERREUR]" + RESET + " Lambda doit etre un entier positif. Valeur par defaut (10) utilisee.");
                        lambda = 10;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ROUGE + "[ERREUR]" + RESET + " Valeur lambda invalide. Valeur par defaut (10) utilisee.");
                }
            }

            System.out.println(CYAN + "[INFO]" + RESET + " Mode fichier active");
            System.out.println(CYAN + "[INFO]" + RESET + " Fichier : " + cheminFichier);
            System.out.println(CYAN + "[INFO]" + RESET + " Lambda  : " + lambda + "\n");

            // Verification et lecture du fichier
            File fichier = new File(cheminFichier);
            
            if (!fichier.exists()) {
                System.out.println(ROUGE + "[ERREUR]" + RESET + " Le fichier '" + cheminFichier + "' n'existe pas.");
                sc.close();
                return;
            }
            
            if (!fichier.isFile()) {
                System.out.println(ROUGE + "[ERREUR]" + RESET + " '" + cheminFichier + "' n'est pas un fichier valide.");
                sc.close();
                return;
            }

            // Lecture du fichier et creation du gestionnaire
            GestionnaireReseau gestionnaire = GestionnaireFichier.lireFichierReseau(fichier);
            
            if (gestionnaire == null) {
                System.out.println(ROUGE + "[ERREUR]" + RESET + " Impossible de charger le reseau. Verifiez le fichier et reessayez.");
                sc.close();
                return;
            }

            System.out.println(VERT + "[OK]" + RESET + " Reseau charge avec succes !\n");

            // Lancer le menu partie 2
            MenuCLI menu = new MenuCLI(sc, lambda, gestionnaire);
            menu.demarrerPartie2();
            
        } else {
            // Mode manuel (partie 1)
            System.out.println(CYAN + "[INFO]" + RESET + " Mode manuel active (partie 1)");
            System.out.println(CYAN + "[INFO]" + RESET + " Pour charger un fichier : java Main <chemin_fichier> [lambda]\n");
            
            MenuCLI menu = new MenuCLI(sc, lambda);
            menu.demarrer();
        }

        sc.close();
    }

    /**
     * Affiche la banniere de l'application.
     */
    private static void afficherBanniere() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║     GESTIONNAIRE DE RESEAU ELECTRIQUE          ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
    }
}
