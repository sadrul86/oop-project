package com.university.research.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TeamMeeting implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final int teamId;
    private final int createdByResearcherId;
    private final String title;
    private final String agenda;
    private final String meetingLink;
    private final LocalDateTime scheduledAt;
    private final LocalDateTime createdAt;

    public TeamMeeting(int id, int teamId, int createdByResearcherId, String title, String agenda,
                       String meetingLink, LocalDateTime scheduledAt, LocalDateTime createdAt) {
        this.id = id;
        this.teamId = teamId;
        this.createdByResearcherId = createdByResearcherId;
        this.title = title;
        this.agenda = agenda;
        this.meetingLink = meetingLink;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getTeamId() { return teamId; }
    public int getCreatedByResearcherId() { return createdByResearcherId; }
    public String getTitle() { return title; }
    public String getAgenda() { return agenda; }
    public String getMeetingLink() { return meetingLink; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
