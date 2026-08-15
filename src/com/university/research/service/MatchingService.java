package com.university.research.service;

import com.university.research.model.ResearchTeam;
import com.university.research.model.Researcher;
import com.university.research.repository.ResearchRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MatchingService {
    private final ResearchRepository repository;

    public MatchingService(ResearchRepository repository) {
        this.repository = repository;
    }

    public List<TeamMatch> findMatches(int researcherId) {
        Researcher researcher = repository.findResearcherById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Researcher not found."));
        List<TeamMatch> matches = new ArrayList<>();
        for (ResearchTeam team : repository.findAllTeams()) {
            if (team.getMemberIds().contains(researcherId) || team.openSlots() <= 0) continue;
            int score = score(researcher, team);
            String reason = reason(researcher, team);
            matches.add(new TeamMatch(team, score, reason));
        }
        return matches.stream()
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .toList();
    }

    private int score(Researcher researcher, ResearchTeam team) {
        Set<String> researcherTokens = new HashSet<>();
        researcher.getResearchInterests().forEach(v -> addTokens(researcherTokens, v));
        researcher.getSkills().forEach(v -> addTokens(researcherTokens, v));

        Set<String> teamTokens = new HashSet<>();
        addTokens(teamTokens, team.getResearchArea());
        addTokens(teamTokens, team.getName());
        addTokens(teamTokens, team.getDescription());

        long overlap = researcherTokens.stream().filter(teamTokens::contains).count();
        int base = (int) Math.min(80, overlap * 14);
        int availability = researcher.isAvailable() ? 10 : 0;
        int roomBonus = team.openSlots() > 0 ? 10 : 0;
        return Math.min(100, base + availability + roomBonus);
    }

    private String reason(Researcher researcher, ResearchTeam team) {
        String area = team.getResearchArea().toLowerCase(Locale.ROOT);
        List<String> overlaps = researcher.getResearchInterests().stream()
                .filter(i -> area.contains(i.toLowerCase(Locale.ROOT)) ||
                        team.getDescription().toLowerCase(Locale.ROOT).contains(i.toLowerCase(Locale.ROOT)))
                .limit(2)
                .toList();
        if (!overlaps.isEmpty()) return "Shared interest: " + String.join(", ", overlaps);
        return "Open team with related research and transferable skills.";
    }

    private void addTokens(Set<String> set, String value) {
        if (value == null) return;
        for (String token : value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (token.length() > 2) set.add(token);
        }
    }

    public record TeamMatch(ResearchTeam team, int score, String reason) { }
}
