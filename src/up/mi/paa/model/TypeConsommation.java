package up.mi.paa.model;

/**
 * Énumération des types de consommation électrique pour les maisons.
 * 
 * <p>Chaque type définit une demande énergétique en kilowatts (kW) :
 * <ul>
 *   <li>{@link #BASSE} - 10 kW : faible consommation</li>
 *   <li>{@link #NORMAL} - 20 kW : consommation standard</li>
 *   <li>{@link #FORTE} - 40 kW : forte consommation</li>
 * </ul>
 * 
 * @author Groupe 10
 * @version 1.0
 * @see Maison
 */
public enum TypeConsommation {
    
    /** Consommation basse : 10 kW. */
    BASSE(10),
    
    /** Consommation normale : 20 kW. */
    NORMAL(20),
    
    /** Consommation forte : 40 kW. */
    FORTE(40);

    private final int kw;

    TypeConsommation(int kw) {
        this.kw = kw;
    }

    /**
     * Retourne la demande électrique associée à ce type de consommation.
     *
     * @return la demande en kW
     */
    public int demande() {
        return kw;
    }
}

