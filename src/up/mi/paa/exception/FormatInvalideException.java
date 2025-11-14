package up.mi.paa.exception;

/**
 * Exception lancee lorsqu'un format d'entree est invalide.
 */
public class FormatInvalideException extends Exception {
    
    private static final long serialVersionUID = -723072652945242612L;

	/**
     * Constructeur avec message d'erreur.
     * 
     * @param message Le message d'erreur
     */
    public FormatInvalideException(String message) {
        super(message);
    }
}
