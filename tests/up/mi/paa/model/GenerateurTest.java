package up.mi.paa.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe Generateur :
 * - normalisation du nom (majuscule)
 * - getters/setter
 * - cohérence de equals (insensible à la casse)
 * - cohérence equals/hashCode (utile dans HashSet/HashMap)
 */
class GenerateurTest {

    @Test
    void testNomEstNormaliseEnMajuscules() {
        Generateur g = new Generateur("gen1", 100);
        assertEquals("GEN1", g.getNom());
    }

    @Test
    void testGetCapaciteEtSetCapacite() {
        Generateur g = new Generateur("GEN1", 100);
        assertEquals(100.0, g.getCapaciteMaximale(), 1e-9);

        g.setCapaciteMaximale(250.5);
        assertEquals(250.5, g.getCapaciteMaximale(), 1e-9);
    }

    @Test
    void testEquals_IgnoreLaCasse() {
        Generateur g1 = new Generateur("gen1", 50);
        Generateur g2 = new Generateur("GEN1", 999);

        assertEquals(g1, g2, "Deux générateurs avec le même nom doivent être égaux");
    }

    @Test
    void testEquals_ObjetDifferent() {
        Generateur g = new Generateur("GEN1", 100);
        assertNotEquals(g, "GEN1");
        assertNotEquals(g, null);
    }

    @Test
    void testCohérenceEqualsHashCode_DansHashSet() {
        Generateur g1 = new Generateur("gen1", 100);
        Generateur g2 = new Generateur("GEN1", 200);

        HashSet<Generateur> set = new HashSet<>();
        set.add(g1);

        // Si hashCode() n'est pas surchargé, ce test peut échouer => bug potentiel
        assertTrue(set.contains(g2));
    }
}
