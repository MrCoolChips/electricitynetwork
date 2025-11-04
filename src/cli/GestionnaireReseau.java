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
        System.out.println("=== Gestionnaire de Réseau Électrique ===");

        while (true) {
            afficherMenu();
            choix = lireChoix();
            if (choix == 4) {
                if (fin()) break;
            } else {
                operation(choix);
            }
        }
        affichageReseau();
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
        System.out.println("1) Ajouter un générateur");
        System.out.println("2) Ajouter une maison");
        System.out.println("3) Ajouter une connexion entre une maison et un générateur existants");
        System.out.println("4) Fin");
        System.out.println("\nVotre choix : ");
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
            default:
                break;
        }
    }

    public void ajouterGenerateur() {
        System.out.println("Nom et capacité (ex: G1 60) : ");
        String[] parts = sc.nextLine().split(" ");

        if(parts.length != 2) {
            System.out.println("Format invalide !");
            return;
        }

        String nom = parts[0].toUpperCase();

        double capacite;
        try {
            capacite = Double.parseDouble(parts[1]);
        } catch(java.lang.NumberFormatException e) {
            System.out.println("Type invalide !");
            return;
        }

        Generateur existantGenerateur = re.trouverGenerateur(nom);

        if(existantGenerateur != null) {
            existantGenerateur.setCapaciteMaximale(capacite);
            System.out.println("Générateur " + nom + " mis à jour !");
        } else {
            Generateur nouvGenerateur = new Generateur(nom, capacite);
            re.ajouterGenerateur(nouvGenerateur);
            System.out.println("Générateur " + nom + " créé !");
        }

    }

    public void ajouterMaison() {
        System.out.println("Nom et Consommation (ex: M1 FORTE) : ");
        System.out.println("Les types de consommation: BASSE, NORMAL, FORTE");
        String[] parts = sc.nextLine().split(" ");

        if(parts.length != 2) {
            System.out.println("Format invalide !");
            return;
        }

        String nom = parts[0].toUpperCase();
        String consommation = parts[1].toUpperCase();
        TypeConsommation type;
        try {
            type = TypeConsommation.valueOf(consommation);
        } catch (IllegalArgumentException e) {
            System.out.println("Type invalide ! Utilisez: BASSE, NORMAL, FORTE");
            return;
        }

        Maison existantMaison = re.trouverMaison(nom);

        if(existantMaison != null) {
            existantMaison.setTypeConsommation(type);
            System.out.println("Maison " + nom + " mise à jour !");
        } else {
            Maison nouvelleMaison = new Maison(nom, type);
            re.ajouterMaison(nouvelleMaison);
            System.out.println("Maison " + nom + " créée !");
        }
    }

    public void ajouterConnexion() {
        System.out.println("Générateur et maison (ex: G1 M1 ou M1 G1) : ");
        String[] parts = sc.nextLine().split(" ");

        if(parts.length != 2) {
            System.out.println("Format invalide !");
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
            System.out.println("Générateur ou maison introuvable !");
            System.out.println("Vérifiez que vous avez bien créé les éléments avant.");
            return;
        }

        if (re.getConnexions().containsKey(maison)) {
            System.out.println("Erreur : La maison " + maison.getNom() + " est déjà connectée !");
            return;
        }

        re.ajouterConnexion(maison, generateur);
        System.out.println("Connexion créée entre " + maison.getNom() + " et " + generateur.getNom() + " !");
    }

    public boolean fin() {

        System.out.println("\n=== Vérification du réseau ===");
    
        List<String> problemes = new ArrayList<>();
    
        for (Maison maison : re.getMaisons()) {
            if (!re.getConnexions().containsKey(maison)) {
                problemes.add(maison.getNom() + " (aucune connexion)");
            }
            
        }
        
        if (problemes.isEmpty()) {
            System.out.println("Réseau valide ! Chaque maison est connectée à exactement un générateur.");
            System.out.println("Au revoir !");
            return true;
        } else {
            System.out.println("Problèmes détectés :");
            for (String probleme : problemes) {
                System.out.println("   - " + probleme);
            }
            System.out.println("Corrigez ces problèmes avant de terminer !");
            return false;
        }
    }
    
    public void afficherMenu2() {
        System.out.println("\n=== Évaluation du réseau ===");
        System.out.println("1) Calculer le coût du réseau électrique actuel");
        System.out.println("2) Modifier une connexion");
        System.out.println("3) Afficher le réseau");
        System.out.println("4) Fin");
        System.out.print("Votre choix : ");
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
                		System.out.println("Modification reussi, voici les connexions: ");
                		affichageReseau();
                		
                	} else {
                		System.out.println("La modification a echoue, les connexions n'ont pas ete modifies");
                	}
                	break;
                case 3: 
                	affichageReseau();
                	break;
                case 4: 
                	fin();
                	return;
                default: 
                	System.out.println("Choix invalide !");
            }
        }
    }
    
    public double cout() {
    	List<Generateur> generateurs = re.getGenerateurs();
    	double cout = disps(generateurs) + (LAMBDA * surcharge(generateurs));
    	System.out.println("le cout du reseau electrique actuel est " + cout);
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
        System.out.print("Ancienne connexion (ex: M1 G1 ou G1 M1) : ");
        String[] ancienneConnexion = sc.nextLine().trim().split("\\s+");
        
        if (ancienneConnexion.length != 2) { 
        	System.out.println("Format invalide !");
        	return false; 
        }

        Maison ancienneMaison = re.trouverMaison(ancienneConnexion[0].toUpperCase());
        Generateur ancienneGenerateur = re.trouverGenerateur(ancienneConnexion[1].toUpperCase());
        
        if (ancienneMaison == null || ancienneGenerateur == null) {
        	ancienneMaison = re.trouverMaison(ancienneConnexion[1].toUpperCase());
        	ancienneGenerateur = re.trouverGenerateur(ancienneConnexion[0].toUpperCase());
        }
        
        if (ancienneMaison == null || ancienneGenerateur == null) {
        	System.out.println("Introuvable");
        	return false;
        }
        
        if (!ancienneGenerateur.equals(re.getConnexions().get(ancienneMaison))) {
        	System.out.println("Cette connexion n'existe pas");
        	return false;
        }

        System.out.print("Nouvelle connexion (ex: M1 G2 ou G2 M1) : ");
        String[] nouvelleConnexion = sc.nextLine().trim().split("\\s+");
        
        if (nouvelleConnexion.length != 2) { System.out.println("Format invalide !");
        return false; 
        }

        Maison nouvelleMaison = re.trouverMaison(nouvelleConnexion[0].toUpperCase());
        Generateur nouvelleGenerateur = re.trouverGenerateur(nouvelleConnexion[1].toUpperCase());
        
        if (nouvelleMaison == null || nouvelleGenerateur == null) {
        	nouvelleMaison = re.trouverMaison(nouvelleConnexion[1].toUpperCase());
        	nouvelleGenerateur = re.trouverGenerateur(nouvelleConnexion[0].toUpperCase());
        }
        
        if (nouvelleMaison == null || nouvelleGenerateur == null) { 
        	System.out.println("Introuvable");
        	return false;
        }
        
        if (!nouvelleMaison.equals(ancienneMaison)) {
        	System.out.println("La maison doit rester la même"); 
        	return false;
        }

        re.ajouterConnexion(nouvelleMaison, nouvelleGenerateur);
        
        System.out.println("Connexion modifiée: " + nouvelleMaison.getNom() + " -> " + nouvelleGenerateur.getNom());
        return true;
    }
    
    public void affichageReseau() {
    	re.affichageMaisons();
    	re.affichageGenerateurs();
        System.out.println("\nCONNEXIONS :");
        for (Map.Entry<Maison, Generateur> connexion : re.getConnexions().entrySet()) {
            System.out.println("   " + connexion.getKey().getNom() + " <-> " + connexion.getValue().getNom());
        }   
    }

}
