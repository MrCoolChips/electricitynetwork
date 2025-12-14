package up.mi.paa.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe Maison :
 * - normalisation du nom (majuscule)
 * - cohérence de equals (insensible à la casse)
 * - cohérence equals/hashCode
 */
class MaisonTest {

    @Test
    void testNomEstNormaliseEnMajuscules() {
        Maison m = new Maison("m1", TypeConsommation.NORMAL);
        assertEquals("M1", m.getNom());
    }

    @Test
    void testEquals_IgnoreLaCasse() {
        Maison m1 = new Maison("m1", TypeConsommation.BASSE);
        Maison m2 = new Maison("M1", TypeConsommation.FORTE);
        assertEquals(m1, m2);
    }

    @Test
    void testCoherenceEqualsHashCode() {
        Maison m1 = new Maison("m1", TypeConsommation.BASSE);
        Maison m2 = new Maison("M1", TypeConsommation.FORTE);

        HashSet<Maison> set = new HashSet<>();
        set.add(m1);

        assertTrue(set.contains(m2));
    }
}
