package com.university.research.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class JoinRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { PENDING, APPROVED, REJECTED }

    private final int id;
    private final int teamId;
    private final int researcherId;
    private final String message;
    private Status status;
    private final LocalDateTime createdAt;

    public JoinRequest(int id, int teamId, int researcherId, String message, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.teamId = teamId;
        this.researcherId = researcherId;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getTeamId() { return teamId; }
    public int getResearcherId() { return researcherId; }
    public String getMessage() { return message; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setStatus(Status status) { this.status = status; }
}
