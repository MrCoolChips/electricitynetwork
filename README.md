# Gestionnaire de Réseau Électrique

> Projet de Programmation Avancée et Application - S5 2025

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)

---

## Table des matières

1. [Informations Projet](#informations-projet)
2. [Analyse du Problème](#analyse-du-problème)
3. [Justification du Choix Algorithmique](#justification-du-choix-algorithmique)
4. [Description de l'Algorithme](#description-de-lalgorithme)
5. [Analyse de Complexité](#analyse-de-complexité)
6. [Références et Sources](#références-et-sources)
7. [Fonctionnalités](#fonctionnalités)
8. [Architecture](#architecture)
9. [Installation et Exécution](#installation-et-exécution)
10. [Guide d'Utilisation](#guide-dutilisation)

---

## Informations Projet

### Classe Principale (Point d'entrée)

La classe principale contenant la méthode `main` est :

```
up.mi.paa.Main
```

Le fichier source correspondant se trouve à l'emplacement suivant :

```
src/up/mi/paa/Main.java
```

### Exécution du Programme

```bash
# Partie 1 : Mode manuel (sans arguments)
java -cp bin up.mi.paa.Main

# Partie 2 : Mode fichier avec optimisation automatique
java -cp bin up.mi.paa.Main <chemin_fichier> [lambda]

# Exemple concret
java -cp bin up.mi.paa.Main reseau.txt 10
```

Le paramètre `lambda` (optionnel, défaut = 10) correspond au coefficient de pénalisation de la surcharge dans la fonction de coût.

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

## Fonctionnalités Implémentées

### Partie 1 : Mode Manuel (Interface Interactive)

Le mode manuel permet la construction et la manipulation du réseau électrique de manière interactive via une interface en ligne de commande.

**Gestion des générateurs** : Ajout et modification de générateurs avec spécification du nom et de la capacité maximale de production. Si un générateur du même nom existe, sa capacité est mise à jour.

**Gestion des maisons** : Ajout et modification de maisons avec spécification du nom et du type de consommation (BASSE, NORMAL, FORTE). Le système supporte la modification du type si la maison existe déjà.

**Gestion des connexions** : Création et suppression de connexions entre maisons et générateurs. L'ordre des arguments est flexible (G1 M1 ou M1 G1 sont équivalents).

**Validation du réseau** : Vérification de l'intégrité du réseau avant passage en mode évaluation. Chaque maison doit être connectée à exactement un générateur.

**Calcul du coût** : Évaluation de la fonction de coût intégrant dispersion et surcharge pondérée.

### Partie 2 : Mode Fichier (Optimisation Automatique)

Le mode fichier permet le chargement d'un réseau depuis un fichier et son optimisation automatique.

**Lecture de fichier** : Import d'un réseau au format Prolog avec validation syntaxique stricte. L'ordre des déclarations est contrôlé (générateurs, puis maisons, puis connexions).

**Gestion des erreurs** : Messages d'erreur détaillés indiquant la ligne problématique et le type d'erreur. Un système de suggestion propose des corrections pour les mots-clés mal orthographiés.

**Résolution automatique** : Application de l'algorithme d'optimisation (Glouton + Recuit Simulé) avec affichage des coûts avant et après optimisation.

**Sauvegarde** : Export de la solution optimisée vers un fichier, avec vérification de l'existence préalable du fichier cible.

### Fonctionnalités Additionnelles

**Interface colorée** : Utilisation de codes ANSI pour améliorer la lisibilité (tags [OK], [ERREUR], [INFO]).

**Suggestions de correction** : Proposition de corrections pour les erreurs de frappe dans les mots-clés du fichier d'entrée.

### Limitations Actuelles

L'interface graphique JavaFX est en cours de développement (fichiers présents mais non finalisés). L'export vers des formats alternatifs (CSV, JSON) n'est pas implémenté. La couverture des tests unitaires JUnit est partielle.

---

## Types de Consommation
- **BASSE** : 10 kW
- **NORMAL** : 20 kW
- **FORTE** : 40 kW

## Architecture du Projet

Le projet adopte une architecture en couches respectant le principe de séparation des responsabilités. Cette organisation facilite la maintenance, les tests et l'évolution du code.

```
src/up/mi/paa/
│
├── Main.java                              Point d'entrée de l'application
│                                          Analyse les arguments et délègue
│                                          au mode approprié (manuel/fichier)
│
├── model/                                 COUCHE MODÈLE
│   ├── Generateur.java                    Entité représentant un générateur
│   ├── Maison.java                        Entité représentant une maison
│   ├── TypeConsommation.java              Énumération {BASSE, NORMAL, FORTE}
│   ├── ReseauElectrique.java              Graphe biparti maisons-générateurs
│   └── Couts.java                         Encapsulation des composantes du coût
│
├── service/                               COUCHE SERVICE (Logique métier)
│   ├── GestionnaireReseau.java            Opérations CRUD sur le réseau
│   ├── CalculateurCouts.java              Calcul de dispersion et surcharge
│   └── OptimiseurReseau.java              Implémentation de l'algorithme
│                                          d'optimisation (Glouton + Recuit)
│
├── io/                                    COUCHE ENTRÉES/SORTIES
│   └── GestionnaireFichier.java           Lecture et écriture de fichiers
│                                          au format Prolog
│
├── ui/                                    COUCHE INTERFACE UTILISATEUR
│   └── MenuCLI.java                       Menus et dialogues en ligne
│                                          de commande
│
└── exception/                             EXCEPTIONS PERSONNALISÉES
    ├── FormatInvalideException.java       Format d'entrée incorrect
    ├── GenerateurIntrouvableException.java
    ├── MaisonIntrouvableException.java
    ├── ConnexionExistanteException.java
    └── ConnexionIntrouvableException.java
```

### Description des Couches

**Couche Modèle** : Définit les structures de données représentant le domaine métier. Le réseau électrique est modélisé comme un graphe biparti où les arêtes représentent les connexions maison-générateur.

**Couche Service** : Contient la logique métier indépendante de l'interface utilisateur. L'algorithme d'optimisation est encapsulé dans `OptimiseurReseau`, permettant son utilisation dans différents contextes (CLI, GUI, tests).

**Couche IO** : Gère la persistance et la sérialisation. Le format Prolog choisi offre une syntaxe lisible et facilement parsable.

**Couche UI** : Gère l'interaction avec l'utilisateur. La séparation permet d'envisager différentes interfaces (CLI, JavaFX) partageant la même logique métier.

**Exceptions** : Hiérarchie d'exceptions spécifiques au domaine, permettant une gestion fine des erreurs et des messages explicites.

---

## Installation et Exécution

### Prérequis

- Java Development Kit (JDK) version 8 ou supérieure
- Système d'exploitation : Windows, Linux ou macOS

### Compilation

```bash
# Compilation de l'ensemble des sources
javac -d bin -encoding UTF-8 \
    src/up/mi/paa/*.java \
    src/up/mi/paa/model/*.java \
    src/up/mi/paa/exception/*.java \
    src/up/mi/paa/service/*.java \
    src/up/mi/paa/ui/*.java \
    src/up/mi/paa/io/*.java
```

Sous Windows (PowerShell) :
```powershell
javac -d bin -encoding UTF-8 `
    src\up\mi\paa\*.java `
    src\up\mi\paa\model\*.java `
    src\up\mi\paa\exception\*.java `
    src\up\mi\paa\service\*.java `
    src\up\mi\paa\ui\*.java `
    src\up\mi\paa\io\*.java
```

### Exécution

```bash
# Mode manuel (Partie 1)
java -cp bin up.mi.paa.Main

# Mode fichier avec optimisation (Partie 2)
java -cp bin up.mi.paa.Main <chemin_fichier> [lambda]

# Exemple
java -cp bin up.mi.paa.Main reseau.txt 10
```

### Script d'Exécution Rapide (Windows)

Un script `run.bat` est fourni pour simplifier la compilation et l'exécution :

```bash
run.bat
```

---

## Format de Fichier

Le format de fichier utilise une syntaxe Prolog :

```prolog
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

## Guide d'Utilisation

---

### PARTIE 1 : Mode Manuel (Interface Interactive)

Le mode manuel se lance sans arguments :

```bash
java -cp bin up.mi.paa.Main
```

#### Menu Principal

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

#### Exemples d'Utilisation - Partie 1

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

#### Menu d'Évaluation

Après validation du réseau (option 5), vous accédez aux fonctionnalités d'analyse :

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

---

### PARTIE 2 : Mode Fichier (Optimisation Automatique)

Le mode fichier se lance avec le chemin du fichier en argument :

```bash
java -cp bin up.mi.paa.Main <chemin_fichier> [lambda]

# Exemple
java -cp bin up.mi.paa.Main reseau.txt 10
```

Le paramètre `lambda` (optionnel, défaut = 10) correspond au coefficient de pénalisation de la surcharge.

#### Menu Optimisation

```
┌────────────────────────────────────────────────┐
│              MENU PARTIE 2                     │
├────────────────────────────────────────────────┤
│  1 | Resolution automatique                    │
│  2 | Sauvegarder la solution                   │
│  3 | Fin                                       │
└────────────────────────────────────────────────┘
```

#### Exemples d'Utilisation - Partie 2

**1. Résolution automatique**

L'option 1 applique l'algorithme d'optimisation (Glouton + Recuit Simulé) et affiche les coûts avant et après :

```
[INFO] Optimisation en cours...
[OK] Optimisation terminee !

Cout avant optimisation : 4.56
Cout apres optimisation : 0.14
```

**2. Sauvegarder la solution**

L'option 2 permet d'exporter la solution optimisée vers un fichier :

```
> Chemin du fichier de sortie : solution.txt
[OK] Solution sauvegardee dans solution.txt
```

---

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

Le système intègre une gestion des erreurs via cinq exceptions personnalisées, chacune correspondant à un cas d'erreur spécifique du domaine métier :

- `FormatInvalideException` : Entrée utilisateur ou fichier mal formaté
- `GenerateurIntrouvableException` : Référence à un générateur inexistant
- `MaisonIntrouvableException` : Référence à une maison inexistante
- `ConnexionExistanteException` : Tentative de création d'une connexion déjà établie
- `ConnexionIntrouvableException` : Tentative de suppression d'une connexion inexistante

Les messages d'erreur sont préfixés par le tag `[ERREUR]` pour une identification immédiate. Les avertissements utilisent le tag `[ATTENTION]`.

---

## Documentation et Génération Javadoc

La documentation technique du code peut être générée via Javadoc :

```bash
javadoc -d docs -encoding UTF-8 -charset UTF-8 -sourcepath src -subpackages up.mi.paa
```

---

## Auteurs

**Groupe 10**

- Ali GOUARAB
- Egemen YAPSIK
- DAI Jérôme

---

*Projet réalisé dans le cadre du cours de Programmation Avancée et Application - Semestre 5 - 2025*


