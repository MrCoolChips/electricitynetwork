package up.mi.paa.model;

import java.util.Objects;

/**
 * Représente un générateur électrique dans le réseau de distribution.
 * 
 * <p>Un générateur est caractérisé par :
 * <ul>
 *   <li>Un nom unique (normalisé en majuscules)</li>
 *   <li>Une capacité maximale de production en kW</li>
 * </ul>
 * 
 * <p>L'égalité entre deux générateurs est basée uniquement sur leur nom,
 * indépendamment de la casse. Cette classe est compatible avec les collections
 * {@code HashSet} et {@code HashMap}.
 * 
 * @author Groupe 10
 * @version 1.0
 * @see ReseauElectrique
 */
public class Generateur {

    private final String nom;
    private double capaciteMaximale;

    /**
     * Construit un nouveau générateur avec le nom et la capacité spécifiés.
     *
     * @param nom              le nom unique du générateur (sera normalisé en majuscules)
     * @param capaciteMaximale la capacité maximale de production en kW (doit être positive)
     * @throws IllegalArgumentException si le nom est null ou vide
     */
    public Generateur(String nom, double capaciteMaximale) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du générateur ne peut pas être null ou vide");
        }
        this.nom = nom.toUpperCase().trim();
        this.capaciteMaximale = capaciteMaximale;
    }

    /**
     * Retourne le nom du générateur.
     *
     * @return le nom normalisé en majuscules
     */
    public String getNom() {
        return nom;
    }

    /**
     * Retourne la capacité maximale de production.
     *
     * @return la capacité maximale en kW
     */
    public double getCapaciteMaximale() {
        return capaciteMaximale;
    }

    /**
     * Modifie la capacité maximale de production.
     *
     * @param capacite la nouvelle capacité en kW
     */
    public void setCapaciteMaximale(double capacite) {
        this.capaciteMaximale = capacite;
    }

    @Override
    public String toString() {
        return String.format("%s (%.0f kW)", nom, capaciteMaximale);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Generateur)) return false;
        return nom.equals(((Generateur) o).nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom);
    }
}
