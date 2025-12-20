package up.mi.paa.exception;

/**
 * Exception levée lorsqu'un format d'entrée est invalide.
 * 
 * <p>Cette exception est lancée lors du parsing de fichiers
 * ou de saisies utilisateur mal formatées.
 * 
 * @author Groupe 10
 * @version 1.0
 */
public class FormatInvalideException extends Exception {

    private static final long serialVersionUID = -723072652945242612L;

    /**
     * Construit une exception avec le message spécifié.
     *
     * @param message le message décrivant l'erreur
     */
    public FormatInvalideException(String message) {
        super(message);
    }
}
