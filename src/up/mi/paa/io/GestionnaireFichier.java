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
 * Classe utilitaire assurant la persistance des données du réseau électrique.
 * Gère la lecture et l'écriture des fichiers selon le format spécifié.
 */
public class GestionnaireFichier {

    /**
     * Constructeur privé (classe utilitaire).
     */
    private GestionnaireFichier() {}

    /**
     * Construit un gestionnaire de réseau à partir d'un fichier texte.
     * 
     * Le fichier doit respecter l'ordre strict : Générateurs -> Maisons -> Connexions.
     * Tout défaut de format ou d'intégrité des données interrompt la lecture.
     *
     * @param f Le fichier source à lire.
     * @return Une instance de {@link GestionnaireReseau} initialisée, ou {@code null} en cas d'erreur.
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
                        throw new IOException("Ordre invalide : définition de générateur après maison/connexion.");
                    }
                    phase = "generateur";

                    String[] info = verifierFormat(line);
                    if (info == null) throw new IOException("Format incorrect : " + line);

                    String nom = info[0].trim().toUpperCase();
                    String capaciteStr = info[1].trim();

                    try {
                        double capacite = Double.parseDouble(capaciteStr);
                        reseau.ajouterOuModifierGenerateur(nom, capacite);
                    } catch (NumberFormatException e) {
                        throw new IOException("Capacité non numérique (" + capaciteStr + ") : " + line);
                    }

                } else if (line.startsWith("maison(")) {
                    if (phase.equals("connexion")) {
                        throw new IOException("Ordre invalide : définition de maison après connexion.");
                    }
                    phase = "maison";

                    String[] info = verifierFormat(line);
                    if (info == null) throw new IOException("Format incorrect : " + line);

                    String nom = info[0].trim().toUpperCase();
                    String typeStr = info[1].trim().toUpperCase();

                    try {
                        TypeConsommation type = TypeConsommation.valueOf(typeStr);
                        reseau.ajouterOuModifierMaison(nom, type);
                    } catch (IllegalArgumentException e) {
                        throw new IOException("Type de consommation inconnu (" + typeStr + ") : " + line);
                    }

                } else if (line.startsWith("connexion(")) {
                    phase = "connexion";
                    String[] info = verifierFormat(line);
                    if (info == null) throw new IOException("Format incorrect : " + line);

                    String nom1 = info[0].trim().toUpperCase();
                    String nom2 = info[1].trim().toUpperCase();

                    reseau.creerConnexion(nom1, nom2);
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("[Erreur IO] Fichier introuvable : " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("[Erreur Format] Lecture impossible : " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("[Erreur Système] Exception inattendue : " + e.getMessage());
            return null;
        }

        return reseau;
    }

    /**
     * Valide la syntaxe d'une ligne et extrait les arguments.
     * Format attendu : {@code cle(arg1,arg2).}
     *
     * @param ligne La ligne brute.
     * @return Tableau contenant les deux arguments, ou {@code null} si invalide.
     */
    public static String[] verifierFormat(String ligne) {
        if (!ligne.endsWith(".")) {
            System.err.println("Syntaxe invalide (point final manquant) : " + ligne);
            return null;
        }
        
        int indiceOuvrante = ligne.indexOf('(');
        int indiceFermante = ligne.indexOf(')', indiceOuvrante + 1);

        if (indiceOuvrante == -1 || indiceFermante == -1) {
            System.err.println("Syntaxe invalide (parenthèses manquantes) : " + ligne);
            return null;
        }

        String contenu = ligne.substring(indiceOuvrante + 1, indiceFermante);
        String[] info = contenu.replaceAll("[\\s\\u00A0]+", "").toUpperCase().split(",");

        if (info.length != 2) {
            System.err.println("Syntaxe invalide (2 arguments attendus) : " + ligne);
            return null;
        }

        return info;
    }
    
    /**
     * Sauvegarde l'état du réseau électrique dans un fichier texte.
     * Le format de sortie est compatible avec la méthode de lecture.
     *
     * @param f Le fichier de destination.
     * @param re Le modèle du réseau à sauvegarder.
     */
    public static void ecrireFichierReseau(File f, ReseauElectrique re) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f));
             PrintWriter pw = new PrintWriter(bw)) {
            
            List<String> lignesConnexions = new ArrayList<>();
            
            // 1. Écriture des générateurs
            for (Generateur g : re.getGenerateurs()) {
                pw.println("generateur(" + g.getNom() + "," + (int) g.getCapaciteMaximale() + ").");
                
                // Préparation des connexions associées
                List<Maison> maisonsConnectees = re.trouverLesMaisonsDeGenerateur(g);
                if (maisonsConnectees != null) {
                    for (Maison m : maisonsConnectees) {
                        lignesConnexions.add("connexion(" + g.getNom() + "," + m.getNom() + ").");
                    }
                }
            }
            
            // 2. Écriture des maisons
            for (Maison m : re.getMaisons()) {
                pw.println("maison(" + m.getNom() + "," + m.getTypeConsommation().name() + ").");
            }
            
            // 3. Écriture des connexions
            for (String ligne : lignesConnexions) {
                pw.println(ligne);
            }
            
            System.out.println("Sauvegarde réussie : " + f.getName());

        } catch (FileNotFoundException e) {
            System.err.println("[Erreur IO] Fichier inaccessible : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[Erreur IO] Échec de l'écriture : " + e.getMessage());
        }
    }
}