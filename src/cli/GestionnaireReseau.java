package cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class GestionnaireReseau {

    private Scanner sc;
    private ReseauElectrique re;
    public static final int LAMBDA = 10;
    
    public GestionnaireReseau(Scanner sc) {
    	this.sc = sc;
    	re = new ReseauElectrique();
    }
    
    public GestionnaireReseau(Scanner sc, ReseauElectrique Re) {
    	this.sc = sc;
    	re = Re;
    }
    
    public ReseauElectrique getReseauElectrique() {
    	return re;
    }

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
    
    private int lireChoix() {
        while (!sc.hasNextInt()) { 
        	sc.nextLine(); 
        	System.out.println("Choix invalide: ");
        }
        int choix = sc.nextInt(); 
        sc.nextLine();
        return choix;
    }

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
                break;
        }
    }

    public void ajouterGenerateur() {
        System.out.println("\n--- AJOUTER UN GENERATEUR ---");
        System.out.print("> Nom et capacite (ex: G1 60) : ");
        String[] parts = sc.nextLine().split(" ");

        if(parts.length != 2) {
            System.out.println("[ERREUR] Format invalide !\n");
            return;
        }

        String nom = parts[0].toUpperCase();

        double capacite;
        try {
            capacite = Double.parseDouble(parts[1]);
        } catch(java.lang.NumberFormatException e) {
            System.out.println("[ERREUR] Type invalide !\n");
            return;
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

    }

    public void ajouterMaison() {
        System.out.println("\n--- AJOUTER UNE MAISON ---");
        System.out.println("Types de consommation: BASSE, NORMAL, FORTE");
        System.out.print("> Nom et Consommation (ex: M1 FORTE) : ");
        String[] parts = sc.nextLine().split(" ");

        if(parts.length != 2) {
            System.out.println("[ERREUR] Format invalide !\n");
            return;
        }

        String nom = parts[0].toUpperCase();
        String consommation = parts[1].toUpperCase();
        TypeConsommation type;
        try {
            type = TypeConsommation.valueOf(consommation);
        } catch (IllegalArgumentException e) {
            System.out.println("[ERREUR] Type invalide ! Utilisez: BASSE, NORMAL, FORTE\n");
            return;
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
    }

    public void ajouterConnexion() {
        System.out.println("\n--- AJOUTER UNE CONNEXION ---");
        System.out.print("> Generateur et maison (ex: G1 M1 ou M1 G1) : ");
        String[] parts = sc.nextLine().split(" ");

        if(parts.length != 2) {
            System.out.println("[ERREUR] Format invalide !\n");
            return;
        }

        String element1 = parts[0].toUpperCase();
        String element2 = parts[1].toUpperCase();

        Generateur generateur = re.trouverGenerateur(element1);
        Maison maison = re.trouverMaison(element2);

        if (generateur == null) {
            generateur = re.trouverGenerateur(element2);
            maison = re.trouverMaison(element1);
        }

        if (generateur == null || maison == null) {
            System.out.println("[ERREUR] Generateur ou maison introuvable !");
            System.out.println("Verifiez que vous avez bien cree les elements avant.\n");
            return;
        }

        if (re.getConnexions().containsKey(maison)) {
            System.out.println("[ERREUR] La maison " + maison.getNom() + " est deja connectee !\n");
            return;
        }

        re.ajouterConnexion(maison, generateur);
        System.out.println("[OK] Connexion creee entre " + maison.getNom() + " <-> " + generateur.getNom() + " !\n");
    }

    public void supprimerConnexion() {
        System.out.println("\n--- SUPPRIMER UNE CONNEXION ---");
        System.out.print("> Generateur et maison (ex: G1 M1 ou M1 G1) : ");
        String[] parts = sc.nextLine().split(" ");

        if(parts.length != 2) {
            System.out.println("[ERREUR] Format invalide !\n");
            return;
        }

        String element1 = parts[0].toUpperCase();
        String element2 = parts[1].toUpperCase();

        Generateur generateur = re.trouverGenerateur(element1);
        Maison maison = re.trouverMaison(element2);

        if (generateur == null) {
            generateur = re.trouverGenerateur(element2);
            maison = re.trouverMaison(element1);
        }

        if (generateur == null || maison == null) {
            System.out.println("[ERREUR] Generateur ou maison introuvable !");
            System.out.println("Verifiez que vous avez bien cree les elements avant.\n");
            return;
        }

        if (!re.getConnexions().containsKey(maison)) {
            System.out.println("[ERREUR] La maison " + maison.getNom() + " n'est pas connectee !\n");
            return;
        }

        Generateur generateurConnecte = re.getConnexions().get(maison);
        if (!generateur.equals(generateurConnecte)) {
            System.out.println("[ERREUR] La connexion entre " + maison.getNom() + " et " + generateur.getNom() + " n'existe pas !");
            System.out.println("La maison " + maison.getNom() + " est connectee a " + generateurConnecte.getNom() + "\n");
            return;
        }

        re.getConnexions().remove(maison);
        System.out.println("[OK] Connexion supprimee entre " + maison.getNom() + " et " + generateur.getNom() + " !\n");
    }

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
    
    public void menuEvaluation() {
        while (true) {
        	afficherMenu2();
            int reponse = lireChoix();
            switch (reponse) {
                case 1: 
                	cout();
                	break;
                case 2: 
                	if (modifierConnexion()) {
                		System.out.println("[OK] Modification reussie, voici les connexions :\n");
                		affichageReseau();
                		
                	} else {
                		System.out.println("[ERREUR] La modification a echoue, les connexions n'ont pas ete modifiees\n");
                	}
                	break;
                case 3: 
                	affichageReseau();
                	break;
                case 4: 
                	fin();
                	return;
                default: 
                	System.out.println("[ERREUR] Choix invalide !\n");
            }
        }
    }
    
    public double cout() {
    	List<Generateur> generateurs = re.getGenerateurs();
    	double cout = disps(generateurs) + (LAMBDA * surcharge(generateurs));
    	System.out.println("\nLe cout du reseau electrique actuel est : " + String.format("%.2f", cout) + "\n");
    	return cout;
    }
    
    public double surcharge(List<Generateur> generateurs) {
    	double somme = 0.0;
    	for (Generateur g : generateurs) {
    		somme += Math.max(0.0, (getSommeDesDemandesElectriques(g) - g.getCapaciteMaximale())/g.getCapaciteMaximale());
    	}
    	
    	return somme;
    }
    
    public double disps(List<Generateur> generateurs) {
    	double tauxDUtilisation = calculerLeTauxDUtilisationGlobale(generateurs);
    	double somme = 0.0;
    	for (Generateur g : generateurs) {
    		somme += Math.abs(calculerLeTauxDUtilisation(g) - tauxDUtilisation);
    	}
    	
    	return somme;
    }
    
    public double getSommeDesDemandesElectriques(Generateur g) {
    	List<Maison> m = re.trouverLesMaisonsDesGenerateurs(g);
    	double sommeDesDemandesElectriques = 0.0;
    	for (int i = 0; i < m.size(); i++) {
    		sommeDesDemandesElectriques += m.get(i).getConsommation();
    	}
    	
    	return sommeDesDemandesElectriques;
    }
    
    public double calculerLeTauxDUtilisation(Generateur g) {
    	return getSommeDesDemandesElectriques(g) / g.getCapaciteMaximale();
    }
    
    public double calculerLeTauxDUtilisationGlobale(List<Generateur> generateurs) {
    	double tauxDUtilisation = 0.0;
    			
    	for (int i = 0; i < generateurs.size(); i++) {
    		tauxDUtilisation += calculerLeTauxDUtilisation(generateurs.get(i));
    	}
    	
    	return tauxDUtilisation / generateurs.size();
    	
    }
    
    public boolean modifierConnexion() {
        System.out.println("\n--- MODIFIER UNE CONNEXION ---");
        System.out.print("> Ancienne connexion (ex: M1 G1 ou G1 M1) : ");
        String[] ancienneConnexion = sc.nextLine().trim().split("\\s+");
        
        if (ancienneConnexion.length != 2) { 
        	System.out.println("[ERREUR] Format invalide !\n");
        	return false; 
        }

        Maison ancienneMaison = re.trouverMaison(ancienneConnexion[0].toUpperCase());
        Generateur ancienneGenerateur = re.trouverGenerateur(ancienneConnexion[1].toUpperCase());
        
        if (ancienneMaison == null || ancienneGenerateur == null) {
        	ancienneMaison = re.trouverMaison(ancienneConnexion[1].toUpperCase());
        	ancienneGenerateur = re.trouverGenerateur(ancienneConnexion[0].toUpperCase());
        }
        
        if (ancienneMaison == null || ancienneGenerateur == null) {
        	System.out.println("[ERREUR] Introuvable\n");
        	return false;
        }
        
        if (!ancienneGenerateur.equals(re.getConnexions().get(ancienneMaison))) {
        	System.out.println("[ERREUR] Cette connexion n'existe pas\n");
        	return false;
        }

        System.out.print("> Nouvelle connexion (ex: M1 G2 ou G2 M1) : ");
        String[] nouvelleConnexion = sc.nextLine().trim().split("\\s+");
        
        if (nouvelleConnexion.length != 2) { 
        	System.out.println("[ERREUR] Format invalide !\n");
        	return false; 
        }

        Maison nouvelleMaison = re.trouverMaison(nouvelleConnexion[0].toUpperCase());
        Generateur nouvelleGenerateur = re.trouverGenerateur(nouvelleConnexion[1].toUpperCase());
        
        if (nouvelleMaison == null || nouvelleGenerateur == null) {
        	nouvelleMaison = re.trouverMaison(nouvelleConnexion[1].toUpperCase());
        	nouvelleGenerateur = re.trouverGenerateur(nouvelleConnexion[0].toUpperCase());
        }
        
        if (nouvelleMaison == null || nouvelleGenerateur == null) { 
        	System.out.println("[ERREUR] Introuvable\n");
        	return false;
        }
        
        if (!nouvelleMaison.equals(ancienneMaison)) {
        	System.out.println("[ERREUR] La maison doit rester la meme\n"); 
        	return false;
        }

        re.ajouterConnexion(nouvelleMaison, nouvelleGenerateur);
        
        System.out.println("[OK] Connexion modifiee : " + nouvelleMaison.getNom() + " <-> " + nouvelleGenerateur.getNom() + "\n");
        return true;
    }
    
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
