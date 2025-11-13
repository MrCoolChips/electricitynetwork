# Gestionnaire de Réseau Électrique

> Projet de Programmation Avancée et Application

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)

## Description

Application Java en ligne de commande pour la gestion et l'optimisation d'un réseau électrique. Le système permet de modéliser des générateurs, des maisons avec différents types de consommation, et d'analyser l'efficacité du réseau via des calculs de coût, de surcharge et de dispersion.

## Fonctionnalités

### Gestion du Réseau
- Ajout et modification de générateurs (nom, capacité en kW)
- Ajout et modification de maisons (nom, type de consommation)
- Création de connexions entre générateurs et maisons
- Suppression de connexions
- Modification de connexions existantes
- Validation de l'intégrité du réseau

### Analyse et Optimisation
- Calcul du coût global du réseau
- Calcul de la surcharge des générateurs
- Calcul de la dispersion (équilibrage de charge)
- Taux d'utilisation par générateur et global
- Affichage détaillé de l'état du réseau

### Types de Consommation
- **BASSE** : 10 kW
- **NORMAL** : 20 kW
- **FORTE** : 40 kW

## Architecture

Le projet suit une architecture en couches respectant les principes de séparation des responsabilités :

```
src/up/mi/paa/
├── Main.java                          # Point d'entrée de l'application
│
├── model/                             # Modèle de données
│   ├── Generateur.java                # Entité générateur
│   ├── Maison.java                    # Entité maison
│   ├── ReseauElectrique.java          # Gestion du réseau
│   └── TypeConsommation.java          # Énumération des types
│
├── service/                           # Logique
│   └── GestionnaireReseau.java        # CRUD et calculs
│
├── ui/                                # Interface utilisateur
│   └── MenuCLI.java                   # Menus et dialogues
│
└── exception/                         # Exceptions
    ├── ConnexionExistanteException.java
    ├── ConnexionIntrouvableException.java
    ├── FormatInvalideException.java
    ├── GenerateurIntrouvableException.java
    └── MaisonIntrouvableException.java
```

## Installation et Exécution

### Prérequis
- Java JDK 8 ou supérieur
- Windows (pour `run.bat`) ou adaptez pour Linux/Mac

### Compilation et Exécution Rapide

**Option 1 : Utiliser le script (Windows)**
```bash
# Double-cliquer sur run.bat
# OU en ligne de commande :
run.bat
```

**Option 2 : Compilation manuelle (Windows)**
```bash
# Compilation
javac -d bin -encoding UTF-8 src\up\mi\paa\*.java src\up\mi\paa\model\*.java src\up\mi\paa\exception\*.java src\up\mi\paa\service\*.java src\up\mi\paa\ui\*.java

# Exécution
java -cp bin up.mi.paa.Main
```

**Option 3 : Linux/Mac**
```bash
# Compilation
javac -d bin -encoding UTF-8 src/up/mi/paa/*.java src/up/mi/paa/model/*.java src/up/mi/paa/exception/*.java src/up/mi/paa/service/*.java src/up/mi/paa/ui/*.java

# Exécution
java -cp bin up.mi.paa.Main
```

## Guide d'Utilisation

### Menu Principal

```
┌────────────────────────────────────────────────┐
│              MENU PRINCIPAL                    │
├────────────────────────────────────────────────┤
│  1 | Ajouter un generateur                     │
│  2 | Ajouter une maison                        │
│  3 | Ajouter une connexion                     │
│  4 | Supprimer une connexion                   │
│  5 | Fin                                       │
└────────────────────────────────────────────────┘
```

### Exemples d'Utilisation

**1. Ajouter un générateur**
```
> Nom et capacite (ex: G1 60) : G1 60
[OK] Generateur G1 cree !
```

**2. Ajouter une maison**
```
Types de consommation: BASSE, NORMAL, FORTE
> Nom et Consommation (ex: M1 FORTE) : M1 FORTE
[OK] Maison M1 creee !
```

**3. Créer une connexion**
```
> Generateur et maison (ex: G1 M1 ou M1 G1) : G1 M1
[OK] Connexion creee !
```

