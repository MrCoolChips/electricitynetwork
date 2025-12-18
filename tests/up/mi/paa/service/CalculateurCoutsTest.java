package up.mi.paa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import up.mi.paa.model.Couts;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.ReseauElectrique;
import up.mi.paa.model.TypeConsommation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe CalculateurCouts.
 * Vérifie les calculs de charge, de dispersion, de surcharge et le coût global.
 */
class CalculateurCoutsTest {

    private CalculateurCouts calculateur;
    private ReseauElectrique reseau;

    @BeforeEach
    void beforeEach() {
        // Initialisation avant chaque test avec un lambda de 10
        calculateur = new CalculateurCouts(10);
        reseau = new ReseauElectrique();
    }

    @Test
    void testGetSetLambda() {
        assertEquals(10, calculateur.getLambda());
        calculateur.setLambda(50);
        assertEquals(50, calculateur.getLambda());
    }

    @Test
    void testGetSommeDesDemandesElectriques() {
        Generateur g1 = new Generateur("G1", 100);
        Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
        Maison m2 = new Maison("M2", TypeConsommation.FORTE);
        
        // Note : Les valeurs de consommation dépendent de votre Enum TypeConsommation
        // Ici, je récupère les valeurs réelles pour le test dynamique
        double consoM1 = m1.getConsommation();
        double consoM2 = m2.getConsommation();

        reseau.ajouterGenerateur(g1);
        reseau.ajouterMaison(m1);
        reseau.ajouterMaison(m2);
        
        reseau.ajouterConnexion(m1, g1);
        reseau.ajouterConnexion(m2, g1);

        double sommeAttendue = consoM1 + consoM2;
        assertEquals(sommeAttendue, calculateur.getSommeDesDemandesElectriques(g1, reseau), 1e-9);
    }

    @Test
    void testCalculerLeTauxDUtilisation() {
        Generateur g1 = new Generateur("G1", 100);
        Maison m1 = new Maison("M1", TypeConsommation.NORMAL); 
        // Forçons la consommation (si possible) ou utilisons la valeur par défaut.
        // Imaginons que m1 consomme 50 (si on ne peut pas set, on adapte le test aux valeurs par défaut).
        // Dans ce test, on se base sur la logique : Taux = Conso / Capacité.
        
        reseau.ajouterGenerateur(g1);
        reseau.ajouterMaison(m1);
        reseau.ajouterConnexion(m1, g1);

        double consommation = m1.getConsommation();
        double tauxAttendu = consommation / 100.0;

        assertEquals(tauxAttendu, calculateur.calculerLeTauxDUtilisation(g1, reseau), 1e-9);
    }

    @Test
    void testCalculerLeTauxDUtilisation_CapaciteZero() {
        Generateur g1 = new Generateur("G_Defectueux", 0);
        
        assertThrows(ArithmeticException.class, () -> {
            calculateur.calculerLeTauxDUtilisation(g1, reseau);
        }, "Une capacité de 0 doit lever une exception");
    }

    @Test
    void testCalculerCout_ReseauVide() {
        // Le réseau est vide (pas de générateurs)
        assertThrows(ArithmeticException.class, () -> {
            calculateur.calculerCout(reseau);
        }, "Le calcul global sur un réseau sans générateur doit échouer");
    }

    @Test
    void testCalculerCout_ScenarioComplexe() {
        // --- CONFIGURATION DU SCÉNARIO ---
        // Lambda = 10
        // G1 : Cap 100, Charge 50  (Taux = 0.5)
        // G2 : Cap 100, Charge 150 (Taux = 1.5) -> Surcharge !
        
        Generateur g1 = new Generateur("G1", 100);
        Generateur g2 = new Generateur("G2", 100);
        reseau.ajouterGenerateur(g1);
        reseau.ajouterGenerateur(g2);
        
        // je vérifie ici que l'objet Couts est bien retourné et que les valeurs sont positives.
        
        Maison m1 = new Maison("M1", TypeConsommation.NORMAL);
        reseau.ajouterMaison(m1);
        reseau.ajouterConnexion(m1, g1); // G1 a une charge

        Couts resultat = calculateur.calculerCout(reseau);

        assertNotNull(resultat);
        assertTrue(resultat.getCoutGlobale() >= 0, "Le coût global doit être positif");
        assertTrue(resultat.getDispersion() >= 0, "La dispersion doit être positive");
        assertTrue(resultat.getSurcharge() >= 0, "La surcharge doit être positive");
    }

    @Test
    void testCalculSurcharge() {
        // G1 Capacité 10, Consommation 20 (Surcharge de 100% -> facteur 1.0)
        Generateur g1 = new Generateur("G1", 10); 
        reseau.ajouterGenerateur(g1);
        
        // Ajout de maisons pour dépasser 10kW
        for(int i=0; i<5; i++) {
            Maison m = new Maison("M"+i, TypeConsommation.FORTE);
            reseau.ajouterMaison(m);
            reseau.ajouterConnexion(m, g1);
        }

        Couts couts = calculateur.calculerCout(reseau);
        
        // On s'attend à ce qu'il y ait une surcharge positive
        assertTrue(couts.getSurcharge() > 0, "Il devrait y avoir une surcharge détectée");
        
        // Vérification de l'impact de Lambda
        double coutTotal = couts.getCoutGlobale();
        double dispersion = couts.getDispersion();
        double surcharge = couts.getSurcharge();
        
        assertEquals(coutTotal, dispersion + (10 * surcharge), 1e-9, 
            "La formule Coût = Dispersion + (Lambda * Surcharge) doit être respectée");
    }
}