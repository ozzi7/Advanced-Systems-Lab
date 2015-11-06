drop function create_queue(integer);
drop function send_message_to_receiver(integer,integer,text,integer);
drop function send_message_to_any(integer,text,integer);
drop function query_queues(integer);
drop function delete_queue(integer,integer);
drop function peek_queue_with_sender(integer,integer,integer);
drop function pop_queue(integer,integer);
drop function pop_queue_with_sender(integer,integer,integer);
drop function peek_queue(integer,integer);
drop table messages;
drop table queues;
drop table clients;
drop type message;
drop type queue;
drop type error;

CREATE TABLE clients (
  client_id SERIAL PRIMARY KEY,
  arrival_time TIMESTAMP DEFAULT NOW()
 );

CREATE TABLE queues (
  queue_id SERIAL PRIMARY KEY,
  creation_time TIMESTAMP DEFAULT NOW(),
  created_by INTEGER NOT NULL,
  CONSTRAINT created_by FOREIGN KEY (created_by)
  REFERENCES clients (client_id)
 );

CREATE TABLE messages(
   message_id serial PRIMARY KEY,
   queue_id integer NOT NULL,
   message text NOT NULL,
   sender_id integer NOT NULL,
   receiver_id integer DEFAULT -1,
   arrival_time TIMESTAMP DEFAULT NOW(),
   CONSTRAINT fk_queue_id FOREIGN KEY (queue_id)
   REFERENCES queues (queue_id)
  ON DELETE CASCADE,
  CONSTRAINT fk_sender_id FOREIGN KEY (sender_id)
  REFERENCES clients (client_id)
);

CREATE TYPE message AS (message_text text, sender_id int, error_text text);
CREATE TYPE queue AS (error_text text, queue_id int);
CREATE TYPE error AS (error_text text);
CREATE OR REPLACE FUNCTION insert_starting_values(_nof_clients INTEGER) RETURNS void AS $$
DECLARE i INTEGER DEFAULT 1;
BEGIN
	WHILE i <= _nof_clients LOOP
		insert into clients (client_id,arrival_time) values (DEFAULT, DEFAULT);
		insert into queues (created_by) values(i);
		i = i + 1 ;
	END LOOP;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION create_queue(_sender_id INTEGER) 
RETURNS queue AS
$$
DECLARE
q queue;
BEGIN
IF NOT EXISTS ( SELECT 1 FROM clients WHERE client_id = _sender_id)
THEN 
	q.error_text := 'ERROR_AUTHENTICATION';
ELSE
	q.error_text := 'SUCCESS';
    INSERT INTO queues (created_by) VALUES (_sender_id);
	SELECT currval(pg_get_serial_sequence('queues','queue_id')) INTO q.queue_id;
END IF;
RETURN q;
END;
$$
LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE FUNCTION delete_queue(_queue_id INTEGER, _client_id INTEGER) 
RETURNS message AS
$$
DECLARE
m message;
BEGIN
PERFORM pg_advisory_lock(_queue_id);
IF NOT EXISTS ( SELECT 1 FROM clients WHERE client_id = _client_id)
THEN 
	m.error_text := 'ERROR_AUTHENTICATION';
ELSE
	IF NOT EXISTS ( SELECT 1 FROM queues WHERE queue_id = _queue_id)
	THEN 
		m.error_text := 'ERROR_NO_SUCH_QUEUE';
	ELSE
		m.error_text := 'SUCCESS';
		DELETE FROM queues
		WHERE queue_id = _queue_id;
	END IF;
END IF;
PERFORM pg_advisory_unlock(_queue_id);
RETURN m;
END;
$$
LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE FUNCTION peek_queue_with_sender(_queue_id INTEGER, _receiver_id INTEGER, _sender_id INTEGER) 
RETURNS message AS
$$
DECLARE
m message;
BEGIN
PERFORM pg_advisory_lock(_queue_id);
IF NOT EXISTS ( SELECT 1 FROM queues WHERE queue_id = _queue_id)
THEN 
	m.error_text := 'ERROR_NO_SUCH_QUEUE';
