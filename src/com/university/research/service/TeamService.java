package com.university.research.service;

import com.university.research.model.JoinRequest;
import com.university.research.model.ResearchTeam;
import com.university.research.model.Researcher;
import com.university.research.repository.ResearchRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeamService {
    private final ResearchRepository repository;

    public TeamService(ResearchRepository repository) {
        this.repository = repository;
    }

    public List<ResearchTeam> listTeams(String query, String status) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String statusFilter = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        return repository.findAllTeams().stream()
                .filter(t -> q.isBlank() || (t.getName() + " " + t.getResearchArea() + " " + t.getDescription())
                        .toLowerCase(Locale.ROOT).contains(q))
                .filter(t -> statusFilter.isBlank() || t.getStatus().name().equals(statusFilter))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    public ResearchTeam getTeam(int id) {
        return repository.findTeamById(id)
                .orElseThrow(() -> new IllegalArgumentException("Research team not found."));
    }

    public ResearchTeam createTeam(String name, String researchArea, String description,
                                   int leaderId, int targetSize) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Team name is required.");
        if (researchArea == null || researchArea.isBlank()) throw new IllegalArgumentException("Research area is required.");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description is required.");
        if (targetSize < 2 || targetSize > 12) throw new IllegalArgumentException("Target team size must be between 2 and 12.");
        Researcher leader = repository.findResearcherById(leaderId)
                .orElseThrow(() -> new IllegalArgumentException("Select a valid team leader."));

        ResearchTeam team = new ResearchTeam(
                repository.nextTeamId(),
                name.trim(),
                researchArea.trim(),
                description.trim(),
                leader.getId(),
                targetSize,
                new ArrayList<>(List.of(leader.getId())),
                ResearchTeam.Status.FORMING,
                LocalDateTime.now()
        );
        return repository.saveTeam(team);
    }

    public JoinRequest requestToJoin(int teamId, int researcherId, String message) {
        ResearchTeam team = getTeam(teamId);
        Researcher researcher = repository.findResearcherById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Researcher not found."));
        if (team.getMemberIds().contains(researcher.getId())) {
            throw new IllegalArgumentException("This researcher is already a member of the team.");
        }
        if (team.openSlots() <= 0) throw new IllegalArgumentException("This team is already full.");

        boolean duplicatePending = repository.findAllJoinRequests().stream()
                .anyMatch(r -> r.getTeamId() == teamId && r.getResearcherId() == researcherId
                        && r.getStatus() == JoinRequest.Status.PENDING);
        if (duplicatePending) throw new IllegalArgumentException("A pending join request already exists for this researcher and team.");

        JoinRequest request = new JoinRequest(
                repository.nextJoinRequestId(), teamId, researcherId,
                message == null ? "" : message.trim(), JoinRequest.Status.PENDING, LocalDateTime.now());
        return repository.saveJoinRequest(request);
    }

    public List<JoinRequest> listRequests() {
        return repository.findAllJoinRequests().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    public void approveRequest(int requestId) {
        JoinRequest request = repository.findJoinRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Join request not found."));
        if (request.getStatus() != JoinRequest.Status.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be approved.");
        }
        ResearchTeam team = getTeam(request.getTeamId());
        if (!team.addMember(request.getResearcherId())) {
            throw new IllegalArgumentException("The researcher cannot be added because the team is full or already contains the member.");
        }
        request.setStatus(JoinRequest.Status.APPROVED);
        repository.saveTeam(team);
        repository.saveJoinRequest(request);
    }

    public void rejectRequest(int requestId) {
        JoinRequest request = repository.findJoinRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Join request not found."));
        if (request.getStatus() != JoinRequest.Status.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be rejected.");
        }
        request.setStatus(JoinRequest.Status.REJECTED);
        repository.saveJoinRequest(request);
    }
}
