package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "audit_logs")
public class AuditLog {
    @Id
    private String id;
    private String actionType; // e.g., ORDER_RESERVED, HUB_DELETED
    private String userPrincipal;
    private String description;
    private String idempotencyOrRef;
    private Date timestamp;

    public AuditLog() {}

    public AuditLog(String actionType, String userPrincipal, String description, String idempotencyOrRef) {
        this.actionType = actionType;
        this.userPrincipal = userPrincipal;
        this.description = description;
        this.idempotencyOrRef = idempotencyOrRef;
        this.timestamp = new Date();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getUserPrincipal() { return userPrincipal; }
    public void setUserPrincipal(String userPrincipal) { this.userPrincipal = userPrincipal; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIdempotencyOrRef() { return idempotencyOrRef; }
    public void setIdempotencyOrRef(String idempotencyOrRef) { this.idempotencyOrRef = idempotencyOrRef; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
