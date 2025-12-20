package up.mi.paa.exception;

/**
 * Exception levée lorsqu'une maison n'est pas trouvée dans le réseau.
 * 
 * <p>Cette exception est lancée lors d'opérations nécessitant
 * une maison inexistante.
 * 
 * @author Groupe 10
 * @version 1.0
 */
public class MaisonIntrouvableException extends Exception {

    private static final long serialVersionUID = 2188974259994949412L;

    /**
     * Construit une exception avec le message spécifié.
     *
     * @param message le message décrivant l'erreur
     */
    public MaisonIntrouvableException(String message) {
        super(message);
    }
}
