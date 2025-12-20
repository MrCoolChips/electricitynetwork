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
import up.mi.paa.ui.cli.StyleCLI;

/**
 * Classe utilitaire assurant la persistance des données du réseau électrique.
 * Gère la lecture et l'écriture des fichiers selon le format spécifié.
 * 
 * @author Groupe 10
 */
public class GestionnaireFichier implements StyleCLI {

    // Mots-clés valides
    private static final String MOT_CLE_GENERATEUR = "generateur";
    private static final String MOT_CLE_MAISON = "maison";
    private static final String MOT_CLE_CONNEXION = "connexion";

    /**
     * Constructeur privé (classe utilitaire).
     */
    private GestionnaireFichier() {}

    /**
     * Construit un gestionnaire de réseau à partir d'un fichier texte.
     * Le fichier doit respecter l'ordre strict : Générateurs -> Maisons -> Connexions.
     * Tout défaut de format ou d'intégrité des données interrompt la lecture.
     *
     * @param f Le fichier source à lire.
     * @return Une instance de {@link GestionnaireReseau} initialisée, ou {@code null} en cas d'erreur.
     */
    public static GestionnaireReseau lireFichierReseau(File f) {

        GestionnaireReseau reseau = new GestionnaireReseau();
        int numeroLigne = 0;
        
        try (BufferedReader bf = new BufferedReader(new FileReader(f))) {

            String line = null;
            String phase = "generateur"; // Phase attendue : generateur -> maison -> connexion

            while ((line = bf.readLine()) != null) {
                numeroLigne++;
                
                // Supprimer les espaces en debut et fin de ligne
                line = line.trim();
                
                // Ignorer les lignes vides
                if (line.isEmpty()) {
                    continue;
                }

                // === ETAPE 1 : Identifier le mot-cle pour verifier l'ORDRE en premier ===
                String motCle = extraireMotCle(line);
                
                // Si pas de mot-cle identifiable, verifier la syntaxe de base
                if (motCle == null) {
                    // Verifier si c'est un probleme de point manquant
                    if (!line.contains(".")) {
                        throw new IOException(formatErreur(numeroLigne, "Point manquant", 
                            "Chaque ligne doit se terminer par un point '.'"));
                    }
                    throw new IOException(formatErreur(numeroLigne, "Syntaxe invalide", 
                        "Format attendu: mot_cle(param1,param2). avec mot_cle = generateur, maison ou connexion"));
                }

                // === ETAPE 2 : Verifier si le mot-cle est valide ===
                boolean motCleValide = motCle.equals(MOT_CLE_GENERATEUR) || 
                                       motCle.equals(MOT_CLE_MAISON) || 
                                       motCle.equals(MOT_CLE_CONNEXION);
                
                if (!motCleValide) {
                    String suggestion = suggererMotCle(motCle);
                    String message = "Mot-cle '" + motCle + "' non reconnu.";
                    if (suggestion != null) {
                        message += " Vouliez-vous dire '" + suggestion + "' ?";
                    }
                    throw new IOException(formatErreur(numeroLigne, "Mot-cle invalide", message));
                }

                // === ETAPE 3 : Verifier l'ORDRE (avant toute autre validation) ===
                // Regle 1: Tous les generateurs doivent etre definis AVANT les maisons
                // Regle 2: Toutes les maisons doivent etre definies APRES les generateurs et AVANT les connexions
                // Regle 3: Toutes les connexions doivent etre definies APRES les maisons
                
                if (motCle.equals(MOT_CLE_GENERATEUR)) {
                    if (phase.equals("maison")) {
                        throw new IOException(formatErreur(numeroLigne, "Ordre invalide", 
                            "Tous les generateurs doivent etre definis AVANT les maisons."));
                    }
                    if (phase.equals("connexion")) {
                        throw new IOException(formatErreur(numeroLigne, "Ordre invalide", 
                            "Tous les generateurs doivent etre definis AVANT les connexions."));
                    }
                    // On reste en phase generateur
                    
                } else if (motCle.equals(MOT_CLE_MAISON)) {
                    if (phase.equals("generateur") && reseau.getReseauElectrique().getGenerateurs().isEmpty()) {
                        throw new IOException(formatErreur(numeroLigne, "Ordre invalide", 
                            "Toutes les maisons doivent etre definies APRES les generateurs. " +
                            "Aucun generateur n'a ete defini."));
                    }
                    if (phase.equals("connexion")) {
                        throw new IOException(formatErreur(numeroLigne, "Ordre invalide", 
                            "Toutes les maisons doivent etre definies AVANT les connexions."));
                    }
                    phase = "maison"; // Transition vers phase maison
                    
                } else if (motCle.equals(MOT_CLE_CONNEXION)) {
                    if (phase.equals("generateur") && reseau.getReseauElectrique().getGenerateurs().isEmpty()) {
                        throw new IOException(formatErreur(numeroLigne, "Ordre invalide", 
                            "Toutes les connexions doivent etre definies APRES les generateurs. " +
                            "Aucun generateur n'a ete defini."));
                    }
                    if (phase.equals("generateur") || 
                        (phase.equals("maison") && reseau.getReseauElectrique().getMaisons().isEmpty())) {
                        throw new IOException(formatErreur(numeroLigne, "Ordre invalide", 
                            "Toutes les connexions doivent etre definies APRES les maisons. " +
                            "Aucune maison n'a ete definie."));
                    }
                    phase = "connexion"; // Transition vers phase connexion
                }

                // === ETAPE 4 : Maintenant valider la SYNTAXE de la ligne ===
                
                // Verifier les espaces
                if (line.contains(" ") || line.contains("\t")) {
                    throw new IOException(formatErreur(numeroLigne, "Espaces interdits", 
                        "La ligne contient des espaces ou tabulations. Format attendu sans espace."));
                }

                // Verifier le point final
                if (!line.endsWith(".")) {
                    throw new IOException(formatErreur(numeroLigne, "Point manquant", 
                        "Chaque ligne doit se terminer par un point '.'"));
                }

                // Verifier la structure des parentheses
                int indiceOuvrante = line.indexOf('(');
                int indiceFermante = line.lastIndexOf(')');
                
                if (indiceOuvrante == -1) {
                    throw new IOException(formatErreur(numeroLigne, "Parenthese manquante", 
                        "Parenthese ouvrante '(' manquante apres '" + motCle + "'"));
                }
                
                if (indiceFermante == -1 || indiceFermante < indiceOuvrante) {
                    throw new IOException(formatErreur(numeroLigne, "Parenthese manquante", 
                        "Parenthese fermante ')' manquante ou mal placee"));
                }

                // Verifier que le point est juste apres la parenthese fermante
                if (indiceFermante != line.length() - 2) {
                    throw new IOException(formatErreur(numeroLigne, "Syntaxe invalide", 
                        "Le point '.' doit etre immediatement apres la parenthese fermante ')'"));
                }

                // === ETAPE 5 : Extraire et valider les parametres ===
                String contenu = line.substring(indiceOuvrante + 1, indiceFermante);
                String[] params = contenu.split(",");

                if (params.length != 2) {
                    throw new IOException(formatErreur(numeroLigne, "Parametres invalides", 
                        "Exactement 2 parametres requis, separes par une virgule. Trouve: " + params.length));
                }

                String param1 = params[0].trim().toUpperCase();
                String param2 = params[1].trim().toUpperCase();

                if (param1.isEmpty() || param2.isEmpty()) {
                    throw new IOException(formatErreur(numeroLigne, "Parametre vide", 
                        "Les parametres ne peuvent pas etre vides"));
                }

                // === ETAPE 6 : Traiter selon le mot-cle ===
                try {
                    if (motCle.equals(MOT_CLE_GENERATEUR)) {
                        // Valider la capacite
                        double capacite;
                        try {
                            capacite = Double.parseDouble(param2);
                        } catch (NumberFormatException e) {
                            throw new IOException(formatErreur(numeroLigne, "Capacite invalide", 
                                "'" + params[1] + "' n'est pas un nombre valide"));
                        }
                        
                        if (capacite <= 0) {
                            throw new IOException(formatErreur(numeroLigne, "Capacite invalide", 
                                "La capacite doit etre un nombre positif. Trouve: " + capacite));
                        }
                        
                        reseau.ajouterOuModifierGenerateur(param1, capacite);

                    } else if (motCle.equals(MOT_CLE_MAISON)) {
                        // Valider le type de consommation
                        TypeConsommation type;
                        try {
                            type = TypeConsommation.valueOf(param2);
                        } catch (IllegalArgumentException e) {
                            throw new IOException(formatErreur(numeroLigne, "Type invalide", 
                                "'" + param2 + "' n'est pas un type valide. Utilisez: BASSE, NORMAL ou FORTE"));
                        }
                        
                        reseau.ajouterOuModifierMaison(param1, type);

                    } else if (motCle.equals(MOT_CLE_CONNEXION)) {
                        reseau.creerConnexion(param1, param2);
                    }
                    
                } catch (Exception e) {
                    if (e.getMessage().contains("Ligne")) {
                        throw new IOException(e.getMessage());
                    }
                    throw new IOException(formatErreur(numeroLigne, "Erreur", e.getMessage()));
                }
            }
            
            // Validation finale du reseau
            String problems = reseau.verifierValiditeReseau();
            if (problems.length() != 0) {
                System.err.println(JAUNE + "[VALIDATION]" + RESET + " Problemes detectes dans le reseau :");
                System.err.println(problems);
                return null;
            }

        } catch (FileNotFoundException e) {
            System.err.println(ROUGE + "[ERREUR]" + RESET + " Fichier introuvable : " + f.getPath());
            return null;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println(ROUGE + "[ERREUR]" + RESET + " Exception inattendue : " + e.getMessage());
            return null;
        }

        return reseau;
    }

