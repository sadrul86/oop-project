package com.university.research.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResearchTeam implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { FORMING, ACTIVE, COMPLETED }

    private final int id;
    private String name;
    private String researchArea;
    private String description;
    private int leaderId;
    private int targetSize;
    private final List<Integer> memberIds;
    private Status status;
    private final LocalDateTime createdAt;

    public ResearchTeam(int id, String name, String researchArea, String description,
                        int leaderId, int targetSize, List<Integer> memberIds, Status status,
                        LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.researchArea = researchArea;
        this.description = description;
        this.leaderId = leaderId;
        this.targetSize = targetSize;
        this.memberIds = new ArrayList<>(memberIds);
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getResearchArea() { return researchArea; }
    public String getDescription() { return description; }
    public int getLeaderId() { return leaderId; }
    public int getTargetSize() { return targetSize; }
    public List<Integer> getMemberIds() { return new ArrayList<>(memberIds); }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setResearchArea(String researchArea) { this.researchArea = researchArea; }
    public void setDescription(String description) { this.description = description; }
    public void setLeaderId(int leaderId) { this.leaderId = leaderId; }
    public void setTargetSize(int targetSize) { this.targetSize = targetSize; }
    public void setStatus(Status status) { this.status = status; }

    public boolean addMember(int researcherId) {
        if (memberIds.contains(researcherId) || memberIds.size() >= targetSize) return false;
        memberIds.add(researcherId);
        if (memberIds.size() >= targetSize) status = Status.ACTIVE;
        return true;
    }

    public int openSlots() {
        return Math.max(0, targetSize - memberIds.size());
    }
}
