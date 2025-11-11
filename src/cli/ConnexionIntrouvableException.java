package cli;

/**
 * Exception lancee lorsqu'une connexion n'existe pas dans le reseau.
 */
public class ConnexionIntrouvableException extends Exception {
    
    /**
     * Constructeur avec message d'erreur.
     * 
     * @param message Le message d'erreur
     */
    public ConnexionIntrouvableException(String message) {
        super(message);
    }
}
