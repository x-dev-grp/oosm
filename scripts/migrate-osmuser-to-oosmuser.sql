-- Copy tenant users left behind in legacy osmuser into oosmuser (skip duplicates by username).
INSERT INTO oosmuser
SELECT o.*
FROM osmuser o
WHERE NOT EXISTS (
  SELECT 1 FROM oosmuser n WHERE LOWER(n.username) = LOWER(o.username)
);

-- Drop legacy table when empty of unique users
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'osmuser'
  ) THEN
    IF NOT EXISTS (
      SELECT 1
      FROM osmuser o
      WHERE NOT EXISTS (
        SELECT 1 FROM oosmuser n WHERE LOWER(n.username) = LOWER(o.username)
      )
    ) THEN
      DROP TABLE osmuser;
    END IF;
  END IF;
END $$;
