package up.mi.paa;

import up.mi.paa.ui.MenuCLI;
import java.util.Scanner;

/**
 * Classe principale pour demarrer l'application de gestion du reseau electrique.
 */
public class Main {

    /**
     * Point d'entree de l'application.
     * 
     * @param args Les arguments de la ligne de commande (non utilises)
     */
    public static void main(String[] args) {
    	Scanner sc = new Scanner(System.in);
    	int lambda = 10;
    	

		if (args.length > 0) {
		    try {
		    	lambda = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Valeur lambda invalide, la valeur par défaut (10) est utilisée.");
            }
        }
    	
        MenuCLI menu = new MenuCLI(sc, lambda);
        menu.demarrer();
        sc.close();
    }
    
}
