package com.comp30022.team_russia.assist.features.message.models;

public class Message {
    private int msg_id;
    private String message; // message body
    private User sender; // the sender of the message
    private String createdAt; // when message was created

    public Message(int id, String message, User sender, String createdAt) {
        this.msg_id = id;
        this.message = message;
        this.sender = sender;
        this.createdAt = createdAt;
    }

    public int getMsgId() { return msg_id; }

    public String getMessage() {
        return message;
    }

    public User getSender() {
        return sender;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
