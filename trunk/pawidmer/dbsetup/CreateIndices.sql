CREATE INDEX index_receiverID ON messages(receiver_id);
CREATE INDEX index_senderID ON messages(sender_id);
CREATE INDEX index_queueID_ReceiverID_SenderID_ArrivalTime ON messages(queue_id, receiver_id, sender_id, arrival_time);
CREATE INDEX index_queueID_ReceiverID_ArrivalTime ON messages(queue_id, receiver_id, arrival_time);
