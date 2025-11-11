package cli;

/**
 * Exception lancee lorsqu'une connexion existe deja pour une maison.
 */
public class ConnexionExistanteException extends Exception {
    
    /**
     * Constructeur avec message d'erreur.
     * 
     * @param message Le message d'erreur
     */
    public ConnexionExistanteException(String message) {
        super(message);
    }
}
