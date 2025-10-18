package electricitynetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class GestionnaireReseau {

    Scanner sc;
    ReseauElectrique re;
    
    public GestionnaireReseau(Scanner sc) {
    	this.sc = sc;
    	re = new ReseauElectrique();
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

    public void affichageReseau() {
        System.out.println("\nCONNEXIONS :");
        for (Map.Entry<Maison, Generateur> connexion : re.getConnexions().entrySet()) {
            Maison maison = connexion.getKey();
            Generateur generateur = connexion.getValue();
            System.out.println("   " + maison.getNom() + " <-> " + generateur.getNom());
        }   
    }
}
