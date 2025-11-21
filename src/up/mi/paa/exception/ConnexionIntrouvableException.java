package up.mi.paa.exception;

/**
 * Exception lancee lorsqu'une connexion n'existe pas dans le reseau.
 */
public class ConnexionIntrouvableException extends Exception {
    
	private static final long serialVersionUID = -3466750384489972582L;

	/**
     * Constructeur avec message d'erreur.
     * 
     * @param message Le message d'erreur
     */
    public ConnexionIntrouvableException(String message) {
        super(message);
    }
}
