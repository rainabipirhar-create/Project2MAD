package com.example.myproject2;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class ChatMessage {

    private String message;
    private String senderId;
    private Date timestamp;

    // Required empty public constructor for Firestore
    public ChatMessage() {}

    public ChatMessage(String message, String senderId) {
        this.message = message;
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderId() {
        return senderId;
    }

    @ServerTimestamp // This annotation is crucial for Firestore to set the server time
    public Date getTimestamp() {
        return timestamp;
    }

    // Setter is needed for Firestore to populate the timestamp field
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
