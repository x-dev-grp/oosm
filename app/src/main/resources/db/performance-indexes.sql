-- Performance indexes for notification inbox and OAuth token lookups.

CREATE INDEX IF NOT EXISTS idx_user_notification_user_list
    ON public.user_notification (user_id, is_deleted, created_date DESC);

CREATE INDEX IF NOT EXISTS idx_user_notification_user_unread
    ON public.user_notification (user_id, is_deleted, read_at, created_date DESC);

CREATE INDEX IF NOT EXISTS idx_authorization_access_token
    ON public.authorization (access_token_value);

CREATE INDEX IF NOT EXISTS idx_authorization_refresh_token
    ON public.authorization (refresh_token_value);
