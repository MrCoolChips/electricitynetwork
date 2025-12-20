package up.mi.paa.ui.cli;

/**
 * Classe utilitaire pour l'affichage formaté dans la console.
 * Fournit des méthodes statiques pour afficher des messages, cadres et menus.
 * Implémente le pattern Singleton implicite via méthodes statiques.
 * 
 * @author Groupe 10
 */
public final class AfficheurCLI implements StyleCLI {

    /** Constructeur privé - classe utilitaire */
    private AfficheurCLI() {}

    // =========================================================================
    //  MESSAGES FORMATÉS
    // =========================================================================

    /**
     * Affiche un message de succès en vert.
     * @param message Le message à afficher
     */
    public static void ok(String message) {
        System.out.println(VERT + "[OK]" + RESET + " " + message + "\n");
    }

    /**
     * Affiche un message d'erreur en rouge.
     * @param message Le message à afficher
     */
    public static void erreur(String message) {
        System.out.println(ROUGE + "[ERREUR]" + RESET + " " + message + "\n");
    }

    /**
     * Affiche un message d'information en cyan.
     * @param message Le message à afficher
     */
    public static void info(String message) {
        System.out.println(CYAN + "[INFO]" + RESET + " " + message + "\n");
    }

    /**
     * Affiche un message d'avertissement en jaune.
     * @param message Le message à afficher
     */
    public static void attention(String message) {
        System.out.println(JAUNE + "[ATTENTION]" + RESET + " " + message);
    }

    // =========================================================================
    //  CADRES ET BANNIÈRES
    // =========================================================================

    /**
     * Affiche une bannière avec cadre double (style titre principal).
     * @param titre Le titre à afficher
     */
    public static void banniere(String titre) {
        String ligne = String.valueOf(LIGNE_HORIZONTALE_DOUBLE).repeat(LARGEUR_CADRE);
        System.out.println();
        System.out.println(INDENT + COIN_HAUT_GAUCHE_DOUBLE + ligne + COIN_HAUT_DROIT_DOUBLE);
        System.out.println(INDENT + LIGNE_VERTICALE_DOUBLE + centrer(titre, LARGEUR_CADRE) + LIGNE_VERTICALE_DOUBLE);
        System.out.println(INDENT + COIN_BAS_GAUCHE_DOUBLE + ligne + COIN_BAS_DROIT_DOUBLE);
        System.out.println();
    }

    /**
     * Affiche un menu avec cadre simple.
     * @param lignes Les lignes du menu (première = titre, suivantes = options)
     */
    public static void menu(String[] lignes) {
        String ligne = String.valueOf(LIGNE_HORIZONTALE).repeat(LARGEUR_CADRE);
        
        System.out.println(INDENT + COIN_HAUT_GAUCHE + ligne + COIN_HAUT_DROIT);
        System.out.println(INDENT + LIGNE_VERTICALE + centrer(lignes[0], LARGEUR_CADRE) + LIGNE_VERTICALE);
        System.out.println(INDENT + JONCTION_GAUCHE + ligne + JONCTION_DROITE);
        
        for (int i = 1; i < lignes.length; i++) {
            System.out.println(INDENT + LIGNE_VERTICALE + "  " + completer(lignes[i], LARGEUR_CADRE - 2) + LIGNE_VERTICALE);
        }
        
        System.out.println(INDENT + COIN_BAS_GAUCHE + ligne + COIN_BAS_DROIT);
        System.out.print("\n" + INDENT + "> Votre choix : ");
    }

    /**
     * Affiche un titre de section avec ligne de séparation.
     * @param titre Le titre de la section
     */
    public static void section(String titre) {
        System.out.println("\n" + INDENT + titre);
        System.out.println(INDENT + String.valueOf(LIGNE_HORIZONTALE).repeat(45));
    }

    /**
     * Affiche une ligne de séparation simple.
     */
    public static void separateur() {
        System.out.println(INDENT + String.valueOf(LIGNE_HORIZONTALE).repeat(45));
    }

    // =========================================================================
    //  UTILITAIRES DE FORMATAGE
    // =========================================================================

    /**
     * Centre une chaîne dans une largeur donnée.
     * @param s La chaîne à centrer
     * @param largeur La largeur totale
     * @return La chaîne centrée avec espaces
     */
    public static String centrer(String s, int largeur) {
        int padding = (largeur - s.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + s + " ".repeat(Math.max(0, largeur - s.length() - padding));
    }

    /**
     * Complète une chaîne avec des espaces jusqu'à la largeur donnée.
     * @param s La chaîne à compléter
     * @param largeur La largeur totale
     * @return La chaîne complétée
     */
    public static String completer(String s, int largeur) {
        return s + " ".repeat(Math.max(0, largeur - s.length()));
    }

    /**
     * Formate un texte avec la couleur verte.
     * @param texte Le texte à colorer
     * @return Le texte formaté
     */
    public static String vert(String texte) {
        return VERT + texte + RESET;
    }

    /**
     * Formate un texte avec la couleur rouge.
     * @param texte Le texte à colorer
     * @return Le texte formaté
     */
    public static String rouge(String texte) {
        return ROUGE + texte + RESET;
    }

    /**
     * Formate un texte avec la couleur cyan.
     * @param texte Le texte à colorer
     * @return Le texte formaté
     */
    public static String cyan(String texte) {
        return CYAN + texte + RESET;
    }

    /**
     * Formate un texte avec la couleur jaune.
     * @param texte Le texte à colorer
     * @return Le texte formaté
     */
    public static String jaune(String texte) {
        return JAUNE + texte + RESET;
    }
}
