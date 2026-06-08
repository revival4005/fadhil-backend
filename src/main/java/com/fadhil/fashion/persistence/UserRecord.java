package com.fadhil.fashion.persistence;

public class UserRecord {

    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private String createdAt;

    public UserRecord() {
    }

    public UserRecord(String id, String name, String email, String passwordHash, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
