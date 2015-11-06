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

