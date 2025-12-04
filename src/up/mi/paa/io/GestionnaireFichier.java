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
import up.mi.paa.service.GestionnaireReseau;

/**
 * Classe utilitaire pour la gestion des entrées/sorties de fichiers liés au réseau électrique.
 */
public class GestionnaireFichier {

    /**
     * Constructeur privé pour empêcher l'instanciation de cette classe utilitaire.
     */
    private GestionnaireFichier() {}

    /**
     * Lit un fichier texte et construit le réseau électrique correspondant.
     * 
     * Le fichier doit impérativement respecter l'ordre de déclaration : 
     * générateurs, maisons, puis connexions. Tout manquement au format ou à l'ordre,
     * ainsi que toute incohérence dans les données, entraînera l'arrêt de la lecture.
     *
     * @param f Le fichier contenant la description du réseau.
     * @return L'objet {@link ReseauElectrique} construit, ou {@code null} en cas d'erreur 
     * (fichier introuvable, format invalide, ordre incorrect, données erronées).
     */
    public static GestionnaireReseau lireFichierReseau(File f) {

    	GestionnaireReseau reseau = new GestionnaireReseau();

        try (BufferedReader bf = new BufferedReader(new FileReader(f))) {

            String line = null;
            String phase = "generateur";

            while ((line = bf.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("generateur(")) {

                    if (phase.equals("maison") || phase.equals("connexion")) {
                        throw new IOException("Erreur d'ordre : générateur trouvé après maison ou connexion.");
                    }
                    phase = "generateur";

                    String[] info = verifierFormat(line);
                    if (info == null) throw new IOException("Format invalide : " + line);

                    String nom = info[0].trim().toUpperCase();
                    String capaciteStr = info[1].trim();

                    try {
                        double capacite = Double.parseDouble(capaciteStr);
                        reseau.ajouterOuModifierGenerateur(nom, capacite);
                    } catch (NumberFormatException e) {
                        throw new IOException("Capacité invalide (" + capaciteStr + ") : " + line);
                    }

                } else if (line.startsWith("maison(")) {

                    if (phase.equals("connexion")) {
                        throw new IOException("Erreur d'ordre : maison trouvée après connexion.");
                    }
                    phase = "maison";

                    String[] info = verifierFormat(line);
                    if (info == null) throw new IOException("Format invalide : " + line);

                    String nom = info[0].trim().toUpperCase();
                    String typeStr = info[1].trim().toUpperCase();

                    try {
                        TypeConsommation type = TypeConsommation.valueOf(typeStr);
                        reseau.ajouterOuModifierMaison(nom, type);
                    } catch (IllegalArgumentException e) {
                        throw new IOException("Type de consommation invalide (" + typeStr + ") : " + line);
                    }

                } else if (line.startsWith("connexion(")) {

                    phase = "connexion";
                    String[] info = verifierFormat(line);
                    if (info == null) throw new IOException("Format invalide : " + line);

                    String nom1 = info[0].trim().toUpperCase();
                    String nom2 = info[1].trim().toUpperCase();

                    reseau.creerConnexion(nom1, nom2);
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Fichier introuvable : " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.out.println("Erreur de lecture ou de format : " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Erreur inattendue : " + e.getMessage());
            return null;
        }

        return reseau;
    }

    /**
     * Vérifie le format d'une ligne du fichier et en extrait les deux arguments entre parenthèses.
     *
     * Exemple de ligne valide :
     * - generateur(G1, 100).
     * - maison(M1, FORTE).
     * - connexion(G1, M1).
     *
     * Conditions :
     * - la ligne doit se terminer par un point,
     * - il doit y avoir une parenthèse ouvrante puis une parenthèse fermante,
     * - à l'intérieur des parenthèses, il doit y avoir exactement deux éléments séparés par une virgule.
     *
     * Si le format est invalide, un message d'erreur est affiché et la méthode renvoie null.
     *
     * @param ligne ligne brute lue dans le fichier
     * @return un tableau de deux chaînes correspondant aux arguments trouvés,
     *         ou null si le format est invalide
     */
    public static String[] verifierFormat(String ligne) {
    	if (!ligne.endsWith(".")) {
            System.out.println("Format invalide ( . attendus) : " + ligne);
            return null;
        }
    	
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
    
    /**
     * Écrit le contenu d'un {@link ReseauElectrique} dans un fichier texte,
     * au même format que celui accepté par {@link #lireFichierReseau(File)}.
     *
     * L'ordre d'écriture est :
     * 1) tous les générateurs,
     * 2) toutes les maisons,
     * 3) toutes les connexions.
     *
     * Exemple de lignes produites :
     * - generateur(G1,60).
     * - maison(M1,FORTE).
     * - connexion(G1,M1).
     *
     * @param f  fichier de sortie (sera créé ou écrasé)
     * @param re réseau électrique à sérialiser dans le fichier
     */
    public static void ecrireFichierReseau(File f, ReseauElectrique re) {
    	try(BufferedWriter bw = new BufferedWriter(new FileWriter(f));
    			PrintWriter pw = new PrintWriter(bw)) {
    		
    		List<String> connexion = new ArrayList<String>();
    		
    		// écriture des générateurs et préparation des lignes de connexions
    		for (Generateur g: re.getGenerateurs()) {
    			pw.println("generateur(" + g.getNom() + "," + (int) g.getCapaciteMaximale() + ").");
    			List<Maison> maisonsConnectees = re.trouverLesMaisonsDeGenerateur(g);
    			if (maisonsConnectees != null) {
    			    for (Maison m: maisonsConnectees) {
    			        connexion.add("connexion(" + g.getNom() + "," + m.getNom() + ").");
    			    }
    			}
    		}
    		
    		// écriture des maisons
    		for (Maison m: re.getMaisons()) {
    			pw.println("maison(" + m.getNom() + "," + m.getTypeConsommation().name() + ").");
    		}
    		
    		// écriture des connexions
    		for(String s: connexion) {
    			pw.println(s);
    		}
    		
    	} catch(FileNotFoundException e) {
    		System.out.println("Fichier introuvable : " + e.getMessage());
    	} catch(IOException e) {
    		System.out.println("Erreur de lecture ou de format : " + e.getMessage());
    	}
    	
    	System.out.println("Fichier écrit avec succès : " + f.getName());
    }
}
