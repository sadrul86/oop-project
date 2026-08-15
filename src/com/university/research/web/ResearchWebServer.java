package com.university.research.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.university.research.model.JoinRequest;
import com.university.research.model.ResearchTeam;
import com.university.research.model.Researcher;
import com.university.research.model.UserAccount;
import com.university.research.repository.ResearchRepository;
import com.university.research.service.AuthenticationService;
import com.university.research.service.MatchingService;
import com.university.research.service.ResearchService;
import com.university.research.service.TeamService;
import com.university.research.util.WebUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class ResearchWebServer {
    private final HttpServer server;
    private final ResearchRepository repository;
    private final AuthenticationService authenticationService;
    private final ResearchService researchService;
    private final TeamService teamService;
    private final MatchingService matchingService;
    private final SessionManager sessionManager;
    private final Path publicDir;

    public ResearchWebServer(int port, ResearchRepository repository, Path publicDir) throws IOException {
        this.repository = repository;
        this.authenticationService = new AuthenticationService(repository);
        this.researchService = new ResearchService(repository);
        this.teamService = new TeamService(repository);
        this.matchingService = new MatchingService(repository);
        this.sessionManager = new SessionManager();
        this.publicDir = publicDir.toAbsolutePath().normalize();
        this.server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        this.server.createContext("/", this::handle);
        this.server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }

    public void start() { server.start(); }

    private void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try {
            if (path.startsWith("/assets/")) {
                serveStatic(exchange, path);
                return;
            }

            if ("GET".equals(method) && "/".equals(path)) showHome(exchange);
            else if ("GET".equals(method) && "/health".equals(path)) sendText(exchange, 200, "OK");
            else if ("GET".equals(method) && "/api/stats".equals(path)) sendJson(exchange, 200, statsJson());
            else if ("GET".equals(method) && "/login".equals(path)) showLogin(exchange);
            else if ("POST".equals(method) && "/login".equals(path)) login(exchange);
            else if ("POST".equals(method) && "/logout".equals(path)) logout(exchange);
            else if ("GET".equals(method) && "/register".equals(path)) showRegister(exchange);
            else if ("POST".equals(method) && "/register".equals(path)) register(exchange);
            else if ("GET".equals(method) && "/dashboard".equals(path)) showDashboard(exchange);
            else if ("GET".equals(method) && "/profile/edit".equals(path)) showProfile(exchange);
            else if ("POST".equals(method) && "/profile".equals(path)) updateProfile(exchange);
            else if ("GET".equals(method) && "/about".equals(path)) showAbout(exchange);
            else if ("GET".equals(method) && "/researchers".equals(path)) showResearchers(exchange);
            else if ("GET".equals(method) && "/researchers/new".equals(path)) redirectLegacyProfileRoute(exchange);
            else if ("POST".equals(method) && "/researchers".equals(path)) sendForbidden(exchange, "Researcher profiles can only be created through account registration.");
            else if ("GET".equals(method) && "/teams".equals(path)) showTeams(exchange);
            else if ("GET".equals(method) && "/teams/new".equals(path)) showNewTeam(exchange);
            else if ("POST".equals(method) && "/teams".equals(path)) createTeam(exchange);
            else if ("GET".equals(method) && "/teams/view".equals(path)) showTeam(exchange);
            else if ("POST".equals(method) && "/teams/join".equals(path)) joinTeam(exchange);
            else if ("GET".equals(method) && "/requests".equals(path)) showRequests(exchange);
            else if ("POST".equals(method) && "/requests/approve".equals(path)) updateRequest(exchange, true);
            else if ("POST".equals(method) && "/requests/reject".equals(path)) updateRequest(exchange, false);
            else if ("GET".equals(method) && "/matches".equals(path)) showMatches(exchange);
            else sendHtml(exchange, 404, HtmlPages.error("Page not found", "The requested page does not exist.", 404, currentUser(exchange), currentProfile(currentUser(exchange))));
        } catch (SecurityException e) {
            sendHtml(exchange, 403, HtmlPages.error("Access denied", e.getMessage(), 403, currentUser(exchange), currentProfile(currentUser(exchange))));
        } catch (IllegalArgumentException e) {
            sendHtml(exchange, 400, HtmlPages.error("Request could not be completed", e.getMessage(), 400, currentUser(exchange), currentProfile(currentUser(exchange))));
        } catch (Exception e) {
            e.printStackTrace();
            sendHtml(exchange, 500, HtmlPages.error("Server error", "An unexpected error occurred. Check the application log for details.", 500, currentUser(exchange), currentProfile(currentUser(exchange))));
        } finally {
            exchange.close();
        }
    }

    private void showHome(HttpExchange exchange) throws IOException {
        UserAccount user = currentUser(exchange);
        Researcher profile = currentProfile(user);
        sendHtml(exchange, 200, HtmlPages.home(repository.findAllResearchers(), repository.findAllTeams(), repository.findAllJoinRequests(), user, profile));
    }

    private void showLogin(HttpExchange exchange) throws IOException {
        if (currentUser(exchange) != null) {
            redirect(exchange, "/dashboard");
            return;
        }
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        sendHtml(exchange, 200, HtmlPages.login(params.get("message")));
    }

    private void login(HttpExchange exchange) throws IOException {
        Map<String, String> form = WebUtil.formParams(exchange);
        try {
            UserAccount account = authenticationService.authenticate(form.get("email"), form.get("password"));
            String token = sessionManager.createSession(account.getId());
            sessionManager.setSessionCookie(exchange, token);
            redirect(exchange, "/dashboard?message=" + WebUtil.encode("Welcome back, " + account.getDisplayName() + "."));
        } catch (IllegalArgumentException e) {
            redirect(exchange, "/login?message=" + WebUtil.encode(e.getMessage()));
        }
    }

    private void logout(HttpExchange exchange) throws IOException {
        sessionManager.logout(exchange);
        redirect(exchange, "/login?message=" + WebUtil.encode("You have been logged out."));
    }

    private void showRegister(HttpExchange exchange) throws IOException {
        if (currentUser(exchange) != null) {
            redirect(exchange, "/dashboard");
            return;
        }
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        sendHtml(exchange, 200, HtmlPages.register(params.get("message")));
    }

    private void register(HttpExchange exchange) throws IOException {
        Map<String, String> form = WebUtil.formParams(exchange);
        try {
            UserAccount account = authenticationService.register(form.get("name"), form.get("email"), form.get("password"), form.get("department"), form.get("role"));
            String token = sessionManager.createSession(account.getId());
            sessionManager.setSessionCookie(exchange, token);
            redirect(exchange, "/profile/edit?message=" + WebUtil.encode("Account created. Complete your research interests and skills."));
        } catch (IllegalArgumentException e) {
            redirect(exchange, "/register?message=" + WebUtil.encode(e.getMessage()));
        }
    }

    private void showDashboard(HttpExchange exchange) throws IOException {
        UserAccount user = requireUser(exchange);
        if (user == null) return;
        Researcher profile = currentProfile(user);
        List<ResearchTeam> ownedTeams = profile == null ? List.of() : repository.findAllTeams().stream()
                .filter(t -> t.getLeaderId() == profile.getId()).toList();
        List<JoinRequest> myRequests = profile == null ? List.of() : teamService.listRequests().stream()
                .filter(r -> r.getResearcherId() == profile.getId()).toList();
        int incomingPending = user.isAdmin()
                ? (int) teamService.listRequests().stream().filter(r -> r.getStatus() == JoinRequest.Status.PENDING).count()
                : (int) teamService.listRequests().stream().filter(r -> r.getStatus() == JoinRequest.Status.PENDING)
                    .filter(r -> ownedTeams.stream().anyMatch(t -> t.getId() == r.getTeamId())).count();
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        sendHtml(exchange, 200, HtmlPages.dashboard(user, profile, ownedTeams, myRequests, incomingPending, params.get("message")));
    }

    private void showProfile(HttpExchange exchange) throws IOException {
        UserAccount user = requireUser(exchange);
        if (user == null) return;
        Researcher profile = requireProfile(exchange, user);
        if (profile == null) return;
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        sendHtml(exchange, 200, HtmlPages.editProfile(user, profile, params.get("message")));
    }

    private void updateProfile(HttpExchange exchange) throws IOException {
        UserAccount user = requireUser(exchange);
        if (user == null) return;
        Researcher profile = requireProfile(exchange, user);
        if (profile == null) return;
        Map<String, String> form = WebUtil.formParams(exchange);
        researchService.updateOwnProfile(profile.getId(), form.get("department"), form.get("interests"), form.get("skills"), form.get("bio"), "yes".equals(form.get("available")));
        redirect(exchange, "/profile/edit?message=" + WebUtil.encode("Your research profile has been updated."));
    }

    private void showAbout(HttpExchange exchange) throws IOException {
        UserAccount user = currentUser(exchange);
        sendHtml(exchange, 200, HtmlPages.about(user, currentProfile(user)));
    }

    private void showResearchers(HttpExchange exchange) throws IOException {
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        String q = params.getOrDefault("q", "");
        String role = params.getOrDefault("role", "");
        UserAccount user = currentUser(exchange);
        Researcher profile = currentProfile(user);
        sendHtml(exchange, 200, HtmlPages.researchers(researchService.listResearchers(q, role), q, role, params.get("message"), user, profile));
    }

    private void redirectLegacyProfileRoute(HttpExchange exchange) throws IOException {
        if (currentUser(exchange) == null) redirect(exchange, "/register");
        else redirect(exchange, "/profile/edit");
    }

    private void showTeams(HttpExchange exchange) throws IOException {
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        String q = params.getOrDefault("q", "");
        String status = params.getOrDefault("status", "");
        UserAccount user = currentUser(exchange);
        Researcher profile = currentProfile(user);
        sendHtml(exchange, 200, HtmlPages.teams(teamService.listTeams(q, status), q, status, params.get("message"), user, profile));
    }

    private void showNewTeam(HttpExchange exchange) throws IOException {
        UserAccount user = requireUser(exchange);
        if (user == null) return;
        Researcher profile = requireProfile(exchange, user);
        if (profile == null) return;
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        sendHtml(exchange, 200, HtmlPages.newTeam(user, profile, params.get("message")));
    }

    private void createTeam(HttpExchange exchange) throws IOException {
        UserAccount user = requireUser(exchange);
        if (user == null) return;
        Researcher profile = requireProfile(exchange, user);
        if (profile == null) return;
        Map<String, String> form = WebUtil.formParams(exchange);
        ResearchTeam team = teamService.createTeam(form.get("name"), form.get("researchArea"), form.get("description"), profile.getId(), WebUtil.intParam(form, "targetSize"));
        redirect(exchange, "/teams/view?id=" + team.getId() + "&message=" + WebUtil.encode("Research team created. You are the team leader."));
    }

    private void showTeam(HttpExchange exchange) throws IOException {
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        int id = WebUtil.intParam(params, "id");
        ResearchTeam team = teamService.getTeam(id);
        Researcher leader = researchService.getResearcher(team.getLeaderId());
        List<Researcher> members = team.getMemberIds().stream().map(researchService::getResearcher).toList();
        List<JoinRequest> requests = repository.findAllJoinRequests().stream().filter(r -> r.getTeamId() == id).toList();
        Map<Integer, Researcher> researcherMap = new HashMap<>();
        repository.findAllResearchers().forEach(r -> researcherMap.put(r.getId(), r));
        UserAccount user = currentUser(exchange);
        Researcher profile = currentProfile(user);
        sendHtml(exchange, 200, HtmlPages.teamDetail(team, leader, members, requests, researcherMap, user, profile, params.get("message")));
    }

    private void joinTeam(HttpExchange exchange) throws IOException {
        UserAccount user = requireUser(exchange);
        if (user == null) return;
        Researcher profile = requireProfile(exchange, user);
        if (profile == null) return;
        Map<String, String> form = WebUtil.formParams(exchange);
        int teamId = WebUtil.intParam(form, "teamId");
        teamService.requestToJoin(teamId, profile.getId(), form.get("message"));
        redirect(exchange, "/teams/view?id=" + teamId + "&message=" + WebUtil.encode("Your join request was submitted to the team leader."));
    }

    private void showRequests(HttpExchange exchange) throws IOException {
        UserAccount user = requireUser(exchange);
        if (user == null) return;
        Researcher profile = currentProfile(user);
        List<JoinRequest> all = teamService.listRequests();
        List<JoinRequest> outgoing = profile == null ? List.of() : all.stream().filter(r -> r.getResearcherId() == profile.getId()).toList();
        List<JoinRequest> incoming;
        if (user.isAdmin()) {
            incoming = all;
        } else if (profile != null) {
            List<Integer> ledTeamIds = repository.findAllTeams().stream().filter(t -> t.getLeaderId() == profile.getId()).map(ResearchTeam::getId).toList();
            incoming = all.stream().filter(r -> ledTeamIds.contains(r.getTeamId())).toList();
        } else incoming = List.of();

        Map<Integer, ResearchTeam> teams = new HashMap<>();
        repository.findAllTeams().forEach(t -> teams.put(t.getId(), t));
        Map<Integer, Researcher> researchers = new HashMap<>();
        repository.findAllResearchers().forEach(r -> researchers.put(r.getId(), r));
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        sendHtml(exchange, 200, HtmlPages.requests(outgoing, incoming, teams, researchers, user, profile, params.get("message")));
    }

    private void updateRequest(HttpExchange exchange, boolean approve) throws IOException {
        UserAccount user = requireUser(exchange);
        if (user == null) return;
        Researcher profile = currentProfile(user);
        if (!user.isAdmin() && profile == null) throw new SecurityException("A research profile is required to manage team requests.");
        Map<String, String> form = WebUtil.formParams(exchange);
        int requestId = WebUtil.intParam(form, "requestId");
        Integer actorResearcherId = profile == null ? null : profile.getId();
        if (approve) {
            teamService.approveRequest(requestId, actorResearcherId, user.isAdmin());
            redirect(exchange, "/requests?message=" + WebUtil.encode("Join request approved. The researcher is now a team member."));
        } else {
            teamService.rejectRequest(requestId, actorResearcherId, user.isAdmin());
            redirect(exchange, "/requests?message=" + WebUtil.encode("Join request rejected."));
        }
    }

    private void showMatches(HttpExchange exchange) throws IOException {
        UserAccount user = requireUser(exchange);
        if (user == null) return;
        Researcher profile = requireProfile(exchange, user);
        if (profile == null) return;
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        sendHtml(exchange, 200, HtmlPages.matches(profile, matchingService.findMatches(profile.getId()), user, params.get("message")));
    }

    private UserAccount currentUser(HttpExchange exchange) {
        return sessionManager.currentUserId(exchange)
                .flatMap(repository::findUserAccountById)
                .filter(UserAccount::isActive)
                .orElse(null);
    }

    private Researcher currentProfile(UserAccount user) {
        if (user == null || user.getResearcherId() == null) return null;
        return repository.findResearcherById(user.getResearcherId()).orElse(null);
    }

    private UserAccount requireUser(HttpExchange exchange) throws IOException {
        UserAccount user = currentUser(exchange);
        if (user == null) {
            redirect(exchange, "/login?message=" + WebUtil.encode("Please log in to continue."));
            return null;
        }
        return user;
    }

    private Researcher requireProfile(HttpExchange exchange, UserAccount user) throws IOException {
        Researcher profile = currentProfile(user);
        if (profile == null) {
            redirect(exchange, "/dashboard?message=" + WebUtil.encode("This account does not have a research profile for that action."));
            return null;
        }
        return profile;
    }

    private String statsJson() {
        long pending = repository.findAllJoinRequests().stream().filter(r -> r.getStatus() == JoinRequest.Status.PENDING).count();
        int openSlots = repository.findAllTeams().stream().mapToInt(ResearchTeam::openSlots).sum();
        return "{\"researchers\":" + repository.findAllResearchers().size() +
                ",\"teams\":" + repository.findAllTeams().size() +
                ",\"pendingRequests\":" + pending +
                ",\"openSlots\":" + openSlots + "}";
    }

    private void serveStatic(HttpExchange exchange, String path) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method not allowed");
            return;
        }
        String relative = path.substring(1);
        Path file = publicDir.resolve(relative).normalize();
        if (!file.startsWith(publicDir) || !Files.isRegularFile(file)) {
            sendText(exchange, 404, "Not found");
            return;
        }
        String contentType = path.endsWith(".css") ? "text/css; charset=utf-8" :
                path.endsWith(".js") ? "application/javascript; charset=utf-8" : "application/octet-stream";
        byte[] bytes = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "public, max-age=300");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private void sendForbidden(HttpExchange exchange, String message) throws IOException {
        UserAccount user = currentUser(exchange);
        sendHtml(exchange, 403, HtmlPages.error("Access denied", message, 403, user, currentProfile(user)));
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
    }

    private void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("X-Frame-Options", "DENY");
        exchange.getResponseHeaders().set("Referrer-Policy", "same-origin");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private void sendText(HttpExchange exchange, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
