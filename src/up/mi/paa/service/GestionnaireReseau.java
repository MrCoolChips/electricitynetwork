package up.mi.paa.service;

import up.mi.paa.model.*;
import up.mi.paa.exception.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe gerant la logique metier du reseau electrique.
 * Gere les operations CRUD et les calculs de cout, surcharge et dispersion.
 */
public class GestionnaireReseau {

    private ReseauElectrique re;
    
    /**
     * Constructeur avec un reseau electrique vide.
     */
    public GestionnaireReseau() {
        re = new ReseauElectrique();
    }
    
    /**
     * Constructeur avec un reseau electrique existant.
     * 
     * @param re Le reseau electrique a gerer
     */
    public GestionnaireReseau(ReseauElectrique re) {
        this.re = re;
    }
    
    /**
     * Retourne le reseau electrique gere.
     * 
     * @return Le reseau electrique
     */
    public ReseauElectrique getReseauElectrique() {
        return re;
    }

    /**
     * Ajoute ou modifie un generateur dans le reseau.
     * 
     * @param nom Le nom du generateur
     * @param capacite La capacite maximale
     * @return true si le generateur existait deja, false sinon
     */
    public boolean ajouterOuModifierGenerateur(String nom, double capacite) {
        Generateur existant = re.trouverGenerateur(nom);
        
        if(existant != null) {
            existant.setCapaciteMaximale(capacite);
            return true;
        } else {
            Generateur nouveau = new Generateur(nom, capacite);
            re.ajouterGenerateur(nouveau);
            return false;
        }
    }

    /**
     * Ajoute ou modifie une maison dans le reseau.
     * 
     * @param nom Le nom de la maison
     * @param type Le type de consommation
     * @return true si la maison existait deja, false sinon
     */
    public boolean ajouterOuModifierMaison(String nom, TypeConsommation type) {
        Maison existante = re.trouverMaison(nom);
        
        if(existante != null) {
            existante.setTypeConsommation(type);
            return true;
        } else {
            Maison nouvelle = new Maison(nom, type);
            re.ajouterMaison(nouvelle);
            return false;
        }
    }

    /**
     * Cree une connexion entre une maison et un generateur.
     * 
     * @param element1 Premier element (maison ou generateur)
     * @param element2 Second element (generateur ou maison)
     * @throws FormatInvalideException si les elements ne sont pas valides
     * @throws GenerateurIntrouvableException si le generateur n'existe pas
     * @throws MaisonIntrouvableException si la maison n'existe pas
     * @throws ConnexionExistanteException si la maison est deja connectee
     */
    public void creerConnexion(String element1, String element2) 
            throws FormatInvalideException, GenerateurIntrouvableException, 
                   MaisonIntrouvableException, ConnexionExistanteException {
        
        Generateur generateur = re.trouverGenerateur(element1);
        Maison maison = re.trouverMaison(element2);

        if (generateur == null || maison == null) {
            generateur = re.trouverGenerateur(element2);
            maison = re.trouverMaison(element1);
        }

        if (generateur == null) {
            throw new GenerateurIntrouvableException("Generateur introuvable ! Verifiez que vous l'avez cree avant.");
        }
        
        if (maison == null) {
            throw new MaisonIntrouvableException("Maison introuvable ! Verifiez que vous l'avez creee avant.");
        }

        if (re.maisonEstConnectee(maison)) {
            throw new ConnexionExistanteException("La maison " + maison.getNom() + " est deja connectee !");
        }

        re.ajouterConnexion(maison, generateur);
    }

    /**
     * Supprime une connexion existante.
     * 
     * @param element1 Premier element (maison ou generateur)
     * @param element2 Second element (generateur ou maison)
     * @throws FormatInvalideException si les elements ne sont pas valides
     * @throws GenerateurIntrouvableException si le generateur n'existe pas
     * @throws MaisonIntrouvableException si la maison n'existe pas
     * @throws ConnexionIntrouvableException si la connexion n'existe pas
     */
    public void supprimerConnexion(String element1, String element2) 
            throws FormatInvalideException, GenerateurIntrouvableException, 
                   MaisonIntrouvableException, ConnexionIntrouvableException {
        
        Generateur generateur = re.trouverGenerateur(element1);
        Maison maison = re.trouverMaison(element2);

        if (generateur == null) {
            generateur = re.trouverGenerateur(element2);
            maison = re.trouverMaison(element1);
        }

        if (generateur == null) {
            throw new GenerateurIntrouvableException("Generateur introuvable !");
        }
        
        if (maison == null) {
            throw new MaisonIntrouvableException("Maison introuvable !");
        }

        if (!re.maisonEstConnectee(maison)) {
            throw new ConnexionIntrouvableException("La maison " + maison.getNom() + " n'est pas connectee !");
        }

        Generateur generateurConnecte = re.trouverGenerateur(maison);
        if (!generateur.equals(generateurConnecte)) {
            throw new ConnexionIntrouvableException(
                "La connexion entre " + maison.getNom() + " et " + generateur.getNom() + " n'existe pas ! " +
                "La maison " + maison.getNom() + " est connectee a " + generateurConnecte.getNom()
            );
        }

        re.supprimerConnexion(maison);
    }

