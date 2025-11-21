package up.mi.paa.exception;

/**
 * Exception lancee lorsqu'un generateur n'est pas trouve dans le reseau.
 */
public class GenerateurIntrouvableException extends Exception {
    
	private static final long serialVersionUID = -6417884171757536826L;

	/**
     * Constructeur avec message d'erreur.
     * 
     * @param message Le message d'erreur
     */
    public GenerateurIntrouvableException(String message) {
        super(message);
    }
}
