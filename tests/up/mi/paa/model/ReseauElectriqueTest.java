package up.mi.paa.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReseauElectriqueTest {

	private ReseauElectrique re;
	
	@BeforeEach
	void beforeEach() {
		re = new ReseauElectrique();
	}
	
	@Test
	void testCreationDeReseau() {
		assertNotNull(re.getConnexions());
		assertNotNull(re.getMaisons());
		assertNotNull(re.getGenerateurs());
	}
	
	@Test
	void ajouterGenerateurTest() {
		Generateur g1 = new Generateur("G1", 40.0);
		Generateur g2 = new Generateur("G1", 40.0);
		Generateur g3 = new Generateur("G2", 60.0);
		Generateur g4 = null;
		
		re.ajouterGenerateur(g1);
		assertTrue(re.getGenerateurs().contains(g1));
		
		re.ajouterGenerateur(g2);
		assertEquals(1, re.getGenerateurs().size());
		
		re.ajouterGenerateur(g3);
		assertEquals(2, re.getGenerateurs().size());
		
		assertThrows(IllegalArgumentException.class, () -> {
			re.ajouterGenerateur(g4);
		});
	}
	
	@Test
	void ajouterMaisonTest() {
		Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
		Maison m2 = new Maison("M1", TypeConsommation.NORMAL);
		Maison m3 = new Maison("M2", TypeConsommation.FORTE);
		Maison m4 = null;
		
		re.ajouterMaison(m1);
		assertTrue(re.getMaisons().contains(m1));
		
		re.ajouterMaison(m2);
		assertEquals(1, re.getMaisons().size());
		
		re.ajouterMaison(m3);
		assertEquals(2, re.getMaisons().size());
		
		assertThrows(IllegalArgumentException.class, () -> {
			re.ajouterMaison(m4);
		});
	}
	
	@Test
	void ajouterConnexionTest() {
		Maison m1 = new Maison("M1", TypeConsommation.NORMAL); 
		Generateur g1 = new Generateur("G1", 40.0);
		Generateur g2 = new Generateur("G2", 50.0);
		
		assertThrows(IllegalArgumentException.class, () -> {
			re.ajouterConnexion(m1, null);
		});
		
		assertThrows(IllegalArgumentException.class, () -> {
			re.ajouterConnexion(null, g1);
		});
		
		re.ajouterGenerateur(g1);
		assertThrows(IllegalStateException.class, () -> {
			re.ajouterConnexion(m1, g1);
		});
		
		
		re.ajouterMaison(m1);
		assertThrows(IllegalStateException.class, () -> {
			re.ajouterConnexion(m1, g2);
		});
		
		assertDoesNotThrow(() -> {
			re.ajouterConnexion(m1, g1);	
		});
		
		assertTrue(re.getConnexions().get(g1).contains(m1));
	}
	
	
	
	
}
