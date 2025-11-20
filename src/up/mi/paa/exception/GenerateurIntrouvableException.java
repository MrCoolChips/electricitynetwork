package up.mi.paa.exception;

/**
 * Exception lancee lorsqu'un generateur n'est pas trouve dans le reseau.
 */
public class GenerateurIntrouvableException extends Exception {
    
	/**
     * Constructeur avec message d'erreur.
     * 
     * @param message Le message d'erreur
     */
    public GenerateurIntrouvableException(String message) {
        super(message);
    }
}
