package cli;

public enum TypeConsommation {
    BASSE(10), NORMAL(20), FORTE(40);

    private final int kw;
    
    TypeConsommation(int kw) {
    	this.kw = kw; 
    }
    
    public int demand() {
    	return kw; 
    }
}
