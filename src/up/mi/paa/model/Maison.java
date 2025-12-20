package up.mi.paa.model;

import java.util.Objects;

/**
 * Représente une maison consommatrice d'électricité dans le réseau.
 * 
 * <p>Une maison est caractérisée par :
 * <ul>
 *   <li>Un nom unique (normalisé en majuscules)</li>
 *   <li>Un type de consommation définissant sa demande énergétique</li>
 * </ul>
 * 
 * <p>L'égalité entre deux maisons est basée uniquement sur leur nom,
 * indépendamment de la casse. Cette classe est compatible avec les collections
 * {@code HashSet} et {@code HashMap}.
 * 
 * @author Groupe 10
 * @version 1.0
 * @see TypeConsommation
 * @see ReseauElectrique
 */
public class Maison {

    private final String nom;
    private TypeConsommation typeConsommation;

    /**
     * Construit une nouvelle maison avec le nom et le type de consommation spécifiés.
     *
     * @param nom              le nom unique de la maison (sera normalisé en majuscules)
     * @param typeConsommation le type de consommation énergétique
     * @throws IllegalArgumentException si le nom est null ou vide
     */
    public Maison(String nom, TypeConsommation typeConsommation) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la maison ne peut pas être null ou vide");
        }
        this.nom = nom.toUpperCase().trim();
        this.typeConsommation = typeConsommation;
    }

    /**
     * Retourne le nom de la maison.
     *
     * @return le nom normalisé en majuscules
     */
    public String getNom() {
        return nom;
    }

    /**
     * Retourne le type de consommation de la maison.
     *
     * @return le type de consommation actuel
     */
    public TypeConsommation getTypeConsommation() {
        return typeConsommation;
    }

    /**
     * Retourne la consommation en kW de cette maison.
     *
     * @return la valeur de consommation définie par le type
     */
    public int getConsommation() {
        return typeConsommation.demande();
    }

    /**
     * Modifie le type de consommation de la maison.
     *
     * @param typeConsommation le nouveau type de consommation
     */
    public void setTypeConsommation(TypeConsommation typeConsommation) {
        this.typeConsommation = typeConsommation;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %d kW)", nom, typeConsommation.name(), typeConsommation.demande());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Maison)) return false;
        return nom.equals(((Maison) o).nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom);
    }
}
