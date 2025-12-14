package up.mi.paa.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Classe de test pour la classe ReseauElectrique.
 * Elle vérifie les fonctionnalités d'ajout, de recherche et de connexion
 * des maisons et des générateurs.
 */
public class ReseauElectriqueTest {

	private ReseauElectrique re;
	
	/**
	 * Initialisation avant chaque test.
	 * Crée une nouvelle instance de ReseauElectrique pour garantir
	 * que les tests sont indépendants les uns des autres.
	 */
	@BeforeEach
	void beforeEach() {
		re = new ReseauElectrique();
	}
	
	/**
	 * Vérifie que le réseau est correctement créé et que ses
	 * collections internes (connexions, maisons, générateurs) ne sont pas nulles.
	 */
	@Test
	void creationDeReseauTest() {
		assertNotNull(re.getConnexions());
		assertNotNull(re.getMaisons());
		assertNotNull(re.getGenerateurs());
	}
	
	/**
	 * Teste l'ajout de générateurs au réseau.
	 * Vérifie :
	 * 1. L'ajout réussi d'un générateur.
	 * 2. L'unicité (un même générateur ne doit pas être ajouté deux fois).
	 * 3. Le rejet des valeurs nulles (IllegalArgumentException).
	 */
	@Test
	void ajouterGenerateurTest() {
		Generateur g1 = new Generateur("G1", 40.0);
		Generateur g2 = new Generateur("G1", 40.0);
		Generateur g3 = new Generateur("G2", 60.0);
		Generateur g4 = null;
		
		re.ajouterGenerateur(g1);
		assertTrue(re.getGenerateurs().contains(g1));
		
		// Vérification des doublons (Set)
		re.ajouterGenerateur(g2);
		assertEquals(1, re.getGenerateurs().size());
		
		re.ajouterGenerateur(g3);
		assertEquals(2, re.getGenerateurs().size());
		
		// Vérification de l'argument null
		assertThrows(IllegalArgumentException.class, () -> {
			re.ajouterGenerateur(g4);
		});
	}
	
	/**
	 * Teste l'ajout de maisons au réseau.
	 * Vérifie l'ajout nominal, la gestion des doublons et le rejet des valeurs nulles.
	 */
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
	
	/**
	 * Teste la création de connexions entre une maison et un générateur.
	 * Vérifie :
	 * 1. Les exceptions si l'un des arguments est null (IllegalArgumentException).
	 * 2. Les exceptions si les éléments n'existent pas dans le réseau (IllegalStateException).
	 * 3. La réussite de la connexion si tout est valide.
	 */
	@Test
	void ajouterConnexionTest() {
		Maison m1 = new Maison("M1", TypeConsommation.NORMAL); 
		Generateur g1 = new Generateur("G1", 40.0);
		Generateur g2 = new Generateur("G2", 50.0);
		
		// Test des arguments nulls
		assertThrows(IllegalArgumentException.class, () -> {
			re.ajouterConnexion(m1, null);
		});
		
		assertThrows(IllegalArgumentException.class, () -> {
			re.ajouterConnexion(null, g1);
		});
		
		// Test d'ajout sans que les objets soient dans le réseau
		re.ajouterGenerateur(g1);
		assertThrows(IllegalStateException.class, () -> {
			re.ajouterConnexion(m1, g1);
		});
		
		
		re.ajouterMaison(m1);
		assertThrows(IllegalStateException.class, () -> {
			re.ajouterConnexion(m1, g2);
		});
		
		// Test du cas valide
		assertDoesNotThrow(() -> {
			re.ajouterConnexion(m1, g1);	
		});
		
		assertTrue(re.getConnexions().get(g1).contains(m1));
	}
	
	/**
	 * Teste la recherche d'un générateur.
	 * Vérifie la recherche par nom (String) et la recherche par Maison connectée.
	 */
	@Test
	void trouverGenerateurTest() {
		Generateur g1 = new Generateur("G1", 40.0);
		re.ajouterGenerateur(g1);
		assertEquals(g1, re.trouverGenerateur("G1"));
		
		Maison m1 = null;
		assertNull(re.trouverGenerateur(m1));
		
		m1 = new Maison("M1", TypeConsommation.NORMAL);
		re.ajouterMaison(m1);
		re.ajouterConnexion(m1, g1);
		assertEquals(g1, re.trouverGenerateur(m1));
	}
	
	/**
	 * Teste la recherche d'une maison par son nom.
	 */
	@Test
	void trouverMaisonTest() {
		Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
		re.ajouterMaison(m1);
		assertEquals(m1, re.trouverMaison("M1"));
	}
	
