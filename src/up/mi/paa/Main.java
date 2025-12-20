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

        switch (args[0].toLowerCase()) {
            case "--help", "-h"       -> MenuCLI.afficherAide();
            case "--version", "-v"    -> System.out.println("Gestionnaire de Réseau Électrique v1.0.0\nGroupe 10 - PAA S5 2025");
            case "--gui", "-g"        -> Application.launch(ReseauElectriqueUI.class);
            default                   -> MenuCLI.lancer(args);
        }
    }
}
