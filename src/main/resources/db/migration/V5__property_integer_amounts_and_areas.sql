-- Refuse lossy conversions: existing fractional or out-of-range values must
-- be corrected explicitly before this migration can run.
LOCK TABLE bien_immobilier, piece IN ACCESS EXCLUSIVE MODE;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM bien_immobilier
        WHERE montant_loyer <> trunc(montant_loyer)
           OR montant_loyer NOT BETWEEN 1 AND 2147483647
           OR montant_caution <> trunc(montant_caution)
           OR montant_caution NOT BETWEEN 0 AND 2147483647
           OR superficie <> trunc(superficie)
           OR superficie NOT BETWEEN 1 AND 2147483647
    ) OR EXISTS (
        SELECT 1 FROM piece
        WHERE superficie <> trunc(superficie)
           OR superficie NOT BETWEEN 1 AND 2147483647
    ) THEN
        RAISE EXCEPTION 'Property amounts and areas must be whole numbers within the INTEGER range before migration V5';
    END IF;
END $$;

ALTER TABLE bien_immobilier
    DROP CONSTRAINT ck_bien_superficie,
    DROP CONSTRAINT ck_bien_rent,
    DROP CONSTRAINT ck_bien_deposit,
    ALTER COLUMN superficie TYPE INTEGER USING superficie::INTEGER,
    ALTER COLUMN montant_loyer TYPE INTEGER USING montant_loyer::INTEGER,
    ALTER COLUMN montant_caution TYPE INTEGER USING montant_caution::INTEGER,
    ADD CONSTRAINT ck_bien_superficie CHECK (superficie > 0),
    ADD CONSTRAINT ck_bien_rent CHECK (montant_loyer > 0),
    ADD CONSTRAINT ck_bien_deposit CHECK (montant_caution >= 0);

ALTER TABLE piece
    DROP CONSTRAINT ck_piece_superficie,
    ALTER COLUMN superficie TYPE INTEGER USING superficie::INTEGER,
    ADD CONSTRAINT ck_piece_superficie CHECK (superficie > 0);
