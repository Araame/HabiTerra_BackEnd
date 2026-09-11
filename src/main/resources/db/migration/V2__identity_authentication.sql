-- Preserve V1 and existing records. Unknown legacy roles deliberately fail migration.
ALTER TABLE users RENAME TO utilisateur;
ALTER TABLE gerantAgence RENAME TO gerant_agence;
ALTER TABLE proprietaire DROP CONSTRAINT fk_proprietaire_users;
ALTER TABLE gerant_agence DROP CONSTRAINT fk_gerantAgence_users;
ALTER TABLE locataire DROP CONSTRAINT fk_gerantAgence_users;
ALTER TABLE utilisateur ALTER COLUMN id_utilisateur TYPE BIGINT;
ALTER SEQUENCE users_id_utilisateur_seq AS BIGINT;
ALTER TABLE proprietaire ALTER COLUMN id_proprietaire TYPE BIGINT;
ALTER TABLE gerant_agence ALTER COLUMN id_gerant TYPE BIGINT;
ALTER TABLE locataire ALTER COLUMN id_locataire TYPE BIGINT;
ALTER TABLE proprietaire ADD CONSTRAINT fk_proprietaire_users FOREIGN KEY(id_proprietaire) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE;
ALTER TABLE gerant_agence ADD CONSTRAINT fk_gerant_users FOREIGN KEY(id_gerant) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE;
ALTER TABLE locataire ADD CONSTRAINT fk_locataire_users FOREIGN KEY(id_locataire) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE;
ALTER TABLE utilisateur RENAME COLUMN numero_telephone TO telephone;
ALTER TABLE utilisateur ALTER COLUMN telephone TYPE VARCHAR(16);
ALTER TABLE utilisateur ADD COLUMN role VARCHAR(255);
UPDATE utilisateur u SET role = CASE UPPER(TRIM(r.libelle))
    WHEN 'AGENCE' THEN 'GERANT_AGENCE' WHEN 'GERANT_AGENCE' THEN 'GERANT_AGENCE'
    WHEN 'LOCATAIRE' THEN 'LOCATAIRE' WHEN 'PROPRIETAIRE' THEN 'PROPRIETAIRE'
    WHEN 'ADMIN' THEN 'ADMIN' ELSE NULL END FROM role r WHERE u.id_role=r.id_role;
ALTER TABLE utilisateur ALTER COLUMN role SET NOT NULL;
ALTER TABLE utilisateur ADD CONSTRAINT ck_utilisateur_role CHECK(role IN ('LOCATAIRE','PROPRIETAIRE','GERANT_AGENCE','ADMIN'));
-- Retain legacy role table and values; new entities persist the enum directly.
ALTER TABLE utilisateur ALTER COLUMN id_role DROP NOT NULL;
ALTER TABLE utilisateur ADD COLUMN statut VARCHAR(255) NOT NULL DEFAULT 'EN_ATTENTE';
ALTER TABLE utilisateur ADD CONSTRAINT ck_utilisateur_statut CHECK(statut IN ('EN_ATTENTE','ACTIF','SUSPENDU','DESACTIVE'));
ALTER TABLE utilisateur ADD COLUMN email_verifie BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE utilisateur ADD COLUMN telephone_verifie BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE utilisateur ADD COLUMN photo_profil VARCHAR(255);
ALTER TABLE utilisateur ADD COLUMN derniere_connexion TIMESTAMP;
UPDATE utilisateur SET email = NULLIF(LOWER(TRIM(email)), '');
UPDATE utilisateur SET telephone = NULLIF(regexp_replace(telephone, '[ ()-]', '', 'g'), '');
UPDATE utilisateur SET telephone = '+' || SUBSTRING(telephone FROM 3) WHERE telephone LIKE '00%';
ALTER TABLE utilisateur ADD CONSTRAINT uk_utilisateur_telephone UNIQUE(telephone);
ALTER TABLE locataire ADD COLUMN profession VARCHAR(255);
ALTER TABLE proprietaire ADD COLUMN profession VARCHAR(255);
ALTER TABLE proprietaire ADD COLUMN numero_identite VARCHAR(255);
ALTER TABLE proprietaire ADD COLUMN adresse_residence VARCHAR(255);
ALTER TABLE gerant_agence ADD COLUMN poste VARCHAR(255);

CREATE TABLE otp_verification (
    id BIGSERIAL PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL,
    identifier_type VARCHAR(255) NOT NULL CHECK(identifier_type IN ('EMAIL','TELEPHONE')),
    code_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified_at TIMESTAMP WITH TIME ZONE,
    invalidated BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_otp_attempts CHECK(attempts >= 0 AND attempts <= max_attempts)
);
CREATE INDEX ix_otp_identifier_latest ON otp_verification(identifier, identifier_type, id DESC);
CREATE INDEX ix_otp_identifier_created ON otp_verification(identifier, created_at);
