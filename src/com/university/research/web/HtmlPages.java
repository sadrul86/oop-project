package com.university.research.web;

import com.university.research.model.JoinRequest;
import com.university.research.model.ResearchTeam;
import com.university.research.model.Researcher;
import com.university.research.model.UserAccount;
import com.university.research.service.MatchingService.TeamMatch;
import com.university.research.util.WebUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class HtmlPages {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private HtmlPages() { }

    public static String home(List<Researcher> researchers, List<ResearchTeam> teams, List<JoinRequest> requests,
                              UserAccount user, Researcher profile) {
        long faculty = researchers.stream().filter(r -> r.getRole() == Researcher.Role.FACULTY).count();
        long pending = requests.stream().filter(r -> r.getStatus() == JoinRequest.Status.PENDING).count();
        int openSlots = teams.stream().mapToInt(ResearchTeam::openSlots).sum();
        String primaryUrl = user == null ? "/login" : "/dashboard";
        String primaryText = user == null ? "Log in to collaborate" : "Open dashboard";
        String secondaryUrl = user == null ? "/register" : "/teams";
        String secondaryText = user == null ? "Create account" : "Explore teams";

        StringBuilder body = new StringBuilder();
        body.append("""
                <section class="hero">
                  <div class="hero-copy">
                    <span class="eyebrow">JAVA + OBJECT-ORIENTED PROGRAMMING</span>
                    <h1>Form realistic university research teams.</h1>
                    <p>Authenticated students and faculty manage their own profiles, discover teams and send join requests. Only the correct team leader can approve or reject a request.</p>
                    <div class="hero-actions">
                """).append("<a class=\"btn primary\" href=\"").append(primaryUrl).append("\">").append(primaryText).append("</a>")
                .append("<a class=\"btn secondary\" href=\"").append(secondaryUrl).append("\">").append(secondaryText).append("</a></div>")
                .append("<div class=\"hero-points\"><span>✓ Secure login sessions</span><span>✓ Role-based authorization</span><span>✓ OOP service architecture</span></div></div>")
                .append("<div class=\"hero-panel\"><div class=\"hero-panel-top\"><span class=\"live-dot\"></span><strong>Research ecosystem overview</strong></div><div class=\"mini-grid\">")
                .append(stat("Researchers", String.valueOf(researchers.size()), "Student and faculty profiles"))
                .append(stat("Research teams", String.valueOf(teams.size()), "Forming and active teams"))
                .append(stat("Open positions", String.valueOf(openSlots), "Available member slots"))
                .append(stat("Pending requests", String.valueOf(pending), "Awaiting team leaders"))
                .append("</div><div class=\"role-line\"><span>Students</span><strong>").append(researchers.size() - faculty).append("</strong></div>")
                .append("<div class=\"role-line\"><span>Faculty</span><strong>").append(faculty).append("</strong></div></div></section>");

        body.append(sectionHeading("Featured research teams", "Teams currently looking for collaborators.", "/teams", "View all teams"));
        body.append("<div class=\"card-grid\">");
        teams.stream().limit(3).forEach(t -> body.append(teamCard(t)));
        body.append("</div>");

        body.append(sectionHeading("Researchers ready to collaborate", "Browse skills and research interests across departments.", "/researchers", "Browse researchers"));
        body.append("<div class=\"card-grid\">");
        researchers.stream().limit(3).forEach(r -> body.append(researcherCard(r, user != null && profile != null && r.getId() == profile.getId())));
        body.append("</div>");

        body.append("""
                <section class="process-section">
                  <div class="section-heading"><div><span class="eyebrow">REALISTIC WORKFLOW</span><h2>How team formation works</h2></div></div>
                  <div class="process-grid">
                    <div class="process-card"><span>01</span><h3>Log in</h3><p>Every action is connected to a real user account and one research profile.</p></div>
                    <div class="process-card"><span>02</span><h3>Find a team</h3><p>Browse research areas or use your profile-based compatibility matches.</p></div>
                    <div class="process-card"><span>03</span><h3>Send your request</h3><p>The logged-in user can only send a request as their own research profile.</p></div>
                    <div class="process-card"><span>04</span><h3>Leader decides</h3><p>Only the destination team's leader, or an administrator, can approve or reject it.</p></div>
                  </div>
                </section>
                """);
        return layout("Home", body.toString(), null, user, profile);
    }

    public static String login(String message) {
        String body = """
                <section class="auth-shell">
                  <div class="auth-card">
                    <span class="eyebrow">ACCOUNT ACCESS</span>
                    <h1>Log in to UniResearch</h1>
                    <p>Use your own account to manage your profile, requests and research teams.</p>
                    <form method="post" action="/login" class="form-grid">
                      <div class="field"><label>Email</label><input type="email" name="email" required autocomplete="username" placeholder="name@example.edu"></div>
                      <div class="field"><label>Password</label><input type="password" name="password" required autocomplete="current-password" placeholder="Your password"></div>
                      <button class="btn primary full-btn" type="submit">Log in</button>
                    </form>
                    <p class="auth-switch">No account? <a href="/register">Create one</a></p>
                  </div>
                  <aside class="demo-card">
                    <span class="eyebrow">DEMO ACCOUNTS</span><h2>Try different roles</h2>
                    <div class="credential"><strong>Student</strong><code>amina@example.edu</code><code>student123</code></div>
                    <div class="credential"><strong>Team Leader</strong><code>nabil@example.edu</code><code>leader123</code></div>
                    <div class="credential"><strong>Faculty Leader</strong><code>sara@example.edu</code><code>faculty123</code></div>
                    <div class="credential"><strong>Administrator</strong><code>admin@example.edu</code><code>admin123</code></div>
                    <p class="muted">Demo passwords are only for this academic project. Passwords are stored as PBKDF2 hashes, not plain text.</p>
                  </aside>
                </section>
                """;
        return layout("Login", body, message, null, null);
    }

    public static String register(String message) {
        String body = pageHeader("Create your account", "Registration creates one authenticated account linked to one research profile.", "/login", "Back to login") + """
                <section class="form-shell">
                  <form class="form-card" method="post" action="/register">
                    <div class="form-grid two">
                      <div class="field"><label>Full name *</label><input name="name" required maxlength="80" placeholder="Your full name"></div>
                      <div class="field"><label>University email *</label><input name="email" type="email" required maxlength="120" placeholder="name@example.edu"></div>
                      <div class="field"><label>Department *</label><input name="department" required maxlength="100" placeholder="Computer Science"></div>
                      <div class="field"><label>Role *</label><select name="role"><option value="STUDENT">Student</option><option value="FACULTY">Faculty</option></select></div>
                      <div class="field full"><label>Password *</label><input name="password" type="password" required minlength="6" maxlength="100" placeholder="At least 6 characters"></div>
                    </div>
                    <div class="form-actions"><a class="btn secondary" href="/login">Cancel</a><button class="btn primary" type="submit">Create account</button></div>
                  </form>
                  <aside class="form-help"><span class="eyebrow">REALISTIC IDENTITY</span><h3>One account, one profile</h3><p>You cannot create arbitrary researcher identities after logging in. Your profile is tied to the account created here.</p><div class="tip">A team created by you automatically records your research profile as the team leader.</div></aside>
                </section>
                """;
        return layout("Register", body, message, null, null);
    }

    public static String dashboard(UserAccount user, Researcher profile, List<ResearchTeam> ownedTeams,
                                   List<JoinRequest> myRequests, int incomingPending, String message) {
        StringBuilder body = new StringBuilder();
        body.append("<section class=\"dashboard-hero\"><div><span class=\"eyebrow\">AUTHENTICATED DASHBOARD</span><h1>Welcome, ")
                .append(WebUtil.escape(user.getDisplayName())).append("</h1><p>Signed in as ")
                .append(user.getRole()).append(" · ").append(WebUtil.escape(user.getEmail())).append("</p></div>")
                .append("<span class=\"badge role\">").append(user.getRole()).append("</span></section>");

        if (user.isAdmin()) {
            body.append("<section class=\"dashboard-grid\">")
                    .append(dashboardTile("Admin access", "Review all join requests and system activity.", "/requests", "Open request center"))
                    .append(dashboardTile("Researchers", "Browse all student and faculty research profiles.", "/researchers", "Browse directory"))
                    .append(dashboardTile("Research teams", "Review all current research teams and capacities.", "/teams", "View teams"))
                    .append("</section>");
            return layout("Dashboard", body.toString(), message, user, null);
        }

        body.append("<section class=\"dashboard-grid\">")
                .append(dashboardTile("My profile", profile == null ? "Complete your research identity." : profile.getDepartment() + " · " + profile.getResearchInterests().size() + " interests", "/profile/edit", "Edit profile"))
                .append(dashboardTile("Teams I lead", String.valueOf(ownedTeams.size()) + " research team(s)", "/teams/new", "Create a team"))
                .append(dashboardTile("Incoming requests", incomingPending + " request(s) waiting for your decision", "/requests", "Manage requests"))
                .append(dashboardTile("My join requests", myRequests.size() + " request(s) sent by you", "/requests", "View statuses"))
                .append("</section>");

        if (!ownedTeams.isEmpty()) {
            body.append(sectionHeading("Teams you lead", "You have approval authority only for these teams.", "/requests", "Manage incoming requests"));
            body.append("<div class=\"card-grid\">");
            ownedTeams.forEach(t -> body.append(teamCard(t)));
            body.append("</div>");
        }

        if (profile != null) {
            body.append("<section class=\"panel security-note\"><span class=\"eyebrow\">AUTHORIZATION RULE</span><h2>Your identity is enforced by the Java backend</h2><p>Join requests are always created using your logged-in profile ID. Approval is accepted only when your profile is the destination team's leader. Hiding buttons is not the security mechanism; the service layer validates the permission again.</p><a class=\"btn secondary compact\" href=\"/matches\">Find my team matches</a></section>");
        }
        return layout("Dashboard", body.toString(), message, user, profile);
    }

    public static String researchers(List<Researcher> researchers, String q, String role, String message,
                                     UserAccount user, Researcher profile) {
        String actionUrl = user == null ? "/register" : (profile == null ? "/dashboard" : "/profile/edit");
        String actionText = user == null ? "Create account" : "My profile";
        StringBuilder body = new StringBuilder(pageHeader("Researcher directory", "Find students and faculty by name, department, research interest or skill.", actionUrl, actionText));
        body.append("<form class=\"filter-bar\" method=\"get\" action=\"/researchers\"><div class=\"field grow\"><label>Search</label><input name=\"q\" placeholder=\"AI, cybersecurity, Java, climate...\" value=\"")
                .append(WebUtil.escape(q)).append("\"></div><div class=\"field\"><label>Role</label><select name=\"role\">")
                .append(option("", "All roles", role)).append(option("STUDENT", "Student", role)).append(option("FACULTY", "Faculty", role))
                .append("</select></div><button class=\"btn primary compact\" type=\"submit\">Search</button></form>");
        body.append("<div class=\"results-meta\"><strong>").append(researchers.size()).append(" researcher(s)</strong><span>Profiles are owned by authenticated accounts</span></div>");
        if (researchers.isEmpty()) body.append(emptyState("No researchers found", "Try another search term.", "/researchers", "Reset search"));
        else {
            body.append("<div class=\"card-grid\">");
            researchers.forEach(r -> body.append(researcherCard(r, profile != null && r.getId() == profile.getId())));
            body.append("</div>");
        }
        return layout("Researchers", body.toString(), message, user, profile);
    }

    public static String editProfile(UserAccount user, Researcher profile, String message) {
        String body = pageHeader("My research profile", "Update research information connected to your authenticated account.", "/dashboard", "Back to dashboard") +
                "<section class=\"form-shell\"><form class=\"form-card\" method=\"post\" action=\"/profile\"><div class=\"identity-box\"><strong>" + WebUtil.escape(profile.getName()) + "</strong><span>" + WebUtil.escape(profile.getEmail()) + " · " + profile.getRole() + "</span></div><div class=\"form-grid two\">" +
                "<div class=\"field full\"><label>Department *</label><input name=\"department\" required maxlength=\"100\" value=\"" + WebUtil.escape(profile.getDepartment()) + "\"></div>" +
                "<div class=\"field full\"><label>Research interests</label><input name=\"interests\" maxlength=\"300\" value=\"" + WebUtil.escape(String.join(", ", profile.getResearchInterests())) + "\" placeholder=\"Machine Learning, Healthcare, Cybersecurity\"></div>" +
                "<div class=\"field full\"><label>Skills</label><input name=\"skills\" maxlength=\"300\" value=\"" + WebUtil.escape(String.join(", ", profile.getSkills())) + "\" placeholder=\"Java, Python, Statistics\"></div>" +
                "<div class=\"field full\"><label>Short bio</label><textarea name=\"bio\" rows=\"5\" maxlength=\"600\">" + WebUtil.escape(profile.getBio()) + "</textarea></div>" +
                "<div class=\"field full check-field\"><label><input type=\"checkbox\" name=\"available\" value=\"yes\"" + (profile.isAvailable() ? " checked" : "") + "> Available for research collaboration</label></div></div>" +
                "<div class=\"form-actions\"><a class=\"btn secondary\" href=\"/dashboard\">Cancel</a><button class=\"btn primary\" type=\"submit\">Save my profile</button></div></form>" +
                "<aside class=\"form-help\"><span class=\"eyebrow\">ACCOUNT OWNERSHIP</span><h3>Identity fields are protected</h3><p>Name, email and account role come from your authenticated account. This form only updates your own research information.</p><div class=\"tip\">Matching uses your interests and skills to rank suitable open teams.</div></aside></section>";
        return layout("My Profile", body, message, user, profile);
    }

    public static String teams(List<ResearchTeam> teams, String q, String status, String message,
                               UserAccount user, Researcher profile) {
        String actionUrl = user == null ? "/login" : (profile == null ? "/dashboard" : "/teams/new");
        String actionText = user == null ? "Log in" : (profile == null ? "Dashboard" : "Create team");
        StringBuilder body = new StringBuilder(pageHeader("Research teams", "Explore research topics, team capacity and collaboration opportunities.", actionUrl, actionText));
        body.append("<form class=\"filter-bar\" method=\"get\" action=\"/teams\"><div class=\"field grow\"><label>Search</label><input name=\"q\" placeholder=\"AI, sustainability, security...\" value=\"")
                .append(WebUtil.escape(q)).append("\"></div><div class=\"field\"><label>Status</label><select name=\"status\">")
                .append(option("", "All statuses", status)).append(option("FORMING", "Forming", status)).append(option("ACTIVE", "Active", status)).append(option("COMPLETED", "Completed", status))
                .append("</select></div><button class=\"btn primary compact\" type=\"submit\">Search</button></form>");
        body.append("<div class=\"results-meta\"><strong>").append(teams.size()).append(" team(s)</strong><span>Available in the system</span></div>");
        if (teams.isEmpty()) body.append(emptyState("No teams found", "Try a different filter.", "/teams", "Reset search"));
        else {
            body.append("<div class=\"card-grid\">");
            teams.forEach(t -> body.append(teamCard(t)));
            body.append("</div>");
        }
        return layout("Teams", body.toString(), message, user, profile);
    }

    public static String newTeam(UserAccount user, Researcher profile, String message) {
        String body = pageHeader("Create research team", "Your logged-in research profile becomes the team leader automatically.", "/teams", "Back to teams") +
                "<section class=\"form-shell\"><form class=\"form-card\" method=\"post\" action=\"/teams\"><div class=\"identity-box\"><strong>Team leader: " + WebUtil.escape(profile.getName()) + "</strong><span>" + WebUtil.escape(profile.getDepartment()) + " · " + profile.getRole() + "</span></div>" +
                "<div class=\"form-grid two\"><div class=\"field full\"><label>Team name *</label><input name=\"name\" required maxlength=\"100\" placeholder=\"e.g. AI for Smart Healthcare\"></div>" +
                "<div class=\"field\"><label>Research area *</label><input name=\"researchArea\" required maxlength=\"100\" placeholder=\"Artificial Intelligence\"></div>" +
                "<div class=\"field\"><label>Target team size *</label><input name=\"targetSize\" type=\"number\" min=\"2\" max=\"12\" value=\"5\" required></div>" +
                "<div class=\"field full\"><label>Research problem / description *</label><textarea name=\"description\" rows=\"6\" required maxlength=\"800\" placeholder=\"Describe the problem, expected contribution and members needed.\"></textarea></div></div>" +
                "<div class=\"form-actions\"><a class=\"btn secondary\" href=\"/teams\">Cancel</a><button class=\"btn primary\" type=\"submit\">Create my team</button></div></form>" +
                "<aside class=\"form-help\"><span class=\"eyebrow\">OWNERSHIP</span><h3>No leader dropdown</h3><p>The previous version allowed any visitor to select any person as team leader. This version uses the authenticated user's profile automatically.</p><div class=\"tip\">Only this profile can later approve or reject requests for the team.</div></aside></section>";
        return layout("New Team", body, message, user, profile);
    }

    public static String teamDetail(ResearchTeam team, Researcher leader, List<Researcher> members,
                                    List<JoinRequest> teamRequests, Map<Integer, Researcher> researchers,
                                    UserAccount user, Researcher profile, String message) {
        StringBuilder body = new StringBuilder();
        boolean admin = user != null && user.isAdmin();
        boolean isLeader = profile != null && profile.getId() == team.getLeaderId();
        boolean isMember = profile != null && team.getMemberIds().contains(profile.getId());
        JoinRequest ownRequest = profile == null ? null : teamRequests.stream()
                .filter(r -> r.getResearcherId() == profile.getId()).findFirst().orElse(null);

        body.append("<div class=\"breadcrumb\"><a href=\"/teams\">Research teams</a><span>/</span><span>")
                .append(WebUtil.escape(team.getName())).append("</span></div><section class=\"team-hero\"><div><div class=\"team-topline\"><span class=\"badge ")
                .append(statusClass(team.getStatus().name())).append("\">").append(team.getStatus()).append("</span><span>").append(WebUtil.escape(team.getResearchArea()))
                .append("</span></div><h1>").append(WebUtil.escape(team.getName())).append("</h1><p>").append(WebUtil.escape(team.getDescription()))
                .append("</p><div class=\"team-meta\"><span>Leader: <strong>").append(WebUtil.escape(leader.getName())).append("</strong></span><span>Created: <strong>")
                .append(team.getCreatedAt().format(DATE)).append("</strong></span></div></div><div class=\"capacity-card\"><span>Team capacity</span><strong>")
                .append(members.size()).append(" / ").append(team.getTargetSize()).append("</strong><div class=\"progress\"><i style=\"width:")
                .append(Math.min(100, (int) Math.round(100.0 * members.size() / team.getTargetSize()))).append("%\"></i></div><small>")
                .append(team.openSlots()).append(" open slot(s)</small></div></section>");

        body.append("<div class=\"detail-grid\"><section class=\"panel\"><div class=\"panel-heading\"><h2>Current members</h2><span>").append(members.size()).append(" member(s)</span></div><div class=\"member-list\">");
        for (Researcher member : members) {
            body.append("<div class=\"member-row\"><div class=\"avatar\">").append(initials(member.getName())).append("</div><div class=\"member-main\"><strong>")
                    .append(WebUtil.escape(member.getName())).append("</strong><span>").append(WebUtil.escape(member.getDepartment())).append(" · ").append(member.getRole()).append("</span></div>");
            if (member.getId() == team.getLeaderId()) body.append("<span class=\"badge leader\">Leader</span>");
            body.append("</div>");
        }
        body.append("</div></section><aside class=\"panel join-panel\"><div class=\"panel-heading\"><h2>Join this team</h2></div>");
        if (user == null) {
            body.append("<div class=\"notice\">Log in first. A join request must come from your own authenticated profile.</div><a class=\"btn primary full-btn\" href=\"/login\">Log in</a>");
        } else if (admin) {
            body.append("<div class=\"notice\">Administrator accounts review system activity but do not join research teams.</div>");
        } else if (isLeader) {
            body.append("<div class=\"notice\">You are the leader of this team. You cannot request to join your own team.</div><a class=\"btn primary full-btn\" href=\"/requests\">Manage team requests</a>");
        } else if (isMember) {
            body.append("<div class=\"notice\">You are already a member of this research team.</div>");
        } else if (ownRequest != null && (ownRequest.getStatus() == JoinRequest.Status.PENDING || ownRequest.getStatus() == JoinRequest.Status.APPROVED)) {
            body.append("<div class=\"notice\">Your request status: <strong>").append(ownRequest.getStatus()).append("</strong>. You cannot submit another request.</div><a class=\"btn secondary full-btn\" href=\"/requests\">View my requests</a>");
        } else if (team.openSlots() <= 0) {
            body.append("<div class=\"notice\">This team is currently full.</div>");
        } else {
            body.append("<p class=\"muted\">Requesting as <strong>").append(WebUtil.escape(profile.getName())).append("</strong>. The backend chooses your ID from the login session.</p><form method=\"post\" action=\"/teams/join\"><input type=\"hidden\" name=\"teamId\" value=\"")
                    .append(team.getId()).append("\"><div class=\"field\"><label>Contribution message</label><textarea name=\"message\" rows=\"5\" maxlength=\"500\" placeholder=\"Explain how you can contribute to this research.\"></textarea></div><button class=\"btn primary full-btn\" type=\"submit\">Send my join request</button></form>");
        }
        body.append("</aside></div>");

        if (isLeader || admin) {
            body.append("<section class=\"panel request-panel\"><div class=\"panel-heading\"><h2>Requests you are allowed to manage</h2><a href=\"/requests\">Open request center</a></div><div class=\"request-stack\">");
            List<JoinRequest> pending = teamRequests.stream().filter(r -> r.getStatus() == JoinRequest.Status.PENDING).toList();
            if (pending.isEmpty()) body.append("<p class=\"muted\">No pending requests for this team.</p>");
            for (JoinRequest request : pending) {
                Researcher requester = researchers.get(request.getResearcherId());
                if (requester != null) body.append(requestRow(request, team, requester, true));
            }
            body.append("</div></section>");
        }
        return layout(team.getName(), body.toString(), message, user, profile);
    }

    public static String requests(List<JoinRequest> outgoing, List<JoinRequest> incoming,
                                  Map<Integer, ResearchTeam> teams, Map<Integer, Researcher> researchers,
                                  UserAccount user, Researcher profile, String message) {
        StringBuilder body = new StringBuilder(pageHeader("Join request center", "Outgoing requests show your status. Incoming requests can only be decided by the correct team leader.", "/teams", "View teams"));
        if (!user.isAdmin()) {
            body.append("<section class=\"panel request-panel\"><div class=\"panel-heading\"><h2>My join requests</h2><span>").append(outgoing.size()).append(" request(s)</span></div><div class=\"request-stack\">");
            if (outgoing.isEmpty()) body.append("<p class=\"muted\">You have not sent any join requests yet.</p>");
            for (JoinRequest request : outgoing) {
                ResearchTeam team = teams.get(request.getTeamId());
                Researcher requester = researchers.get(request.getResearcherId());
                if (team != null && requester != null) body.append(requestRow(request, team, requester, false));
            }
            body.append("</div></section>");
        }

        body.append("<section class=\"panel request-panel\"><div class=\"panel-heading\"><h2>")
                .append(user.isAdmin() ? "All system requests" : "Requests to teams I lead")
                .append("</h2><span>").append(incoming.size()).append(" request(s)</span></div><div class=\"request-stack\">");
        if (incoming.isEmpty()) body.append("<p class=\"muted\">No requests are waiting for your decision.</p>");
        for (JoinRequest request : incoming) {
            ResearchTeam team = teams.get(request.getTeamId());
            Researcher requester = researchers.get(request.getResearcherId());
            if (team != null && requester != null) body.append(requestRow(request, team, requester, request.getStatus() == JoinRequest.Status.PENDING));
        }
        body.append("</div></section>");
        return layout("Join Requests", body.toString(), message, user, profile);
    }

    public static String matches(Researcher researcher, List<TeamMatch> matches, UserAccount user, String message) {
        StringBuilder body = new StringBuilder();
        body.append("<div class=\"breadcrumb\"><a href=\"/dashboard\">Dashboard</a><span>/</span><span>My team matches</span></div><section class=\"match-hero\"><div class=\"avatar large\">")
                .append(initials(researcher.getName())).append("</div><div><span class=\"eyebrow\">PERSONALIZED TEAM MATCHING</span><h1>Best teams for ")
                .append(WebUtil.escape(researcher.getName())).append("</h1><p>Scores use your authenticated profile's interests, skills, availability and team capacity.</p></div></section>");
        if (matches.isEmpty()) body.append(emptyState("No open matches", "You are already in the available teams or the remaining teams are full.", "/teams", "View all teams"));
        else {
            body.append("<div class=\"match-list\">");
            for (TeamMatch match : matches) {
                ResearchTeam team = match.team();
                body.append("<article class=\"match-card\"><div class=\"score-ring\"><strong>").append(match.score()).append("%</strong><span>match</span></div><div class=\"match-main\"><div class=\"team-topline\"><span class=\"badge ")
                        .append(statusClass(team.getStatus().name())).append("\">").append(team.getStatus()).append("</span><span>").append(WebUtil.escape(team.getResearchArea())).append("</span></div><h3>")
                        .append(WebUtil.escape(team.getName())).append("</h3><p>").append(WebUtil.escape(match.reason())).append("</p><span class=\"muted\">").append(team.openSlots()).append(" open slot(s)</span></div><a class=\"btn secondary compact\" href=\"/teams/view?id=")
                        .append(team.getId()).append("\">View team</a></article>");
            }
            body.append("</div>");
        }
        return layout("My Team Matches", body.toString(), message, user, researcher);
    }

    public static String about(UserAccount user, Researcher profile) {
        String body = pageHeader("About the system", "A Java OOP mini-project with realistic identity and authorization rules.", "/", "Back home") + """
                <section class="about-grid">
                  <article class="panel"><span class="eyebrow">PROBLEM</span><h2>Why this system exists</h2><p>Students and faculty need a structured way to discover collaborators and form research groups instead of relying only on informal communication.</p></article>
                  <article class="panel"><span class="eyebrow">AUTHENTICATION</span><h2>One account, one identity</h2><p>Users log in before performing personal actions. A research profile is linked to the account, so a visitor cannot impersonate another researcher when sending a request.</p></article>
                  <article class="panel"><span class="eyebrow">AUTHORIZATION</span><h2>Team ownership controls approval</h2><p>A join request can only be approved or rejected by the leader of the destination team or by an administrator. Permission is checked in Java service-layer code.</p></article>
                  <article class="panel"><span class="eyebrow">OOP DESIGN</span><h2>Separated responsibilities</h2><p>Domain models, repository interfaces, authentication, team services, matching services and the web layer are separated to demonstrate encapsulation, abstraction and low coupling.</p></article>
                </section>
                """;
        return layout("About", body, null, user, profile);
    }

    public static String error(String title, String message, int statusCode, UserAccount user, Researcher profile) {
        String body = "<section class=\"error-page\"><span>" + statusCode + "</span><h1>" + WebUtil.escape(title) + "</h1><p>" + WebUtil.escape(message) + "</p><a class=\"btn primary\" href=\"/\">Return home</a></section>";
        return layout(title, body, null, user, profile);
    }

    private static String researcherCard(Researcher r, boolean ownProfile) {
        String action = ownProfile ? "<a href=\"/profile/edit\">Edit my profile →</a>" : "<span>Profile ID: R" + r.getId() + "</span>";
        return "<article class=\"person-card\"><div class=\"person-head\"><div class=\"avatar\">" + initials(r.getName()) + "</div><div><div class=\"badge-row\"><span class=\"badge role\">" + r.getRole() + "</span>" + (r.isAvailable() ? "<span class=\"availability\">Available</span>" : "") + "</div><h3>" + WebUtil.escape(r.getName()) + "</h3><p>" + WebUtil.escape(r.getDepartment()) + "</p></div></div>" +
                "<p class=\"card-bio\">" + WebUtil.escape(r.getBio().isBlank() ? "Research profile is being completed." : r.getBio()) + "</p><div class=\"tag-wrap\">" + tags(r.getResearchInterests(), 3) + "</div>" +
                "<div class=\"card-footer\"><span>Skills: " + WebUtil.escape(r.getSkills().isEmpty() ? "Not added" : String.join(", ", r.getSkills().stream().limit(3).toList())) + "</span>" + action + "</div></article>";
    }

    private static String teamCard(ResearchTeam t) {
        int filled = t.getMemberIds().size();
        int pct = Math.min(100, (int) Math.round(100.0 * filled / t.getTargetSize()));
        return "<article class=\"team-card\"><div class=\"team-topline\"><span class=\"badge " + statusClass(t.getStatus().name()) + "\">" + t.getStatus() + "</span><span>" + WebUtil.escape(t.getResearchArea()) + "</span></div><h3>" + WebUtil.escape(t.getName()) + "</h3><p>" + WebUtil.escape(t.getDescription()) + "</p><div class=\"capacity-line\"><span>Team members</span><strong>" + filled + " / " + t.getTargetSize() + "</strong></div><div class=\"progress\"><i style=\"width:" + pct + "%\"></i></div><div class=\"card-footer\"><span>" + t.openSlots() + " open slot(s)</span><a href=\"/teams/view?id=" + t.getId() + "\">View team →</a></div></article>";
    }

    private static String requestRow(JoinRequest request, ResearchTeam team, Researcher researcher, boolean actions) {
        StringBuilder row = new StringBuilder();
        row.append("<article class=\"request-row\"><div class=\"avatar\">").append(initials(researcher.getName())).append("</div><div class=\"request-main\"><div class=\"request-title\"><strong>")
                .append(WebUtil.escape(researcher.getName())).append("</strong><span class=\"badge ").append(statusClass(request.getStatus().name())).append("\">").append(request.getStatus()).append("</span></div><p>")
                .append(actions ? "Wants to join " : "Request to ").append("<a href=\"/teams/view?id=").append(team.getId()).append("\">").append(WebUtil.escape(team.getName())).append("</a></p><blockquote>")
                .append(WebUtil.escape(request.getMessage().isBlank() ? "No message provided." : request.getMessage())).append("</blockquote><small>").append(request.getCreatedAt().format(DATETIME)).append("</small></div>");
        if (actions) {
            row.append("<div class=\"request-actions\"><form method=\"post\" action=\"/requests/approve\"><input type=\"hidden\" name=\"requestId\" value=\"").append(request.getId()).append("\"><button class=\"btn primary compact\" type=\"submit\">Approve</button></form>")
                    .append("<form method=\"post\" action=\"/requests/reject\"><input type=\"hidden\" name=\"requestId\" value=\"").append(request.getId()).append("\"><button class=\"btn danger compact\" type=\"submit\">Reject</button></form></div>");
        }
        row.append("</article>");
        return row.toString();
    }

    private static String layout(String title, String body, String message, UserAccount user, Researcher profile) {
        String flash = (message == null || message.isBlank()) ? "" : "<div class=\"flash\"><span>" + WebUtil.escape(message) + "</span><button type=\"button\" onclick=\"this.parentElement.remove()\">×</button></div>";
        String authNav;
        if (user == null) {
            authNav = "<a href=\"/login\">Login</a><a class=\"nav-cta\" href=\"/register\">Register</a>";
        } else {
            authNav = "<a href=\"/dashboard\">Dashboard</a>" + (profile == null ? "" : "<a href=\"/profile/edit\">My Profile</a>") + "<a href=\"/requests\">Requests</a><form class=\"logout-form\" method=\"post\" action=\"/logout\"><button type=\"submit\">Logout</button></form>";
        }
        return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><meta name=\"description\" content=\"University Research Formation Team System built with Java and OOP.\"><title>" + WebUtil.escape(title) + " | UniResearch</title><link rel=\"stylesheet\" href=\"/assets/style.css\"></head><body>" +
                "<header class=\"site-header\"><div class=\"container nav-wrap\"><a class=\"brand\" href=\"/\"><span class=\"brand-mark\">UR</span><span><strong>UniResearch</strong><small>Team Formation System</small></span></a><button class=\"nav-toggle\" aria-label=\"Toggle navigation\" onclick=\"document.querySelector('.nav-links').classList.toggle('open')\">☰</button><nav class=\"nav-links\"><a href=\"/\">Home</a><a href=\"/researchers\">Researchers</a><a href=\"/teams\">Teams</a><a href=\"/about\">About</a>" + authNav + "</nav></div></header>" +
                "<main class=\"container main-content\">" + flash + body + "</main>" +
                "<footer class=\"site-footer\"><div class=\"container footer-grid\"><div><div class=\"brand footer-brand\"><span class=\"brand-mark\">UR</span><span><strong>UniResearch</strong><small>Java OOP Project</small></span></div><p>Authenticated university research collaboration with role-based permissions.</p></div><div><strong>Project modules</strong><a href=\"/researchers\">Researcher Directory</a><a href=\"/teams\">Team Formation</a><a href=\"/requests\">Request Management</a></div><div><strong>Technology</strong><span>Java 21</span><span>Core Java HttpServer</span><span>PBKDF2 Password Hashing</span></div></div><div class=\"container footer-bottom\"><span>Academic OOP mini-project</span><span>Identity and authorization enforced in Java</span></div></footer><script src=\"/assets/app.js\"></script></body></html>";
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

    private static String dashboardTile(String title, String text, String url, String action) {
        return "<article class=\"dashboard-tile\"><h3>" + WebUtil.escape(title) + "</h3><p>" + WebUtil.escape(text) + "</p><a href=\"" + url + "\">" + WebUtil.escape(action) + " →</a></article>";
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
