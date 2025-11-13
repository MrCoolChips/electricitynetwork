package up.mi.paa.model;

/**
 * Enumeration des types de consommation electrique possibles pour une maison.
 * Chaque type est associe a une demande en kW.
 */
public enum TypeConsommation {
    /** Consommation basse : 10 kW */
    BASSE(10), 
    
    /** Consommation normale : 20 kW */
    NORMAL(20), 
    
    /** Consommation forte : 40 kW */
    FORTE(40);

    private final int kw;
    
    /**
     * Constructeur prive du type de consommation.
     * 
     * @param kw La demande electrique en kW
     */
    TypeConsommation(int kw) {
    	this.kw = kw; 
    }
    
    /**
     * Retourne la demande electrique associee a ce type de consommation.
     * 
     * @return La demande en kW
     */
    public int demande() {
    	return kw; 
    }
}
