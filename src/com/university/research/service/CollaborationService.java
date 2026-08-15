package com.university.research.service;

import com.university.research.model.ResearchTeam;
import com.university.research.model.TeamDiscussionPost;
import com.university.research.model.TeamMeeting;
import com.university.research.repository.ResearchRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class CollaborationService {
    private final ResearchRepository repository;

    public CollaborationService(ResearchRepository repository) {
        this.repository = repository;
    }

    public List<TeamDiscussionPost> listDiscussionPosts(int teamId, Integer viewerResearcherId, boolean admin) {
        ResearchTeam team = getTeam(teamId);
        authorizeWorkspaceView(team, viewerResearcherId, admin);
        return repository.findAllDiscussionPosts().stream()
                .filter(post -> post.getTeamId() == teamId)
                .sorted(Comparator.comparing(TeamDiscussionPost::getCreatedAt).reversed())
                .toList();
    }

    public TeamDiscussionPost addDiscussionPost(int teamId, int authorResearcherId, String message) {
        ResearchTeam team = getTeam(teamId);
        if (!team.getMemberIds().contains(authorResearcherId)) {
            throw new SecurityException("Only members of this research team can post in its discussion.");
        }
        String cleanMessage = message == null ? "" : message.trim();
        if (cleanMessage.isBlank()) throw new IllegalArgumentException("Discussion message cannot be empty.");
        if (cleanMessage.length() > 1000) throw new IllegalArgumentException("Discussion message must be 1000 characters or fewer.");

        TeamDiscussionPost post = new TeamDiscussionPost(
                repository.nextDiscussionPostId(), teamId, authorResearcherId, cleanMessage, LocalDateTime.now());
        return repository.saveDiscussionPost(post);
    }

    public List<TeamMeeting> listMeetings(int teamId, Integer viewerResearcherId, boolean admin) {
        ResearchTeam team = getTeam(teamId);
        authorizeWorkspaceView(team, viewerResearcherId, admin);
        LocalDateTime now = LocalDateTime.now();
        return repository.findAllMeetings().stream()
                .filter(meeting -> meeting.getTeamId() == teamId)
                .sorted(Comparator
                        .comparing((TeamMeeting meeting) -> meeting.getScheduledAt().isBefore(now))
                        .thenComparing(TeamMeeting::getScheduledAt))
                .toList();
    }

    public TeamMeeting scheduleMeeting(int teamId, int actorResearcherId, String title, String agenda,
                                       String meetingLink, LocalDateTime scheduledAt) {
        ResearchTeam team = getTeam(teamId);
        if (team.getLeaderId() != actorResearcherId) {
            throw new SecurityException("Only this team's leader can schedule a meeting.");
        }

        String cleanTitle = title == null ? "" : title.trim();
        String cleanAgenda = agenda == null ? "" : agenda.trim();
        String cleanLink = meetingLink == null ? "" : meetingLink.trim();
        if (cleanTitle.isBlank()) throw new IllegalArgumentException("Meeting title is required.");
        if (cleanTitle.length() > 120) throw new IllegalArgumentException("Meeting title must be 120 characters or fewer.");
        if (cleanAgenda.length() > 800) throw new IllegalArgumentException("Meeting agenda must be 800 characters or fewer.");
        if (scheduledAt == null || !scheduledAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Meeting date and time must be in the future.");
        }
        if (!cleanLink.isBlank()) validateMeetingLink(cleanLink);

        TeamMeeting meeting = new TeamMeeting(
                repository.nextMeetingId(), teamId, actorResearcherId, cleanTitle, cleanAgenda,
                cleanLink, scheduledAt, LocalDateTime.now());
        return repository.saveMeeting(meeting);
    }

    private ResearchTeam getTeam(int teamId) {
        return repository.findTeamById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Research team not found."));
    }

    private void authorizeWorkspaceView(ResearchTeam team, Integer viewerResearcherId, boolean admin) {
        if (admin) return;
        if (viewerResearcherId == null || !team.getMemberIds().contains(viewerResearcherId)) {
            throw new SecurityException("Team discussions and meetings are private to team members.");
        }
    }

    private void validateMeetingLink(String link) {
        try {
            URI uri = URI.create(link);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException("Meeting link must be a valid http or https URL.");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Meeting link must be a valid http or https URL.");
        }
    }
}
