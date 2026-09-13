# HabiTerra BackEnd

Backend immobilier organise en monolithe modulaire : une application Spring Boot,
un projet Maven, un deploiement et une base PostgreSQL commune.

## Modules

Les modules metier se trouvent dans `src/main/java/com/habiterra` :

| Module | Responsabilite prevue |
| --- | --- |
| `identity` | Utilisateurs et roles |
| `agency` | Agences immobilieres |
| `property` | Biens immobiliers |
| `listing` | Annonces |
| `tenancy` | Locations et baux |
| `tenantprofile` | Profils des locataires |
| `payment` | Paiements |
| `document` | Documents |
| `notification` | Notifications |
| `search` | Recherche |

`shared` regroupe les fonctions techniques communes : `audit`, `common`, `config`,
`exception`, `security` et `storage`.

## Organisation d'un module

```text
module/
├── controller/   Controleurs REST
├── service/      Logique metier et transactions
├── repository/   Repositories Spring Data JPA
├── entity/       Entites JPA
├── dto/          Objets de requete et de reponse
└── exception/    Exceptions du module
```

Le flux habituel est `Controller -> Service -> Repository -> PostgreSQL`.
Les services utilisent directement les repositories Spring Data et les entites
JPA. Cette organisation ne demande ni ports/adaptateurs ni duplication des
entites dans un modele de domaine distinct.

Chaque module gere ses traitements et ses donnees. Les appels entre modules
passent par des services exposes explicitement, en evitant les acces directs
aux repositories d'un autre module et les dependances circulaires. `shared`
regroupe les fonctions techniques communes ; sa configuration de securite
assemble les services d'identite avec la chaine HTTP.
Spring Modulith est disponible pour accompagner cette organisation ; aucune
verification automatique des frontieres n'est encore configuree.

Les dossiers reserves sont conserves dans Git avec des fichiers `.gitkeep`.
Le module `identity` implemente le parcours OTP, inscription et authentification
JWT. Les autres modules metier restent a developper.

Le [guide d'authentification](docs/AUTHENTIFICATION.md) detaille les endpoints,
les variables d'environnement, les fournisseurs email/SMS et le parcours Swagger.
Le [guide de soutenance](docs/SOUTENANCE_BIENS_FILTRES_PAGINATION.md) propose
les questions, reponses et concepts sur les biens, les filtres et la pagination.
Voir aussi la [liste des fichiers de cette implementation](docs/AUTH_FILES.md).

La [documentation Swagger / OpenAPI](docs/OPENAPI.md) indique les URLs,
la compatibilite springdoc et le parcours JWT a effectuer en fin de projet.

## Persistance et verification

Les migrations Flyway se trouvent dans `src/main/resources/db/migration`.
La connexion PostgreSQL est configuree dans `src/main/resources/application.properties`.
Hibernate valide le schema avec `ddl-auto=validate`.

Compilation sous Windows :

```powershell
.\mvnw.cmd '-Dmaven.test.skip=true' compile
```

La migration V2 aligne le schema historique avec les entites et ajoute les OTP.
Les secrets PostgreSQL et JWT doivent etre fournis par l'environnement.
Aucun test ajoute ou execute pour cette implementation. Le test initial
`contextLoads` est conserve ; son execution necessiterait PostgreSQL et les secrets.

## Documentation technique

- [Authentification et sécurité](docs/authentification/README.md)
