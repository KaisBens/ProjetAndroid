Ce projet est une implémentation du jeu **Flow Free** (aussi appelé Dot Link) sous forme d'application Android en Java, réalisée dans le cadre d'un projet de SAE. Il propose une interface simple permettant de choisir un puzzle, de le résoudre en reliant les points, et de configurer un mode achromate (niveaux de gris).

## Fonctionnement du jeu

- Le joueur choisit un puzzle depuis le **menu principal**.
- Le puzzle apparaît sous forme de **grille carrée** avec des **paires de points colorées**.
- Le but est de **relier chaque paire** avec un chemin continu horizontal/vertical.
- **Toutes les cases** de la grille doivent être couvertes.
- Le jeu se fige une fois le puzzle résolu, sans possibilité de modifier les chemins.
- Un **mode achromate** peut être activé dans les options, affichant les chemins en nuances de gris.

## Fonctionnalités principales

- Système de sélection de puzzles depuis les fichiers XML
- Analyse et validation des puzzles (vérification de la taille, des points, de la syntaxe)
- Tracé de chemins avec annulation automatique en cas d'erreur
- Détection automatique de la complétion
- Sauvegarde de l'état de la partie si l'application est mise en pause
- Activité de configuration avec sauvegarde des préférences


## Dépendances et compatibilité

- **Langage** : Java uniquement
- **Compatibilité minimale** : API 19 (Android 4.4 KitKat)
- **Compatibilité maximale testée** : API 34 (Android 14)
- Aucune bibliothèque externe utilisée

## Format des puzzles XML

Chaque puzzle est décrit dans un fichier XML dans `assets/puzzles/`. Exemple :

```xml
<puzzle size="5" nom="Exemple simple">
  <paire>
    <point colonne="0" ligne="0" />
    <point colonne="3" ligne="3" />
  </paire>
  <paire>
    <point colonne="2" ligne="0" />
    <point colonne="2" ligne="2" />
  </paire>
</puzzle>
```

  - `size` entre **5 et 14**
  - chaque `<paire>` contient **exactement 2** balises `<point>`
  - attributs `colonne` et `ligne` **obligatoires** (valeurs entre `0` et `size - 1`)
  - Les puzzles invalides sont visibles dans le menu mais **ne sont pas sélectionnables**

---

## Détails techniques

- Dessin des chemins via une **`View` personnalisée** (`DrawView`)
- Tracé graphique réalisé avec **`Canvas`** et **`Paint`**
- Lecture des puzzles XML via **`XmlPullParser`**
- Préférences utilisateurs enregistrées via **`SharedPreferences`**


