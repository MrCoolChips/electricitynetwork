package up.mi.paa.ui.cli;

import up.mi.paa.io.GestionnaireFichier;
import up.mi.paa.service.CalculateurCouts;
import up.mi.paa.service.GestionnaireReseau;
import up.mi.paa.service.OptimiseurReseau;
import up.mi.paa.model.*;
import up.mi.paa.exception.*;
import up.mi.paa.util.ComparateurNaturel;

import static up.mi.paa.ui.cli.AfficheurCLI.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Interface utilisateur en ligne de commande (CLI) pour le gestionnaire de réseau électrique.
 * Gère les menus, les entrées utilisateur et coordonne les opérations avec le GestionnaireReseau.
 * 
 * <p>Cette classe délègue l'affichage à {@link AfficheurCLI} et utilise les constantes
 * de style définies dans {@link StyleCLI}.
 * 
 * @author Groupe 10
 * @see AfficheurCLI
 * @see StyleCLI
 */
public class MenuCLI implements StyleCLI {

    private final Scanner sc;
    private final GestionnaireReseau gestionnaire;
    private final CalculateurCouts calculateur;
    private final OptimiseurReseau optimiseur;
    private final String fichierSource;

    // =========================================================================
    //  DÉFINITIONS DES MENUS
    // =========================================================================

    private static final String[] MENU_PRINCIPAL = {
        "MENU PRINCIPAL",
        "1 | Ajouter un generateur",
        "2 | Ajouter une maison",
        "3 | Ajouter une connexion",
        "4 | Supprimer une connexion",
        "5 | Fin"
    };

    private static final String[] MENU_EVALUATION = {
        "EVALUATION DU RESEAU",
        "1 | Calculer le cout du reseau",
        "2 | Modifier une connexion",
        "3 | Afficher le reseau",
        "4 | Fin"
    };

    private static final String[] MENU_PARTIE2 = {
        "MENU PARTIE 2",
        "1 | Resolution automatique",
        "2 | Sauvegarder la solution",
        "3 | Fin"
    };

    // =========================================================================
    //  CONSTRUCTEURS
    // =========================================================================

    /**
     * Constructeur pour le mode manuel (Partie 1).
     * 
     * @param sc     Scanner pour les entrées utilisateur
     * @param lambda Coefficient de pénalisation de la surcharge
     */
    public MenuCLI(Scanner sc, int lambda) {
        this.sc = sc;
        this.gestionnaire = new GestionnaireReseau();
        this.calculateur = new CalculateurCouts(lambda);
        this.optimiseur = new OptimiseurReseau(calculateur);
        this.fichierSource = null;
    }

    /**
     * Constructeur pour le mode fichier (Partie 2).
     * 
     * @param sc            Scanner pour les entrées utilisateur
     * @param lambda        Coefficient de pénalisation de la surcharge
     * @param gestionnaire  Gestionnaire pré-chargé depuis un fichier
     * @param fichierSource Chemin du fichier source (pour éviter l'écrasement)
     */
    public MenuCLI(Scanner sc, int lambda, GestionnaireReseau gestionnaire, String fichierSource) {
        this.sc = sc;
        this.gestionnaire = gestionnaire;
        this.calculateur = new CalculateurCouts(lambda);
        this.optimiseur = new OptimiseurReseau(calculateur);
        this.fichierSource = fichierSource;
    }

    // =========================================================================
    //  POINTS D'ENTRÉE
    // =========================================================================

    /**
     * Démarre l'application en mode manuel (Partie 1).
     */
    public void demarrer() {
        banniere("GESTIONNAIRE DE RESEAU ELECTRIQUE");
        
        while (true) {
            menu(MENU_PRINCIPAL);
            int choix = lireChoix();
            
            if (choix == 5) {
                if (verifierReseau()) break;
            } else {
                traiterChoixPrincipal(choix);
            }
        }
        menuEvaluation();
    }

    /**
     * Démarre l'application en mode fichier (Partie 2).
     */
    public void demarrerPartie2() {
        afficherReseau();
        
        while (true) {
            menu(MENU_PARTIE2);
            int choix = lireChoix();
            
            switch (choix) {
                case 1: resolutionAutomatique(); break;
                case 2: sauvegarderSolution();   break;
                case 3: 
                    info("Au revoir !");
                    return;
                default: 
                    erreur("Choix invalide ! Veuillez choisir entre 1 et 3.");
            }
        }
    }

