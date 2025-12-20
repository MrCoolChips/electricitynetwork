package up.mi.paa.service;

import up.mi.paa.model.*;
import up.mi.paa.exception.*;

/**
 * Service de gestion du réseau électrique.
 * 
 * <p>Cette classe fournit les opérations métier pour manipuler le réseau :
 * <ul>
 *   <li>CRUD des générateurs et maisons</li>
 *   <li>Gestion des connexions</li>
 *   <li>Validation du réseau</li>
 * </ul>
 * 
 * @author Groupe 10
 * @version 1.0
 * @see ReseauElectrique
 */
public class GestionnaireReseau {

    private final ReseauElectrique reseau;

    /**
     * Construit un gestionnaire avec un réseau vide.
     */
    public GestionnaireReseau() {
        this.reseau = new ReseauElectrique();
    }

    /**
     * Construit un gestionnaire pour un réseau existant.
     *
     * @param reseau le réseau à gérer
     */
    public GestionnaireReseau(ReseauElectrique reseau) {
        this.reseau = reseau;
    }

    /**
     * Retourne le réseau électrique géré.
     *
     * @return le réseau électrique
     */
    public ReseauElectrique getReseauElectrique() {
        return reseau;
    }

    /**
     * Ajoute ou modifie un générateur.
     *
     * @param nom      le nom du générateur
     * @param capacite la capacité maximale en kW
     * @return {@code true} si modification, {@code false} si création
     */
    public boolean ajouterOuModifierGenerateur(String nom, double capacite) {
        Generateur existant = reseau.trouverGenerateur(nom);
        if (existant != null) {
            existant.setCapaciteMaximale(capacite);
            return true;
        }
        reseau.ajouterGenerateur(new Generateur(nom, capacite));
        return false;
    }

    /**
     * Ajoute ou modifie une maison.
     *
     * @param nom  le nom de la maison
     * @param type le type de consommation
     * @return {@code true} si modification, {@code false} si création
     */
    public boolean ajouterOuModifierMaison(String nom, TypeConsommation type) {
        Maison existante = reseau.trouverMaison(nom);
        if (existante != null) {
            existante.setTypeConsommation(type);
            return true;
        }
        reseau.ajouterMaison(new Maison(nom, type));
        return false;
    }

    /**
     * Crée une connexion entre une maison et un générateur.
     * 
     * <p>L'ordre des paramètres est flexible : la méthode détecte automatiquement
     * quel élément est la maison et lequel est le générateur.
     *
     * @param element1 premier élément (maison ou générateur)
     * @param element2 second élément (générateur ou maison)
     * @throws GenerateurIntrouvableException si le générateur n'existe pas
     * @throws MaisonIntrouvableException     si la maison n'existe pas
     * @throws ConnexionExistanteException    si la maison est déjà connectée
     */
    public void creerConnexion(String element1, String element2)
            throws GenerateurIntrouvableException, MaisonIntrouvableException, ConnexionExistanteException {

        Generateur generateur = reseau.trouverGenerateur(element1);
        Maison maison = reseau.trouverMaison(element2);

        if (generateur == null || maison == null) {
            generateur = reseau.trouverGenerateur(element2);
            maison = reseau.trouverMaison(element1);
        }

        if (generateur == null) {
            throw new GenerateurIntrouvableException("Générateur introuvable ! Vérifiez qu'il existe.");
        }
        if (maison == null) {
            throw new MaisonIntrouvableException("Maison introuvable ! Vérifiez qu'elle existe.");
        }
        if (reseau.maisonEstConnectee(maison)) {
            throw new ConnexionExistanteException("La maison " + maison.getNom() + " est déjà connectée !");
        }

        reseau.ajouterConnexion(maison, generateur);
    }

    /**
     * Supprime une connexion existante.
     *
     * @param element1 premier élément (maison ou générateur)
     * @param element2 second élément (générateur ou maison)
     * @throws GenerateurIntrouvableException si le générateur n'existe pas
     * @throws MaisonIntrouvableException     si la maison n'existe pas
     * @throws ConnexionIntrouvableException  si la connexion n'existe pas
     */
    public void supprimerConnexion(String element1, String element2)
            throws GenerateurIntrouvableException, MaisonIntrouvableException, ConnexionIntrouvableException {

        Generateur generateur = reseau.trouverGenerateur(element1);
        Maison maison = reseau.trouverMaison(element2);

        if (generateur == null) {
            generateur = reseau.trouverGenerateur(element2);
            maison = reseau.trouverMaison(element1);
        }

        if (generateur == null) {
            throw new GenerateurIntrouvableException("Générateur introuvable !");
        }
        if (maison == null) {
            throw new MaisonIntrouvableException("Maison introuvable !");
        }
        if (!reseau.maisonEstConnectee(maison)) {
            throw new ConnexionIntrouvableException("La maison " + maison.getNom() + " n'est pas connectée !");
        }

        Generateur generateurConnecte = reseau.trouverGenerateur(maison);
        if (!generateur.equals(generateurConnecte)) {
            throw new ConnexionIntrouvableException(
                    "La connexion entre " + maison.getNom() + " et " + generateur.getNom() + " n'existe pas ! " +
                    "La maison est connectée à " + generateurConnecte.getNom());
        }

        reseau.supprimerConnexion(maison);
    }

