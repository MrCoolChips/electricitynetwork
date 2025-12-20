package up.mi.paa.exception;

/**
 * Exception levée lorsqu'une connexion existe déjà pour une maison.
 * 
 * <p>Cette exception est lancée quand on tente de connecter une maison
 * qui est déjà reliée à un générateur.
 * 
 * @author Groupe 10
 * @version 1.0
 */
public class ConnexionExistanteException extends Exception {

    private static final long serialVersionUID = 4683518919567664416L;

    /**
     * Construit une exception avec le message spécifié.
     *
     * @param message le message décrivant l'erreur
     */
    public ConnexionExistanteException(String message) {
        super(message);
    }
}
