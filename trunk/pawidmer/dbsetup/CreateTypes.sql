CREATE TYPE message AS (message_text text, sender_id int, error_text text);
CREATE TYPE queue AS (error_text text, queue_id int);
CREATE TYPE error AS (error_text text);