    /**
     * Vérifie la validité du réseau.
     *
     * @return une chaîne décrivant les problèmes (vide si valide)
     */
    public String verifierValiditeReseau() {
        StringBuilder problemes = new StringBuilder();
        int compteur = 1;

        if (reseau.getMaisons().isEmpty()) {
            problemes.append(compteur++).append(") Le réseau doit contenir au moins une maison\n");
        }
        if (reseau.getGenerateurs().isEmpty()) {
            problemes.append(compteur++).append(") Le réseau doit contenir au moins un générateur\n");
        }

        for (Maison maison : reseau.maisonsNonConnectees()) {
            problemes.append(compteur++).append(") ").append(maison.getNom()).append(" (aucune connexion)\n");
        }

        double demandeTotale = reseau.getMaisons().stream()
                .mapToDouble(Maison::getConsommation).sum();
        double capaciteTotale = reseau.getGenerateurs().stream()
                .mapToDouble(Generateur::getCapaciteMaximale).sum();

        if (demandeTotale > capaciteTotale) {
            problemes.append(compteur).append(") Demande totale (")
                    .append(String.format("%.2f", demandeTotale))
                    .append(" kW) supérieure à la capacité totale (")
                    .append(String.format("%.2f", capaciteTotale)).append(" kW)\n");
        }

        return problemes.toString();
    }

    /**
     * Modifie une connexion existante en changeant le générateur.
     *
     * @param ancienElement1 premier élément de l'ancienne connexion
     * @param ancienElement2 second élément de l'ancienne connexion
     * @param nouvelElement1 premier élément de la nouvelle connexion
     * @param nouvelElement2 second élément de la nouvelle connexion
     * @throws FormatInvalideException        si le format est incorrect
     * @throws GenerateurIntrouvableException si un générateur n'existe pas
     * @throws MaisonIntrouvableException     si une maison n'existe pas
     * @throws ConnexionIntrouvableException  si la connexion n'existe pas
     */
    public void modifierConnexion(String ancienElement1, String ancienElement2,
                                  String nouvelElement1, String nouvelElement2)
            throws FormatInvalideException, GenerateurIntrouvableException,
                   MaisonIntrouvableException, ConnexionIntrouvableException {

        Maison ancienneMaison = resolveMaison(ancienElement1, ancienElement2);
        Generateur ancienGenerateur = resolveGenerateur(ancienElement1, ancienElement2);

        if (ancienneMaison == null) {
            throw new MaisonIntrouvableException("Maison introuvable dans l'ancienne connexion");
        }
        if (ancienGenerateur == null) {
            throw new GenerateurIntrouvableException("Générateur introuvable dans l'ancienne connexion");
        }
        if (!ancienGenerateur.equals(reseau.trouverGenerateur(ancienneMaison))) {
            throw new ConnexionIntrouvableException("Cette connexion n'existe pas");
        }

        Maison nouvelleMaison = resolveMaison(nouvelElement1, nouvelElement2);
        Generateur nouvelGenerateur = resolveGenerateur(nouvelElement1, nouvelElement2);

        if (nouvelleMaison == null) {
            throw new MaisonIntrouvableException("Maison introuvable dans la nouvelle connexion");
        }
        if (nouvelGenerateur == null) {
            throw new GenerateurIntrouvableException("Générateur introuvable dans la nouvelle connexion");
        }
        if (!nouvelleMaison.equals(ancienneMaison)) {
            throw new FormatInvalideException("La maison doit rester la même");
        }

        reseau.supprimerConnexion(ancienneMaison);
        reseau.ajouterConnexion(nouvelleMaison, nouvelGenerateur);
    }

    private Maison resolveMaison(String elem1, String elem2) {
        Maison m = reseau.trouverMaison(elem1);
        return m != null ? m : reseau.trouverMaison(elem2);
    }

    private Generateur resolveGenerateur(String elem1, String elem2) {
        Generateur g = reseau.trouverGenerateur(elem1);
        return g != null ? g : reseau.trouverGenerateur(elem2);
    }
}
