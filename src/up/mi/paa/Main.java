package up.mi.paa;

import up.mi.paa.ui.cli.MenuCLI;
import up.mi.paa.ui.gui.ReseauElectriqueUI;
import javafx.application.Application;

/**
 * Point d'entrée de l'application.
 * @author Groupe 10
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) { MenuCLI.lancer(args); return; }

        String arg0 = args[0].toLowerCase();
        
        if (arg0.startsWith("-")) {
            switch (arg0) {
                case "--help", "-h"       -> MenuCLI.afficherAide();
                case "--version", "-v"    -> System.out.println("Gestionnaire de Réseau Électrique v1.0.0\nGroupe 10 - PAA S5 2025");
                case "--gui", "-g"        -> Application.launch(ReseauElectriqueUI.class);
                case "--cli", "-c"        -> MenuCLI.lancer(java.util.Arrays.copyOfRange(args, 1, args.length));
                default -> {
                    System.err.println("Option invalide : " + args[0]);
                    MenuCLI.afficherAide();
                }
            }
        } else {
            MenuCLI.lancer(args);
        }
    }
}
