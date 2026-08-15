# Free GitHub + Render Deployment

## Why not GitHub Pages?

GitHub Pages is for static HTML/CSS/JavaScript. This project runs Java server-side code, so GitHub stores the source code while Render runs the Java application.

## 1. Test locally

Install JDK 21.

Windows:

```bat
run.bat
```

Linux/macOS:

```bash
./run.sh
```

Open `http://localhost:8080`.

## 2. Push to GitHub

Create an empty public repository, then from this project folder run:

```bash
git init
git add .
git commit -m "Initial university research team system"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/university-research-team-system.git
git push -u origin main
```

## 3. Deploy free on Render

1. Sign in to Render with GitHub.
2. Choose **New > Blueprint**.
3. Select the GitHub repository.
4. Render reads `render.yaml`.
5. Check that the service plan says **Free**.
6. Apply/deploy the Blueprint.
7. Wait for the Docker build to finish.
8. Open the generated `onrender.com` URL.

Every push to `main` triggers a new deployment.

## Free-tier limitation

The application intentionally uses a file repository to avoid paid services and external dependencies. On a free Render web service, the local filesystem is ephemeral. Data added by users can disappear after the service sleeps, restarts or redeploys. The seeded demonstration data is automatically recreated when the file is missing.

For a classroom demo this keeps hosting completely free and simple. For long-term persistent data, replace `FileResearchRepository` with a database-backed repository.
