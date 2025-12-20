package up.mi.paa.ui.cli;

import up.mi.paa.io.GestionnaireFichier;
import up.mi.paa.service.CalculateurCouts;
import up.mi.paa.service.GestionnaireReseau;
import up.mi.paa.service.OptimiseurReseau;
import up.mi.paa.model.*;
import up.mi.paa.exception.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Interface utilisateur en ligne de commande (CLI) pour le gestionnaire de réseau électrique.
 * Gère les menus, les entrées utilisateur et coordonne les opérations avec le GestionnaireReseau.
 * 
 * @author Groupe 10
 */
public class MenuCLI {

    private static final String RESET  = "\033[0m";
    private static final String VERT   = "\033[32m";
    private static final String ROUGE  = "\033[31m";
    private static final String JAUNE  = "\033[33m";
    private static final String CYAN   = "\033[36m";

    private final Scanner sc;
    private final GestionnaireReseau gestionnaire;
    private final CalculateurCouts calculateur;
    private final OptimiseurReseau optimiseur;
    private String fichierSource = null;

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
    }

    /**
     * Constructeur pour le mode fichier (Partie 2).
     * 
     * @param sc          Scanner pour les entrées utilisateur
     * @param lambda      Coefficient de pénalisation de la surcharge
     * @param gestionnaire Gestionnaire pré-chargé depuis un fichier
     * @param fichierSource Chemin du fichier source (pour éviter l'écrasement)
     */
    public MenuCLI(Scanner sc, int lambda, GestionnaireReseau gestionnaire, String fichierSource) {
        this.sc = sc;
        this.gestionnaire = gestionnaire;
        this.calculateur = new CalculateurCouts(lambda);
        this.optimiseur = new OptimiseurReseau(calculateur);
        this.fichierSource = fichierSource;
    }

    /**
     * Démarre l'application en mode manuel (Partie 1).
     */
    public void demarrer() {
        afficherBanniere();
        
        while (true) {
            afficherMenu(MENU_PRINCIPAL);
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
            afficherMenu(MENU_PARTIE2);
            int choix = lireChoix();
            
            switch (choix) {
                case 1: resolutionAutomatique(); break;
                case 2: sauvegarderSolution();   break;
                case 3: 
                    afficherInfo("Au revoir !");
                    return;
                default: 
                    afficherErreur("Choix invalide ! Veuillez choisir entre 1 et 3.");
            }
        }
    }

    // =========================================================================
    //  MENUS ET AFFICHAGE
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

    private void afficherBanniere() {
        System.out.println();
        System.out.println("  ╔════════════════════════════════════════════╗");
        System.out.println("  ║     GESTIONNAIRE DE RESEAU ELECTRIQUE     ║");
        System.out.println("  ╚════════════════════════════════════════════╝");
        System.out.println();
    }

    private void afficherMenu(String[] lignes) {
        System.out.println("  ┌────────────────────────────────────────────┐");
        System.out.println("  │" + centrer(lignes[0], 44) + "│");
        System.out.println("  ├────────────────────────────────────────────┤");
        for (int i = 1; i < lignes.length; i++) {
            System.out.println("  │  " + completer(lignes[i], 42) + "│");
        }
        System.out.println("  └────────────────────────────────────────────┘");
        System.out.print("\n  > Votre choix : ");
    }

    private String centrer(String s, int largeur) {
        int padding = (largeur - s.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + s + " ".repeat(Math.max(0, largeur - s.length() - padding));
    }

    private String completer(String s, int largeur) {
        return s + " ".repeat(Math.max(0, largeur - s.length()));
    }

    private void afficherOK(String message)     { System.out.println(VERT + "[OK]" + RESET + " " + message + "\n"); }
    private void afficherErreur(String message) { System.out.println(ROUGE + "[ERREUR]" + RESET + " " + message + "\n"); }
    private void afficherInfo(String message)   { System.out.println(CYAN + "[INFO]" + RESET + " " + message + "\n"); }
    private void afficherAttention(String msg)  { System.out.println(JAUNE + "[ATTENTION]" + RESET + " " + msg); }

    // =========================================================================
    //  SAISIE UTILISATEUR
    // =========================================================================

    private int lireChoix() {
        while (!sc.hasNextInt()) {
            sc.nextLine();
            afficherErreur("Entrée invalide ! Veuillez saisir un nombre.");
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
    //  TRAITEMENT DES CHOIX
    // =========================================================================

    private void traiterChoixPrincipal(int choix) {
        switch (choix) {
            case 1: ajouterGenerateur();   break;
            case 2: ajouterMaison();       break;
            case 3: ajouterConnexion();    break;
            case 4: supprimerConnexion();  break;
            default: afficherErreur("Choix invalide ! Veuillez choisir entre 1 et 5.");
        }
    }

    private void ajouterGenerateur() {
        System.out.println("\n--- AJOUTER UN GENERATEUR ---");
        try {
            String[] parts = lireDeuxElements("> Nom et capacité (ex: G1 60) : ");
            double capacite = Double.parseDouble(parts[1]);
            
            if (capacite <= 0) throw new FormatInvalideException("La capacité doit être positive");
            
            boolean existe = gestionnaire.ajouterOuModifierGenerateur(parts[0], capacite);
            afficherOK("Générateur " + parts[0] + (existe ? " mis à jour !" : " créé !"));
            
        } catch (NumberFormatException e) {
            afficherErreur("La capacité doit être un nombre valide");
        } catch (FormatInvalideException e) {
            afficherErreur(e.getMessage());
        }
    }

    private void ajouterMaison() {
        System.out.println("\n--- AJOUTER UNE MAISON ---");
        System.out.println("Types de consommation: BASSE, NORMAL, FORTE");
        try {
            String[] parts = lireDeuxElements("> Nom et consommation (ex: M1 FORTE) : ");
            TypeConsommation type = TypeConsommation.valueOf(parts[1]);
            
            boolean existe = gestionnaire.ajouterOuModifierMaison(parts[0], type);
            afficherOK("Maison " + parts[0] + (existe ? " mise à jour !" : " créée !"));
            
        } catch (IllegalArgumentException e) {
            afficherErreur("Type invalide ! Utilisez: BASSE, NORMAL ou FORTE");
        } catch (FormatInvalideException e) {
            afficherErreur(e.getMessage());
        }
    }

    private void ajouterConnexion() {
        System.out.println("\n--- AJOUTER UNE CONNEXION ---");
        try {
            String[] parts = lireDeuxElements("> Générateur et maison (ex: G1 M1) : ");
            gestionnaire.creerConnexion(parts[0], parts[1]);
            afficherOK("Connexion créée !");
        } catch (FormatInvalideException | GenerateurIntrouvableException |
                 MaisonIntrouvableException | ConnexionExistanteException e) {
            afficherErreur(e.getMessage());
        }
    }

    private void supprimerConnexion() {
        System.out.println("\n--- SUPPRIMER UNE CONNEXION ---");
        try {
            String[] parts = lireDeuxElements("> Générateur et maison (ex: G1 M1) : ");
            gestionnaire.supprimerConnexion(parts[0], parts[1]);
            afficherOK("Connexion supprimée !");
        } catch (FormatInvalideException | GenerateurIntrouvableException |
                 MaisonIntrouvableException | ConnexionIntrouvableException e) {
            afficherErreur(e.getMessage());
        }
    }

    // =========================================================================
    //  VERIFICATION ET EVALUATION
    // =========================================================================

    private boolean verifierReseau() {
        System.out.println();
        System.out.println("  ╔════════════════════════════════════════════╗");
        System.out.println("  ║           VERIFICATION DU RESEAU           ║");
        System.out.println("  ╚════════════════════════════════════════════╝");

        String problemes = gestionnaire.verifierValiditeReseau();
        
        if (problemes.isEmpty()) {
            afficherOK("Réseau valide ! Chaque maison est connectée à exactement un générateur.");
            return true;
        }
        
        afficherAttention("Problèmes détectés :");
        System.out.println(problemes);
        System.out.println("\nCorrigez ces problèmes avant de terminer !\n");
        return false;
    }

    private void menuEvaluation() {
        while (true) {
            afficherMenu(MENU_EVALUATION);
            int choix = lireChoix();
            
            switch (choix) {
                case 1: calculerCout();       break;
                case 2: modifierConnexion();  break;
                case 3: afficherReseau();     break;
                case 4: verifierReseau();     return;
                default: afficherErreur("Choix invalide ! Veuillez choisir entre 1 et 4.");
            }
        }
    }

    private void calculerCout() {
        try {
            Couts cout = calculateur.calculerCout(gestionnaire.getReseauElectrique());
            System.out.println("\n" + CYAN + "Coût du réseau : " + cout + RESET + "\n");
        } catch (ArithmeticException e) {
            afficherErreur("Impossible de calculer le coût : " + e.getMessage());
        }
    }

    private void modifierConnexion() {
        System.out.println("\n--- MODIFIER UNE CONNEXION ---");
        try {
            String[] ancienne = lireDeuxElements("> Ancienne connexion (ex: M1 G1) : ");
            String[] nouvelle = lireDeuxElements("> Nouvelle connexion (ex: M1 G2) : ");
            
            gestionnaire.modifierConnexion(ancienne[0], ancienne[1], nouvelle[0], nouvelle[1]);
            afficherOK("Modification réussie !");
            afficherReseau();
        } catch (FormatInvalideException | GenerateurIntrouvableException |
                 MaisonIntrouvableException | ConnexionIntrouvableException e) {
            afficherErreur(e.getMessage());
        }
    }

    private void afficherReseau() {
        ReseauElectrique reseau = gestionnaire.getReseauElectrique();
        
        System.out.println();
        System.out.println("  ╔════════════════════════════════════════════╗");
        System.out.println("  ║              ETAT DU RESEAU                ║");
        System.out.println("  ╚════════════════════════════════════════════╝");

        // Trier les maisons par numero
        List<Maison> maisonsTries = new ArrayList<>(reseau.getMaisons());
        maisonsTries.sort(comparateurNaturel(Maison::getNom));

        // Trier les generateurs par numero
        List<Generateur> generateursTries = new ArrayList<>(reseau.getGenerateurs());
        generateursTries.sort(comparateurNaturel(Generateur::getNom));

        // Calculer la largeur max pour l'alignement
        int largeurMaxMaison = Math.max(7, maisonsTries.stream().mapToInt(m -> m.getNom().length()).max().orElse(7));
        int largeurMaxGen = Math.max(4, generateursTries.stream().mapToInt(g -> g.getNom().length()).max().orElse(4));

        System.out.println("\n  MAISONS (" + maisonsTries.size() + ")");
        System.out.println("  ─────────────────────────────────────────────");
        for (Maison m : maisonsTries) {
            String type = formaterType(m.getTypeConsommation());
            System.out.printf("    %s : %d kW (%s)%n", m.getNom(), m.getConsommation(), type);
        }

        System.out.println("\n  GENERATEURS (" + generateursTries.size() + ")");
        System.out.println("  ─────────────────────────────────────────────");
        for (Generateur g : generateursTries) {
            double usage = calculateur.getSommeDesDemandesElectriques(g, reseau);
            boolean surcharge = usage > g.getCapaciteMaximale();
            String statut = surcharge ? ROUGE + "SURCHARGE" + RESET : VERT + "OK" + RESET;
            System.out.printf("    %s : %.0f/%.0f kW [%s]%n", g.getNom(), usage, g.getCapaciteMaximale(), statut);
        }

        System.out.println("\n  CONNEXIONS");
        System.out.println("  ─────────────────────────────────────────────");
        for (Generateur g : generateursTries) {
            List<Maison> maisonsGen = reseau.trouverLesMaisonsDeGenerateur(g);
            if (maisonsGen != null && !maisonsGen.isEmpty()) {
                List<Maison> maisonsTriees = new ArrayList<>(maisonsGen);
                maisonsTriees.sort(comparateurNaturel(Maison::getNom));
                
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
            case BASSE: return "Basse";
            case NORMAL: return "Normal";
            case FORTE: return "Forte";
            default: return type.name();
        }
    }

    private <T> Comparator<T> comparateurNaturel(java.util.function.Function<T, String> extracteur) {
        return (a, b) -> {
            String nomA = extracteur.apply(a);
            String nomB = extracteur.apply(b);
            
            // Extraire préfixe et numéro
            String prefixA = nomA.replaceAll("[0-9]+$", "");
            String prefixB = nomB.replaceAll("[0-9]+$", "");
            String numStrA = nomA.substring(prefixA.length());
            String numStrB = nomB.substring(prefixB.length());
            
            int cmpPrefix = prefixA.compareTo(prefixB);
            if (cmpPrefix != 0) return cmpPrefix;
            
            // Comparer les numéros
            if (numStrA.isEmpty() && numStrB.isEmpty()) return 0;
            if (numStrA.isEmpty()) return -1;
            if (numStrB.isEmpty()) return 1;
            
            try {
                return Integer.compare(Integer.parseInt(numStrA), Integer.parseInt(numStrB));
            } catch (NumberFormatException e) {
                return numStrA.compareTo(numStrB);
            }
        };
    }

    // =========================================================================
    //  PARTIE 2 - OPTIMISATION
    // =========================================================================

    private void resolutionAutomatique() {
        System.out.println();
        System.out.println("  ╔════════════════════════════════════════════╗");
        System.out.println("  ║          RESOLUTION AUTOMATIQUE            ║");
        System.out.println("  ╚════════════════════════════════════════════╝");

        ReseauElectrique reseau = gestionnaire.getReseauElectrique();

        Map<Maison, Generateur> connexionsAvant = sauvegarderConnexions(reseau);
        Couts ancienCout = calculateur.calculerCout(reseau);
        
        System.out.println("\n  AVANT optimisation");
        System.out.println("  ─────────────────────────────────────────────");
        afficherCoutsFormate(ancienCout);

        System.out.println("\n  Optimisation en cours...");

        optimiseur.optimiser(reseau);

        Couts nouveauCout = calculateur.calculerCout(reseau);
        System.out.println("  APRES optimisation");
        System.out.println("  ─────────────────────────────────────────────");
        afficherCoutsFormate(nouveauCout);

        double amelioration = ancienCout.getCoutGlobale() - nouveauCout.getCoutGlobale();
        System.out.println();
        if (amelioration > 0) {
            System.out.println("  " + VERT + "[+] Gain : -" + String.format("%.2f", amelioration) + RESET);
        } else {
            System.out.println("  " + CYAN + "[=] Le reseau etait deja optimal" + RESET);
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
        // Collecter les modifications
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

        System.out.println("\n  CONNEXIONS MODIFIEES");
        System.out.println("  ─────────────────────────────────────────────");

        if (modifications.isEmpty()) {
            System.out.println("    Aucune modification");
        } else {
            // Trier par nom de maison
            modifications.sort((a, b) -> {
                String numA = a[0].replaceAll("[^0-9]", "");
                String numB = b[0].replaceAll("[^0-9]", "");
                if (numA.isEmpty() || numB.isEmpty()) return a[0].compareTo(b[0]);
                return Integer.compare(Integer.parseInt(numA), Integer.parseInt(numB));
            });

            // Calculer largeur max pour alignement
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

    private void sauvegarderSolution() {
        System.out.println("\n--- SAUVEGARDER LA SOLUTION ---");
        
        String nomFichier;
        File fichier;
        
        while (true) {
            System.out.print("> Nom du fichier : ");
            nomFichier = sc.nextLine().trim();

            if (nomFichier.isEmpty()) {
                afficherErreur("Le nom du fichier ne peut pas être vide.");
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
                    afficherErreur("Impossible de sauvegarder sous le meme nom que le fichier source !");
                    continue;
                }
            }

            // Vérifier si le fichier existe déjà
            if (fichier.exists()) {
                afficherErreur("Le fichier '" + nomFichier + "' existe deja ! Choisissez un autre nom.");
                continue;
            }
            
            break;
        }

        GestionnaireFichier.ecrireFichierReseau(fichier, gestionnaire.getReseauElectrique());
        afficherOK("Solution sauvegardée dans '" + nomFichier + "' !");
    }
}
