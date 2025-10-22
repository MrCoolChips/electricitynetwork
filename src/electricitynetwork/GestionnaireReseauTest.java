package electricitynetwork;

public class GestionnaireReseauTest {
    public static void main(String[] args) {
    	GestionnaireReseauTest grt = new GestionnaireReseauTest();
    	grt.testCalculationDeCout();
    }
    
    public void testCalculationDeCout() {
    	ReseauElectrique re = new ReseauElectrique();
    	Generateur g1 = new Generateur("G1", 60);
    	Generateur g2 = new Generateur("G2", 60);
    	re.ajouterGenerateur(g1);
    	re.ajouterGenerateur(g2);
    	
    	Maison m1 = new Maison("M1", TypeConsommation.valueOf("FORTE"));
    	Maison m2 = new Maison("M2", TypeConsommation.valueOf("FORTE"));
    	Maison m3 = new Maison("M3", TypeConsommation.valueOf("NORMAL"));
    	Maison m4 = new Maison("M4", TypeConsommation.valueOf("BASSE"));
    	re.ajouterMaison(m1);
    	re.ajouterMaison(m2);
    	re.ajouterMaison(m3);
    	re.ajouterMaison(m4);
    	
    	re.ajouterConnexion(m1, g2);
    	re.ajouterConnexion(m2, g2);
    	re.ajouterConnexion(m4, g2);
    	re.ajouterConnexion(m3, g1);
    	
    	GestionnaireReseau gr = new GestionnaireReseau(null, re);
    	gr.affichageReseau();
    	System.out.println("Le résultat de Test Disp(S) est " + gr.disps(gr.getReseauElectrique().getGenerateurs()));
    	System.out.println("Le résultat de Test Surcharge(S) est " + gr.surcharge(gr.getReseauElectrique().getGenerateurs()));
    	System.out.println("Le résultat de Test Cout(S) est " + (gr.disps(gr.getReseauElectrique().getGenerateurs()) + (10.0 * gr.surcharge(gr.getReseauElectrique().getGenerateurs()))));
    	
    	double testCout = gr.disps(gr.getReseauElectrique().getGenerateurs()) + (10.0 * gr.surcharge(gr.getReseauElectrique().getGenerateurs()));
    	double reelCout = gr.cout();
    	
    	if (testCout != reelCout) {
    		//throw new 
    	} else {
    		System.out.println("Test est passee");
    	}
    }
}