ELSE
	IF NOT EXISTS ( SELECT 1 FROM messages WHERE queue_id = _queue_id)
	THEN 
		m.error_text := 'ERROR_QUEUE_EMPTY';
	ELSE
		IF NOT EXISTS ( SELECT 1 FROM messages WHERE queue_id = _queue_id AND (receiver_id = -1 OR receiver_id =_receiver_id) AND _sender_id = sender_id)
		THEN
			m.error_text := 'ERROR_NO_MESSAGE';
		ELSE
			m.error_text := 'NO_ERROR';
			SELECT sender_id, message
			INTO m.sender_id, m.message_text
 			FROM messages
			WHERE queue_id = _queue_id AND (receiver_id = _receiver_id OR receiver_id = -1
				AND _sender_id = sender_id)
			ORDER BY arrival_time ASC LIMIT 1;
		END IF;
	END IF;
END IF;
PERFORM pg_advisory_unlock(_queue_id);
RETURN m;
END;
$$
LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE FUNCTION peek_queue(_queue_id INTEGER, _receiver_id INTEGER) 
RETURNS message AS
$$
DECLARE
m message;
BEGIN
PERFORM pg_advisory_lock(_queue_id);
IF NOT EXISTS ( SELECT 1 FROM queues WHERE queue_id = _queue_id)
THEN 
	m.error_text := 'ERROR_NO_SUCH_QUEUE';
ELSE
	IF NOT EXISTS ( SELECT 1 FROM messages WHERE queue_id = _queue_id)
	THEN 
		m.error_text := 'ERROR_QUEUE_EMPTY';
	ELSE
		IF NOT EXISTS ( SELECT 1 FROM messages WHERE queue_id = _queue_id AND (receiver_id = -1 OR receiver_id = _receiver_id))
		THEN
			m.error_text := 'ERROR_NO_MESSAGE';
		ELSE
			m.error_text := 'NO_ERROR';
			SELECT sender_id, message
			INTO m.sender_id, m.message_text
 			FROM messages
			WHERE queue_id = _queue_id AND (receiver_id = _receiver_id OR receiver_id = -1)
			ORDER BY arrival_time ASC LIMIT 1;
		END IF;
	END IF;
END IF;
PERFORM pg_advisory_unlock(_queue_id);
RETURN m;
END;
$$
LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE FUNCTION pop_queue_with_sender(_queue_id INTEGER, _receiver_id INTEGER, _sender_id INTEGER) 
RETURNS message AS
$$
DECLARE
m message;
BEGIN
PERFORM pg_advisory_lock(_queue_id);
IF NOT EXISTS ( SELECT 1 FROM queues WHERE queue_id = _queue_id)
THEN 
	m.error_text := 'ERROR_NO_SUCH_QUEUE';
ELSE
	IF NOT EXISTS ( SELECT 1 FROM messages WHERE queue_id = _queue_id)
	THEN 
		m.error_text := 'ERROR_QUEUE_EMPTY';
	ELSE
		IF NOT EXISTS ( SELECT 1 FROM messages WHERE queue_id = _queue_id AND
		(receiver_id = _receiver_id OR receiver_id = -1) AND _sender_id = sender_id)
		THEN
			m.error_text := 'ERROR_NO_MESSAGE';
		ELSE
			m.error_text := 'NO_ERROR';
			SELECT sender_id, message
			INTO m.sender_id, m.message_text
 			FROM messages
			WHERE queue_id = _queue_id AND (receiver_id = _receiver_id OR receiver_id = -1)
				AND _sender_id = sender_id
			ORDER BY arrival_time ASC
			LIMIT 1;

			DELETE FROM messages
			WHERE message_id = (
			SELECT m2.message_id
			FROM messages AS m2
			WHERE queue_id = _queue_id AND (receiver_id = -1 OR receiver_id = _receiver_id) AND _sender_id = sender_id
			ORDER BY arrival_time ASC
			LIMIT 1);
		END IF;
	END IF;
