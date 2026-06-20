CREATE TABLE IF NOT EXISTS public.chat_conversation (
    id UUID PRIMARY KEY,
    tenant_id UUID,
    participant_low_id UUID NOT NULL,
    participant_high_id UUID NOT NULL,
    last_message_preview VARCHAR(500),
    last_message_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    external_id UUID
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_chat_conversation_pair
    ON public.chat_conversation (tenant_id, participant_low_id, participant_high_id);

CREATE INDEX IF NOT EXISTS idx_chat_conversation_user
    ON public.chat_conversation (tenant_id, participant_low_id, participant_high_id, last_message_at DESC);

CREATE TABLE IF NOT EXISTS public.chat_message (
    id UUID PRIMARY KEY,
    tenant_id UUID,
    conversation_id UUID NOT NULL,
    sender_user_id UUID NOT NULL,
    body TEXT NOT NULL,
    read_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    external_id UUID
);

CREATE INDEX IF NOT EXISTS idx_chat_message_conversation
    ON public.chat_message (conversation_id, created_date DESC);

CREATE INDEX IF NOT EXISTS idx_chat_message_unread
    ON public.chat_message (conversation_id, sender_user_id, read_at);
