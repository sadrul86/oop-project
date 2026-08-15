package com.university.research.model;

import java.io.Serializable;

public class UserAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Role { STUDENT, FACULTY, ADMIN }

    private final int id;
    private final String email;
    private String displayName;
    private final Role role;
    private final String passwordSalt;
    private final String passwordHash;
    private Integer researcherId;
    private boolean active;

    public UserAccount(int id, String email, String displayName, Role role,
                       String passwordSalt, String passwordHash,
                       Integer researcherId, boolean active) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.role = role;
        this.passwordSalt = passwordSalt;
        this.passwordHash = passwordHash;
        this.researcherId = researcherId;
        this.active = active;
    }

    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public Role getRole() { return role; }
    public String getPasswordSalt() { return passwordSalt; }
    public String getPasswordHash() { return passwordHash; }
    public Integer getResearcherId() { return researcherId; }
    public boolean isActive() { return active; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setResearcherId(Integer researcherId) { this.researcherId = researcherId; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isAdmin() { return role == Role.ADMIN; }
    public boolean hasResearchProfile() { return researcherId != null; }
}
