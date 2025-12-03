package up.mi.paa.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.ReseauElectrique;
import up.mi.paa.model.TypeConsommation;

public class GestionnaireFichier {
	
	private GestionnaireFichier() {}
	
    public static ReseauElectrique lireFichierReseau(File f) {

    	ReseauElectrique re = new ReseauElectrique();
    	
        try (BufferedReader bf = new BufferedReader(new FileReader(f))) {

            String line = null;
            while ((line = bf.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("generateur(")) {

                    String[] info = verifierFormat(line);
                    if (info != null) {

                        String nom = info[0].trim().toLowerCase();
                        String capaciteStr = info[1].trim();

                        try {
                            double capacite = Double.parseDouble(capaciteStr);
                            Generateur existant = re.trouverGenerateur(nom);

                            if (existant == null) {
                                Generateur generateur = new Generateur(nom, capacite);
                                re.getConnexions().put(generateur, new ArrayList<Maison>());
                            } else {
                                existant.setCapaciteMaximale(capacite);
                            }

                        } catch (NumberFormatException e) {
                            System.out.println("Capacité invalide pour le générateur \"" 
                                    + nom + "\" dans la ligne : " + line);
                        }
                    }

                } else if (line.startsWith("maison(")) {

                    String[] info = verifierFormat(line);
                    if (info != null) {
                        String nom = info[0].trim().toLowerCase();
                        String typeStr = info[1].trim().toUpperCase();

                        try {
                            TypeConsommation type = TypeConsommation.valueOf(typeStr);
                            
                            Maison existant = re.trouverMaison(nom);
                            if (existant == null) {
                            	re.ajouterMaison(new Maison(nom, type));
                            } else {
                                existant.setTypeConsommation(type);
                            }

                        } catch (IllegalArgumentException e) {
                            System.out.println("Type de consommation invalide \"" 
                                    + typeStr + "\" pour la maison dans la ligne : " + line);
                        }
                    }

                } else if (line.startsWith("connexion(")) {

                    String[] info = verifierFormat(line);
                    if (info != null) {

                        String nom1 = info[0].trim().toLowerCase();
                        String nom2 = info[1].trim().toLowerCase();

                        Generateur generateur = re.trouverGenerateur(nom1);
                        Maison maison = re.trouverMaison(nom2);

                        if (generateur == null || maison == null) {
                            generateur = re.trouverGenerateur(nom2);
                            maison = re.trouverMaison(nom1);
                        }

                        if (generateur == null || maison == null) {
                            System.out.println("Générateur ou maison n'existe pas (ligne : " + line + ")");
                        } else {
                        	re.ajouterConnexion(maison, generateur);
                        }
                    }
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return re;
    }

	
    public static String[] verifierFormat(String ligne) {
        int indiceParentheseOuvrante = ligne.indexOf('(');
        int indiceParentheseFermante = ligne.indexOf(')', indiceParentheseOuvrante + 1);
        String[] info = null;

        if (indiceParentheseOuvrante == -1 || indiceParentheseFermante == -1) {
            System.out.println("Ligne invalide (parenthèses manquantes) : " + ligne);
        } else {
            String contenu = ligne.substring(indiceParentheseOuvrante + 1, indiceParentheseFermante);
            info = contenu.split(",");

            if (info.length != 2) {
                System.out.println("Format invalide (deux éléments attendus) : " + ligne);
                info = null;
            }
        }

        return info;
    }
    
    public static void ecrireFichierReseau(File f, ReseauElectrique re) {
    	try(BufferedWriter bw = new BufferedWriter(new FileWriter(f));
    			PrintWriter pw = new PrintWriter(bw)) {
    		
    		List<String> connexion = new ArrayList<String>();
    		for (Generateur g: re.getConnexions().keySet()) {
    			pw.println("generateur(" + g.getNom() + "," + (int) g.getCapaciteMaximale() + ").");
    			for (Maison m: re.getConnexions().get(g)) {
    				connexion.add("connexion(" + g.getNom() + "," + m.getNom() + ").");
    			}
    		}
    		
    		for (Maison m: re.getMaisons()) {
    			pw.println("maison(" + m.getNom() + "," + m.getTypeConsommation().name() + ").");
    		}
    		
    		for(String s: connexion) {
    			pw.println(s);
    		}
    		pw.close();
    	} catch(FileNotFoundException e) {
    		e.printStackTrace();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
     	
    	
    }
}