    /**
     * Verifie la validite du reseau.
     * 
     * @return Un tableau de messages d'erreur (vide si valide)
     */
    public String[] verifierValiditeReseau() {
        List<String> problemes = new ArrayList<>();
    
        // Vérifier qu'il y a au moins une maison et un générateur
        if (re.getMaisons().isEmpty()) {
            problemes.add("Le reseau doit contenir au moins une maison");
        }
        
        if (re.getGenerateurs().isEmpty()) {
            problemes.add("Le reseau doit contenir au moins un generateur");
        }
        
        // Vérifier que toutes les maisons sont connectées
        for (Maison maison : re.maisonsNonConnectees()) {
        	problemes.add(maison.getNom() + " (aucune connexion)");
        }
        
        double demandeTotale = 0.0;
        double capaciteTotale = 0.0;
        
        for (Maison maison : re.getMaisons()) {
            demandeTotale += maison.getConsommation();
        }
        
        for (Generateur generateur : re.getGenerateurs()) {
            capaciteTotale += generateur.getCapaciteMaximale();
        }
        
        if (demandeTotale > capaciteTotale) {
            problemes.add("Demande totale (" + String.format("%.2f", demandeTotale) + " kW) superieure a la capacite totale (" + String.format("%.2f", capaciteTotale) + " kW)");
        }
        
        return problemes.toArray(new String[0]);
    }
    
    /**
     * Modifie une connexion existante en changeant le generateur connecte a une maison.
     * 
     * @param ancienElement1 Premier element de l'ancienne connexion
     * @param ancienElement2 Second element de l'ancienne connexion
     * @param nouvelElement1 Premier element de la nouvelle connexion
     * @param nouvelElement2 Second element de la nouvelle connexion
     * @throws FormatInvalideException si le format d'entree est incorrect
     * @throws GenerateurIntrouvableException si un generateur n'existe pas
     * @throws MaisonIntrouvableException si une maison n'existe pas
     * @throws ConnexionIntrouvableException si la connexion a modifier n'existe pas
     */
    public void modifierConnexion(String ancienElement1, String ancienElement2,
                                   String nouvelElement1, String nouvelElement2) 
            throws FormatInvalideException, GenerateurIntrouvableException, 
                   MaisonIntrouvableException, ConnexionIntrouvableException {
        
        // Ancienne connexion
        Maison ancienneMaison = re.trouverMaison(ancienElement1);
        Generateur ancienGenerateur = re.trouverGenerateur(ancienElement2);
        
        if (ancienneMaison == null) {
            ancienneMaison = re.trouverMaison(ancienElement2);
        }
        
        if (ancienGenerateur == null) {
            ancienGenerateur = re.trouverGenerateur(ancienElement1);
        }
        
        if (ancienneMaison == null) {
            throw new MaisonIntrouvableException("Maison introuvable dans l'ancienne connexion");
        }
        
        if (ancienGenerateur == null) {
            throw new GenerateurIntrouvableException("Generateur introuvable dans l'ancienne connexion");
        }
        
        if (!ancienGenerateur.equals(re.trouverGenerateur(ancienneMaison))) {
            throw new ConnexionIntrouvableException("Cette connexion n'existe pas");
        }

        Maison nouvelleMaison = re.trouverMaison(nouvelElement1);
        Generateur nouvelGenerateur = re.trouverGenerateur(nouvelElement2);
        
        if (nouvelleMaison == null) {
            nouvelleMaison = re.trouverMaison(nouvelElement2);
        }
        
        if (nouvelGenerateur == null) {
            nouvelGenerateur = re.trouverGenerateur(nouvelElement1);
        }
        
        if (nouvelleMaison == null) { 
            throw new MaisonIntrouvableException("Maison introuvable dans la nouvelle connexion");
        }
        
        if (nouvelGenerateur == null) { 
            throw new GenerateurIntrouvableException("Generateur introuvable dans la nouvelle connexion");
        }
        
        if (!nouvelleMaison.equals(ancienneMaison)) {
            throw new FormatInvalideException("La maison doit rester la meme"); 
        }
        
        re.supprimerConnexion(ancienneMaison);
        re.ajouterConnexion(nouvelleMaison, nouvelGenerateur);
    }

}
