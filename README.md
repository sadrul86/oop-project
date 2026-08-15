# University Research Team Formation System

A Java 21 + Object-Oriented Programming university mini-project for realistic research team formation. The application uses only the JDK (no paid services and no external Java dependencies) and can be deployed from GitHub to Render using the included `Dockerfile` and `render.yaml`.

## Main Features

- Authentication with login/logout sessions
- Student, Faculty and Administrator account roles
- One authenticated account linked to one research profile
- Users edit only their own research profile
- Searchable researcher directory
- Logged-in researchers create teams as themselves (no fake leader dropdown)
- Team ownership: the creator becomes the team leader
- Logged-in researchers send join requests only as themselves
- Duplicate join-request prevention
- A team leader cannot request to join their own team
- Team leaders can approve/reject only requests sent to teams they lead
- Users cannot approve their own join request
- Administrators can review and manage all requests
- Team capacity validation
- Private team discussion for approved members
- Team leaders can schedule research meetings with date, time, agenda and meeting link
- Approved members can view the meeting schedule and open the meeting link
- Discussion and meeting data are persisted with the rest of the project data
- Interest/skill-based team matching
- File-backed demo persistence
- PBKDF2-HMAC-SHA256 password hashing with per-user salt
- Core Java `HttpServer` web layer

## Demo Accounts

| Role | Email | Password | Purpose |
|---|---|---|---|
| Student | `amina@example.edu` | `student123` | Send requests and view status |
| Team Leader | `nabil@example.edu` | `leader123` | Leads "Secure Research Collaboration" and can manage its requests |
| Faculty Leader | `sara@example.edu` | `faculty123` | Faculty account and leader of "AI for Student Success" |
| Administrator | `admin@example.edu` | `admin123` | Review/manage all requests |

These credentials are for academic demonstration only. The application does not store these passwords in plain text; seeded accounts store salted PBKDF2 hashes.

## Realistic Workflow

1. User registers or logs in.
2. Account identity is connected to one `Researcher` profile.
3. The user updates their own research interests, skills and availability.
4. The user browses available teams or uses **My Team Matches**.
5. A join request is submitted using the logged-in user's researcher ID from the session.
6. The requester sees the request under **My Join Requests** but cannot approve it.
7. The destination team's leader logs in and sees the request under **Requests to Teams I Lead**.
8. Only that leader (or an admin) can approve/reject.
9. Approval adds the requester to the team and updates the request status.
10. Approved members unlock the private **Team Discussion** and **Team Meetings** workspace.
11. Members can post research updates and questions; only the team leader can schedule meetings.
12. Meeting entries can contain a title, date/time, agenda and optional Google Meet/Zoom/other HTTPS link.

## OOP Structure

```text
src/com/university/research/
├── Main.java
├── model/
│   ├── UserAccount.java
│   ├── Researcher.java
│   ├── ResearchTeam.java
│   ├── JoinRequest.java
│   ├── TeamDiscussionPost.java
│   └── TeamMeeting.java
├── repository/
│   ├── ResearchRepository.java
│   └── FileResearchRepository.java
├── service/
│   ├── AuthenticationService.java
│   ├── ResearchService.java
│   ├── TeamService.java
│   ├── CollaborationService.java
│   └── MatchingService.java
├── util/
│   ├── PasswordUtil.java
│   └── WebUtil.java
└── web/
    ├── SessionManager.java
    ├── ResearchWebServer.java
    └── HtmlPages.java
```

### OOP concepts demonstrated

- **Encapsulation:** model fields are private and modified through controlled methods.
- **Abstraction:** `ResearchRepository` defines persistence operations independently of the file implementation.
- **Polymorphism:** services depend on the `ResearchRepository` interface, so another implementation could replace file storage.
- **Composition:** services collaborate with repository objects rather than putting all logic in the web server.
- **Separation of concerns:** authentication, research profiles, teams, collaboration, matching, persistence and HTTP presentation are separate classes.
- **Workspace authorization:** `CollaborationService` keeps team discussions and meetings private to approved team members and restricts meeting creation to the team leader.
- **Authorization in business logic:** `TeamService` verifies who is permitted to decide a request even if a user manually submits an HTTP request.

## Run Locally

Requires JDK 21.

### Windows

```bat
run.bat
```

### Linux/macOS

```bash
chmod +x run.sh
./run.sh
```

Then open `http://localhost:8080`.

## Free Deployment

This repository already contains:

- `Dockerfile`
- `render.yaml`

Push the project to GitHub. If your existing Render Blueprint is already connected to that GitHub repository, committing these updated files to the `main` branch will trigger a new deployment automatically.

The application reads Render's `PORT` environment variable and listens on `0.0.0.0`.

## Important Demo Note

The free deployment uses a local binary data file (`data/research-data.bin`). This is suitable for a course demonstration, but cloud free-tier local storage can be ephemeral. For a production university system, use a managed relational database and stronger production security controls.
