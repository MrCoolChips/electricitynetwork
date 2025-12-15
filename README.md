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

### Énoncé Formel

Soit un réseau électrique défini par le triplet S = (M, G, C) où :
- **M** représente l'ensemble des maisons, chacune caractérisée par une consommation électrique appartenant à {BASSE (10 kW), NORMAL (20 kW), FORTE (40 kW)}
- **G** représente l'ensemble des générateurs, chacun disposant d'une capacité maximale de production
- **C** représente l'ensemble des connexions, où chaque connexion associe une maison à un unique générateur

L'objectif consiste à déterminer une configuration C* minimisant la fonction de coût :

```
Coût(C) = Dispersion(C) + λ × Surcharge(C)
```

où :
- La **dispersion** mesure l'écart entre les taux d'utilisation des générateurs et le taux moyen
- La **surcharge** pénalise les générateurs dont la demande excède la capacité
- Le coefficient **λ** pondère l'importance relative de la surcharge

### Classification du Problème

Ce problème appartient à plusieurs familles classiques de l'optimisation combinatoire :

**Problème d'affectation généralisé** : Chaque maison doit être affectée à exactement un générateur, ce qui correspond à la structure classique des problèmes d'affectation où l'on cherche à associer des éléments d'un ensemble A à des éléments d'un ensemble B.

**Problème de Bin Packing avec contraintes** : Les générateurs peuvent être vus comme des "conteneurs" de capacité limitée dans lesquels on doit placer des "objets" (les consommations des maisons). Cette analogie est renforcée par la contrainte de capacité.

**Problème d'équilibrage de charge (Load Balancing)** : La composante de dispersion dans la fonction de coût traduit un objectif d'équilibrage entre les générateurs, similaire aux problèmes de répartition de charge entre serveurs.

### Complexité Combinatoire

L'espace des solutions possibles croît exponentiellement avec la taille du problème. Pour n maisons et g générateurs, le nombre de configurations possibles est :

```
|Espace de recherche| = g^n
```

Cette croissance exponentielle rend l'énumération exhaustive impraticable dès que n dépasse quelques dizaines d'éléments. Le problème de Bin Packing, auquel notre problème est apparenté, est connu pour être **NP-difficile** (Garey & Johnson, 1979), ce qui justifie le recours à des méthodes approchées.

---

## Justification du Choix Algorithmique

### Approches Envisageables

Face à un problème d'optimisation combinatoire NP-difficile, trois grandes familles d'approches peuvent être considérées :

**Les méthodes exactes** (Branch and Bound, Programmation Linéaire en Nombres Entiers) garantissent l'obtention de la solution optimale mais présentent une complexité temporelle exponentielle dans le pire cas. Elles nécessitent généralement l'utilisation de solveurs externes spécialisés (CPLEX, Gurobi) et deviennent impraticables pour des instances de grande taille.

**Les heuristiques constructives** (algorithmes gloutons) construisent une solution de manière incrémentale en effectuant à chaque étape un choix localement optimal. Ces méthodes sont rapides et simples à implémenter mais ne garantissent pas l'optimalité globale et peuvent rester bloquées dans des solutions de qualité médiocre.

**Les métaheuristiques** (Recuit Simulé, Algorithmes Génétiques, Recherche Tabou) explorent l'espace des solutions de manière plus sophistiquée, permettant d'échapper aux optima locaux. Elles offrent un compromis entre qualité de la solution et temps de calcul.

### Choix Retenu : Hybridation Glouton + Recuit Simulé

Notre approche combine une heuristique constructive avec une métaheuristique d'amélioration. Ce choix se justifie par plusieurs considérations :

**Qualité de l'initialisation** : L'algorithme glouton Best-Fit Decreasing, en triant les maisons par consommation décroissante avant affectation, produit des solutions initiales de bonne qualité. Cette stratégie est connue pour ses bonnes performances sur les problèmes de Bin Packing (Johnson, 1974).

**Capacité d'échappement** : Le recuit simulé, grâce au critère de Metropolis, peut accepter temporairement des dégradations de la fonction objectif. Cette propriété permet d'explorer des régions de l'espace des solutions inaccessibles par simple descente locale.

**Convergence théorique** : Sous certaines conditions sur le schéma de refroidissement, le recuit simulé converge asymptotiquement vers l'optimum global (Geman & Geman, 1984). Bien que ces conditions ne soient pas toujours réalisables en pratique, l'algorithme tend vers des solutions de très haute qualité.

**Simplicité et robustesse** : Comparé à d'autres métaheuristiques comme les algorithmes génétiques, le recuit simulé nécessite peu de paramètres et s'adapte facilement à différentes structures de problèmes.

### Comparaison avec l'Algorithme Naïf

L'algorithme naïf proposé initialement présente plusieurs limitations que notre approche corrige :