    // =========================================================================
    //  SAISIE UTILISATEUR
    // =========================================================================

    private int lireChoix() {
        while (!sc.hasNextInt()) {
            sc.nextLine();
            erreur("Entrée invalide ! Veuillez saisir un nombre.");
            System.out.print("> Votre choix : ");
        }
        int choix = sc.nextInt();
        sc.nextLine();
        return choix;
    }

    private String[] lireDeuxElements(String invite) throws FormatInvalideException {
        System.out.print(invite);
        String[] parts = sc.nextLine().trim().split("\\s+");
        if (parts.length != 2) {
            throw new FormatInvalideException("Le format doit être: ELEMENT1 ELEMENT2");
        }
        return new String[] { parts[0].toUpperCase(), parts[1].toUpperCase() };
    }

    // =========================================================================
    //  TRAITEMENT DES CHOIX - MENU PRINCIPAL
    // =========================================================================

    private void traiterChoixPrincipal(int choix) {
        switch (choix) {
            case 1: ajouterGenerateur();   break;
            case 2: ajouterMaison();       break;
            case 3: ajouterConnexion();    break;
            case 4: supprimerConnexion();  break;
            default: erreur("Choix invalide ! Veuillez choisir entre 1 et 5.");
        }
    }

    private void ajouterGenerateur() {
        System.out.println("\n--- AJOUTER UN GENERATEUR ---");
        try {
            String[] parts = lireDeuxElements("> Nom et capacité (ex: G1 60) : ");
            double capacite = Double.parseDouble(parts[1]);
            
            if (capacite <= 0) throw new FormatInvalideException("La capacité doit être positive");
            
            boolean existe = gestionnaire.ajouterOuModifierGenerateur(parts[0], capacite);
            ok("Générateur " + parts[0] + (existe ? " mis à jour !" : " créé !"));
            
        } catch (NumberFormatException e) {
            erreur("La capacité doit être un nombre valide");
        } catch (FormatInvalideException e) {
            erreur(e.getMessage());
        }
    }

    private void ajouterMaison() {
        System.out.println("\n--- AJOUTER UNE MAISON ---");
        System.out.println("Types de consommation: BASSE, NORMAL, FORTE");
        try {
            String[] parts = lireDeuxElements("> Nom et consommation (ex: M1 FORTE) : ");
            TypeConsommation type = TypeConsommation.valueOf(parts[1]);
            
            boolean existe = gestionnaire.ajouterOuModifierMaison(parts[0], type);
            ok("Maison " + parts[0] + (existe ? " mise à jour !" : " créée !"));
            
        } catch (IllegalArgumentException e) {
            erreur("Type invalide ! Utilisez: BASSE, NORMAL ou FORTE");
        } catch (FormatInvalideException e) {
            erreur(e.getMessage());
        }
    }

    private void ajouterConnexion() {
        System.out.println("\n--- AJOUTER UNE CONNEXION ---");
        try {
            String[] parts = lireDeuxElements("> Générateur et maison (ex: G1 M1) : ");
            gestionnaire.creerConnexion(parts[0], parts[1]);
            ok("Connexion créée !");
        } catch (FormatInvalideException | GenerateurIntrouvableException |
                 MaisonIntrouvableException | ConnexionExistanteException e) {
            erreur(e.getMessage());
        }
    }

    private void supprimerConnexion() {
        System.out.println("\n--- SUPPRIMER UNE CONNEXION ---");
        try {
            String[] parts = lireDeuxElements("> Générateur et maison (ex: G1 M1) : ");
            gestionnaire.supprimerConnexion(parts[0], parts[1]);
            ok("Connexion supprimée !");
        } catch (FormatInvalideException | GenerateurIntrouvableException |
                 MaisonIntrouvableException | ConnexionIntrouvableException e) {
            erreur(e.getMessage());
        }
    }

    // =========================================================================
    //  VERIFICATION ET EVALUATION
    // =========================================================================

    private boolean verifierReseau() {
        banniere("VERIFICATION DU RESEAU");
        
        String problemes = gestionnaire.verifierValiditeReseau();
        
        if (problemes.isEmpty()) {
            ok("Réseau valide ! Chaque maison est connectée à exactement un générateur.");
            return true;
        }
        
        attention("Problèmes détectés :");
        System.out.println(problemes);
        System.out.println("\nCorrigez ces problèmes avant de terminer !\n");
        return false;
    }

