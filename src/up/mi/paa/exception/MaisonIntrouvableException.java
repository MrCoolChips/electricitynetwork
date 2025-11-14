package up.mi.paa.exception;

/**
 * Exception lancee lorsqu'une maison n'est pas trouvee dans le reseau.
 */
public class MaisonIntrouvableException extends Exception {
    
    private static final long serialVersionUID = 2188974259994949472L;

	/**
     * Constructeur avec message d'erreur.
     * 
     * @param message Le message d'erreur
     */
    public MaisonIntrouvableException(String message) {
        super(message);
    }
}