**4. Supprimer une connexion**
```
> Generateur et maison (ex: G1 M1 ou M1 G1) : G1 M1
[OK] Connexion supprimee !
```

### Menu d'Évaluation

Après validation du réseau, accédez aux fonctionnalités d'analyse :

```
┌────────────────────────────────────────────────┐
│           EVALUATION DU RESEAU                 │
├────────────────────────────────────────────────┤
│  1 | Calculer le cout du reseau                │
│  2 | Modifier une connexion                    │
│  3 | Afficher le reseau                        │
│  4 | Fin                                       │
└────────────────────────────────────────────────┘
```

## Formules de Calcul

### Coût Total
```
Coût = dispersion + (λ × surcharge)
où λ = 10 (constante de pénalité)
```

### Surcharge
```
surcharge = Σ max(0, (demande - capacité) / capacité)
```

### Dispersion
```
dispersion = Σ |taux_utilisation(générateur) - taux_moyen|
```

### Taux d'Utilisation
```
taux = demande_totale / capacité_maximale
```

## Gestion des Erreurs

Le système intègre une gestion robuste des erreurs avec 5 exceptions personnalisées :

- `FormatInvalideException` : Format d'entrée incorrect
- `GenerateurIntrouvableException` : Générateur non trouvé
- `MaisonIntrouvableException` : Maison non trouvée
- `ConnexionExistanteException` : Connexion déjà établie
- `ConnexionIntrouvableException` : Connexion inexistante

Tous les messages d'erreur sont préfixés par `[ERREUR]` pour une meilleure lisibilité.

## Validation du Réseau

Le réseau est considéré comme valide si :
- Toutes les maisons sont connectées à exactement un générateur
- Tous les générateurs et maisons existent

Les erreurs de validation sont affichées avec le préfixe `[ATTENTION]`.

## Interface Utilisateur

L'interface CLI utilise des éléments visuels ASCII pour améliorer l'expérience :

- Bordures décoratives (`╔═══╗`, `┌───┐`)
- Tags de statut (`[OK]`, `[ERREUR]`, `[ATTENTION]`)
- Séparateurs visuels (`─────`)
- Prompts clairs (`> Votre choix :`)

## Tests

### Scénario de Test Complet

```
1. Ajouter G1 avec capacité 60 kW
2. Ajouter G2 avec capacité 80 kW
3. Ajouter M1 (BASSE - 10 kW)
4. Ajouter M2 (NORMAL - 20 kW)
5. Ajouter M3 (FORTE - 40 kW)
6. Ajouter M4 (NORMAL - 20 kW)
7. Connecter M1 à G1
8. Connecter M2 à G1
9. Connecter M3 à G2
10. Connecter M4 à G2
11. Supprimer la connexion M4-G2
12. Reconnecter M4 à G1
13. Valider le réseau
14. Calculer le coût initial
15. Modifier la connexion M3 : G2 → G1
16. Recalculer le coût et observer la différence
17. Afficher l'état final du réseau
```

## Améliorations Possibles

- [ ] Sauvegarde et chargement de réseaux
- [ ] Export des résultats en CSV/JSON
- [ ] Interface graphique (JavaFX)
- [ ] Algorithme d'optimisation automatique
- [ ] Tests unitaires avec JUnit

## Documentation

La documentation complète du code est disponible via Javadoc :

```bash
# Générer la documentation
javadoc -d docs -encoding UTF-8 -charset UTF-8 -sourcepath src -subpackages up.mi.paa
```

## Création d'un JAR

```bash
# 1. Compiler
javac -encoding UTF-8 -d bin src/up/mi/paa/*.java src/up/mi/paa/model/*.java src/up/mi/paa/service/*.java src/up/mi/paa/ui/*.java src/up/mi/paa/exception/*.java

# 2. Créer le JAR
cd bin
jar cfm ReseauElectrique.jar MANIFEST.MF up/mi/paa/*.class up/mi/paa/model/*.class up/mi/paa/service/*.class up/mi/paa/ui/*.class up/mi/paa/exception/*.class
cd ..

# 3. Lancer
java -jar bin/ReseauElectrique.jar
```

## Auteurs

- Ali GOUARAB
- Egemen YAPSIK
- DAI Jérôme


