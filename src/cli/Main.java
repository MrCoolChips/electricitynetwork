package cli;

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
        GestionnaireReseau gestionnaireReseau = new GestionnaireReseau(sc);
        gestionnaireReseau.demarrer();
        sc.close();
    }
    
}
