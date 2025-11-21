package up.mi.paa.exception;

/**
 * Exception lancee lorsqu'une connexion existe deja pour une maison.
 */
public class ConnexionExistanteException extends Exception {
    
	private static final long serialVersionUID = 4683518919567664416L;

	/**
     * Constructeur avec message d'erreur.
     * 
     * @param message Le message d'erreur
     */
    public ConnexionExistanteException(String message) {
        super(message);
    }
}
