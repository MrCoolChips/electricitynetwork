package up.mi.paa.exception;

/**
 * Exception levée lorsqu'un générateur n'est pas trouvé dans le réseau.
 * 
 * <p>Cette exception est lancée lors d'opérations nécessitant
 * un générateur inexistant.
 * 
 * @author Groupe 10
 * @version 1.0
 */
public class GenerateurIntrouvableException extends Exception {

    private static final long serialVersionUID = -6417884171757536826L;

    /**
     * Construit une exception avec le message spécifié.
     *
     * @param message le message décrivant l'erreur
     */
    public GenerateurIntrouvableException(String message) {
        super(message);
    }
}
