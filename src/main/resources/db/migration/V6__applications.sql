CREATE TABLE candidature (
    id BIGSERIAL PRIMARY KEY,
    statut VARCHAR(255) NOT NULL DEFAULT 'EN_ATTENTE',
    date_candidature TIMESTAMP NOT NULL,
    locataire_id BIGINT NOT NULL REFERENCES locataire(id_locataire),
    bien_immobilier_id BIGINT NOT NULL REFERENCES bien_immobilier(id),
    CONSTRAINT uk_candidature_locataire_bien UNIQUE (locataire_id, bien_immobilier_id),
    CONSTRAINT ck_candidature_statut CHECK (statut IN ('EN_ATTENTE', 'EN_ETUDE', 'REJETEE', 'ACCEPTEE', 'ANNULEE'))
);

-- Support the two paginated lists and their default ordering without indexing unused filters.
CREATE INDEX ix_candidature_locataire_date ON candidature(locataire_id, date_candidature DESC, id);
CREATE INDEX ix_candidature_bien_date ON candidature(bien_immobilier_id, date_candidature DESC, id);
