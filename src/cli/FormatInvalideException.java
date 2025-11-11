package cli;

/**
 * Exception lancee lorsqu'un format d'entree est invalide.
 */
public class FormatInvalideException extends Exception {
    
    /**
     * Constructeur avec message d'erreur.
     * 
     * @param message Le message d'erreur
     */
    public FormatInvalideException(String message) {
        super(message);
    }
}