    /**
     * Extrait le mot-cle d'une ligne (avant la parenthese ouvrante).
     * 
     * @param ligne La ligne a analyser
     * @return Le mot-cle en minuscules, ou null si pas de parenthese
     */
    private static String extraireMotCle(String ligne) {
        int indiceParenthese = ligne.indexOf('(');
        if (indiceParenthese <= 0) {
            return null;
        }
        return ligne.substring(0, indiceParenthese).toLowerCase();
    }

    /**
     * Suggere un mot-cle valide basé sur la similarité.
     * 
     * @param motCle Le mot-cle invalide saisi
     * @return Une suggestion ou null
     */
    private static String suggererMotCle(String motCle) {
        String motCleLower = motCle.toLowerCase();
        
        // Verifier si ca ressemble a "generateur"
        if (motCleLower.startsWith("gen") || motCleLower.contains("gener") || 
            motCleLower.contains("gene") || calculerSimilarite(motCleLower, MOT_CLE_GENERATEUR) > 0.5) {
            return MOT_CLE_GENERATEUR;
        }
        
        // Verifier si ca ressemble a "maison"
        if (motCleLower.startsWith("mai") || motCleLower.contains("mais") || 
            motCleLower.contains("aison") || calculerSimilarite(motCleLower, MOT_CLE_MAISON) > 0.5) {
            return MOT_CLE_MAISON;
        }
        
        // Verifier si ca ressemble a "connexion"
        if (motCleLower.startsWith("con") || motCleLower.contains("connex") || 
            motCleLower.contains("nexion") || calculerSimilarite(motCleLower, MOT_CLE_CONNEXION) > 0.5) {
            return MOT_CLE_CONNEXION;
        }
        
        return null;
    }

