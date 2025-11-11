package up.mi.paa.exception;

/**
 * Exception lancee lorsqu'une maison n'est pas trouvee dans le reseau.
 */
public class MaisonIntrouvableException extends Exception {
    
    /**
     * Constructeur avec message d'erreur.
     * 
     * @param message Le message d'erreur
     */
    public MaisonIntrouvableException(String message) {
        super(message);
    }
}
