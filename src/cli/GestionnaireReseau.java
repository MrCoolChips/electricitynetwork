package cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Classe gerant l'interface utilisateur et les operations sur le reseau electrique.
 * Permet d'ajouter, modifier, supprimer des elements et calculer le cout du reseau.
 */
public class GestionnaireReseau {

    private Scanner sc;
    private ReseauElectrique re;
    /** Constante lambda utilisee pour le calcul du cout (penalite de surcharge) */
    public static final int LAMBDA = 10;
    
    /**
     * Constructeur avec un reseau electrique vide.
     * 
     * @param sc Le scanner pour lire les entrees utilisateur
     */
    public GestionnaireReseau(Scanner sc) {
    	this.sc = sc;
    	re = new ReseauElectrique();
    }
    
    /**
     * Constructeur avec un reseau electrique existant.
     * 
     * @param sc Le scanner pour lire les entrees utilisateur
     * @param Re Le reseau electrique a gerer
     */
    public GestionnaireReseau(Scanner sc, ReseauElectrique Re) {
    	this.sc = sc;
    	re = Re;
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
     * Demarre l'application et affiche le menu principal.
     * Gere la boucle principale du programme jusqu'a validation du reseau.
     */
    public void demarrer() {
        int choix;
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║     GESTIONNAIRE DE RESEAU ELECTRIQUE          ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        while (true) {
            afficherMenu();
            choix = lireChoix();
            if (choix == 5) {
                if (fin()) break;
            } else {
                operation(choix);
            }
        }
        menuEvaluation();
    }
    
    /**
     * Lit et valide un choix numerique saisi par l'utilisateur.
     * 
     * @return Le choix de l'utilisateur
     */
    private int lireChoix() {
        while (!sc.hasNextInt()) { 
        	sc.nextLine(); 
        	System.out.println("\n[ERREUR] Entree invalide ! Veuillez saisir un nombre.\n");
        	System.out.print("> Votre choix : ");
        }
        int choix = sc.nextInt(); 
        sc.nextLine();
        return choix;
    }

    /**
     * Affiche le menu principal avec les options disponibles.
     */
    public void afficherMenu() {
        System.out.println("┌────────────────────────────────────────────────┐");
        System.out.println("│              MENU PRINCIPAL                    │");
        System.out.println("├────────────────────────────────────────────────┤");
        System.out.println("│  1 | Ajouter un generateur                     │");
        System.out.println("│  2 | Ajouter une maison                        │");
        System.out.println("│  3 | Ajouter une connexion                     │");
        System.out.println("│  4 | Supprimer une connexion                   │");
        System.out.println("│  5 | Fin                                       │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.print("\n> Votre choix : ");
    }

    /**
     * Execute l'operation correspondant au choix de l'utilisateur.
     * 
     * @param choix Le numero de l'option choisie
     */
    public void operation(int choix) {
        switch (choix) {
            case 1:
                ajouterGenerateur();
                break;
            case 2:
                ajouterMaison();
                break;
            case 3:
                ajouterConnexion();
                break;
            case 4:
                supprimerConnexion();
                break;
            default:
                System.out.println("\n[ERREUR] Choix invalide ! Veuillez choisir entre 1 et 5.\n");
                break;
        }
    }

    /**
     * Ajoute ou met a jour un generateur dans le reseau.
     * Demande a l'utilisateur le nom et la capacite du generateur.
     * Gere les exceptions FormatInvalideException.
     */
    public void ajouterGenerateur() {
        System.out.println("\n--- AJOUTER UN GENERATEUR ---");
        System.out.print("> Nom et capacite (ex: G1 60) : ");
        
        try {
            String[] parts = sc.nextLine().split(" ");
            
            if(parts.length != 2) {
                throw new FormatInvalideException("Le format doit etre: NOM CAPACITE");
            }

            String nom = parts[0].toUpperCase();
            double capacite;
            
            try {
                capacite = Double.parseDouble(parts[1]);
                if(capacite <= 0) {
                    throw new NumberFormatException("La capacite doit etre positive");
                }
            } catch(NumberFormatException e) {
                throw new FormatInvalideException("La capacite doit etre un nombre positif valide");
            }

            Generateur existantGenerateur = re.trouverGenerateur(nom);

            if(existantGenerateur != null) {
                existantGenerateur.setCapaciteMaximale(capacite);
                System.out.println("[OK] Generateur " + nom + " mis a jour !\n");
            } else {
                Generateur nouvGenerateur = new Generateur(nom, capacite);
                re.ajouterGenerateur(nouvGenerateur);
                System.out.println("[OK] Generateur " + nom + " cree !\n");
            }
            
        } catch(FormatInvalideException e) {
            System.out.println("[ERREUR] " + e.getMessage() + "\n");
        }

    }

    /**
     * Ajoute ou met a jour une maison dans le reseau.
     * Demande a l'utilisateur le nom et le type de consommation de la maison.
     * Gere les exceptions FormatInvalideException.
     */
    public void ajouterMaison() {
        System.out.println("\n--- AJOUTER UNE MAISON ---");
        System.out.println("Types de consommation: BASSE, NORMAL, FORTE");
        System.out.print("> Nom et Consommation (ex: M1 FORTE) : ");
        
        try {
            String[] parts = sc.nextLine().split(" ");

            if(parts.length != 2) {
                throw new FormatInvalideException("Le format doit etre: NOM TYPE_CONSOMMATION");
            }

            String nom = parts[0].toUpperCase();
            String consommation = parts[1].toUpperCase();
            TypeConsommation type;
            
            try {
                type = TypeConsommation.valueOf(consommation);
            } catch (IllegalArgumentException e) {
                throw new FormatInvalideException("Type invalide ! Utilisez: BASSE, NORMAL ou FORTE");
            }

            Maison existantMaison = re.trouverMaison(nom);

            if(existantMaison != null) {
                existantMaison.setTypeConsommation(type);
                System.out.println("[OK] Maison " + nom + " mise a jour !\n");
            } else {
                Maison nouvelleMaison = new Maison(nom, type);
                re.ajouterMaison(nouvelleMaison);
                System.out.println("[OK] Maison " + nom + " creee !\n");
            }
            
        } catch(FormatInvalideException e) {
            System.out.println("[ERREUR] " + e.getMessage() + "\n");
        }
    }

    /**
     * Cree une connexion entre une maison et un generateur existants.
     * Verifie que les deux elements existent et que la maison n'est pas deja connectee.
     * Gere les exceptions FormatInvalideException, GenerateurIntrouvableException,
     * MaisonIntrouvableException et ConnexionExistanteException.
     */
    public void ajouterConnexion() {
        System.out.println("\n--- AJOUTER UNE CONNEXION ---");
        System.out.print("> Generateur et maison (ex: G1 M1 ou M1 G1) : ");
        
        try {
            String[] parts = sc.nextLine().split(" ");

            if(parts.length != 2) {
                throw new FormatInvalideException("Le format doit etre: ELEMENT1 ELEMENT2");
            }

            String element1 = parts[0].toUpperCase();
            String element2 = parts[1].toUpperCase();

            Generateur generateur = re.trouverGenerateur(element1);
            Maison maison = re.trouverMaison(element2);

            if (generateur == null) {
                generateur = re.trouverGenerateur(element2);
                maison = re.trouverMaison(element1);
            }

            if (generateur == null) {
                throw new GenerateurIntrouvableException("Generateur introuvable ! Verifiez que vous l'avez cree avant.");
            }
            
            if (maison == null) {
                throw new MaisonIntrouvableException("Maison introuvable ! Verifiez que vous l'avez creee avant.");
            }

            if (re.getConnexions().containsKey(maison)) {
                throw new ConnexionExistanteException("La maison " + maison.getNom() + " est deja connectee !");
            }

            re.ajouterConnexion(maison, generateur);
            System.out.println("[OK] Connexion creee entre " + maison.getNom() + " <-> " + generateur.getNom() + " !\n");
            
        } catch(FormatInvalideException | GenerateurIntrouvableException | 
                MaisonIntrouvableException | ConnexionExistanteException e) {
            System.out.println("[ERREUR] " + e.getMessage() + "\n");
        }
    }

    /**
     * Supprime une connexion existante entre une maison et un generateur.
     * Verifie que les elements existent et que la connexion specifique existe.
     * Gere les exceptions FormatInvalideException, GenerateurIntrouvableException,
     * MaisonIntrouvableException et ConnexionIntrouvableException.
     */
    public void supprimerConnexion() {
        System.out.println("\n--- SUPPRIMER UNE CONNEXION ---");
        System.out.print("> Generateur et maison (ex: G1 M1 ou M1 G1) : ");
        
        try {
            String[] parts = sc.nextLine().split(" ");

            if(parts.length != 2) {
                throw new FormatInvalideException("Le format doit etre: ELEMENT1 ELEMENT2");
            }

            String element1 = parts[0].toUpperCase();
            String element2 = parts[1].toUpperCase();

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

            if (!re.getConnexions().containsKey(maison)) {
                throw new ConnexionIntrouvableException("La maison " + maison.getNom() + " n'est pas connectee !");
            }

            Generateur generateurConnecte = re.getConnexions().get(maison);
            if (!generateur.equals(generateurConnecte)) {
                throw new ConnexionIntrouvableException(
                    "La connexion entre " + maison.getNom() + " et " + generateur.getNom() + " n'existe pas ! " +
                    "La maison " + maison.getNom() + " est connectee a " + generateurConnecte.getNom()
                );
            }

            re.getConnexions().remove(maison);
            System.out.println("[OK] Connexion supprimee entre " + maison.getNom() + " et " + generateur.getNom() + " !\n");
            
        } catch(FormatInvalideException | GenerateurIntrouvableException | 
                MaisonIntrouvableException | ConnexionIntrouvableException e) {
            System.out.println("[ERREUR] " + e.getMessage() + "\n");
        }
    }

    /**
     * Verifie que le reseau est valide avant de terminer.
     * Un reseau est valide si toutes les maisons sont connectees a un generateur.
     * 
     * @return true si le reseau est valide, false sinon
     */
    public boolean fin() {

        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│         VERIFICATION DU RESEAU                 │");
        System.out.println("└────────────────────────────────────────────────┘");
    
        List<String> problemes = new ArrayList<>();
    
        for (Maison maison : re.getMaisons()) {
            if (!re.getConnexions().containsKey(maison)) {
                problemes.add(maison.getNom() + " (aucune connexion)");
            }
            
        }
        
        if (problemes.isEmpty()) {
            System.out.println("[OK] Reseau valide ! Chaque maison est connectee a exactement un generateur.");
            return true;
        } else {
            System.out.println("[ATTENTION] Problemes detectes :");
            for (String probleme : problemes) {
                System.out.println("  - " + probleme);
            }
            System.out.println("\nCorrigez ces problemes avant de terminer !\n");
            return false;
        }
    }
    
    /**
     * Affiche le menu d'evaluation du reseau avec les options de calcul et modification.
     */
    public void afficherMenu2() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│           EVALUATION DU RESEAU                 │");
        System.out.println("├────────────────────────────────────────────────┤");
        System.out.println("│  1 | Calculer le cout du reseau                │");
        System.out.println("│  2 | Modifier une connexion                    │");
        System.out.println("│  3 | Afficher le reseau                        │");
        System.out.println("│  4 | Fin                                       │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.print("\n> Votre choix : ");
    }
    
    /**
     * Affiche le menu d'evaluation et gere les operations avancees.
     * Permet de calculer le cout, modifier des connexions, afficher le reseau ou quitter.
     */
    public void menuEvaluation() {
        while (true) {
        	afficherMenu2();
            int reponse = lireChoix();
            switch (reponse) {
                case 1: 
                	try {
                		cout();
                	} catch(ArithmeticException e) {
                		System.out.println("[ERREUR] Impossible de calculer le cout : " + e.getMessage() + "\n");
                	}
                	break;
                case 2: 
                	try {
                		modifierConnexion();
                		System.out.println("[OK] Modification reussie, voici les connexions :\n");
                		affichageReseau();
                	} catch(Exception e) {
                		System.out.println("[ERREUR] " + e.getMessage() + "\n");
                	}
                	break;
                case 3: 
                	affichageReseau();
                	break;
                case 4: 
                	fin();
                	return;
                default: 
                	System.out.println("\n[ERREUR] Choix invalide ! Veuillez choisir entre 1 et 4.\n");
            }
        }
    }
    
    /**
     * Calcule et affiche le cout total du reseau electrique.
     * Le cout est compose de la dispersion (disps) et de la surcharge ponderee par LAMBDA.
     * 
     * @return Le cout total du reseau
     * @throws ArithmeticException si le calcul est impossible (pas de generateurs, capacite nulle)
     */
    public double cout() {
    	List<Generateur> generateurs = re.getGenerateurs();
    	double cout = disps(generateurs) + (LAMBDA * surcharge(generateurs));
    	System.out.println("\nLe cout du reseau electrique actuel est : " + String.format("%.2f", cout) + "\n");
    	return cout;
    }
    
    /**
     * Calcule la surcharge du reseau.
     * La surcharge represente le depassement de capacite des generateurs.
     * 
     * @param generateurs La liste des generateurs du reseau
     * @return La somme des surcharges normalisees
     */
    public double surcharge(List<Generateur> generateurs) {
    	double somme = 0.0;
    	for (Generateur g : generateurs) {
    		somme += Math.max(0.0, (getSommeDesDemandesElectriques(g) - g.getCapaciteMaximale())/g.getCapaciteMaximale());
    	}
    	
    	return somme;
    }
    
    /**
     * Calcule la dispersion du reseau.
     * La dispersion mesure l'ecart entre les taux d'utilisation des generateurs.
     * 
     * @param generateurs La liste des generateurs du reseau
     * @return La somme des ecarts absolus au taux d'utilisation moyen
     */
    public double disps(List<Generateur> generateurs) {
    	double tauxDUtilisation = calculerLeTauxDUtilisationGlobale(generateurs);
    	double somme = 0.0;
    	for (Generateur g : generateurs) {
    		somme += Math.abs(calculerLeTauxDUtilisation(g) - tauxDUtilisation);
    	}
    	
    	return somme;
    }
    
    /**
     * Calcule la somme des demandes electriques des maisons connectees a un generateur.
     * 
     * @param g Le generateur dont on veut calculer la demande totale
     * @return La somme des consommations en kW
     */
    public double getSommeDesDemandesElectriques(Generateur g) {
    	List<Maison> m = re.trouverLesMaisonsDesGenerateurs(g);
    	double sommeDesDemandesElectriques = 0.0;
    	for (int i = 0; i < m.size(); i++) {
    		sommeDesDemandesElectriques += m.get(i).getConsommation();
    	}
    	
    	return sommeDesDemandesElectriques;
    }
    
    /**
     * Calcule le taux d'utilisation d'un generateur.
     * Le taux est le rapport entre la demande totale et la capacite maximale.
     * 
     * @param g Le generateur
     * @return Le taux d'utilisation (demande / capacite)
     * @throws ArithmeticException si la capacite du generateur est nulle
     */
    public double calculerLeTauxDUtilisation(Generateur g) {
    	if(g.getCapaciteMaximale() == 0) {
    		throw new ArithmeticException("La capacite du generateur " + g.getNom() + " ne peut pas etre 0");
    	}
    	return getSommeDesDemandesElectriques(g) / g.getCapaciteMaximale();
    }
    
    /**
     * Calcule le taux d'utilisation global (moyen) de tous les generateurs.
     * 
     * @param generateurs La liste des generateurs
     * @return Le taux d'utilisation moyen
     * @throws ArithmeticException si la liste est vide
     */
    public double calculerLeTauxDUtilisationGlobale(List<Generateur> generateurs) {
    	if(generateurs.isEmpty()) {
    		throw new ArithmeticException("Aucun generateur dans le reseau");
    	}
    	
    	double tauxDUtilisation = 0.0;
    			
    	for (int i = 0; i < generateurs.size(); i++) {
    		tauxDUtilisation += calculerLeTauxDUtilisation(generateurs.get(i));
    	}
    	
    	return tauxDUtilisation / generateurs.size();
    	
    }
    
    /**
     * Modifie une connexion existante en changeant le generateur connecte a une maison.
     * La maison doit rester la meme, seul le generateur peut changer.
     * 
     * @throws FormatInvalideException si le format d'entree est incorrect
     * @throws GenerateurIntrouvableException si un generateur n'existe pas
     * @throws MaisonIntrouvableException si une maison n'existe pas
     * @throws ConnexionIntrouvableException si la connexion a modifier n'existe pas
     */
    public void modifierConnexion() throws FormatInvalideException, GenerateurIntrouvableException, 
                                           MaisonIntrouvableException, ConnexionIntrouvableException {
        System.out.println("\n--- MODIFIER UNE CONNEXION ---");
        System.out.print("> Ancienne connexion (ex: M1 G1 ou G1 M1) : ");
        String[] ancienneConnexion = sc.nextLine().trim().split("\\s+");
        
        if (ancienneConnexion.length != 2) { 
        	throw new FormatInvalideException("Le format doit etre: ELEMENT1 ELEMENT2");
        }

        Maison ancienneMaison = re.trouverMaison(ancienneConnexion[0].toUpperCase());
        Generateur ancienneGenerateur = re.trouverGenerateur(ancienneConnexion[1].toUpperCase());
        
        if (ancienneMaison == null || ancienneGenerateur == null) {
        	ancienneMaison = re.trouverMaison(ancienneConnexion[1].toUpperCase());
        	ancienneGenerateur = re.trouverGenerateur(ancienneConnexion[0].toUpperCase());
        }
        
        if (ancienneMaison == null) {
        	throw new MaisonIntrouvableException("Maison introuvable dans l'ancienne connexion");
        }
        
        if (ancienneGenerateur == null) {
        	throw new GenerateurIntrouvableException("Generateur introuvable dans l'ancienne connexion");
        }
        
        if (!ancienneGenerateur.equals(re.getConnexions().get(ancienneMaison))) {
        	throw new ConnexionIntrouvableException("Cette connexion n'existe pas");
        }

        System.out.print("> Nouvelle connexion (ex: M1 G2 ou G2 M1) : ");
        String[] nouvelleConnexion = sc.nextLine().trim().split("\\s+");
        
        if (nouvelleConnexion.length != 2) { 
        	throw new FormatInvalideException("Le format doit etre: ELEMENT1 ELEMENT2");
        }

        Maison nouvelleMaison = re.trouverMaison(nouvelleConnexion[0].toUpperCase());
        Generateur nouvelleGenerateur = re.trouverGenerateur(nouvelleConnexion[1].toUpperCase());
        
        if (nouvelleMaison == null || nouvelleGenerateur == null) {
        	nouvelleMaison = re.trouverMaison(nouvelleConnexion[1].toUpperCase());
        	nouvelleGenerateur = re.trouverGenerateur(nouvelleConnexion[0].toUpperCase());
        }
        
        if (nouvelleMaison == null) { 
        	throw new MaisonIntrouvableException("Maison introuvable dans la nouvelle connexion");
        }
        
        if (nouvelleGenerateur == null) { 
        	throw new GenerateurIntrouvableException("Generateur introuvable dans la nouvelle connexion");
        }
        
        if (!nouvelleMaison.equals(ancienneMaison)) {
        	throw new FormatInvalideException("La maison doit rester la meme"); 
        }

        re.ajouterConnexion(nouvelleMaison, nouvelleGenerateur);
        
        System.out.println("[OK] Connexion modifiee : " + nouvelleMaison.getNom() + " <-> " + nouvelleGenerateur.getNom() + "\n");
    }
    
    /**
     * Affiche l'etat complet du reseau electrique.
     * Affiche toutes les maisons, tous les generateurs et toutes les connexions.
     */
    public void affichageReseau() {
    	System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║              ETAT DU RESEAU                    ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    	re.affichageMaisons();
    	re.affichageGenerateurs();
        System.out.println("\nCONNEXIONS :");
        System.out.println("─────────────────────────────────");
        for (Map.Entry<Maison, Generateur> connexion : re.getConnexions().entrySet()) {
            System.out.println("   " + connexion.getKey().getNom() + " <-> " + connexion.getValue().getNom());
        }
        System.out.println();
    }

}
