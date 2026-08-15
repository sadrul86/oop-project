package com.university.research.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TeamDiscussionPost implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final int teamId;
    private final int authorResearcherId;
    private final String message;
    private final LocalDateTime createdAt;

    public TeamDiscussionPost(int id, int teamId, int authorResearcherId, String message, LocalDateTime createdAt) {
        this.id = id;
        this.teamId = teamId;
        this.authorResearcherId = authorResearcherId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getTeamId() { return teamId; }
    public int getAuthorResearcherId() { return authorResearcherId; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