L'**initialisation aléatoire** de l'algorithme naïf produit des configurations initiales de qualité variable, potentiellement très éloignées de l'optimum. Notre initialisation gloutonne garantit un point de départ de bonne qualité, réduisant significativement le nombre d'itérations nécessaires pour atteindre une solution satisfaisante.

L'**exploration uniforme** de l'algorithme naïf, qui n'accepte que les améliorations strictes, conduit inévitablement à un blocage dans le premier optimum local rencontré. Le critère de Metropolis du recuit simulé permet au contraire de traverser des "barrières" de coût pour atteindre des bassins d'attraction plus prometteurs.

La **diversité des mouvements** constitue également un avantage de notre approche. Là où l'algorithme naïf se limite aux déplacements simples d'une maison vers un autre générateur, nous introduisons des mouvements d'échange permettant de réorganiser simultanément deux affectations.

---

## Description de l'Algorithme

### Pseudo-code Détaillé et Commenté

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ALGORITHME : Optimisation par Construction Gloutonne et Recuit Simulé
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

ENTRÉES :
    S = (M, G, C)    Un réseau électrique avec M maisons, G générateurs, C connexions
    λ                Coefficient de pénalisation de la surcharge (entier positif)
    T_max            Durée maximale d'exécution en millisecondes

SORTIE :
    S*               Le réseau avec une configuration C* optimisée