    private void menuEvaluation() {
        while (true) {
            menu(MENU_EVALUATION);
            int choix = lireChoix();
            
            switch (choix) {
                case 1: calculerCout();       break;
                case 2: modifierConnexion();  break;
                case 3: afficherReseau();     break;
                case 4: verifierReseau();     return;
                default: erreur("Choix invalide ! Veuillez choisir entre 1 et 4.");
            }
        }
    }

    private void calculerCout() {
        try {
            Couts cout = calculateur.calculerCout(gestionnaire.getReseauElectrique());
            System.out.println("\n" + cyan("Coût du réseau : " + cout) + "\n");
        } catch (ArithmeticException e) {
            erreur("Impossible de calculer le coût : " + e.getMessage());
        }
    }

    private void modifierConnexion() {
        System.out.println("\n--- MODIFIER UNE CONNEXION ---");
        try {
            String[] ancienne = lireDeuxElements("> Ancienne connexion (ex: M1 G1) : ");
            String[] nouvelle = lireDeuxElements("> Nouvelle connexion (ex: M1 G2) : ");
            
            gestionnaire.modifierConnexion(ancienne[0], ancienne[1], nouvelle[0], nouvelle[1]);
            ok("Modification réussie !");
            afficherReseau();
        } catch (FormatInvalideException | GenerateurIntrouvableException |
                 MaisonIntrouvableException | ConnexionIntrouvableException e) {
            erreur(e.getMessage());
        }
    }

    // =========================================================================
    //  AFFICHAGE DU RÉSEAU
    // =========================================================================

