# OreRespawn

Mod NeoForge (1.21.1) qui fait reapparaitre automatiquement les minerais mines, cote serveur,
apres un delai configurable (24h par defaut).

## Fonctionnement

- Quand un joueur mine un bloc detecte comme minerai, sa position, le bloc mine et la roche
  d'origine (stone/deepslate/...) sont enregistres avec l'heure reelle du minage.
- Un planificateur verifie regulierement (toutes les 10s par defaut) les minerais mines dont le
  delai configure est ecoule, et les fait reapparaitre.
- Un minerai n'est regenere que si son emplacement est toujours vide ou contient encore la roche
  d'origine, afin de ne pas ecraser les constructions des joueurs.
- Si le chunk n'est pas charge au moment de la verification, l'entree est conservee et reessayee
  au prochain passage.
- Un rayon (en blocs) autour des joueurs peut etre configure : un minerai hors de portee de tout
  joueur de la dimension n'est pas regenere et attend le prochain passage (desactive par defaut,
  toute la dimension est alors consideree).
- La detection des minerais se base sur trois sources cumulables : les tags vanilla
  `#minecraft:*_ores` (coal, copper, diamond, emerald, gold, iron, lapis, redstone), le tag de
  convention NeoForge `#c:ores` (couvre deja tout le vanilla, y compris le quartz du Nether et
  les debris antiques, et c'est le tag que la plupart des mods utilisent pour leurs propres
  minerais), et le tag personnalise et extensible `#orerespawn:ores`
  (`data/orerespawn/tags/block/ores.json`). Une whitelist/blacklist configurables complete le tout
  (la blacklist est prioritaire).

C'est un mod **server-only** : il n'a aucun contenu cote client et n'a pas besoin d'etre installe
sur le client pour se connecter a un serveur qui l'utilise.

## Commandes

- `/orerespawn list [page]` - liste les minerais en attente de reapparition dans la dimension
  courante (10 par page).
- `/orerespawn count` - affiche le nombre de minerais en attente dans la dimension courante.
- `/orerespawn respawn` - force la reapparition immediate de tous les minerais en attente dans la
  dimension courante, sans attendre le delai configure. Respecte toujours la protection
  anti-ecrasement des builds, le rayon autour des joueurs (`respawnRadius`) et laisse en attente
  les minerais dont le chunk n'est pas charge ou dont l'emplacement est occupe.

Ces commandes necessitent le niveau d'operateur 2.

## Configuration

Chaque fonction du mod a son propre fichier de configuration serveur, genere dans le dossier
`config/` au premier demarrage du serveur (verifie en lancant reellement un serveur de test) :

**`orerespawn-respawn.toml`** - delai et securite de la reapparition
- `respawnDelaySeconds` - delai de reapparition, en secondes de temps reel (defaut : 86400).
- `checkIntervalTicks` - intervalle entre deux verifications, en ticks serveur (defaut : 200).
- `requireEmptySpaceOrOriginalFiller` - protection anti-ecrasement des builds (defaut : true).
- `respawnRadius` - rayon en blocs autour d'un joueur dans lequel un minerai peut reapparaitre
  (defaut : 0, desactive - toute la dimension est consideree).

**`orerespawn-detection.toml`** - quels blocs comptent comme des minerais
- `useVanillaOreTag` / `useNeoForgeOreTag` / `useCustomOreTag` - active/desactive chaque source de
  detection.
- `whitelist` / `blacklist` - identifiants de blocs (ex: `"mymod:ruby_ore"`) toujours/jamais
  traites comme des minerais.

**`orerespawn-dimensions.toml`** - dans quelles dimensions le mod agit
- `enableAllDimensions` - si vrai (defaut), OreRespawn est actif dans toutes les dimensions
  chargees, y compris celles ajoutees par d'autres mods, sans avoir a les lister.
- `enabledDimensions` - utilise seulement si `enableAllDimensions` est desactive : liste explicite
  des dimensions actives (defaut : overworld, nether, end).
- `disabledDimensions` - dimensions toujours exclues, meme avec `enableAllDimensions` actif (ex:
  une dimension "lobby" jetable d'un autre mod).

## Compiler

Prerequis : JDK 21.

```sh
./gradlew build
```

Le jar compile se trouve ensuite dans `build/libs/orerespawn-1.0.0.jar`.

## Tester

Le mod est annote `@Mod(dist = Dist.DEDICATED_SERVER)` : il ne se charge que sur un **serveur
dedie** (NeoForge 1.21.1, version 21.1.87 ou compatible). En solo (singleplayer), le jeu tourne
toujours cote client (avec un serveur integre en interne), donc le mod ne se chargera pas - il
faut lancer ou rejoindre un vrai serveur dedie pour le tester :

1. Installer un serveur NeoForge 1.21.1 (21.1.87+).
2. Copier `orerespawn-1.0.0.jar` dans le dossier `mods/` du serveur.
3. Demarrer le serveur, verifier dans les logs que `OreRespawn 1.0.0 (orerespawn)` apparait dans
   la liste des mods charges.
4. Les fichiers `orerespawn-respawn.toml`, `orerespawn-detection.toml` et
   `orerespawn-dimensions.toml` apparaissent dans `config/`.
5. En jeu (avec les droits OP niveau 2) : miner un minerai, verifier `/orerespawn list` et
   `/orerespawn count`, puis `/orerespawn respawn` pour le faire reapparaitre immediatement sans
   attendre le delai.

## Structure du projet

```
com.marc33.orerespawn
├── OreRespawnMod.java             - classe principale, enregistre les 3 fichiers de config
├── config/
│   ├── RespawnConfig.java         - delai, intervalle, protection anti-ecrasement, rayon
│   ├── DetectionConfig.java       - tags, whitelist/blacklist
│   └── DimensionConfig.java       - dimensions activees/exclues
├── data/
│   ├── OreDetector.java          - detection des minerais (tags + whitelist/blacklist)
│   ├── MinedOreEntry.java        - record d'un minerai mine (pos, bloc, filler, timestamp) + NBT
│   └── OreRespawnSavedData.java  - persistance NBT par dimension (SavedData NeoForge)
├── event/
│   ├── OreBreakListener.java     - enregistre les minerais casses (BlockEvent.BreakEvent)
│   └── OreRespawnTicker.java     - regenere les minerais dont le delai est ecoule (ServerTickEvent.Post)
└── command/OreRespawnCommand.java - /orerespawn list [page], count et respawn (OP niveau 2)
```
