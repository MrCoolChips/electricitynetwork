package up.mi.paa.model;

public class Couts {
	private double coutGlobale;
	private double dispersion;
	private double surcharge;
	
	public Couts(double coutGlobale, double dispersion, double surcharge) {
		this.coutGlobale = coutGlobale;
		this.dispersion = dispersion;
		this.surcharge = surcharge;
	}
	
	public double getCoutGlobale() {
		return coutGlobale;
	}
	
	public double getDispersion() {
		return dispersion;
	}
	
	public double getSurcharge() {
		return surcharge;
	}
	
	public void setCoutGlobale(double val) {
		this.coutGlobale = val;
	}
	
	public void setDispersion(double val) {
		this.dispersion = val;
	}
	
	public void setSurcharge(double val) {
		this.surcharge = val;
	}
	
    @Override
	public String toString() {
		return String.format("%.2f", coutGlobale) + " (dispersion = " + String.format("%.2f", dispersion) + ", surcharge = " + String.format("%.2f", surcharge) + ")";
	}

}