    /**
     * Calcule un score de similarité simple entre deux chaines.
     * 
     * @param s1 Premiere chaine
     * @param s2 Deuxieme chaine
     * @return Score entre 0 et 1
     */
    private static double calculerSimilarite(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        
        int matchCount = 0;
        int minLen = Math.min(s1.length(), s2.length());
        
        for (int i = 0; i < minLen; i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                matchCount++;
            }
        }
        
        return (double) matchCount / maxLen;
    }

    /**
     * Formate un message d'erreur avec numero de ligne et details.
     * 
     * @param ligne Numero de la ligne
     * @param type Type d'erreur
     * @param message Description de l'erreur
     * @return Message formate
     */
    private static String formatErreur(int ligne, String type, String message) {
        return ROUGE + "[ERREUR]" + RESET + " Ligne " + ligne + " - " + 
               JAUNE + type + RESET + " : " + message;
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
            return null;
        }
        
        int indiceOuvrante = ligne.indexOf('(');
        int indiceFermante = ligne.indexOf(')', indiceOuvrante + 1);

        if (indiceOuvrante == -1 || indiceFermante == -1) {
            return null;
        }

        String contenu = ligne.substring(indiceOuvrante + 1, indiceFermante);
        String[] info = contenu.replaceAll("[\\s\\u00A0]+", "").toUpperCase().split(",");

        if (info.length != 2) {
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
            
            System.out.println("Sauvegarde reussie : " + f.getName());

        } catch (FileNotFoundException e) {
            System.err.println(ROUGE + "[ERREUR]" + RESET + " Fichier inaccessible : " + e.getMessage());
        } catch (IOException e) {
            System.err.println(ROUGE + "[ERREUR]" + RESET + " Echec de l'ecriture : " + e.getMessage());
        }
    }
}