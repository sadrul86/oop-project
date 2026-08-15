# Deployment Update Guide

The project is designed for GitHub + Render.

## If the Render service already exists

Do not create a new Blueprint. Update the same GitHub repository that is already connected to Render.

1. Replace/upload the updated project files in the repository.
2. Commit the changes to the `main` branch.
3. Open Render and select the existing `university-research-team-system` web service.
4. Render should automatically start a new deployment from the latest GitHub commit.
5. Wait for the deployment status to become Live.
6. Open the same `onrender.com` website URL. The URL does not need to change.

Keep `Dockerfile` and `render.yaml` at the repository root.
