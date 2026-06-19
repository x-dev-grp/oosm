-- Persist supplier display name derived from name + lastname
ALTER TABLE supplier ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);

UPDATE supplier
SET full_name = NULLIF(TRIM(CONCAT(COALESCE(name, ''), ' ', COALESCE(lastname, ''))), '')
WHERE full_name IS NULL;
