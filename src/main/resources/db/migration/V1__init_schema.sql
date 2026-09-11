-- =========================================================================
-- TABLE ROLE
-- =========================================================================
CREATE TABLE role (
                      id_role SERIAL PRIMARY KEY,
                      libelle VARCHAR(50) NOT NULL UNIQUE
);

-- =========================================================================
-- Table USERS
-- =========================================================================
CREATE TABLE users (
                             id_utilisateur SERIAL PRIMARY KEY,
                             nom VARCHAR(100) NOT NULL,
                             prenom VARCHAR(100) NOT NULL,
                             email VARCHAR(100) UNIQUE,
                             mot_de_passe VARCHAR(255) NOT NULL,
                             numero_telephone VARCHAR(13),
                             adresse VARCHAR(255),
                             date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             id_role INT NOT NULL,
                             CONSTRAINT fk_utilisateur_role FOREIGN KEY (id_role)
                                 REFERENCES role(id_role) ON DELETE RESTRICT
);

-- =========================================================================
-- TABLE PROPRIETAIRE
-- =========================================================================
CREATE TABLE proprietaire (
                              id_proprietaire INT PRIMARY KEY,
                              CONSTRAINT fk_proprietaire_users FOREIGN KEY (id_proprietaire)
                                  REFERENCES users(id_utilisateur) ON DELETE CASCADE
);



-- =========================================================================
-- TABLE GERANT AGENCE
-- =========================================================================
CREATE TABLE gerantAgence (
                              id_gerant INT PRIMARY KEY,
                              CONSTRAINT fk_gerantAgence_users FOREIGN KEY (id_gerant)
                                  REFERENCES users(id_utilisateur) ON DELETE CASCADE
);


-- =========================================================================
-- TABLE LOCATAIRE
-- =========================================================================
CREATE TABLE locataire (
                              id_locataire INT PRIMARY KEY,
                              CONSTRAINT fk_gerantAgence_users FOREIGN KEY (id_locataire)
                                  REFERENCES users(id_utilisateur) ON DELETE CASCADE
);