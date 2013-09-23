ALTER TABLE ebms_message ADD message_nr INT NOT NULL DEFAULT 0;

ALTER TABLE ebms_message DROP CONSTRAINT uc_ebms_message_id;

ALTER TABLE ebms_message ADD CONSTRAINT uc_ebms_message_id UNIQUE (message_id,message_nr);

ALTER TABLE ebms_send_event ADD error_message TEXT NULL;

ALTER TABLE ebms_send_event ADD CONSTRAINT uc_ebms_send_event UNIQUE (ebms_message_id,time);