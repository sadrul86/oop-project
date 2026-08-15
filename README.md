# University Research Formation Team System

A complete academic mini-project website built with **Java 21** and **Object-Oriented Programming (OOP)**. It helps students and faculty discover researchers, form research teams, request team membership and match research interests.

## Main features

- Responsive university research portal
- Researcher directory with search and role filters
- Create researcher profiles
- Create research teams
- Team leader and member management
- Team capacity and lifecycle status
- Join-request submission
- Approve/reject join requests
- Interest-based team matching score
- Demo dashboard and statistics API
- File-backed repository with automatic demo data
- HTML escaping and basic security headers
- Docker deployment configuration
- Render free deployment Blueprint

## OOP concepts demonstrated

- **Encapsulation:** private model state and controlled methods
- **Abstraction:** `ResearchRepository` interface
- **Polymorphism:** services depend on the repository interface, not a concrete class
- **Separation of concerns:** model, repository, service, utility and web layers
- **Composition:** teams contain researcher/member IDs and services coordinate objects

## Project structure

```text
university-research-team-system/
├── src/com/university/research/
│   ├── Main.java
│   ├── model/
│   │   ├── Researcher.java
│   │   ├── ResearchTeam.java
│   │   └── JoinRequest.java
│   ├── repository/
│   │   ├── ResearchRepository.java
│   │   └── FileResearchRepository.java
│   ├── service/
│   │   ├── ResearchService.java
│   │   ├── TeamService.java
│   │   └── MatchingService.java
│   ├── util/
│   │   └── WebUtil.java
│   └── web/
│       ├── HtmlPages.java
│       └── ResearchWebServer.java
├── public/assets/
│   ├── style.css
│   └── app.js
├── docs/
│   ├── OOP-DESIGN.md
│   └── DEPLOYMENT.md
├── Dockerfile
├── render.yaml
├── run.sh
├── run.bat
└── README.md
```

## Run locally

### Requirements

Install **JDK 21**.

### Windows

Double-click `run.bat` or run:

```bat
run.bat
```

### Linux/macOS

```bash
chmod +x run.sh
./run.sh
```

Then open:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/health
```

Statistics API:

```text
http://localhost:8080/api/stats
```

## Put the project on GitHub

Create a new empty **public** GitHub repository named `university-research-team-system`.

From the extracted project folder:

```bash
git init
git add .
git commit -m "Initial university research team system"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/university-research-team-system.git
git push -u origin main
```

Replace `YOUR-USERNAME` with your GitHub username.

## Make it online for free

This Java project cannot run directly on GitHub Pages because it needs a Java server process. Use GitHub to store the source code and connect the repository to a free Render web service.

The included `render.yaml` and `Dockerfile` already contain the required deployment configuration.

### Render steps

1. Sign in to Render with GitHub.
2. Select **New > Blueprint**.
3. Connect this GitHub repository.
4. Render detects `render.yaml`.
5. Confirm the web service uses the **Free** plan.
6. Deploy.
7. Wait for the Docker build to complete.
8. Open the generated `https://...onrender.com` address.

When you later change the code:

```bash
git add .
git commit -m "Update website"
git push
```

Render will redeploy the latest `main` branch automatically.

## Important free-hosting note

The current repository stores application data in `data/research-data.bin`. This is ideal for a simple OOP classroom demo and requires no paid database.

On a free cloud web service, local storage may be temporary. If the service sleeps, restarts or redeploys, newly entered data can be lost. The application automatically recreates the included demo data when the data file does not exist.

If your teacher requires long-term persistent data, the clean repository interface makes it possible to add a PostgreSQL-backed implementation later.

## Demo workflow for presentation

1. Open the home dashboard.
2. Show the **Researchers** page and search by interest.
3. Add a new researcher.
4. Open **Teams** and create a new research team.
5. Open a team and submit a join request.
6. Open **Requests** and approve the request.
7. Return to the team and show the new member.
8. Open a researcher's **Find matches** page and explain the matching score.
9. Explain the OOP package structure using `docs/OOP-DESIGN.md`.

## Suggested viva explanation

> The system follows a layered object-oriented design. Domain classes represent researchers, teams and join requests. Business rules are handled by service classes. Persistence is abstracted through the `ResearchRepository` interface, so storage can be replaced without changing the business layer. The web layer only handles HTTP requests and renders responses. This separation improves maintainability, testability and extensibility.

## Academic scope

The project is intentionally generic: no student name, student ID, supervisor name or university identity is hard-coded into the site.
