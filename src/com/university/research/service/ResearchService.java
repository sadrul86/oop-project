package com.university.research.service;

import com.university.research.model.Researcher;
import com.university.research.repository.ResearchRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ResearchService {
    private final ResearchRepository repository;

    public ResearchService(ResearchRepository repository) {
        this.repository = repository;
    }

    public List<Researcher> listResearchers(String query, String role) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String roleFilter = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        return repository.findAllResearchers().stream()
                .filter(r -> q.isBlank() || searchableText(r).contains(q))
                .filter(r -> roleFilter.isBlank() || r.getRole().name().equals(roleFilter))
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
    }

    public Researcher getResearcher(int id) {
        return repository.findResearcherById(id)
                .orElseThrow(() -> new IllegalArgumentException("Researcher not found."));
    }

    public Researcher createResearcher(String name, String email, String department, String role,
                                       String interests, String skills, String bio) {
        require(name, "Name");
        require(email, "Email");
        require(department, "Department");
        if (!email.contains("@")) throw new IllegalArgumentException("Enter a valid email address.");

        Researcher.Role parsedRole;
        try {
            parsedRole = Researcher.Role.valueOf(role == null ? "STUDENT" : role.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new IllegalArgumentException("Role must be STUDENT or FACULTY.");
        }

        Researcher researcher = new Researcher(
                repository.nextResearcherId(),
                name.trim(),
                email.trim(),
                department.trim(),
                parsedRole,
                splitCsv(interests),
                splitCsv(skills),
                bio == null ? "" : bio.trim(),
                true
        );
        return repository.saveResearcher(researcher);
    }

    private String searchableText(Researcher r) {
        return String.join(" ", r.getName(), r.getDepartment(), r.getRole().name(),
                String.join(" ", r.getResearchInterests()), String.join(" ", r.getSkills()), r.getBio())
                .toLowerCase(Locale.ROOT);
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }

    private void require(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required.");
    }
}