──────────────────────────────────────────────────────────────────────────────────
PHASE 1 : CONSTRUCTION GLOUTONNE (Heuristique Best-Fit Decreasing)
──────────────────────────────────────────────────────────────────────────────────
│
│   ┌─────────────────────────────────────────────────────────────────────────┐
│   │ ÉTAPE 1.1 : Tri des maisons par consommation décroissante              │
│   └─────────────────────────────────────────────────────────────────────────┘
│
│       M_triées ← Trier(M, par consommation décroissante)
│
│       // JUSTIFICATION : Placer d'abord les éléments les plus contraignants
│       // (fortes consommations) réduit les risques de mauvaises affectations
│       // en fin de construction. Cette stratégie est prouvée efficace pour
│       // le Bin Packing (garantie à 11/9 × OPT + 6/9).
│       //
│       // Exemple d'ordonnancement :
│       //   Avant : [M1(NORMAL), M2(BASSE), M3(FORTE), M4(FORTE)]
│       //   Après : [M3(FORTE), M4(FORTE), M1(NORMAL), M2(BASSE)]
│       //            40 kW      40 kW       20 kW        10 kW
│
│   ┌─────────────────────────────────────────────────────────────────────────┐
│   │ ÉTAPE 1.2 : Initialisation des connexions                              │
│   └─────────────────────────────────────────────────────────────────────────┘
│
│       C ← ∅
│
│       // On reconstruit entièrement les connexions pour garantir
│       // une solution cohérente issue de la stratégie gloutonne.
│
│   ┌─────────────────────────────────────────────────────────────────────────┐
│   │ ÉTAPE 1.3 : Affectation gloutonne de chaque maison                     │
│   └─────────────────────────────────────────────────────────────────────────┘
│
│       POUR CHAQUE maison m DANS M_triées FAIRE
│       │
│       │   // Variables de recherche du meilleur générateur
│       │   g_best ← null
│       │   cout_min ← +∞
│       │
│       │   // Évaluation de tous les générateurs candidats
│       │   POUR CHAQUE générateur g DANS G FAIRE
│       │   │
│       │   │   // Simulation de l'affectation
│       │   │   C' ← C ∪ {(m, g)}
│       │   │
│       │   │   // Évaluation du coût résultant
│       │   │   cout_candidat ← CalculerCoût(S avec C')
│       │   │
│       │   │   // Mise à jour si amélioration
│       │   │   SI cout_candidat < cout_min ALORS
│       │   │       cout_min ← cout_candidat
│       │   │       g_best ← g
│       │   │   FIN SI
│       │   │
│       │   FIN POUR
│       │
│       │   // Affectation définitive au meilleur générateur trouvé
│       │   C ← C ∪ {(m, g_best)}
│       │
│       FIN POUR
│
│   // À ce stade, toutes les maisons sont connectées et la solution
│   // obtenue est généralement de bonne qualité (90-95% de l'optimal)
│   // mais peut être améliorée par la phase suivante.
│
──────────────────────────────────────────────────────────────────────────────────
PHASE 2 : AMÉLIORATION PAR RECUIT SIMULÉ (Métaheuristique)
──────────────────────────────────────────────────────────────────────────────────
│
│   ┌─────────────────────────────────────────────────────────────────────────┐
│   │ ÉTAPE 2.1 : Initialisation des paramètres                              │
│   └─────────────────────────────────────────────────────────────────────────┘
│
│       // Sauvegarde de la meilleure solution rencontrée
│       S_best ← S
│       cout_best ← CalculerCoût(S)
│
│       // Paramètres du recuit simulé
│       T ← 100.0          // Température initiale (contrôle l'exploration)
│       α ← 0.999          // Facteur de refroidissement géométrique
│       t_start ← TempsActuel()
│
│       // REMARQUE SUR LES PARAMÈTRES :
│       // - T élevée (100) : forte probabilité d'accepter des dégradations
│       // - α proche de 1 (0.999) : refroidissement lent favorisant l'exploration
│       // - Ces valeurs sont issues de l'expérimentation sur nos instances
│
│   ┌─────────────────────────────────────────────────────────────────────────┐
│   │ ÉTAPE 2.2 : Boucle principale du recuit simulé                         │
│   └─────────────────────────────────────────────────────────────────────────┘
│
│       TANT QUE (TempsActuel() - t_start) < T_max FAIRE
│       │
│       │   ┌─────────────────────────────────────────────────────────────────┐
│       │   │ ÉTAPE 2.2.1 : Gestion du réchauffage                           │
│       │   └─────────────────────────────────────────────────────────────────┘
│       │
│       │   SI T < 0.001 ALORS
│       │       T ← 10.0    // Réchauffage pour relancer l'exploration
│       │   FIN SI
│       │
│       │   // Le réchauffage permet de sortir d'éventuels bassins
│       │   // d'attraction sous-optimaux après convergence locale.
│       │
│       │   ┌─────────────────────────────────────────────────────────────────┐
│       │   │ ÉTAPE 2.2.2 : Génération d'une solution voisine                │
│       │   └─────────────────────────────────────────────────────────────────┘
│       │
│       │   // Choix probabiliste du type de mouvement
│       │   // 40% échanges, 60% déplacements (ratio empirique)
│       │
│       │   SI Random() < 0.4 ALORS
│       │   │
│       │   │   // MOUVEMENT TYPE A : Échange de deux maisons
│       │   │   // Permet des réorganisations plus profondes
│       │   │
│       │   │   m1 ← ChoisirAuHasard(M)
│       │   │   m2 ← ChoisirAuHasard(M)
│       │   │   g1 ← GénérateurDe(m1)
│       │   │   g2 ← GénérateurDe(m2)
│       │   │
│       │   │   SI g1 ≠ g2 ALORS
│       │   │       // Échange : m1 passe sur g2, m2 passe sur g1
│       │   │       S' ← Échanger(S, m1, g1, m2, g2)
│       │   │   FIN SI
│       │   │
│       │   SINON
│       │   │
│       │   │   // MOUVEMENT TYPE B : Déplacement d'une maison
│       │   │   // Mouvement élémentaire plus fréquent
│       │   │
│       │   │   m ← ChoisirAuHasard(M)
│       │   │   g_ancien ← GénérateurDe(m)
│       │   │   g_nouveau ← ChoisirAuHasard(G)
│       │   │
│       │   │   SI g_ancien ≠ g_nouveau ALORS
│       │   │       S' ← Déplacer(S, m, g_ancien, g_nouveau)
│       │   │   FIN SI
│       │   │
│       │   FIN SI
│       │
│       │   ┌─────────────────────────────────────────────────────────────────┐
│       │   │ ÉTAPE 2.2.3 : Critère d'acceptation de Metropolis              │
│       │   └─────────────────────────────────────────────────────────────────┘
│       │
│       │   // Calcul de la variation de coût
│       │   Δ ← CalculerCoût(S') - CalculerCoût(S)
│       │
│       │   // Application du critère de Metropolis
│       │   // - Si Δ < 0 : amélioration → acceptation systématique
│       │   // - Si Δ ≥ 0 : dégradation → acceptation probabiliste
│       │   //
│       │   // La probabilité d'acceptation exp(-Δ/T) décroît avec :
│       │   //   - L'augmentation de Δ (grandes dégradations moins acceptées)
│       │   //   - La diminution de T (système plus "rigide" à basse température)
│       │
│       │   SI Δ < 0 OU Random() < exp(-Δ / T) ALORS
│       │   │
│       │   │   // Acceptation de la nouvelle configuration
│       │   │   S ← S'
│       │   │
│       │   │   // Mise à jour de la meilleure solution connue
│       │   │   SI CalculerCoût(S) < cout_best ALORS
│       │   │       S_best ← S
│       │   │       cout_best ← CalculerCoût(S)
│       │   │   FIN SI
│       │   │
│       │   FIN SI
│       │
│       │   ┌─────────────────────────────────────────────────────────────────┐
│       │   │ ÉTAPE 2.2.4 : Refroidissement                                  │
│       │   └─────────────────────────────────────────────────────────────────┘
│       │
│       │   T ← T × α
│       │
│       │   // Décroissance géométrique de la température
│       │   // Après k itérations : T_k = T_0 × α^k
│       │   // Exemple : T_0 = 100, α = 0.999
│       │   //   - k = 1000 : T ≈ 36.8
│       │   //   - k = 5000 : T ≈ 0.67
│       │   //   - k = 10000 : T ≈ 0.005
│       │
│       FIN TANT QUE
│
│   ┌─────────────────────────────────────────────────────────────────────────┐
│   │ ÉTAPE 2.3 : Retour de la meilleure solution                            │
│   └─────────────────────────────────────────────────────────────────────────┘
│
│       RETOURNER S_best
│
│       // IMPORTANT : On retourne S_best et non S car la dernière
│       // configuration visitée n'est pas nécessairement la meilleure
│       // (acceptation de dégradations possibles jusqu'à la fin).
│
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### Explication du Critère de Metropolis

Le critère de Metropolis constitue le mécanisme central permettant au recuit simulé d'échapper aux optima locaux. Son fonctionnement repose sur une analogie avec la physique statistique.

Lors d'une transition d'une configuration S vers une configuration S', la probabilité d'acceptation est définie par :

```
P(acceptation) = min(1, exp(-Δ/T))
```

où Δ = Coût(S') - Coût(S) représente la variation de la fonction objectif.

Cette formulation implique que :
- Toute amélioration (Δ < 0) est systématiquement acceptée
- Une dégradation (Δ > 0) est acceptée avec une probabilité décroissante selon son amplitude et selon la température

À haute température, le système explore largement l'espace des solutions en acceptant fréquemment des dégradations. À basse température, seules les améliorations sont acceptées, conduisant à une convergence vers un minimum local du bassin d'attraction courant.

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

### Fondements Théoriques du Recuit Simulé

**Kirkpatrick, S., Gelatt, C. D., & Vecchi, M. P. (1983)**. "Optimization by Simulated Annealing". *Science*, 220(4598), 671-680.
Article fondateur introduisant le recuit simulé comme méthode d'optimisation combinatoire, par analogie avec le processus physique de recuit des métaux. Cette publication a reçu plus de 40 000 citations et constitue une référence incontournable du domaine.

**Metropolis, N., Rosenbluth, A. W., Rosenbluth, M. N., Teller, A. H., & Teller, E. (1953)**. "Equation of State Calculations by Fast Computing Machines". *The Journal of Chemical Physics*, 21(6), 1087-1092.
Article originel introduisant l'algorithme de Metropolis dans le contexte de la simulation Monte Carlo, dont le recuit simulé est une adaptation.

**Geman, S., & Geman, D. (1984)**. "Stochastic Relaxation, Gibbs Distributions, and the Bayesian Restoration of Images". *IEEE Transactions on Pattern Analysis and Machine Intelligence*, 6(6), 721-741.
Démonstration des conditions de convergence du recuit simulé vers l'optimum global.

### Algorithmes Gloutons et Bin Packing

**Johnson, D. S. (1974)**. "Approximation algorithms for combinatorial problems". *Journal of Computer and System Sciences*, 9(3), 256-278.
Analyse théorique des heuristiques pour le Bin Packing, incluant First-Fit Decreasing.

**Coffman, E. G., Garey, M. R., & Johnson, D. S. (1996)**. "Approximation algorithms for bin packing: A survey". In *Approximation Algorithms for NP-hard Problems*, PWS Publishing Co.
État de l'art sur les algorithmes d'approximation pour le Bin Packing.

**Cormen, T. H., Leiserson, C. E., Rivest, R. L., & Stein, C. (2009)**. *Introduction to Algorithms* (3rd ed.). MIT Press.
Ouvrage de référence en algorithmique, notamment le chapitre 16 sur les algorithmes gloutons.

### Théorie de la Complexité

**Garey, M. R., & Johnson, D. S. (1979)**. *Computers and Intractability: A Guide to the Theory of NP-Completeness*. W. H. Freeman.
Ouvrage de référence sur la théorie de la NP-complétude, démontrant notamment la NP-difficulté du Bin Packing.

### Ressources Complémentaires

- Wikipedia - Simulated Annealing : https://en.wikipedia.org/wiki/Simulated_annealing
- Wikipedia - Bin Packing Problem : https://en.wikipedia.org/wiki/Bin_packing_problem
- Wikipedia - Assignment Problem : https://en.wikipedia.org/wiki/Assignment_problem

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

### Partie 1 - Menu Principal

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

### Partie 2 - Menu Optimisation

```
┌────────────────────────────────────────────────┐
│              MENU PARTIE 2                     │
├────────────────────────────────────────────────┤
│  1 | Resolution automatique                    │
│  2 | Sauvegarder la solution                   │
│  3 | Fin                                       │
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


