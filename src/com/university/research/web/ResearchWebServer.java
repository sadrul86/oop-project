package com.university.research.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.university.research.model.JoinRequest;
import com.university.research.model.ResearchTeam;
import com.university.research.model.Researcher;
import com.university.research.repository.ResearchRepository;
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
    private final ResearchService researchService;
    private final TeamService teamService;
    private final MatchingService matchingService;
    private final Path publicDir;

    public ResearchWebServer(int port, ResearchRepository repository, Path publicDir) throws IOException {
        this.repository = repository;
        this.researchService = new ResearchService(repository);
        this.teamService = new TeamService(repository);
        this.matchingService = new MatchingService(repository);
        this.publicDir = publicDir.toAbsolutePath().normalize();
        this.server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        this.server.createContext("/", this::handle);
        this.server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }

    public void start() {
        server.start();
    }

    private void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try {
            if (path.startsWith("/assets/")) {
                serveStatic(exchange, path);
                return;
            }

            if ("GET".equals(method) && "/".equals(path)) {
                sendHtml(exchange, 200, HtmlPages.home(repository.findAllResearchers(), repository.findAllTeams(), repository.findAllJoinRequests()));
            } else if ("GET".equals(method) && "/health".equals(path)) {
                sendText(exchange, 200, "OK");
            } else if ("GET".equals(method) && "/api/stats".equals(path)) {
                sendJson(exchange, 200, statsJson());
            } else if ("GET".equals(method) && "/about".equals(path)) {
                sendHtml(exchange, 200, HtmlPages.about());
            } else if ("GET".equals(method) && "/researchers".equals(path)) {
                showResearchers(exchange);
            } else if ("GET".equals(method) && "/researchers/new".equals(path)) {
                Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
                sendHtml(exchange, 200, HtmlPages.newResearcher(params.get("message")));
            } else if ("POST".equals(method) && "/researchers".equals(path)) {
                createResearcher(exchange);
            } else if ("GET".equals(method) && "/teams".equals(path)) {
                showTeams(exchange);
            } else if ("GET".equals(method) && "/teams/new".equals(path)) {
                Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
                sendHtml(exchange, 200, HtmlPages.newTeam(repository.findAllResearchers(), params.get("message")));
            } else if ("POST".equals(method) && "/teams".equals(path)) {
                createTeam(exchange);
            } else if ("GET".equals(method) && "/teams/view".equals(path)) {
                showTeam(exchange);
            } else if ("POST".equals(method) && "/teams/join".equals(path)) {
                joinTeam(exchange);
            } else if ("GET".equals(method) && "/requests".equals(path)) {
                showRequests(exchange);
            } else if ("POST".equals(method) && "/requests/approve".equals(path)) {
                updateRequest(exchange, true);
            } else if ("POST".equals(method) && "/requests/reject".equals(path)) {
                updateRequest(exchange, false);
            } else if ("GET".equals(method) && "/matches".equals(path)) {
                showMatches(exchange);
            } else {
                sendHtml(exchange, 404, HtmlPages.error("Page not found", "The requested page does not exist.", 404));
            }
        } catch (IllegalArgumentException e) {
            sendHtml(exchange, 400, HtmlPages.error("Request could not be completed", e.getMessage(), 400));
        } catch (Exception e) {
            e.printStackTrace();
            sendHtml(exchange, 500, HtmlPages.error("Server error", "An unexpected error occurred. Check the application log for details.", 500));
        } finally {
            exchange.close();
        }
    }

    private void showResearchers(HttpExchange exchange) throws IOException {
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        String q = params.getOrDefault("q", "");
        String role = params.getOrDefault("role", "");
        sendHtml(exchange, 200, HtmlPages.researchers(researchService.listResearchers(q, role), q, role, params.get("message")));
    }

    private void createResearcher(HttpExchange exchange) throws IOException {
        Map<String, String> form = WebUtil.formParams(exchange);
        Researcher created = researchService.createResearcher(
                form.get("name"), form.get("email"), form.get("department"), form.get("role"),
                form.get("interests"), form.get("skills"), form.get("bio"));
        redirect(exchange, "/researchers?message=" + WebUtil.encode("Profile created for " + created.getName() + "."));
    }

    private void showTeams(HttpExchange exchange) throws IOException {
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        String q = params.getOrDefault("q", "");
        String status = params.getOrDefault("status", "");
        sendHtml(exchange, 200, HtmlPages.teams(teamService.listTeams(q, status), q, status, params.get("message")));
    }

    private void createTeam(HttpExchange exchange) throws IOException {
        Map<String, String> form = WebUtil.formParams(exchange);
        ResearchTeam team = teamService.createTeam(
                form.get("name"), form.get("researchArea"), form.get("description"),
                WebUtil.intParam(form, "leaderId"), WebUtil.intParam(form, "targetSize"));
        redirect(exchange, "/teams/view?id=" + team.getId() + "&message=" + WebUtil.encode("Research team created successfully."));
    }

    private void showTeam(HttpExchange exchange) throws IOException {
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        int id = WebUtil.intParam(params, "id");
        ResearchTeam team = teamService.getTeam(id);
        Researcher leader = researchService.getResearcher(team.getLeaderId());
        List<Researcher> allResearchers = repository.findAllResearchers();
        List<Researcher> members = team.getMemberIds().stream().map(researchService::getResearcher).toList();
        List<JoinRequest> requests = repository.findAllJoinRequests().stream().filter(r -> r.getTeamId() == id).toList();
        sendHtml(exchange, 200, HtmlPages.teamDetail(team, leader, members, allResearchers, requests, params.get("message")));
    }

    private void joinTeam(HttpExchange exchange) throws IOException {
        Map<String, String> form = WebUtil.formParams(exchange);
        int teamId = WebUtil.intParam(form, "teamId");
        teamService.requestToJoin(teamId, WebUtil.intParam(form, "researcherId"), form.get("message"));
        redirect(exchange, "/teams/view?id=" + teamId + "&message=" + WebUtil.encode("Join request submitted."));
    }

    private void showRequests(HttpExchange exchange) throws IOException {
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        Map<Integer, ResearchTeam> teams = new HashMap<>();
        repository.findAllTeams().forEach(t -> teams.put(t.getId(), t));
        Map<Integer, Researcher> researchers = new HashMap<>();
        repository.findAllResearchers().forEach(r -> researchers.put(r.getId(), r));
        sendHtml(exchange, 200, HtmlPages.requests(teamService.listRequests(), teams, researchers, params.get("message")));
    }

    private void updateRequest(HttpExchange exchange, boolean approve) throws IOException {
        Map<String, String> form = WebUtil.formParams(exchange);
        int requestId = WebUtil.intParam(form, "requestId");
        if (approve) {
            teamService.approveRequest(requestId);
            redirect(exchange, "/requests?message=" + WebUtil.encode("Join request approved and member added to the team."));
        } else {
            teamService.rejectRequest(requestId);
            redirect(exchange, "/requests?message=" + WebUtil.encode("Join request rejected."));
        }
    }

    private void showMatches(HttpExchange exchange) throws IOException {
        Map<String, String> params = WebUtil.queryParams(exchange.getRequestURI().getRawQuery());
        int researcherId = WebUtil.intParam(params, "researcherId");
        Researcher researcher = researchService.getResearcher(researcherId);
        sendHtml(exchange, 200, HtmlPages.matches(researcher, matchingService.findMatches(researcherId), params.get("message")));
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

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
    }

    private void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("X-Frame-Options", "DENY");
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
