package com.university.research.repository;

import com.university.research.model.JoinRequest;
import com.university.research.model.ResearchTeam;
import com.university.research.model.Researcher;
import com.university.research.model.UserAccount;
import com.university.research.util.PasswordUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class FileResearchRepository implements ResearchRepository {
    private final Path dataFile;
    private State state;

    public FileResearchRepository(Path dataFile) {
        this.dataFile = dataFile;
        this.state = loadState();
        normalizeState();
        if (state.researchers.isEmpty()) seedDemoData();
        if (state.userAccounts.isEmpty()) seedDemoAccounts();
        persist();
    }

    @Override
    public synchronized List<Researcher> findAllResearchers() { return new ArrayList<>(state.researchers); }

    @Override
    public synchronized Optional<Researcher> findResearcherById(int id) {
        return state.researchers.stream().filter(r -> r.getId() == id).findFirst();
    }

    @Override
    public synchronized Researcher saveResearcher(Researcher researcher) {
        state.researchers.removeIf(r -> r.getId() == researcher.getId());
        state.researchers.add(researcher);
        persist();
        return researcher;
    }

    @Override
    public synchronized int nextResearcherId() { return state.nextResearcherId++; }

    @Override
    public synchronized List<ResearchTeam> findAllTeams() { return new ArrayList<>(state.teams); }

    @Override
    public synchronized Optional<ResearchTeam> findTeamById(int id) {
        return state.teams.stream().filter(t -> t.getId() == id).findFirst();
    }

    @Override
    public synchronized ResearchTeam saveTeam(ResearchTeam team) {
        state.teams.removeIf(t -> t.getId() == team.getId());
        state.teams.add(team);
        persist();
        return team;
    }

    @Override
    public synchronized int nextTeamId() { return state.nextTeamId++; }

    @Override
    public synchronized List<JoinRequest> findAllJoinRequests() { return new ArrayList<>(state.joinRequests); }

    @Override
    public synchronized Optional<JoinRequest> findJoinRequestById(int id) {
        return state.joinRequests.stream().filter(r -> r.getId() == id).findFirst();
    }

    @Override
    public synchronized JoinRequest saveJoinRequest(JoinRequest request) {
        state.joinRequests.removeIf(r -> r.getId() == request.getId());
        state.joinRequests.add(request);
        persist();
        return request;
    }

    @Override
    public synchronized int nextJoinRequestId() { return state.nextJoinRequestId++; }

    @Override
    public synchronized List<UserAccount> findAllUserAccounts() { return new ArrayList<>(state.userAccounts); }

    @Override
    public synchronized Optional<UserAccount> findUserAccountById(int id) {
        return state.userAccounts.stream().filter(u -> u.getId() == id).findFirst();
    }

    @Override
    public synchronized Optional<UserAccount> findUserAccountByEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        return state.userAccounts.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(normalized))
                .findFirst();
    }

    @Override
    public synchronized UserAccount saveUserAccount(UserAccount account) {
        state.userAccounts.removeIf(u -> u.getId() == account.getId());
        state.userAccounts.add(account);
        persist();
        return account;
    }

    @Override
    public synchronized int nextUserAccountId() { return state.nextUserAccountId++; }

    private State loadState() {
        try {
            if (!Files.exists(dataFile)) return new State();
            try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(dataFile))) {
                Object object = input.readObject();
                if (object instanceof State loaded) return loaded;
            }
        } catch (Exception e) {
            System.err.println("Could not read existing data file. Starting with demo data: " + e.getMessage());
        }
        return new State();
    }

    private void normalizeState() {
        if (state.researchers == null) state.researchers = new ArrayList<>();
        if (state.teams == null) state.teams = new ArrayList<>();
        if (state.joinRequests == null) state.joinRequests = new ArrayList<>();
        if (state.userAccounts == null) state.userAccounts = new ArrayList<>();
        state.nextResearcherId = Math.max(state.nextResearcherId,
                state.researchers.stream().mapToInt(Researcher::getId).max().orElse(0) + 1);
        state.nextTeamId = Math.max(state.nextTeamId,
                state.teams.stream().mapToInt(ResearchTeam::getId).max().orElse(0) + 1);
        state.nextJoinRequestId = Math.max(state.nextJoinRequestId,
                state.joinRequests.stream().mapToInt(JoinRequest::getId).max().orElse(0) + 1);
        state.nextUserAccountId = Math.max(state.nextUserAccountId,
                state.userAccounts.stream().mapToInt(UserAccount::getId).max().orElse(0) + 1);
    }

    private void persist() {
        try {
            Path parent = dataFile.getParent();
            if (parent != null) Files.createDirectories(parent);
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(dataFile))) {
                output.writeObject(state);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save application data", e);
        }
    }

    private void seedDemoData() {
        Researcher r1 = new Researcher(nextResearcherId(), "Amina Rahman", "amina@example.edu", "Computer Science",
                Researcher.Role.STUDENT,
                List.of("Artificial Intelligence", "Machine Learning", "Healthcare"),
                List.of("Java", "Python", "Data Analysis"),
                "Interested in responsible AI and data-driven healthcare research.", true);
        Researcher r2 = new Researcher(nextResearcherId(), "Nabil Hasan", "nabil@example.edu", "Computer Science",
                Researcher.Role.STUDENT,
                List.of("Cybersecurity", "Networks", "Cloud Computing"),
                List.of("Java", "Linux", "Networking"),
                "Works on secure distributed systems and network monitoring.", true);
        Researcher r3 = new Researcher(nextResearcherId(), "Dr. Sara Karim", "sara@example.edu", "Software Engineering",
                Researcher.Role.FACULTY,
                List.of("Software Engineering", "Human Computer Interaction", "Education Technology"),
                List.of("Research Design", "Java", "Usability Testing"),
                "Faculty researcher focused on usable software systems and student learning.", true);
        Researcher r4 = new Researcher(nextResearcherId(), "Farhan Islam", "farhan@example.edu", "Environmental Science",
                Researcher.Role.STUDENT,
                List.of("Climate Change", "GIS", "Sustainability"),
                List.of("GIS", "Statistics", "Field Survey"),
                "Interested in climate resilience, spatial data and sustainability.", true);
        Researcher r5 = new Researcher(nextResearcherId(), "Mehjabin Noor", "mehjabin@example.edu", "Electrical Engineering",
                Researcher.Role.STUDENT,
                List.of("Internet of Things", "Renewable Energy", "Smart Campus"),
                List.of("IoT", "Arduino", "Java"),
                "Explores smart-campus sensing and energy-efficiency applications.", true);
        Researcher r6 = new Researcher(nextResearcherId(), "Dr. Imran Chowdhury", "imran@example.edu", "Data Science",
                Researcher.Role.FACULTY,
                List.of("Machine Learning", "Data Mining", "Smart Cities"),
                List.of("Machine Learning", "Statistics", "Research Supervision"),
                "Supervises applied machine learning and urban analytics projects.", true);

        state.researchers.addAll(List.of(r1, r2, r3, r4, r5, r6));

        ResearchTeam t1 = new ResearchTeam(nextTeamId(), "AI for Student Success", "Artificial Intelligence",
                "Build an interpretable model that identifies learning-support needs while protecting student privacy.",
                r3.getId(), 5, new ArrayList<>(List.of(r3.getId(), r1.getId())), ResearchTeam.Status.FORMING, LocalDateTime.now().minusDays(12));
        ResearchTeam t2 = new ResearchTeam(nextTeamId(), "Smart Green Campus", "Sustainability & IoT",
                "Develop low-cost sensing and analytics for campus energy use and environmental quality.",
                r6.getId(), 6, new ArrayList<>(List.of(r6.getId(), r4.getId(), r5.getId())), ResearchTeam.Status.FORMING, LocalDateTime.now().minusDays(7));
        ResearchTeam t3 = new ResearchTeam(nextTeamId(), "Secure Research Collaboration", "Cybersecurity",
                "Design a secure workflow for sharing research files, roles and approvals across student teams.",
                r2.getId(), 4, new ArrayList<>(List.of(r2.getId())), ResearchTeam.Status.FORMING, LocalDateTime.now().minusDays(3));

        state.teams.addAll(List.of(t1, t2, t3));

        JoinRequest req = new JoinRequest(nextJoinRequestId(), t2.getId(), r1.getId(),
                "I can contribute data analysis and machine-learning support for sensor data.", JoinRequest.Status.PENDING, LocalDateTime.now().minusHours(4));
        state.joinRequests.add(req);
    }

    private void seedDemoAccounts() {
        attachDemoAccount("amina@example.edu", "Amina Rahman", UserAccount.Role.STUDENT, "student123");
        attachDemoAccount("nabil@example.edu", "Nabil Hasan", UserAccount.Role.STUDENT, "leader123");
        attachDemoAccount("sara@example.edu", "Dr. Sara Karim", UserAccount.Role.FACULTY, "faculty123");
        attachDemoAccount("farhan@example.edu", "Farhan Islam", UserAccount.Role.STUDENT, "student123");
        attachDemoAccount("mehjabin@example.edu", "Mehjabin Noor", UserAccount.Role.STUDENT, "student123");
        attachDemoAccount("imran@example.edu", "Dr. Imran Chowdhury", UserAccount.Role.FACULTY, "faculty123");
        createAccount("admin@example.edu", "System Admin", UserAccount.Role.ADMIN, "admin123", null);
    }

    private void attachDemoAccount(String email, String name, UserAccount.Role role, String password) {
        Integer researcherId = state.researchers.stream()
                .filter(r -> r.getEmail().equalsIgnoreCase(email))
                .map(Researcher::getId).findFirst().orElse(null);
        createAccount(email, name, role, password, researcherId);
    }

    private void createAccount(String email, String name, UserAccount.Role role, String password, Integer researcherId) {
        String salt = PasswordUtil.newSalt();
        state.userAccounts.add(new UserAccount(nextUserAccountId(), email.toLowerCase(Locale.ROOT), name, role,
                salt, PasswordUtil.hash(password, salt), researcherId, true));
    }

    private static class State implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<Researcher> researchers = new ArrayList<>();
        private List<ResearchTeam> teams = new ArrayList<>();
        private List<JoinRequest> joinRequests = new ArrayList<>();
        private List<UserAccount> userAccounts = new ArrayList<>();
        private int nextResearcherId = 1;
        private int nextTeamId = 1;
        private int nextJoinRequestId = 1;
        private int nextUserAccountId = 1;
    }
}
