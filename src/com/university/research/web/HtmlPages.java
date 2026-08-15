package com.university.research.web;

import com.university.research.model.JoinRequest;
import com.university.research.model.ResearchTeam;
import com.university.research.model.Researcher;
import com.university.research.service.MatchingService.TeamMatch;
import com.university.research.util.WebUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class HtmlPages {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private HtmlPages() { }

    public static String home(List<Researcher> researchers, List<ResearchTeam> teams, List<JoinRequest> requests) {
        long faculty = researchers.stream().filter(r -> r.getRole() == Researcher.Role.FACULTY).count();
        long students = researchers.size() - faculty;
        long pending = requests.stream().filter(r -> r.getStatus() == JoinRequest.Status.PENDING).count();
        int openSlots = teams.stream().mapToInt(ResearchTeam::openSlots).sum();

        StringBuilder body = new StringBuilder();
        body.append("""
                <section class="hero">
                  <div class="hero-copy">
                    <span class="eyebrow">JAVA + OBJECT-ORIENTED PROGRAMMING</span>
                    <h1>Form stronger university research teams.</h1>
                    <p>Discover researchers, match shared interests, create teams and manage join requests from one simple research collaboration platform.</p>
                    <div class="hero-actions">
                      <a class="btn primary" href="/teams">Explore research teams</a>
                      <a class="btn secondary" href="/researchers/new">Create researcher profile</a>
                    </div>
                    <div class="hero-points">
                      <span>✓ Interest-based matching</span>
                      <span>✓ Team formation workflow</span>
                      <span>✓ Java OOP architecture</span>
                    </div>
                  </div>
                  <div class="hero-panel">
                    <div class="hero-panel-top">
                      <span class="live-dot"></span>
                      <strong>Research ecosystem overview</strong>
                    </div>
                    <div class="mini-grid">
                """);
        body.append(stat("Researchers", String.valueOf(researchers.size()), "Available profiles"));
        body.append(stat("Research teams", String.valueOf(teams.size()), "Active/forming teams"));
        body.append(stat("Open positions", String.valueOf(openSlots), "Slots across teams"));
        body.append(stat("Pending requests", String.valueOf(pending), "Awaiting decisions"));
        body.append("</div>");
        body.append("<div class=\"role-line\"><span>Students</span><strong>").append(students).append("</strong></div>");
        body.append("<div class=\"role-line\"><span>Faculty</span><strong>").append(faculty).append("</strong></div>");
        body.append("</div></section>");

        body.append(sectionHeading("Featured research teams", "Teams currently looking for collaborators.", "/teams", "View all teams"));
        body.append("<div class=\"card-grid\">");
        teams.stream().limit(3).forEach(t -> body.append(teamCard(t)));
        body.append("</div>");

        body.append(sectionHeading("Researchers ready to collaborate", "Browse skills and research interests across departments.", "/researchers", "Browse researchers"));
        body.append("<div class=\"card-grid\">");
        researchers.stream().limit(3).forEach(r -> body.append(researcherCard(r)));
        body.append("</div>");

        body.append("""
                <section class="process-section">
                  <div class="section-heading"><div><span class="eyebrow">WORKFLOW</span><h2>How team formation works</h2></div></div>
                  <div class="process-grid">
                    <div class="process-card"><span>01</span><h3>Create a profile</h3><p>Add department, research interests and technical skills.</p></div>
                    <div class="process-card"><span>02</span><h3>Find a match</h3><p>Search teams or use the built-in compatibility score.</p></div>
                    <div class="process-card"><span>03</span><h3>Request to join</h3><p>Send a short research contribution message to a team.</p></div>
                    <div class="process-card"><span>04</span><h3>Build the team</h3><p>Approve suitable members until the target team size is reached.</p></div>
                  </div>
                </section>
                """);

        return layout("Home", body.toString(), null);
    }

    public static String researchers(List<Researcher> researchers, String q, String role, String message) {
        StringBuilder body = new StringBuilder();
        body.append(pageHeader("Researcher directory", "Find students and faculty by name, department, research interest or skill.",
                "/researchers/new", "Add researcher"));
        body.append("<form class=\"filter-bar\" method=\"get\" action=\"/researchers\"><div class=\"field grow\"><label>Search</label><input name=\"q\" placeholder=\"AI, cybersecurity, Java, climate...\" value=\"")
                .append(WebUtil.escape(q)).append("\"></div>")
                .append("<div class=\"field\"><label>Role</label><select name=\"role\">")
                .append(option("", "All roles", role))
                .append(option("STUDENT", "Student", role))
                .append(option("FACULTY", "Faculty", role))
                .append("</select></div><button class=\"btn primary compact\" type=\"submit\">Search</button></form>");

        body.append("<div class=\"results-meta\"><strong>").append(researchers.size()).append(" researcher(s)</strong><span>Matching your filters</span></div>");
        if (researchers.isEmpty()) {
            body.append(emptyState("No researchers found", "Try another search term or add a new researcher profile.", "/researchers/new", "Add researcher"));
        } else {
            body.append("<div class=\"card-grid\">");
            researchers.forEach(r -> body.append(researcherCard(r)));
            body.append("</div>");
        }
        return layout("Researchers", body.toString(), message);
    }

    public static String newResearcher(String message) {
        String body = pageHeader("Create researcher profile", "Add a profile that can be searched and matched with research teams.", "/researchers", "Back to researchers") + """
                <section class="form-shell">
                  <form class="form-card" method="post" action="/researchers">
                    <div class="form-grid two">
                      <div class="field"><label>Full name *</label><input name="name" required maxlength="80" placeholder="e.g. Student Name"></div>
                      <div class="field"><label>Email *</label><input name="email" type="email" required maxlength="120" placeholder="name@example.edu"></div>
                      <div class="field"><label>Department *</label><input name="department" required maxlength="100" placeholder="Computer Science"></div>
                      <div class="field"><label>Role *</label><select name="role"><option value="STUDENT">Student</option><option value="FACULTY">Faculty</option></select></div>
                      <div class="field full"><label>Research interests</label><input name="interests" maxlength="250" placeholder="Artificial Intelligence, Healthcare, Data Mining"></div>
                      <div class="field full"><label>Skills</label><input name="skills" maxlength="250" placeholder="Java, Python, Statistics, GIS"></div>
                      <div class="field full"><label>Short bio</label><textarea name="bio" rows="5" maxlength="600" placeholder="Briefly describe research goals and experience."></textarea></div>
                    </div>
                    <div class="form-actions"><a class="btn secondary" href="/researchers">Cancel</a><button class="btn primary" type="submit">Create profile</button></div>
                  </form>
                  <aside class="form-help"><span class="eyebrow">GOOD PROFILE</span><h3>Use specific interests</h3><p>Matching works better when interests contain meaningful research terms such as “Machine Learning”, “Renewable Energy” or “Cybersecurity”.</p><div class="tip">The system demonstrates encapsulation, inheritance-ready domain models, interfaces, service classes and polymorphic repository design.</div></aside>
                </section>
                """;
        return layout("New Researcher", body, message);
    }

    public static String teams(List<ResearchTeam> teams, String q, String status, String message) {
        StringBuilder body = new StringBuilder();
        body.append(pageHeader("Research teams", "Explore research topics, team capacity and collaboration opportunities.", "/teams/new", "Create team"));
        body.append("<form class=\"filter-bar\" method=\"get\" action=\"/teams\"><div class=\"field grow\"><label>Search</label><input name=\"q\" placeholder=\"AI, sustainability, security...\" value=\"")
                .append(WebUtil.escape(q)).append("\"></div>")
                .append("<div class=\"field\"><label>Status</label><select name=\"status\">")
                .append(option("", "All statuses", status))
                .append(option("FORMING", "Forming", status))
                .append(option("ACTIVE", "Active", status))
                .append(option("COMPLETED", "Completed", status))
                .append("</select></div><button class=\"btn primary compact\" type=\"submit\">Search</button></form>");
        body.append("<div class=\"results-meta\"><strong>").append(teams.size()).append(" team(s)</strong><span>Available in the system</span></div>");
        if (teams.isEmpty()) {
            body.append(emptyState("No teams found", "Try a different filter or create a new research team.", "/teams/new", "Create team"));
        } else {
            body.append("<div class=\"card-grid\">");
            teams.forEach(t -> body.append(teamCard(t)));
            body.append("</div>");
        }
        return layout("Teams", body.toString(), message);
    }

    public static String newTeam(List<Researcher> researchers, String message) {
        StringBuilder options = new StringBuilder();
        researchers.stream().filter(Researcher::isAvailable).forEach(r -> options.append("<option value=\"")
                .append(r.getId()).append("\">").append(WebUtil.escape(r.getName()))
                .append(" — ").append(WebUtil.escape(r.getDepartment())).append("</option>"));

        String body = pageHeader("Create research team", "Define a research problem and invite suitable collaborators.", "/teams", "Back to teams") + """
                <section class="form-shell">
                  <form class="form-card" method="post" action="/teams">
                    <div class="form-grid two">
                      <div class="field full"><label>Team name *</label><input name="name" required maxlength="100" placeholder="e.g. AI for Smart Healthcare"></div>
                      <div class="field"><label>Research area *</label><input name="researchArea" required maxlength="100" placeholder="Artificial Intelligence"></div>
                      <div class="field"><label>Target team size *</label><input name="targetSize" type="number" min="2" max="12" value="5" required></div>
                      <div class="field full"><label>Team leader *</label><select name="leaderId" required>
                """ + options + """
                      </select></div>
                      <div class="field full"><label>Research problem / description *</label><textarea name="description" rows="6" required maxlength="800" placeholder="Describe the problem, expected contribution and type of members needed."></textarea></div>
                    </div>
                    <div class="form-actions"><a class="btn secondary" href="/teams">Cancel</a><button class="btn primary" type="submit">Create team</button></div>
                  </form>
                  <aside class="form-help"><span class="eyebrow">TEAM DESIGN</span><h3>Make the research scope clear</h3><p>A focused team description helps members understand the problem before they request to join.</p><div class="tip">The leader is automatically added as the first team member. The team becomes ACTIVE automatically when the target team size is reached.</div></aside>
                </section>
                """;
        return layout("New Team", body, message);
    }

    public static String teamDetail(ResearchTeam team, Researcher leader, List<Researcher> members,
                                    List<Researcher> researchers, List<JoinRequest> teamRequests, String message) {
        StringBuilder body = new StringBuilder();
        body.append("<div class=\"breadcrumb\"><a href=\"/teams\">Research teams</a><span>/</span><span>")
                .append(WebUtil.escape(team.getName())).append("</span></div>");
        body.append("<section class=\"team-hero\"><div><div class=\"team-topline\"><span class=\"badge ")
                .append(statusClass(team.getStatus().name())).append("\">").append(team.getStatus()).append("</span><span>")
                .append(WebUtil.escape(team.getResearchArea())).append("</span></div><h1>")
                .append(WebUtil.escape(team.getName())).append("</h1><p>").append(WebUtil.escape(team.getDescription()))
                .append("</p><div class=\"team-meta\"><span>Leader: <strong>").append(WebUtil.escape(leader.getName()))
                .append("</strong></span><span>Created: <strong>").append(team.getCreatedAt().format(DATE))
                .append("</strong></span></div></div><div class=\"capacity-card\"><span>Team capacity</span><strong>")
                .append(members.size()).append(" / ").append(team.getTargetSize()).append("</strong><div class=\"progress\"><i style=\"width:")
                .append(Math.min(100, (int) Math.round(100.0 * members.size() / team.getTargetSize()))).append("%\"></i></div><small>")
                .append(team.openSlots()).append(" open slot(s)</small></div></section>");

        body.append("<div class=\"detail-grid\"><section class=\"panel\"><div class=\"panel-heading\"><h2>Current members</h2><span>")
                .append(members.size()).append(" member(s)</span></div><div class=\"member-list\">");
        for (Researcher member : members) {
            body.append("<div class=\"member-row\"><div class=\"avatar\">").append(initials(member.getName())).append("</div><div class=\"member-main\"><strong>")
                    .append(WebUtil.escape(member.getName())).append("</strong><span>").append(WebUtil.escape(member.getDepartment())).append(" · ")
                    .append(member.getRole()).append("</span></div>");
            if (member.getId() == team.getLeaderId()) body.append("<span class=\"badge leader\">Leader</span>");
            body.append("</div>");
        }
        body.append("</div></section>");

        body.append("<aside class=\"panel join-panel\"><div class=\"panel-heading\"><h2>Request to join</h2></div>");
        if (team.openSlots() <= 0) {
            body.append("<div class=\"notice\">This team is currently full.</div>");
        } else {
            body.append("<form method=\"post\" action=\"/teams/join\"><input type=\"hidden\" name=\"teamId\" value=\"")
                    .append(team.getId()).append("\"><div class=\"field\"><label>Select researcher</label><select name=\"researcherId\" required>");
            researchers.stream().filter(r -> !team.getMemberIds().contains(r.getId())).forEach(r -> body.append("<option value=\"")
                    .append(r.getId()).append("\">").append(WebUtil.escape(r.getName())).append(" — ").append(WebUtil.escape(r.getDepartment())).append("</option>"));
            body.append("</select></div><div class=\"field\"><label>Contribution message</label><textarea name=\"message\" rows=\"4\" maxlength=\"500\" placeholder=\"Explain how you can contribute to this research.\"></textarea></div><button class=\"btn primary full-btn\" type=\"submit\">Send join request</button></form>");
        }
        body.append("</aside></div>");

        body.append("<section class=\"panel request-panel\"><div class=\"panel-heading\"><h2>Team join requests</h2><a href=\"/requests\">Open request center</a></div>");
        List<JoinRequest> pending = teamRequests.stream().filter(r -> r.getStatus() == JoinRequest.Status.PENDING).toList();
        if (pending.isEmpty()) {
            body.append("<p class=\"muted\">No pending request for this team.</p>");
        } else {
            for (JoinRequest request : pending) {
                Researcher requester = researchers.stream().filter(r -> r.getId() == request.getResearcherId()).findFirst().orElse(null);
                if (requester != null) {
                    body.append(requestRow(request, team, requester, true));
                }
            }
        }
        body.append("</section>");

        return layout(team.getName(), body.toString(), message);
    }

    public static String requests(List<JoinRequest> requests, Map<Integer, ResearchTeam> teams,
                                  Map<Integer, Researcher> researchers, String message) {
        StringBuilder body = new StringBuilder();
        body.append(pageHeader("Join request center", "Review collaboration requests and approve members into teams.", "/teams", "View teams"));
        if (requests.isEmpty()) {
            body.append(emptyState("No join requests yet", "Requests will appear here after a researcher asks to join a team.", "/teams", "Explore teams"));
        } else {
            body.append("<section class=\"panel request-panel\"><div class=\"request-stack\">");
            for (JoinRequest request : requests) {
                ResearchTeam team = teams.get(request.getTeamId());
                Researcher researcher = researchers.get(request.getResearcherId());
                if (team != null && researcher != null) body.append(requestRow(request, team, researcher, request.getStatus() == JoinRequest.Status.PENDING));
            }
            body.append("</div></section>");
        }
        return layout("Join Requests", body.toString(), message);
    }

    public static String matches(Researcher researcher, List<TeamMatch> matches, String message) {
        StringBuilder body = new StringBuilder();
        body.append("<div class=\"breadcrumb\"><a href=\"/researchers\">Researchers</a><span>/</span><span>Team matches</span></div>");
        body.append("<section class=\"match-hero\"><div class=\"avatar large\">").append(initials(researcher.getName())).append("</div><div><span class=\"eyebrow\">TEAM MATCHING</span><h1>Best teams for ")
                .append(WebUtil.escape(researcher.getName())).append("</h1><p>Scores are calculated from shared research terms, skills, availability and open team capacity.</p></div></section>");
        if (matches.isEmpty()) {
            body.append(emptyState("No open matches", "This researcher is already in the available teams or the remaining teams are full.", "/teams", "View all teams"));
        } else {
            body.append("<div class=\"match-list\">");
            for (TeamMatch match : matches) {
                ResearchTeam team = match.team();
                body.append("<article class=\"match-card\"><div class=\"score-ring\"><strong>").append(match.score()).append("%</strong><span>match</span></div><div class=\"match-main\"><div class=\"team-topline\"><span class=\"badge ")
                        .append(statusClass(team.getStatus().name())).append("\">").append(team.getStatus()).append("</span><span>")
                        .append(WebUtil.escape(team.getResearchArea())).append("</span></div><h3>").append(WebUtil.escape(team.getName())).append("</h3><p>")
                        .append(WebUtil.escape(match.reason())).append("</p><span class=\"muted\">").append(team.openSlots()).append(" open slot(s)</span></div><a class=\"btn secondary compact\" href=\"/teams/view?id=")
                        .append(team.getId()).append("\">View team</a></article>");
            }
            body.append("</div>");
        }
        return layout("Team Matches", body.toString(), message);
    }

    public static String about() {
        String body = pageHeader("About the system", "A Java OOP mini-project for structured university research collaboration.", "/", "Back home") + """
                <section class="about-grid">
                  <article class="panel"><span class="eyebrow">PROBLEM</span><h2>Why this system exists</h2><p>Research team formation is often informal. Students may not know who shares their research interests, while faculty members need a structured way to identify suitable collaborators. This system centralizes profiles, teams, matching and join requests.</p></article>
                  <article class="panel"><span class="eyebrow">OOP DESIGN</span><h2>Object-oriented architecture</h2><p>The application separates domain objects, repository interfaces, service-layer business rules and the web layer. Encapsulation protects state, interfaces reduce coupling and service classes keep business logic outside the HTTP handlers.</p></article>
                  <article class="panel"><span class="eyebrow">FREE DEPLOYMENT</span><h2>Built for zero-cost demonstration</h2><p>The project has no external Java libraries. It compiles with JDK 21 and can be deployed from a GitHub repository using the included Dockerfile on a free web-service host.</p></article>
                  <article class="panel"><span class="eyebrow">PROJECT SCOPE</span><h2>Main functions</h2><p>Researcher profile creation, searchable researcher directory, research team creation, team capacity tracking, join request approval/rejection and interest-based team matching.</p></article>
                </section>
                """;
        return layout("About", body, null);
    }

    public static String error(String title, String message, int statusCode) {
        String body = "<section class=\"error-page\"><span>" + statusCode + "</span><h1>" + WebUtil.escape(title) + "</h1><p>" + WebUtil.escape(message) + "</p><a class=\"btn primary\" href=\"/\">Return home</a></section>";
        return layout(title, body, null);
    }

    private static String researcherCard(Researcher r) {
        return "<article class=\"person-card\"><div class=\"person-head\"><div class=\"avatar\">" + initials(r.getName()) + "</div><div><div class=\"badge-row\"><span class=\"badge role\">" + r.getRole() + "</span>" + (r.isAvailable() ? "<span class=\"availability\">Available</span>" : "") + "</div><h3>" + WebUtil.escape(r.getName()) + "</h3><p>" + WebUtil.escape(r.getDepartment()) + "</p></div></div>" +
                "<p class=\"card-bio\">" + WebUtil.escape(r.getBio()) + "</p>" +
                "<div class=\"tag-wrap\">" + tags(r.getResearchInterests(), 3) + "</div>" +
                "<div class=\"card-footer\"><span>Skills: " + WebUtil.escape(String.join(", ", r.getSkills().stream().limit(3).toList())) + "</span><a href=\"/matches?researcherId=" + r.getId() + "\">Find matches →</a></div></article>";
    }

    private static String teamCard(ResearchTeam t) {
        int filled = t.getMemberIds().size();
        int pct = Math.min(100, (int) Math.round(100.0 * filled / t.getTargetSize()));
        return "<article class=\"team-card\"><div class=\"team-topline\"><span class=\"badge " + statusClass(t.getStatus().name()) + "\">" + t.getStatus() + "</span><span>" + WebUtil.escape(t.getResearchArea()) + "</span></div>" +
                "<h3>" + WebUtil.escape(t.getName()) + "</h3><p>" + WebUtil.escape(t.getDescription()) + "</p>" +
                "<div class=\"capacity-line\"><span>Team members</span><strong>" + filled + " / " + t.getTargetSize() + "</strong></div>" +
                "<div class=\"progress\"><i style=\"width:" + pct + "%\"></i></div>" +
                "<div class=\"card-footer\"><span>" + t.openSlots() + " open slot(s)</span><a href=\"/teams/view?id=" + t.getId() + "\">View team →</a></div></article>";
    }

    private static String requestRow(JoinRequest request, ResearchTeam team, Researcher researcher, boolean actions) {
        StringBuilder row = new StringBuilder();
        row.append("<article class=\"request-row\"><div class=\"avatar\">").append(initials(researcher.getName())).append("</div><div class=\"request-main\"><div class=\"request-title\"><strong>")
                .append(WebUtil.escape(researcher.getName())).append("</strong><span class=\"badge ").append(statusClass(request.getStatus().name())).append("\">")
                .append(request.getStatus()).append("</span></div><p>Wants to join <a href=\"/teams/view?id=").append(team.getId()).append("\">")
                .append(WebUtil.escape(team.getName())).append("</a></p><blockquote>").append(WebUtil.escape(request.getMessage().isBlank() ? "No message provided." : request.getMessage()))
                .append("</blockquote><small>").append(request.getCreatedAt().format(DATETIME)).append("</small></div>");
        if (actions) {
            row.append("<div class=\"request-actions\"><form method=\"post\" action=\"/requests/approve\"><input type=\"hidden\" name=\"requestId\" value=\"")
                    .append(request.getId()).append("\"><button class=\"btn primary compact\" type=\"submit\">Approve</button></form><form method=\"post\" action=\"/requests/reject\"><input type=\"hidden\" name=\"requestId\" value=\"")
                    .append(request.getId()).append("\"><button class=\"btn danger compact\" type=\"submit\">Reject</button></form></div>");
        }
        row.append("</article>");
        return row.toString();
    }

    private static String layout(String title, String body, String message) {
        String flash = (message == null || message.isBlank()) ? "" : "<div class=\"flash\"><span>" + WebUtil.escape(message) + "</span><button type=\"button\" onclick=\"this.parentElement.remove()\">×</button></div>";
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <meta name="description" content="University Research Formation Team System built with Java and object-oriented programming.">
                  <title>""" + WebUtil.escape(title) + " | University Research Team System</title>" + """
                  <link rel="stylesheet" href="/assets/style.css">
                </head>
                <body>
                  <header class="site-header">
                    <div class="container nav-wrap">
                      <a class="brand" href="/"><span class="brand-mark">UR</span><span><strong>UniResearch</strong><small>Team Formation System</small></span></a>
                      <button class="nav-toggle" aria-label="Toggle navigation" onclick="document.querySelector('.nav-links').classList.toggle('open')">☰</button>
                      <nav class="nav-links">
                        <a href="/">Home</a>
                        <a href="/researchers">Researchers</a>
                        <a href="/teams">Teams</a>
                        <a href="/requests">Requests</a>
                        <a href="/about">About</a>
                      </nav>
                    </div>
                  </header>
                  <main class="container main-content">
                """ + flash + body + """
                  </main>
                  <footer class="site-footer">
                    <div class="container footer-grid"><div><div class="brand footer-brand"><span class="brand-mark">UR</span><span><strong>UniResearch</strong><small>Java OOP Project</small></span></div><p>University research collaboration made structured, searchable and transparent.</p></div><div><strong>Project modules</strong><a href="/researchers">Researcher Directory</a><a href="/teams">Team Formation</a><a href="/requests">Request Management</a></div><div><strong>Technology</strong><span>Java 21</span><span>Core Java HttpServer</span><span>Object-Oriented Architecture</span></div></div>
                    <div class="container footer-bottom"><span>University / student information intentionally left generic.</span><span>Academic mini-project demonstration</span></div>
                  </footer>
                  <script src="/assets/app.js"></script>
                </body>
                </html>
                """;
    }

    private static String pageHeader(String title, String subtitle, String actionUrl, String actionText) {
        return "<section class=\"page-header\"><div><span class=\"eyebrow\">UNIVERSITY RESEARCH COLLABORATION</span><h1>" + WebUtil.escape(title) + "</h1><p>" + WebUtil.escape(subtitle) + "</p></div><a class=\"btn primary\" href=\"" + actionUrl + "\">" + WebUtil.escape(actionText) + "</a></section>";
    }

    private static String sectionHeading(String title, String subtitle, String url, String action) {
        return "<div class=\"section-heading\"><div><h2>" + WebUtil.escape(title) + "</h2><p>" + WebUtil.escape(subtitle) + "</p></div><a href=\"" + url + "\">" + WebUtil.escape(action) + " →</a></div>";
    }

    private static String stat(String label, String value, String hint) {
        return "<div class=\"mini-stat\"><span>" + WebUtil.escape(label) + "</span><strong>" + WebUtil.escape(value) + "</strong><small>" + WebUtil.escape(hint) + "</small></div>";
    }

    private static String tags(List<String> values, int limit) {
        StringBuilder result = new StringBuilder();
        values.stream().limit(limit).forEach(v -> result.append("<span class=\"tag\">").append(WebUtil.escape(v)).append("</span>"));
        return result.toString();
    }

    private static String option(String value, String label, String selected) {
        return "<option value=\"" + WebUtil.escape(value) + "\"" + (value.equalsIgnoreCase(selected == null ? "" : selected) ? " selected" : "") + ">" + WebUtil.escape(label) + "</option>";
    }

    private static String emptyState(String title, String text, String url, String action) {
        return "<section class=\"empty-state\"><div>◎</div><h3>" + WebUtil.escape(title) + "</h3><p>" + WebUtil.escape(text) + "</p><a class=\"btn primary\" href=\"" + url + "\">" + WebUtil.escape(action) + "</a></section>";
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) return "UR";
        String[] words = name.trim().split("\\s+");
        String first = words[0].substring(0, 1);
        String second = words.length > 1 ? words[words.length - 1].substring(0, 1) : "";
        return WebUtil.escape((first + second).toUpperCase());
    }

    private static String statusClass(String status) {
        return switch (status) {
            case "FORMING", "PENDING" -> "forming";
            case "ACTIVE", "APPROVED" -> "active";
            case "COMPLETED" -> "completed";
            case "REJECTED" -> "rejected";
            default -> "role";
        };
    }
}
