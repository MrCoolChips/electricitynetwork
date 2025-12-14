package up.mi.paa.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'énumération TypeConsommation :
 * vérifie que chaque type renvoie la demande (kW) attendue.
 */
class TypeConsommationTest {

    @ParameterizedTest
    @EnumSource(TypeConsommation.class)
    void testDemande_Basse(TypeConsommation type) {
    	int expected = 0;
  
    	switch (type.name()) {
    		case "FORTE":
    			expected = 40;
    			break;
    		case "NORMAL":
    			expected = 20;
    			break;
    		case "BASSE":
    			expected = 10;
    			break;
    	}
    	
    	
        assertEquals(expected, type.demande());
    }
}
