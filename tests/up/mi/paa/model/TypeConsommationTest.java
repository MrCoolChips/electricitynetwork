package up.mi.paa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'énumération {@link TypeConsommation}.
 * 
 * <p>Vérifie que chaque type de consommation renvoie la demande électrique
 * correcte en kilowatts (kW).
 * 
 * @author Groupe 10
 * @version 1.0
 */
@DisplayName("Tests de TypeConsommation")
class TypeConsommationTest {

    @Test
    @DisplayName("BASSE consomme 10 kW")
    void testDemandeBasse() {
        assertEquals(10, TypeConsommation.BASSE.demande());
    }

    @Test
    @DisplayName("NORMAL consomme 20 kW")
    void testDemandeNormal() {
        assertEquals(20, TypeConsommation.NORMAL.demande());
    }

    @Test
    @DisplayName("FORTE consomme 40 kW")
    void testDemandeForte() {
        assertEquals(40, TypeConsommation.FORTE.demande());
    }

    @ParameterizedTest(name = "{0} doit avoir une demande de {1} kW")
    @CsvSource({
        "BASSE, 10",
        "NORMAL, 20",
        "FORTE, 40"
    })
    @DisplayName("Chaque type a sa demande associée")
    void testDemandeParametree(String typeName, int demande) {
        TypeConsommation type = TypeConsommation.valueOf(typeName);
        assertEquals(demande, type.demande());
    }

    @ParameterizedTest
    @EnumSource(TypeConsommation.class)
    @DisplayName("Tous les types ont une demande positive")
    void testDemandePositive(TypeConsommation type) {
        assertTrue(type.demande() > 0, 
            type.name() + " doit avoir une demande positive");
    }

    @Test
    @DisplayName("L'enum contient exactement 3 valeurs")
    void testNombreDeValeurs() {
        assertEquals(3, TypeConsommation.values().length);
    }

    @Test
    @DisplayName("Les valeurs sont dans l'ordre croissant de consommation")
    void testOrdreValeurs() {
        TypeConsommation[] types = TypeConsommation.values();
        assertTrue(types[0].demande() < types[1].demande());
        assertTrue(types[1].demande() < types[2].demande());
    }
}