END IF;
PERFORM pg_advisory_unlock(_queue_id);
RETURN m;
END;
$$
LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE FUNCTION pop_queue(_queue_id INTEGER, _receiver_id INTEGER) 
RETURNS message AS
$$
DECLARE
m message;
BEGIN
PERFORM pg_advisory_lock(_queue_id);
IF NOT EXISTS ( SELECT 1 FROM queues WHERE queue_id = _queue_id)
THEN 
	m.error_text := 'ERROR_NO_SUCH_QUEUE';
ELSE
	IF NOT EXISTS ( SELECT 1 FROM messages WHERE queue_id = _queue_id)
	THEN 
		m.error_text := 'ERROR_QUEUE_EMPTY';
	ELSE
		IF NOT EXISTS ( SELECT 1 FROM messages WHERE queue_id = _queue_id AND
		(receiver_id = _receiver_id OR receiver_id = -1))
		THEN
			m.error_text := 'ERROR_NO_MESSAGE';
		ELSE
			m.error_text := 'NO_ERROR';
			SELECT sender_id, message
			INTO m.sender_id, m.message_text
 			FROM messages
			WHERE queue_id = _queue_id AND (receiver_id = _receiver_id OR receiver_id = -1)
			ORDER BY arrival_time ASC
			LIMIT 1;

			DELETE FROM messages
			WHERE message_id = (
			SELECT m2.message_id
			FROM messages AS m2
			WHERE queue_id = _queue_id AND (receiver_id = -1 OR receiver_id = _receiver_id)
			ORDER BY arrival_time ASC
			LIMIT 1);
		END IF;
	END IF;
END IF;
PERFORM pg_advisory_unlock(_queue_id);
RETURN m;
END;
$$
LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE FUNCTION send_message_to_receiver(_sender_id INTEGER, _receiver_id INTEGER, _message text, _queue_id INTEGER) 
RETURNS error AS
$$
DECLARE
e error;
BEGIN
IF NOT EXISTS ( SELECT 1 FROM queues WHERE queue_id = _queue_id)
THEN 
	e.error_text := 'ERROR_NO_SUCH_QUEUE';
ELSE
	IF NOT EXISTS ( SELECT 1 FROM clients WHERE client_id = _sender_id)
	THEN 
		e.error_text := 'ERROR_AUTHENTICATION';
	ELSE
		e.error_text := 'SUCCESS';
		INSERT INTO messages(sender_id, receiver_id, queue_id, message)
    	VALUES(_sender_id, _receiver_id, _queue_id, _message);
	END IF;
END IF;
RETURN e;
END;
$$
LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE FUNCTION send_message_to_any(_sender_id INTEGER, _message text, _queue_id INTEGER) 
RETURNS error AS
$$
DECLARE
e error;
BEGIN
IF NOT EXISTS ( SELECT 1 FROM queues WHERE queue_id = _queue_id)
THEN 
	e.error_text := 'ERROR_NO_SUCH_QUEUE';
ELSE
	IF NOT EXISTS ( SELECT 1 FROM clients WHERE client_id = _sender_id)
	THEN 
		e.error_text := 'ERROR_AUTHENTICATION';
	ELSE
		e.error_text := 'SUCCESS';
	INSERT INTO messages(sender_id, queue_id, message)
    VALUES(_sender_id, _queue_id, _message);
	END IF;
END IF;
RETURN e;
END;
$$
LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE FUNCTION query_queues(
	_receiver_id INTEGER) RETURNS TABLE(queue_id INTEGER) AS $$
	SELECT DISTINCT ON (queue_id) queue_id
    FROM messages
    WHERE receiver_id = _receiver_id
$$ LANGUAGE sql VOLATILE;

CREATE INDEX index_receiverID ON messages(receiver_id);
CREATE INDEX index_senderID ON messages(sender_id);
CREATE INDEX index_queueID_ReceiverID_SenderID_ArrivalTime ON messages(queue_id, receiver_id, sender_id, arrival_time);
CREATE INDEX index_queueID_ReceiverID_ArrivalTime ON messages(queue_id, receiver_id, arrival_time);

SELECT insert_starting_values(100);


