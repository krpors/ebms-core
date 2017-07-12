CREATE SEQUENCE seq_ebms_message_id
	START WITH 1
	INCREMENT BY 1
	NOMAXVALUE;

CREATE TABLE cpa
(
	cpa_id						VARCHAR(256)		NOT NULL PRIMARY KEY,
	cpa								CLOB						NOT NULL
);

CREATE TABLE url
(
	source						VARCHAR(256)		NOT NULL UNIQUE,
	destination				VARCHAR(256)		NOT NULL
);

CREATE TABLE ebms_message
(
	id								NUMBER					PRIMARY KEY,
	time_stamp				TIMESTAMP				NOT NULL,
	cpa_id						VARCHAR(256)		NOT NULL,
	conversation_id		VARCHAR(256)		NOT NULL,
	message_id				VARCHAR(256)		NOT NULL,
	ref_to_message_id	VARCHAR(256)		NULL,
	time_to_live			TIMESTAMP				NULL,
	persist_time			TIMESTAMP				NULL,
	from_party_id			VARCHAR(256)		NOT NULL,
	from_role					VARCHAR(256)		NULL,
	to_party_id				VARCHAR(256)		NOT NULL,
	to_role						VARCHAR(256)		NULL,
	service						VARCHAR(256)		NOT NULL,
	action						VARCHAR(256)		NOT NULL,
	content						CLOB						NULL,
	status						NUMBER(5)				NULL,
	status_time				TIMESTAMP				NULL,
	FOREIGN KEY (cpa_id) REFERENCES cpa(cpa_id)
);

CREATE TABLE ebms_attachment
(
	ebms_message_id		NUMBER					NOT NULL REFERENCES ebms_message(id),
	order_nr					NUMBER(5)				NOT NULL,
	name							VARCHAR(256)		NULL,
	content_id 				VARCHAR(256) 		NOT NULL,
	content_type			VARCHAR(255)		NOT NULL,
	content						BLOB						NOT NULL
);

CREATE TABLE ebms_event
(
	ebms_message_id		NUMBER					NOT NULL REFERENCES ebms_message(id) UNIQUE,
	cpa_id						VARCHAR(256)		NOT NULL REFERENCES cpa(cpa_id),
	channel_id				VARCHAR(256)		NOT NULL,
	time_to_live			TIMESTAMP				NULL,
	time_stamp				TIMESTAMP				NOT NULL,
	is_confidential		NUMBER(1)				NOT NULL,
	retries						NUMBER(5)				DEFAULT 0 NOT NULL
);

CREATE INDEX i_ebms_event ON ebms_event (time_stamp);

CREATE TABLE ebms_event_log
(
	ebms_message_id		NUMBER					NOT NULL REFERENCES ebms_message(id),
	time_stamp				TIMESTAMP				NOT NULL,
	uri								VARCHAR(256)		NULL,
	status						NUMBER(5)				NOT NULL,
	error_message			CLOB						NULL
);

CREATE TABLE ebms_message_event
(
	ebms_message_id		NUMBER					NOT NULL REFERENCES ebms_message(id) UNIQUE,
	event_type				NUMBER(5)				NOT NULL,
	time_stamp				TIMESTAMP				NOT NULL,
	processed					NUMBER(5)				DEFAULT 0 NOT NULL
);

CREATE INDEX i_ebms_message_event ON ebms_message_event (time_stamp);
