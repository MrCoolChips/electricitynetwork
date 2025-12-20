package up.mi.paa.ui.cli;

/**
 * Interface définissant les constantes de style pour l'interface en ligne de commande.
 * Centralise les codes couleur ANSI et les caractères de formatage Unicode.
 * 
 * @author Groupe 10
 */
public interface StyleCLI {

    // =========================================================================
    //  CODES COULEUR ANSI
    // =========================================================================
    
    /** Reset - Réinitialise le style */
    String RESET  = "\033[0m";
    
    /** Vert - Pour les messages de succès */
    String VERT   = "\033[32m";
    
    /** Rouge - Pour les messages d'erreur */
    String ROUGE  = "\033[31m";
    
    /** Jaune - Pour les avertissements */
    String JAUNE  = "\033[33m";
    
    /** Cyan - Pour les informations */
    String CYAN   = "\033[36m";

    // =========================================================================
    //  CARACTERES UNICODE - CADRES DOUBLES (Bannières)
    // =========================================================================
    
    /** Coin supérieur gauche double */
    char COIN_HAUT_GAUCHE_DOUBLE = '╔';
    
    /** Coin supérieur droit double */
    char COIN_HAUT_DROIT_DOUBLE = '╗';
    
    /** Coin inférieur gauche double */
    char COIN_BAS_GAUCHE_DOUBLE = '╚';
    
    /** Coin inférieur droit double */
    char COIN_BAS_DROIT_DOUBLE = '╝';
    
    /** Ligne horizontale double */
    char LIGNE_HORIZONTALE_DOUBLE = '═';
    
    /** Ligne verticale double */
    char LIGNE_VERTICALE_DOUBLE = '║';

    // =========================================================================
    //  CARACTERES UNICODE - CADRES SIMPLES (Menus)
    // =========================================================================
    
    /** Coin supérieur gauche simple */
    char COIN_HAUT_GAUCHE = '┌';
    
    /** Coin supérieur droit simple */
    char COIN_HAUT_DROIT = '┐';
    
    /** Coin inférieur gauche simple */
    char COIN_BAS_GAUCHE = '└';
    
    /** Coin inférieur droit simple */
    char COIN_BAS_DROIT = '┘';
    
    /** Ligne horizontale simple */
    char LIGNE_HORIZONTALE = '─';
    
    /** Ligne verticale simple */
    char LIGNE_VERTICALE = '│';
    
    /** Jonction en T vers le bas */
    char JONCTION_T_BAS = '┬';
    
    /** Jonction en T vers le haut */
    char JONCTION_T_HAUT = '┴';
    
    /** Jonction gauche */
    char JONCTION_GAUCHE = '├';
    
    /** Jonction droite */
    char JONCTION_DROITE = '┤';

    // =========================================================================
    //  DIMENSIONS PAR DEFAUT
    // =========================================================================
    
    /** Largeur standard des cadres */
    int LARGEUR_CADRE = 44;
    
    /** Indentation standard */
    String INDENT = "  ";
}
