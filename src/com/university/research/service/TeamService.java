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
                .orElseThrow(() -> new IllegalArgumentException("A valid research profile is required to create a team."));

        ResearchTeam team = new ResearchTeam(
                repository.nextTeamId(), name.trim(), researchArea.trim(), description.trim(),
                leader.getId(), targetSize, new ArrayList<>(List.of(leader.getId())),
                ResearchTeam.Status.FORMING, LocalDateTime.now());
        return repository.saveTeam(team);
    }

    public JoinRequest requestToJoin(int teamId, int researcherId, String message) {
        ResearchTeam team = getTeam(teamId);
        Researcher researcher = repository.findResearcherById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Researcher not found."));
        if (team.getLeaderId() == researcherId) {
            throw new IllegalArgumentException("A team leader cannot send a join request to their own team.");
        }
        if (team.getMemberIds().contains(researcher.getId())) {
            throw new IllegalArgumentException("You are already a member of this team.");
        }
        if (team.openSlots() <= 0) throw new IllegalArgumentException("This team is already full.");

        boolean existingRequest = repository.findAllJoinRequests().stream()
                .anyMatch(r -> r.getTeamId() == teamId && r.getResearcherId() == researcherId
                        && (r.getStatus() == JoinRequest.Status.PENDING || r.getStatus() == JoinRequest.Status.APPROVED));
        if (existingRequest) throw new IllegalArgumentException("You already have a pending or approved request for this team.");

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

    public void approveRequest(int requestId, Integer actorResearcherId, boolean admin) {
        JoinRequest request = getPendingRequest(requestId);
        ResearchTeam team = getTeam(request.getTeamId());
        authorizeDecision(team, request, actorResearcherId, admin);
        if (!team.addMember(request.getResearcherId())) {
            throw new IllegalArgumentException("The researcher cannot be added because the team is full or already contains the member.");
        }
        request.setStatus(JoinRequest.Status.APPROVED);
        repository.saveTeam(team);
        repository.saveJoinRequest(request);
    }

    public void rejectRequest(int requestId, Integer actorResearcherId, boolean admin) {
        JoinRequest request = getPendingRequest(requestId);
        ResearchTeam team = getTeam(request.getTeamId());
        authorizeDecision(team, request, actorResearcherId, admin);
        request.setStatus(JoinRequest.Status.REJECTED);
        repository.saveJoinRequest(request);
    }

    public boolean isLeaderOf(int researcherId, int teamId) {
        return getTeam(teamId).getLeaderId() == researcherId;
    }

    private JoinRequest getPendingRequest(int requestId) {
        JoinRequest request = repository.findJoinRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Join request not found."));
        if (request.getStatus() != JoinRequest.Status.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be changed.");
        }
        return request;
    }

    private void authorizeDecision(ResearchTeam team, JoinRequest request, Integer actorResearcherId, boolean admin) {
        if (!admin) {
            if (actorResearcherId == null || team.getLeaderId() != actorResearcherId) {
                throw new IllegalArgumentException("Only this team's leader can approve or reject its requests.");
            }
            if (request.getResearcherId() == actorResearcherId) {
                throw new IllegalArgumentException("You cannot approve or reject your own join request.");
            }
        }
    }
}
