package up.mi.paa.exception;

/**
 * Exception levée lorsqu'une connexion n'existe pas dans le réseau.
 * 
 * <p>Cette exception est lancée quand on tente de supprimer ou modifier
 * une connexion qui n'existe pas.
 * 
 * @author Groupe 10
 * @version 1.0
 */
public class ConnexionIntrouvableException extends Exception {

    private static final long serialVersionUID = -3466750384489972582L;

    /**
     * Construit une exception avec le message spécifié.
     *
     * @param message le message décrivant l'erreur
     */
    public ConnexionIntrouvableException(String message) {
        super(message);
    }
}
