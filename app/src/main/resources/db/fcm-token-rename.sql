-- Rename OneSignal player id column to FCM device token (idempotent).
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'oosmuser' AND column_name = 'one_signal_player_id'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'oosmuser' AND column_name = 'fcm_token'
  ) THEN
    ALTER TABLE oosmuser RENAME COLUMN one_signal_player_id TO fcm_token;
  ELSIF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'oosmuser' AND column_name = 'one_signal_player_id'
  ) AND EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'oosmuser' AND column_name = 'fcm_token'
  ) THEN
    UPDATE oosmuser
    SET fcm_token = COALESCE(NULLIF(fcm_token, ''), one_signal_player_id)
    WHERE one_signal_player_id IS NOT NULL;
    ALTER TABLE oosmuser DROP COLUMN one_signal_player_id;
  ELSIF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'oosmuser' AND column_name = 'fcm_token'
  ) THEN
    ALTER TABLE oosmuser ADD COLUMN fcm_token varchar(512);
  END IF;
END $$;
