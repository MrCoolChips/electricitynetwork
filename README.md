# Gestionnaire de Réseau Électrique

> Projet de Programmation Avancée et Application - S5 2025

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![JavaFX](https://img.shields.io/badge/JavaFX-007396?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)

---

## Table des matières

1. [Informations Projet](#informations-projet)
2. [Prérequis et Dépendances](#prérequis-et-dépendances)
3. [Installation et Compilation](#installation-et-compilation)
4. [Exécution](#exécution)
5. [Tests Unitaires](#tests-unitaires)
6. [Analyse du Problème](#analyse-du-problème)
7. [Justification du Choix Algorithmique](#justification-du-choix-algorithmique)
8. [Description de l'Algorithme](#description-de-lalgorithme)
9. [Analyse de Complexité](#analyse-de-complexité)
10. [Références et Sources](#références-et-sources)
11. [Fonctionnalités](#fonctionnalités)
12. [Architecture](#architecture)
13. [Format de Fichier](#format-de-fichier)
14. [Formules de Calcul](#formules-de-calcul)
15. [Auteurs](#auteurs)

---

## Informations Projet

### Arborescence du Projet

```
electricitynetwork/
│
├── src/                                   CODE SOURCE
│   └── up/mi/paa/
│       ├── Main.java                      Point d'entrée
│       ├── model/                         Entités métier
│       │   ├── Generateur.java
│       │   ├── Maison.java
│       │   ├── TypeConsommation.java
│       │   ├── ReseauElectrique.java
│       │   └── Couts.java
│       ├── service/                       Logique métier
│       │   ├── GestionnaireReseau.java
│       │   ├── CalculateurCouts.java
│       │   └── OptimiseurReseau.java
│       ├── io/                            Entrées/Sorties
│       │   └── GestionnaireFichier.java
│       ├── ui/                            Interfaces utilisateur
│       │   ├── cli/
│       │   │   ├── MenuCLI.java
│       │   │   ├── AfficheurCLI.java
│       │   │   └── StyleCLI.java
│       │   └── gui/
│       │       ├── ReseauElectriqueUI.java
│       │       ├── StyleUI.java
│       │       └── components/
│       │           ├── VueReseau.java
│       │           ├── VueStatistiques.java
│       │           ├── VueInventaire.java
│       │           └── VueTopBar.java
│       ├── util/                          Utilitaires
│       │   └── ComparateurNaturel.java
│       └── exception/                     Exceptions personnalisées
│           ├── FormatInvalideException.java
│           ├── GenerateurIntrouvableException.java
│           ├── MaisonIntrouvableException.java
│           ├── ConnexionExistanteException.java
│           └── ConnexionIntrouvableException.java
│
├── tests/                                 TESTS UNITAIRES
│   └── up/mi/paa/
│       ├── model/
│       │   ├── CoutsTest.java
│       │   ├── GenerateurTest.java
│       │   ├── MaisonTest.java
│       │   ├── ReseauElectriqueTest.java
│       │   └── TypeConsommationTest.java
│       ├── service/
│       │   ├── CalculateurCoutsTest.java
│       │   ├── GestionnaireReseauTest.java
│       │   └── OptimiseurReseauTest.java
│       ├── io/
│       │   └── GestionnaireFichierTest.java
│       └── util/
│           └── ComparateurNaturelTest.java
│
├── libs/                                  DÉPENDANCES
│   ├── javafx/                            JavaFX SDK
│   │   ├── windows/                       JavaFX pour windows
│   │   ├── macos/                         JavaFX pour macOS
│   │   └── linux/                         JavaFX pour Linux
│   └── junit/                             JUnit 5
│
├── docs/                                  Documentation Javadoc
├── assets/                                Ressources
│
└── README.md                              Ce fichier
```

### Point d'Entrée

La classe principale contenant la méthode `main` est :

```
up.mi.paa.Main
```

Fichier source : `src/up/mi/paa/Main.java`

---

## Prérequis et Dépendances

### Java

- **JDK 17** ou supérieur (recommandé : JDK 21)
- Vérifier l'installation : `java --version`

### JavaFX (pour l'interface graphique)

> **Important** : Les bibliothèques JavaFX sont déjà fournies dans le dossier `libs/` pour Windows, macOS et linux. Vous pouvez les utiliser directement sans téléchargement supplémentaire. Si vous souhaitez utiliser une version différente, vous pouvez télécharger JavaFX SDK depuis [openjfx.io](https://openjfx.io/).

| OS | Version | Lien |
|----|---------|------|
| Windows | JavaFX 17 | [Download](https://download2.gluonhq.com/openjfx/17.0.17/openjfx-17.0.17_windows-x64_bin-sdk.zip) |
| macOS | JavaFX 17 | [Download](https://download2.gluonhq.com/openjfx/17.0.17/openjfx-17.0.17_osx-x64_bin-sdk.zip) |
| Linux | JavaFX 17 | [Download](https://download2.gluonhq.com/openjfx/17.0.17/openjfx-17.0.17_linux-x64_bin-sdk.zip) |

Extraire dans `libs/javafx/windows/`, `libs/javafx/macos/` ou `libs/javafx/linux/` selon votre OS.

### JUnit 5 (pour les tests)

Télécharger [junit-platform-console-standalone](https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.14.1/junit-platform-console-standalone-1.14.1.jar) et placer dans `libs/junit/`.

---

## Installation et Compilation

### Windows (PowerShell)

```powershell
# 1. Compiler les sources
javac -encoding UTF-8 -d bin -cp "bin;libs/javafx/windows/lib/*" src/up/mi/paa/util/*.java src/up/mi/paa/model/*.java src/up/mi/paa/exception/*.java src/up/mi/paa/io/*.java src/up/mi/paa/service/*.java src/up/mi/paa/ui/cli/*.java src/up/mi/paa/ui/gui/components/*.java src/up/mi/paa/ui/gui/*.java src/up/mi/paa/Main.java

# 2. Compiler les tests
javac -encoding UTF-8 -d bin -cp "bin;libs/junit/junit-platform-console-standalone-1.14.1.jar" tests/up/mi/paa/model/*.java tests/up/mi/paa/service/*.java tests/up/mi/paa/io/*.java tests/up/mi/paa/util/*.java
```

### macOS (Terminal)

```bash
# 1. Compiler les sources
javac -encoding UTF-8 -d bin -cp "bin:libs/javafx/macos/lib/*" src/up/mi/paa/util/*.java src/up/mi/paa/model/*.java src/up/mi/paa/exception/*.java src/up/mi/paa/io/*.java src/up/mi/paa/service/*.java src/up/mi/paa/ui/cli/*.java src/up/mi/paa/ui/gui/components/*.java src/up/mi/paa/ui/gui/*.java src/up/mi/paa/Main.java

# 2. Compiler les tests
javac -encoding UTF-8 -d bin -cp "bin:libs/junit/junit-platform-console-standalone-1.14.1.jar" tests/up/mi/paa/model/*.java tests/up/mi/paa/service/*.java tests/up/mi/paa/io/*.java tests/up/mi/paa/util/*.java
```

### Linux (Terminal)

```bash
# 1. Compiler les sources
javac -encoding UTF-8 -d bin -cp "bin:libs/javafx/linux/lib/*" src/up/mi/paa/util/*.java src/up/mi/paa/model/*.java src/up/mi/paa/exception/*.java src/up/mi/paa/io/*.java src/up/mi/paa/service/*.java src/up/mi/paa/ui/cli/*.java src/up/mi/paa/ui/gui/components/*.java src/up/mi/paa/ui/gui/*.java src/up/mi/paa/Main.java

# 2. Compiler les tests
javac -encoding UTF-8 -d bin -cp "bin:libs/junit/junit-platform-console-standalone-1.14.1.jar" tests/up/mi/paa/model/*.java tests/up/mi/paa/service/*.java tests/up/mi/paa/io/*.java tests/up/mi/paa/util/*.java
```

> **Note** : Le séparateur de classpath est `;` sous Windows et `:` sous macOS/Linux.

---

## Exécution

### Méthode 1 : Via les classes compilées

```bash
# Afficher l'aide
java -cp bin up.mi.paa.Main --help

# Afficher la version
java -cp bin up.mi.paa.Main --version

# Mode CLI interactif (Partie 1)
java -cp bin up.mi.paa.Main

# Mode CLI avec fichier (Partie 2)
java -cp bin up.mi.paa.Main <fichier.txt> [lambda]

# Mode GUI - Windows (nécessite JavaFX)
java --module-path libs/javafx/windows/lib --add-modules javafx.controls,javafx.fxml -cp bin up.mi.paa.Main --gui

# Mode GUI - macOS (nécessite JavaFX)
java --module-path libs/javafx/macos/lib --add-modules javafx.controls,javafx.fxml -cp bin up.mi.paa.Main --gui

# Mode GUI - Linux (nécessite JavaFX)
java --module-path libs/javafx/linux/lib --add-modules javafx.controls,javafx.fxml -cp bin up.mi.paa.Main --gui
```

### Méthode 2 : Via le JAR exécutable

```bash
# Afficher l'aide
java -jar ReseauElectrique.jar --help

# Afficher la version
java -jar ReseauElectrique.jar --version

# Mode CLI interactif
java -jar ReseauElectrique.jar

# Mode CLI avec fichier
java -jar ReseauElectrique.jar reseau.txt 10

# Mode GUI
java -jar ReseauElectrique.jar --gui
```

### Options de ligne de commande

| Option | Description |
|--------|-------------|
| `--help`, `-h` | Affiche l'aide |
| `--version`, `-v` | Affiche la version |
| `--gui`, `-g` | Lance l'interface graphique |
| `--cli`, `-c` | Lance le mode CLI |
| `<fichier>` | Charge un réseau depuis un fichier |
| `<fichier> <lambda>` | Charge un réseau avec un coefficient lambda personnalisé |
---

## Tests Unitaires

### Exécuter tous les tests

```bash
java -jar libs/junit/junit-platform-console-standalone-1.14.1.jar -cp bin --scan-classpath
```

### Exécuter un test spécifique

```bash
java -jar libs/junit/junit-platform-console-standalone-1.14.1.jar -cp bin --select-class <nom.complet.de.la.classe>

# Exemples :
java -jar libs/junit/junit-platform-console-standalone-1.14.1.jar -cp bin --select-class up.mi.paa.model.GenerateurTest
java -jar libs/junit/junit-platform-console-standalone-1.14.1.jar -cp bin --select-class up.mi.paa.service.OptimiseurReseauTest
```

### Classes de test disponibles

| Package | Classes |
|---------|---------|
| `up.mi.paa.model` | `CoutsTest`, `GenerateurTest`, `MaisonTest`, `ReseauElectriqueTest`, `TypeConsommationTest` |
| `up.mi.paa.service` | `CalculateurCoutsTest`, `GestionnaireReseauTest`, `OptimiseurReseauTest` |
| `up.mi.paa.io` | `GestionnaireFichierTest` |
| `up.mi.paa.util` | `ComparateurNaturelTest` |

---

## Analyse du Problème

### Énoncé

Soit un réseau électrique défini par le triplet S = (M, G, C) où :
- **M** représente l'ensemble des maisons, chacune caractérisée par une consommation électrique appartenant à {BASSE (10 kW), NORMAL (20 kW), FORTE (40 kW)}
- **G** représente l'ensemble des générateurs, chacun disposant d'une capacité maximale de production
- **C** représente l'ensemble des connexions, où chaque connexion associe une maison à un unique générateur

L'objectif consiste à déterminer une configuration C minimisant la fonction de coût :

```
Coût(C) = Dispersion(C) + λ × Surcharge(C)
```

où :
- La **dispersion** mesure l'écart entre les taux d'utilisation des générateurs et le taux moyen
- La **surcharge** pénalise les générateurs dont la demande excède la capacité
- Le coefficient **λ** pondère l'importance relative de la surcharge

### Classification du Problème

Ce problème appartient à plusieurs familles classiques de l'optimisation combinatoire :

**Problème d'affectation** : Chaque maison doit être affectée à exactement un générateur, ce qui correspond à la structure classique des problèmes d'affectation où l'on cherche à associer des éléments d'un ensemble A à des éléments d'un ensemble B.

**Problème de Bin Packing** : Les générateurs peuvent être vus comme des "conteneurs" de capacité limitée dans lesquels on doit placer des "objets" (les consommations des maisons). Cette analogie est renforcée par la contrainte de capacité.

**Problème d'équilibrage de charge** : La composante de dispersion dans la fonction de coût traduit un objectif d'équilibrage entre les générateurs, similaire aux problèmes de répartition de charge entre serveurs.

### Complexité Combinatoire

L'espace des solutions possibles croît exponentiellement avec la taille du problème. Pour n maisons et g générateurs, le nombre de configurations possibles est :

```
|Espace de recherche| = g^n
```

Cette croissance exponentielle rend l'énumération de toutes les solutions impraticable dès que n dépasse quelques dizaines d'éléments. ce problème est donc **NP-difficile**, ce qui justifie le recours à des méthodes approchées.

---

## Justification du Choix Algorithmique

### Approches Envisageables

Face à un problème d'optimisation combinatoire NP-difficile, trois grandes familles d'approches peuvent être considérées :

**Les méthodes exactes** (Programmation Linéaire en Nombres Entiers) garantissent l'obtention de la solution optimale mais présentent une complexité temporelle exponentielle dans le pire cas. impraticables pour des instances de grande taille.

**Les heuristiques constructives** (algorithmes gloutons) construisent une solution de manière incrémentale en effectuant à chaque étape un choix localement optimal. Ces méthodes sont rapides et simples à implémenter mais ne garantissent pas l'optimalité globale et peuvent rester bloquées dans un optimum local.

**Les métaheuristiques** (Recuit Simulé, Algorithmes Génétiques) explorent l'espace des solutions de manière plus sophistiquée, permettant d'échapper aux optima locaux. Elles offrent un compromis entre qualité de la solution et temps de calcul.

### Choix Retenu : Hybridation Glouton + Recuit Simulé

Notre approche combine une heuristique constructive avec une métaheuristique d'amélioration. Ce choix se justifie par plusieurs considérations :

**Qualité de l'initialisation** : L'algorithme glouton Best-Fit Decreasing (meilleur remplissage par ordre décroissant), en triant les maisons par consommation décroissante avant affectation, produit des solutions initiales de bonne qualité. Cette stratégie est connue pour ses bonnes performances sur les problèmes de Bin Packing (remplissage de sacs).

**Capacité d'échappement** : Le recuit simulé, grâce au critère de Metropolis, peut accepter temporairement des dégradations de la fonction objectif. Cette propriété permet d'explorer des régions de l'espace des solutions inaccessibles par simple descente locale.

**Convergence théorique** : Sous certaines conditions sur le schéma de refroidissement, le recuit simulé converge vers l'optimum global. l'algorithme tend vers des solutions de très haute qualité.

**Simplicité et robustesse** : Comparé à d'autres métaheuristiques comme les algorithmes génétiques, le recuit simulé nécessite peu de paramètres et s'adapte facilement à différentes structures de problèmes.

### Comparaison avec l'Algorithme Naïf

L'algorithme naïf proposé initialement présente plusieurs limitations que notre approche corrige :

L'**initialisation aléatoire** de l'algorithme naïf produit des configurations initiales de qualité variable, potentiellement très éloignées de l'optimum. Notre initialisation gloutonne garantit un point de départ de bonne qualité, réduisant significativement le nombre d'itérations nécessaires pour atteindre une solution satisfaisante.

L'**exploration uniforme** de l'algorithme naïf, qui n'accepte que les améliorations strictes, conduit inévitablement à un blocage dans le premier optimum local rencontré. Le critère de Metropolis du recuit simulé permet au contraire d'échapper aux optima locaux pour atteindre potentiellement l'optimum globale.

La **diversité des mouvements** constitue également un avantage de notre approche. Là où l'algorithme naïf se limite aux déplacements simples d'une maison vers un autre générateur, nous introduisons des mouvements d'échange permettant de réorganiser simultanément deux affectations.

---

## Description de l'Algorithme

### Pseudo-code

```
ALGORITHME : Optimisation par Construction Gloutonne et Recuit Simulé

ENTRÉES :
    S = (M, G, C)    Réseau électrique
    λ                Coefficient de pénalisation
    T_max            Durée maximale (ms)

SORTIE :
    S*               Réseau avec configuration optimisée

PHASE 1 : CONSTRUCTION GLOUTONNE

    M_triées ← Trier(M, par consommation décroissante)
    C ← ∅

    POUR CHAQUE maison m DANS M_triées FAIRE
        g_best ← null
        cout_min ← +∞

        POUR CHAQUE générateur g DANS G FAIRE
            C' ← C ∪ {(m, g)}
            cout_candidat ← CalculerCoût(S avec C')

            SI cout_candidat < cout_min ALORS
                cout_min ← cout_candidat
                g_best ← g
            FIN SI
        FIN POUR

        C ← C ∪ {(m, g_best)}
    FIN POUR

PHASE 2 : RECUIT SIMULÉ

    S_best ← S
    cout_best ← CalculerCoût(S)
    T ← 100.0
    α ← 0.999
    t_start ← TempsActuel()

    TANT QUE (TempsActuel() - t_start) < T_max FAIRE

        SI T < 0.001 ALORS
            T ← 10.0
        FIN SI

        SI Random() < 0.4 ALORS
            m1 ← ChoisirAuHasard(M)
            m2 ← ChoisirAuHasard(M)
            g1 ← GénérateurDe(m1)
            g2 ← GénérateurDe(m2)

            SI g1 ≠ g2 ALORS
                S' ← Échanger(S, m1, g1, m2, g2)
            FIN SI
        SINON
            m ← ChoisirAuHasard(M)
            g_ancien ← GénérateurDe(m)
            g_nouveau ← ChoisirAuHasard(G)

            SI g_ancien ≠ g_nouveau ALORS
                S' ← Déplacer(S, m, g_ancien, g_nouveau)
            FIN SI
        FIN SI

        Δ ← CalculerCoût(S') - CalculerCoût(S)

        SI Δ < 0 OU Random() < exp(-Δ / T) ALORS
            S ← S'

            SI CalculerCoût(S) < cout_best ALORS
                S_best ← S
                cout_best ← CalculerCoût(S)
            FIN SI
        FIN SI

        T ← T × α

    FIN TANT QUE

    RETOURNER S_best
```

---

### Explication Ligne par Ligne

#### Phase 1 : Construction Gloutonne

**`M_triées ← Trier(M, par consommation décroissante)`**

Les maisons sont triées par consommation décroissante (FORTE 40kW, puis NORMAL 20kW, puis BASSE 10kW). Cette stratégie, connue sous le nom de Best-Fit Decreasing, place d'abord les éléments les plus contraignants. Cela réduit les risques de mauvaises affectations en fin de construction car les "gros" éléments sont placés quand il reste le plus de choix possibles.

**`C ← ∅`**

On initialise l'ensemble des connexions à vide. Toutes les connexions seront reconstruites par l'algorithme glouton.

**`POUR CHAQUE maison m DANS M_triées FAIRE`**

On parcourt chaque maison dans l'ordre décroissant de consommation.

**`g_best ← null` et `cout_min ← +∞`**

Variables pour mémoriser le meilleur générateur trouvé et le coût minimal associé. En initialisant cout_min à l'infini, on garantit que le premier générateur évalué sera toujours meilleur.

**`POUR CHAQUE générateur g DANS G FAIRE`**

On teste chaque générateur comme candidat pour accueillir la maison courante.

**`C' ← C ∪ {(m, g)}`**

On simule l'affectation de la maison m au générateur g en créant un ensemble de connexions temporaire C'.

**`cout_candidat ← CalculerCoût(S avec C')`**

On évalue le coût global du réseau si cette affectation était réalisée. Le coût inclut la dispersion et la surcharge pondérée par λ.

**`SI cout_candidat < cout_min ALORS ... FIN SI`**

Si cette affectation produit un coût inférieur au meilleur trouvé jusqu'ici, on met à jour le meilleur générateur et le coût minimal.

**`C ← C ∪ {(m, g_best)}`**

Une fois tous les générateurs évalués, on affecte définitivement la maison au générateur qui minimise le coût. Ce choix est irrévocable dans la phase gloutonne.

À la fin de la Phase 1, toutes les maisons sont connectées. La solution obtenue est généralement de bonne qualité (90-95% de l'optimal) mais peut être améliorée.

---

#### Phase 2 : Recuit Simulé

**`S_best ← S` et `cout_best ← CalculerCoût(S)`**

On sauvegarde la solution gloutonne comme meilleure solution connue. Cette sauvegarde élitiste garantit qu'on ne perdra jamais une bonne solution même si l'exploration dégrade temporairement la configuration courante.

**`T ← 100.0`**

T est la température, paramètre central du recuit simulé. Une température élevée (100) signifie que le système est "chaud" et accepte facilement des dégradations, favorisant l'exploration de l'espace des solutions.

**`α ← 0.999`**

α est le facteur de refroidissement. À chaque itération, la température est multipliée par α. Une valeur proche de 1 (0.999) assure un refroidissement lent, laissant le temps à l'algorithme d'explorer avant de converger.

**`t_start ← TempsActuel()`**

On mémorise l'instant de départ pour contrôler la durée d'exécution.

**`TANT QUE (TempsActuel() - t_start) < T_max FAIRE`**

La boucle principale s'exécute jusqu'à épuisement du temps alloué (T_max = 3000 ms dans notre implémentation).

**`SI T < 0.001 ALORS T ← 10.0 FIN SI`**

Mécanisme de réchauffage : si la température devient trop basse, on la remonte à 10. Cela permet de relancer l'exploration si l'algorithme s'est prématurément figé dans un optimum local.

**`SI Random() < 0.4 ALORS ... SINON ... FIN SI`**

Choix probabiliste du type de mouvement : 40% d'échanges, 60% de déplacements. Cette diversité de mouvements améliore l'exploration.

**Mouvement de type Échange (40% des cas) :**

On sélectionne deux maisons au hasard (m1, m2) et on identifie leurs générateurs respectifs (g1, g2). Si elles sont sur des générateurs différents, on les échange : m1 passe sur g2 et m2 passe sur g1. Ce mouvement permet des réorganisations plus profondes qu'un simple déplacement.

**Mouvement de type Déplacement (60% des cas) :**

On sélectionne une maison au hasard (m) et un nouveau générateur au hasard (g_nouveau). Si ce n'est pas le même que l'actuel, on déplace la maison vers ce nouveau générateur. C'est le mouvement élémentaire de base.

**`Δ ← CalculerCoût(S') - CalculerCoût(S)`**

Δ (delta) représente la variation de coût entre la nouvelle configuration S' et la configuration actuelle S. Si Δ < 0, la nouvelle solution est meilleure. Si Δ > 0, elle est pire.

**`SI Δ < 0 OU Random() < exp(-Δ / T) ALORS`**

C'est le critère de Metropolis, cœur du recuit simulé :
- Si Δ < 0 (amélioration) : on accepte toujours
- Si Δ > 0 (dégradation) : on accepte avec probabilité exp(-Δ/T)

La probabilité exp(-Δ/T) décroît quand Δ augmente (grandes dégradations moins acceptées) et quand T diminue (système plus rigide à basse température). Ce mécanisme permet d'échapper aux optima locaux en acceptant temporairement des solutions moins bonnes.

**`S ← S'`**

On adopte la nouvelle configuration.

**`SI CalculerCoût(S) < cout_best ALORS S_best ← S ... FIN SI`**

Si la solution acceptée est la meilleure jamais rencontrée, on la sauvegarde. Cela garantit que le résultat final sera au moins aussi bon que la meilleure solution intermédiaire.

**`T ← T × α`**

Refroidissement géométrique : la température diminue progressivement. Après k itérations : T_k = T_0 × α^k. Par exemple avec T_0 = 100 et α = 0.999 : après 1000 itérations T ≈ 36.8, après 5000 itérations T ≈ 0.67.

**`RETOURNER S_best`**

On retourne la meilleure solution trouvée (et non la dernière visitée, qui peut être moins bonne à cause des dégradations acceptées).

---

### Optimisations Techniques Implémentées

**Cache des charges** : Pour éviter de recalculer intégralement la charge de chaque générateur à chaque évaluation, nous maintenons un tableau associatif mémorisant la charge courante. Lors d'un mouvement, seules les charges des générateurs impliqués sont mises à jour.

**Évaluation incrémentale** : Le calcul du coût après un mouvement exploite la localité de la modification. Plutôt que de recalculer la dispersion et la surcharge globales, nous mettons à jour uniquement les contributions des générateurs affectés.

**Sauvegarde élitiste** : La meilleure configuration rencontrée au cours de l'exécution est systématiquement conservée, garantissant que le résultat final soit au moins aussi bon que la meilleure solution intermédiaire.

---

## Analyse de Complexité

### Complexité Temporelle

**Phase gloutonne** : Pour chaque maison parmi n, on évalue g générateurs, chaque évaluation nécessitant un parcours des g générateurs pour le calcul du coût. La complexité résultante est O(n × g²), réduite à O(n × g) avec l'évaluation incrémentale.

**Phase de recuit simulé** : Chaque itération effectue un mouvement et une évaluation en O(g). Le nombre d'itérations k dépend du temps alloué T_max. La complexité est donc O(k × g).

**Complexité globale** : O(n × g + k × g) = O((n + k) × g)

### Complexité Spatiale

L'algorithme maintient en mémoire :
- La configuration courante : O(n) pour les affectations maison-générateur
- Le cache des charges : O(g) pour les charges de chaque générateur
- La meilleure configuration : O(n) pour la sauvegarde

La complexité spatiale totale est donc O(n + g).

---

## Références et Sources

### Recuit Simulé

Wikipédia. Recuit simulé. Wikipédia, l’encyclopédie libre. https://fr.wikipedia.org/wiki/Recuit_simul%C3%A9

Recuit simulé. LIRIS – CNRS. https://perso.liris.cnrs.fr/pierre-edouard.portier/teaching_2015_2016/ia/sima/sima.html

Wanabilini. Sur la route de l’optimum : recuit simulé pour le TSP. Medium. https://medium.com/wanabilini/sur-la-route-de-loptimum-recuit-simul%C3%A9-pour-le-tsp-9cb037e74979

### Algorithmes Gloutons

LABRI. Problème de Bin Packing. Département d’informatique, Université de Bordeaux. https://dept-info.labri.fr/ENSEIGNEMENT/projet2/supports/Bin-Packing/probleme-bin-packing.pdf

Wikipédia. Algorithme glouton. Wikipédia, l’encyclopédie libre. https://fr.wikipedia.org/wiki/Algorithme_glouton

Wikipédia. Problème de Bin Packing. Wikipédia, l’encyclopédie libre. https://fr.wikipedia.org/wiki/Probl%C3%A8me_de_bin_packing

---

## Fonctionnalités

### Partie 1 : Mode CLI Interactif

- **Gestion des générateurs** : Ajout, modification et suppression
- **Gestion des maisons** : Ajout avec type de consommation (BASSE, NORMAL, FORTE)
- **Gestion des connexions** : Création et suppression de liens maison-générateur
- **Validation du réseau** : Vérification de l'intégrité avant évaluation
- **Calcul du coût** : Évaluation de la dispersion et surcharge

### Partie 2 : Mode Fichier avec Optimisation

- **Lecture de fichier** : Import de fichiers avec validation syntaxique
- **Gestion des erreurs** : Messages détaillés avec numéro de ligne
- **Optimisation automatique** : Algorithme Glouton + Recuit Simulé
- **Sauvegarde** : Export de la solution optimisée

### Interface Graphique (JavaFX)

- **Visualisation du réseau** : Représentation graphique des connexions
- **Statistiques en temps réel** : Affichage des coûts et taux d'utilisation
- **Inventaire** : Liste des générateurs et maisons
- **Optimisation interactive** : Lancement et visualisation de l'optimisation

### Tests unitaires
- **Tests unitaires** : Couverture complète avec JUnit 5 (158 tests)

### Documentation
- **JavaDoc** : Javadoc complète pour toutes les classes

---

## Architecture du Projet

Le projet adopte une architecture en couches respectant le principe de séparation des responsabilités. Cette organisation facilite la maintenance, les tests et l'évolution du code.

### Description des Couches

**Couche Modèle** : Définit les structures de données représentant le domaine métier. Le réseau électrique est modélisé comme un graphe biparti où les arêtes représentent les connexions maison-générateur.

**Couche Service** : Contient la logique métier indépendante de l'interface utilisateur. L'algorithme d'optimisation est encapsulé dans `OptimiseurReseau`, permettant son utilisation dans différents contextes (CLI, GUI, tests).

**Couche IO** : Gère la persistance et la sérialisation. Le format fichier choisi offre une syntaxe lisible et facilement parsable.

**Couche UI** : Gère l'interaction avec l'utilisateur. La séparation permet d'envisager différentes interfaces (CLI, JavaFX) partageant la même logique métier.

**Exceptions** : Hiérarchie d'exceptions spécifiques au domaine, permettant une gestion fine des erreurs et des messages explicites.

---

## Format de Fichier

Le format de fichier utilise une syntaxe Prolog :

```
generateur(G1,100).
generateur(G2,80).
maison(M1,FORTE).
maison(M2,NORMAL).
connexion(G1,M1).
connexion(G2,M2).
```

**Règles :**
- Ordre obligatoire : générateurs → maisons → connexions
- Noms alphanumériques uniquement
- Chaque ligne termine par un point `.`

---

## Formules de Calcul

### Coût Total
```
Coût = dispersion + (λ × surcharge)
où λ = 10 (par défaut)
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

---

## Auteurs

**Groupe 10**

- Ali GOUARAB
- Egemen YAPSIK
- DAI Jérôme

---

*Projet réalisé dans le cadre du cours de Programmation Avancée et Application - Semestre 5 - 2025*


