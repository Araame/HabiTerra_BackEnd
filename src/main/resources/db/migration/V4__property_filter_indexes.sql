-- V3 already indexes status/creation, type and address foreign keys.
-- Rent is a common range filter; status is always constrained by the public API.
CREATE INDEX ix_bien_statut_loyer ON bien_immobilier(statut, montant_loyer);

-- Match the lower(...) expressions used by the location specifications.
-- Existing case-sensitive indexes cannot serve these equality predicates directly.
CREATE INDEX ix_adresse_ville_lower ON adresse(lower(ville));
CREATE INDEX ix_adresse_commune_lower ON adresse(lower(commune));
CREATE INDEX ix_adresse_quartier_lower ON adresse(lower(quartier));
