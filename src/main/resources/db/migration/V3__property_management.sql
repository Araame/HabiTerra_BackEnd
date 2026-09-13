-- Minimal agency identity and assignments. No implicit grants for existing users.
CREATE TABLE agence (
    id BIGSERIAL PRIMARY KEY
);
ALTER TABLE gerant_agence ADD COLUMN agency_id BIGINT REFERENCES agence(id);
ALTER TABLE proprietaire ADD COLUMN agency_id BIGINT REFERENCES agence(id);
CREATE INDEX ix_proprietaire_agency ON proprietaire(agency_id);
CREATE INDEX ix_gerant_agence_agency ON gerant_agence(agency_id);

CREATE TABLE adresse (
    id BIGSERIAL PRIMARY KEY,
    pays VARCHAR(255) NOT NULL,
    ville VARCHAR(255) NOT NULL,
    commune VARCHAR(255),
    quartier VARCHAR(255),
    rue VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    CONSTRAINT ck_adresse_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_adresse_longitude CHECK (longitude BETWEEN -180 AND 180)
);
CREATE INDEX ix_adresse_ville ON adresse(ville);
CREATE INDEX ix_adresse_commune ON adresse(commune);

CREATE TABLE type_bien (
    id BIGSERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE galerie_photo (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(255)
);

CREATE TABLE bien_immobilier (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    superficie DOUBLE PRECISION NOT NULL,
    nombre_pieces INTEGER NOT NULL,
    nombre_chambres INTEGER NOT NULL,
    nombre_salles_de_bain INTEGER NOT NULL,
    montant_loyer NUMERIC(19, 2) NOT NULL,
    montant_caution NUMERIC(19, 2) NOT NULL,
    statut VARCHAR(255) NOT NULL DEFAULT 'DRAFT',
    meuble BOOLEAN NOT NULL,
    colocation_autorisee BOOLEAN NOT NULL,
    capacite_colocation INTEGER,
    disponible_a_partir_du DATE,
    date_creation TIMESTAMP NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES proprietaire(id_proprietaire),
    agency_id BIGINT REFERENCES agence(id),
    type_bien_id BIGINT NOT NULL REFERENCES type_bien(id),
    adresse_id BIGINT NOT NULL UNIQUE REFERENCES adresse(id),
    galerie_photo_id BIGINT NOT NULL UNIQUE REFERENCES galerie_photo(id),
    CONSTRAINT ck_bien_statut CHECK (statut IN ('DRAFT', 'AVAILABLE', 'RENTED', 'UNAVAILABLE')),
    CONSTRAINT ck_bien_superficie CHECK (superficie > 0 AND superficie < 'Infinity'::DOUBLE PRECISION),
    CONSTRAINT ck_bien_room_counts CHECK (
        nombre_pieces > 0 AND nombre_chambres >= 0 AND nombre_salles_de_bain >= 0
        AND nombre_chambres <= nombre_pieces
    ),
    CONSTRAINT ck_bien_rent CHECK (montant_loyer > 0 AND montant_loyer < 'NaN'::NUMERIC),
    CONSTRAINT ck_bien_deposit CHECK (montant_caution >= 0 AND montant_caution < 'NaN'::NUMERIC),
    CONSTRAINT ck_bien_shared_housing CHECK (
        (colocation_autorisee = FALSE AND capacite_colocation IS NULL)
        OR (colocation_autorisee = TRUE AND capacite_colocation IS NOT NULL AND capacite_colocation > 0)
    )
);
CREATE INDEX ix_bien_statut_creation ON bien_immobilier(statut, date_creation DESC, id);
CREATE INDEX ix_bien_owner ON bien_immobilier(owner_id);
CREATE INDEX ix_bien_agency ON bien_immobilier(agency_id);
CREATE INDEX ix_bien_type ON bien_immobilier(type_bien_id);

CREATE TABLE piece (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    superficie DOUBLE PRECISION NOT NULL,
    description TEXT,
    bien_immobilier_id BIGINT NOT NULL REFERENCES bien_immobilier(id) ON DELETE CASCADE,
    CONSTRAINT ck_piece_superficie CHECK (superficie > 0 AND superficie < 'Infinity'::DOUBLE PRECISION)
);
CREATE INDEX ix_piece_bien ON piece(bien_immobilier_id);

CREATE TABLE photo_bien (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(1024) NOT NULL,
    description TEXT,
    galerie_photo_id BIGINT NOT NULL REFERENCES galerie_photo(id) ON DELETE CASCADE
);
CREATE INDEX ix_photo_gallery ON photo_bien(galerie_photo_id);