	/**
	 * Teste la récupération de la liste des maisons connectées à un générateur spécifique.
	 * Vérifie les cas où la liste est vide et où elle contient plusieurs maisons.
	 */
	@Test
	void trouverLesMaisonsDeGenerateurTest() {
		Generateur g1 = null;
		List<Maison> maisons = new ArrayList<Maison>();
		
		// Cas null ou vide
		assertIterableEquals(maisons, re.trouverLesMaisonsDeGenerateur(g1));
		assertEquals(0, re.trouverLesMaisonsDeGenerateur(g1).size());
		
		// Cas avec maisons connectées
		g1 = new Generateur("G1", 40.0);
		re.ajouterGenerateur(g1);
		Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
		Maison m2 = new Maison("M2", TypeConsommation.NORMAL);
		Maison m3 = new Maison("M3", TypeConsommation.FORTE);
		re.ajouterMaison(m1);
		re.ajouterMaison(m2);
		re.ajouterMaison(m3);
		re.ajouterConnexion(m1, g1);
		re.ajouterConnexion(m2, g1);
		re.ajouterConnexion(m3, g1);
		maisons.add(m1);
		maisons.add(m2);
		maisons.add(m3);
		
		assertIterableEquals(maisons, re.trouverLesMaisonsDeGenerateur(g1));
		assertEquals(3, re.trouverLesMaisonsDeGenerateur(g1).size());	
	}
	
	/**
	 * Vérifie si une maison est considérée comme connectée.
	 * Doit retourner faux si la maison est null ou présente mais non connectée.
	 * Doit retourner vrai si la maison est reliée à un générateur.
	 */
	@Test
	void maisonEstConnecteeTest() {
		Generateur g1 = new Generateur("G1", 40.0);
		re.ajouterGenerateur(g1);
		Maison m1 = null;
		assertFalse(re.maisonEstConnectee(m1));
		
		m1 = new Maison("M1", TypeConsommation.BASSE);
		re.ajouterMaison(m1);
		assertFalse(re.maisonEstConnectee(m1)); // Présente mais pas encore connectée
		
		re.ajouterConnexion(m1, g1);
		Maison m2 = new Maison("M2", TypeConsommation.NORMAL);
		re.ajouterMaison(m2);
		re.ajouterConnexion(m2, g1);
		Maison m3 = new Maison("M3", TypeConsommation.FORTE);
		re.ajouterMaison(m3);
		re.ajouterConnexion(m3, g1);
		
		assertTrue(re.maisonEstConnectee(m1));
		assertTrue(re.maisonEstConnectee(m2));
		assertTrue(re.maisonEstConnectee(m3));
	}
	
	/**
	 * Teste deux méthodes : maisonsNonConnectees() et toutesLesMaisonsConnectees().
	 * Vérifie que la liste des maisons non connectées diminue au fur et à mesure
	 * que l'on ajoute des connexions, jusqu'à ce que le réseau soit complet.
	 */
	@Test
	void maisonsNonConnecteesTest() {
		List<Maison> maisons = new ArrayList<>();
		assertIterableEquals(maisons, re.maisonsNonConnectees());
		assertEquals(0, re.maisonsNonConnectees().size());
		assertTrue(re.toutesLesMaisonsConnectees()); // Aucune maison = tout est connecté (trivial)
		
		Maison m1 = new Maison("M1", TypeConsommation.BASSE);
		Maison m2 = new Maison("M2", TypeConsommation.NORMAL);
		Maison m3 = new Maison("M3", TypeConsommation.FORTE);
		re.ajouterMaison(m1);
		re.ajouterMaison(m2);
		re.ajouterMaison(m3);
		maisons.add(m1);
		maisons.add(m2);
		maisons.add(m3);
		
		// Toutes les maisons sont présentes mais non connectées
		assertIterableEquals(maisons, re.maisonsNonConnectees());
		assertEquals(3, re.maisonsNonConnectees().size());
		assertFalse(re.toutesLesMaisonsConnectees());
		
		Generateur g1 = new Generateur("G1", 50.0);
		re.ajouterGenerateur(g1);
		
		// Connexion progressive
		re.ajouterConnexion(m1, g1);
		maisons.remove(m1);
		assertIterableEquals(maisons, re.maisonsNonConnectees());
		assertEquals(2, re.maisonsNonConnectees().size());
		assertFalse(re.toutesLesMaisonsConnectees());
		
		re.ajouterConnexion(m2, g1);
		maisons.remove(m2);
		re.ajouterConnexion(m3, g1);
		maisons.remove(m3);
		
		// Tout est connecté
		assertIterableEquals(maisons, re.maisonsNonConnectees());
		assertEquals(0, re.maisonsNonConnectees().size());
		assertTrue(re.toutesLesMaisonsConnectees());
	}
	
	/**
	 * Teste la suppression d'une connexion.
	 * Vérifie que la maison est bien retirée de la liste du générateur
	 * et que la méthode gère correctement les arguments nuls ou les maisons non connectées.
	 */
	@Test
	void supprimerConnexionTest() {
	    Generateur g1 = new Generateur("G1", 40.0);
	    Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
	    Maison m2 = new Maison("M2", TypeConsommation.BASSE);

	    re.ajouterGenerateur(g1);
	    re.ajouterMaison(m1);
	    re.ajouterMaison(m2);
	    re.ajouterConnexion(m1, g1);
	    re.ajouterConnexion(m2, g1);

	    assertTrue(re.getConnexions().get(g1).contains(m1));
	    assertTrue(re.getConnexions().get(g1).contains(m2));

	    re.supprimerConnexion(m1);

	    assertFalse(re.getConnexions().get(g1).contains(m1));
	    assertTrue(re.getConnexions().get(g1).contains(m2));

	    assertDoesNotThrow(() -> re.supprimerConnexion(null));
	    
	    Maison m3 = new Maison("M3", TypeConsommation.BASSE);
	    assertDoesNotThrow(() -> re.supprimerConnexion(m3));
	}	
}