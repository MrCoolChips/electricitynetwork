package up.mi.paa.service;

import java.util.List;

import up.mi.paa.model.Couts;
import up.mi.paa.model.Generateur;
import up.mi.paa.model.Maison;
import up.mi.paa.model.ReseauElectrique;

/**
 * Service responsable du calcul des coûts et de l'évaluation de l'efficacité d'un réseau électrique.
 * Cette classe permet de calculer le coût global d'une configuration donnée en se basant sur deux critères :
 * la dispersion (équilibre des charges) et la surcharge des générateurs.
 * Le calcul prend en compte un paramètre de sévérité (lambda).
 */
public class CalculateurCouts {
	
   /**
    * Le paramètre lambda (λ) qui contrôle la sévérité de la pénalisation 
    * en cas de dépassement de la capacité maximale (surcharge).
    */
   private int lambda;
   
   /**
    * Construit un nouveau calculateur de coûts avec une valeur de pénalisation initiale.
    *
    * @param lambda La valeur initiale de la sévérité de la pénalisation (entier positif).
    */
   public CalculateurCouts(int lambda) {
       this.lambda = lambda;
   }
   
   /**
    * Met à jour la valeur de lambda (sévérité de la pénalisation).
    * Utile pour ajuster dynamiquement la pénalité via une interface graphique (ex: Slider)
    * sans avoir à recréer l'objet calculateur.
    *
    * @param lambda La nouvelle valeur de lambda.
    */
   public void setLambda(int lambda) {
       this.lambda = lambda;
   }
	
   /**
    * Calcule le coût complet pour une instance donnée du réseau électrique.
    *
    * @param reseau Le réseau électrique à évaluer.
    * @return Un objet {@link Couts} contenant le coût global, la dispersion et la surcharge.
    */
   public Couts calculerCout(ReseauElectrique reseau) {
    	double dispersion = calculerDisps(reseau);
    	double surcharge = calculerSurcharge(reseau);
        double coutsGlobale = dispersion + (lambda * surcharge);
        return new Couts(coutsGlobale, dispersion, surcharge);
    }
	
   /**
    * Calcule la pénalisation due aux surcharges des générateurs.
    *
    * @param reseau Le réseau actuel.
    * @return La somme des surcharges normalisées.
    */
   private double calculerSurcharge(ReseauElectrique reseau) {
       double somme = 0.0;
       for (Generateur g : reseau.getGenerateurs()) {
           somme += Math.max(0.0, (getSommeDesDemandesElectriques(g, reseau) - g.getCapaciteMaximale())/g.getCapaciteMaximale());
       }
       
       return somme;
   }
   
   /**
    * Calcule la dispersion (déséquilibre) des charges entre les générateurs.
    * Mesure l'écart absolu entre le taux d'utilisation de chaque générateur et la moyenne.
    *
    * @param reseau Le réseau actuel.
    * @return La somme des écarts absolus.
    */
   private double calculerDisps(ReseauElectrique reseau) {
       double tauxDUtilisation = calculerLeTauxDUtilisationGlobale(reseau);
       double somme = 0.0;
       for (Generateur g : reseau.getGenerateurs()) {
           somme += Math.abs(calculerLeTauxDUtilisation(g, reseau) - tauxDUtilisation);
       }
       
       return somme;
   }
   
   /**
    * Calcule la somme des demandes électriques (consommation) des maisons connectées à un générateur.
    * Cette méthode est publique pour permettre l'affichage des charges dans l'interface utilisateur.
    *
    * @param g Le générateur concerné.
    * @param reseau Le réseau contenant les connexions.
    * @return La charge totale en kW.
    */
   public double getSommeDesDemandesElectriques(Generateur g, ReseauElectrique reseau) {
       List<Maison> m = reseau.trouverLesMaisonsDeGenerateur(g);
       double sommeDesDemandesElectriques = 0.0;
       for (int i = 0; i < m.size(); i++) {
           sommeDesDemandesElectriques += m.get(i).getConsommation();
       }
       
       return sommeDesDemandesElectriques;
   }
   
   /**
    * Calcule le taux d'utilisation individuel d'un générateur.
    * Le taux est le rapport entre la demande totale et la capacité maximale.
    *
    * @param g Le générateur concerné.
    * @param reseau Le réseau actuel.
    * @return Le taux d'utilisation (ex: 1.0 = 100%, 1.2 = 120%).
    * @throws ArithmeticException Si la capacité du générateur est 0.
    */
   private double calculerLeTauxDUtilisation(Generateur g, ReseauElectrique reseau) {
       if(g.getCapaciteMaximale() == 0) {
           throw new ArithmeticException("La capacite du generateur " + g.getNom() + " ne peut pas etre 0");
       }
       return getSommeDesDemandesElectriques(g, reseau) / g.getCapaciteMaximale();
   }
   
   /**
    * Calcule le taux d'utilisation moyen de tous les générateurs du réseau.
    *
    * @param reseau Le réseau actuel.
    * @return La moyenne des taux d'utilisation.
    * @throws ArithmeticException Si le réseau ne contient aucun générateur.
    */
   private double calculerLeTauxDUtilisationGlobale(ReseauElectrique reseau) {
    	List<Generateur> listeDeGenerateurs = reseau.getGenerateurs();
       if(listeDeGenerateurs.isEmpty()) {
           throw new ArithmeticException("Aucun generateur dans le reseau");
       }
       
       double tauxDUtilisation = 0.0;
               
       for (int i = 0; i < listeDeGenerateurs.size(); i++) {
           tauxDUtilisation += calculerLeTauxDUtilisation(listeDeGenerateurs.get(i), reseau);
       }
       
       return tauxDUtilisation / listeDeGenerateurs.size();
       
   }

}