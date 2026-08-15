package com.university.research.repository;

import com.university.research.model.JoinRequest;
import com.university.research.model.ResearchTeam;
import com.university.research.model.Researcher;

import java.util.List;
import java.util.Optional;

public interface ResearchRepository {
    List<Researcher> findAllResearchers();
    Optional<Researcher> findResearcherById(int id);
    Researcher saveResearcher(Researcher researcher);
    int nextResearcherId();

    List<ResearchTeam> findAllTeams();
    Optional<ResearchTeam> findTeamById(int id);
    ResearchTeam saveTeam(ResearchTeam team);
    int nextTeamId();

    List<JoinRequest> findAllJoinRequests();
    Optional<JoinRequest> findJoinRequestById(int id);
    JoinRequest saveJoinRequest(JoinRequest request);
    int nextJoinRequestId();
}
