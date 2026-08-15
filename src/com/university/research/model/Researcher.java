package com.university.research.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Researcher implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Role { STUDENT, FACULTY }

    private final int id;
    private String name;
    private String email;
    private String department;
    private Role role;
    private List<String> researchInterests;
    private List<String> skills;
    private String bio;
    private boolean available;

    public Researcher(int id, String name, String email, String department, Role role,
                      List<String> researchInterests, List<String> skills, String bio, boolean available) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.role = role;
        this.researchInterests = new ArrayList<>(researchInterests);
        this.skills = new ArrayList<>(skills);
        this.bio = bio;
        this.available = available;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public Role getRole() { return role; }
    public List<String> getResearchInterests() { return new ArrayList<>(researchInterests); }
    public List<String> getSkills() { return new ArrayList<>(skills); }
    public String getBio() { return bio; }
    public boolean isAvailable() { return available; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setDepartment(String department) { this.department = department; }
    public void setRole(Role role) { this.role = role; }
    public void setResearchInterests(List<String> researchInterests) { this.researchInterests = new ArrayList<>(researchInterests); }
    public void setSkills(List<String> skills) { this.skills = new ArrayList<>(skills); }
    public void setBio(String bio) { this.bio = bio; }
    public void setAvailable(boolean available) { this.available = available; }
}