    private void afficherReseau() {
        ReseauElectrique reseau = gestionnaire.getReseauElectrique();
        
        banniere("ETAT DU RESEAU");

        // Trier les éléments
        List<Maison> maisonsTries = new ArrayList<>(reseau.getMaisons());
        maisonsTries.sort(ComparateurNaturel.de(Maison::getNom));

        List<Generateur> generateursTries = new ArrayList<>(reseau.getGenerateurs());
        generateursTries.sort(ComparateurNaturel.de(Generateur::getNom));

        // Afficher les maisons
        section("MAISONS (" + maisonsTries.size() + ")");
        for (Maison m : maisonsTries) {
            String type = formaterType(m.getTypeConsommation());
            System.out.printf("    %s : %d kW (%s)%n", m.getNom(), m.getConsommation(), type);
        }

        // Afficher les générateurs avec statut
        section("GENERATEURS (" + generateursTries.size() + ")");
        for (Generateur g : generateursTries) {
            double usage = calculateur.getSommeDesDemandesElectriques(g, reseau);
            boolean surcharge = usage > g.getCapaciteMaximale();
            String statut = surcharge ? rouge("SURCHARGE") : vert("OK");
            System.out.printf("    %s : %.0f/%.0f kW [%s]%n", g.getNom(), usage, g.getCapaciteMaximale(), statut);
        }

        // Afficher les connexions
        section("CONNEXIONS");
        for (Generateur g : generateursTries) {
            List<Maison> maisonsGen = reseau.trouverLesMaisonsDeGenerateur(g);
            if (maisonsGen != null && !maisonsGen.isEmpty()) {
                List<Maison> maisonsTriees = new ArrayList<>(maisonsGen);
                maisonsTriees.sort(ComparateurNaturel.de(Maison::getNom));
                
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < maisonsTriees.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(maisonsTriees.get(i).getNom());
                }
                System.out.printf("    %s -> %s%n", g.getNom(), sb.toString());
            }
        }
        System.out.println();
    }

    private String formaterType(TypeConsommation type) {
        switch (type) {
            case BASSE:  return "Basse";
            case NORMAL: return "Normal";
            case FORTE:  return "Forte";
            default:     return type.name();
        }
    }

    // =========================================================================
    //  PARTIE 2 - OPTIMISATION
    // =========================================================================

    private void resolutionAutomatique() {
        banniere("RESOLUTION AUTOMATIQUE");

        ReseauElectrique reseau = gestionnaire.getReseauElectrique();

        Map<Maison, Generateur> connexionsAvant = sauvegarderConnexions(reseau);
        Couts ancienCout = calculateur.calculerCout(reseau);
        
        section("AVANT optimisation");
        afficherCoutsFormate(ancienCout);

        System.out.println("\n  Optimisation en cours...");

        optimiseur.optimiser(reseau);

        Couts nouveauCout = calculateur.calculerCout(reseau);
        section("APRES optimisation");
        afficherCoutsFormate(nouveauCout);

        double amelioration = ancienCout.getCoutGlobale() - nouveauCout.getCoutGlobale();
        System.out.println();
        if (amelioration > 0) {
            System.out.println("  " + vert("[+] Gain : -" + String.format("%.2f", amelioration)));
        } else {
            System.out.println("  " + cyan("[=] Le reseau etait deja optimal"));
        }

        afficherModifications(reseau, connexionsAvant);
    }

    private void afficherCoutsFormate(Couts c) {
        System.out.printf("    %-12s : %.4f%n", "Cout global", c.getCoutGlobale());
        System.out.printf("    %-12s : %.4f%n", "Dispersion", c.getDispersion());
        System.out.printf("    %-12s : %.4f%n", "Surcharge", c.getSurcharge());
    }

    private Map<Maison, Generateur> sauvegarderConnexions(ReseauElectrique reseau) {
        Map<Maison, Generateur> map = new HashMap<>();
        for (Generateur g : reseau.getGenerateurs()) {
            List<Maison> maisons = reseau.trouverLesMaisonsDeGenerateur(g);
            if (maisons != null) {
                for (Maison m : maisons) {
                    map.put(m, g);
                }
            }
        }
        return map;
    }

    private void afficherModifications(ReseauElectrique reseau, Map<Maison, Generateur> avant) {
        List<String[]> modifications = new ArrayList<>();
        
        for (Generateur g : reseau.getGenerateurs()) {
            List<Maison> maisonsApres = reseau.trouverLesMaisonsDeGenerateur(g);
            if (maisonsApres != null) {
                for (Maison m : maisonsApres) {
                    Generateur ancienGen = avant.get(m);
                    if (ancienGen != null && !ancienGen.equals(g)) {
                        modifications.add(new String[]{m.getNom(), ancienGen.getNom(), g.getNom()});
                    }
                }
            }
        }

        section("CONNEXIONS MODIFIEES");

        if (modifications.isEmpty()) {
            System.out.println("    Aucune modification");
        } else {
            modifications.sort(ComparateurNaturel.de(m -> m[0]));

            int largeurMax = modifications.stream().mapToInt(m -> m[0].length()).max().orElse(5);

            System.out.println("    " + modifications.size() + " modification(s) :");
            System.out.println();
            for (String[] mod : modifications) {
                String nomFormate = String.format("%-" + largeurMax + "s", mod[0]);
                System.out.printf("      %s : %s -> %s%n", nomFormate, mod[1], mod[2]);
            }
        }
        System.out.println();
    }

    // =========================================================================
    //  SAUVEGARDE
    // =========================================================================

    private void sauvegarderSolution() {
        System.out.println("\n--- SAUVEGARDER LA SOLUTION ---");
        
        String nomFichier;
        File fichier;
        
        while (true) {
            System.out.print("> Nom du fichier : ");
            nomFichier = sc.nextLine().trim();

            if (nomFichier.isEmpty()) {
                erreur("Le nom du fichier ne peut pas être vide.");
                continue;
            }

            if (!nomFichier.endsWith(".txt")) {
                nomFichier += ".txt";
            }

            fichier = new File(nomFichier);

            // Vérifier si c'est le même fichier que le fichier source
            if (fichierSource != null) {
                boolean memeFichier = false;
                try {
                    File source = new File(fichierSource);
                    memeFichier = fichier.getCanonicalPath().equals(source.getCanonicalPath());
                } catch (Exception e) {
                    memeFichier = fichier.getName().equalsIgnoreCase(new File(fichierSource).getName());
                }
                
                if (memeFichier) {
                    erreur("Impossible de sauvegarder sous le meme nom que le fichier source !");
                    continue;
                }
            }

            // Vérifier si le fichier existe déjà
            if (fichier.exists()) {
                erreur("Le fichier '" + nomFichier + "' existe deja ! Choisissez un autre nom.");
                continue;
            }
            
            break;
        }

        GestionnaireFichier.ecrireFichierReseau(fichier, gestionnaire.getReseauElectrique());
        ok("Solution sauvegardée dans '" + nomFichier + "' !");
    }
}
