# OOP and Authorization Design

## Domain Objects

`UserAccount` represents authenticated identity and role. `Researcher` represents the academic research profile. `ResearchTeam` owns its member IDs and leader ID. `JoinRequest` stores the requester, destination team and lifecycle status. `TeamDiscussionPost` represents a private member post inside one team. `TeamMeeting` represents a scheduled team meeting with its creator, time, agenda and optional online meeting link.

## Repository Abstraction

`ResearchRepository` is an interface. `FileResearchRepository` is one concrete implementation. Services depend on the interface rather than the concrete storage class, demonstrating abstraction and polymorphism.

## Service Layer

- `AuthenticationService`: account login and registration.
- `ResearchService`: research profile searching and self-profile updates.
- `TeamService`: team creation, join-request validation and authorization.
- `CollaborationService`: private team discussion, workspace access control and team-leader-only meeting scheduling.
- `MatchingService`: profile-to-team compatibility calculation.

## Important Authorization Rules

`ResearchWebServer` gets the current account from a secure random session token. Personal actions do not accept a researcher ID chosen by the browser. Instead, the server resolves the researcher ID from the authenticated account.

`TeamService.approveRequest(...)` and `rejectRequest(...)` verify that the acting researcher is the leader of the destination team unless the account is an administrator. This protects the operation even if someone tries to bypass the user interface.

The service also blocks:

- self-joining by a team leader
- duplicate pending or approved join requests
- joining a team that is already full
- joining a team when already a member
- deciding a request that is no longer pending
- non-leaders deciding another team's requests


## Collaboration Authorization

The collaboration workspace is intentionally private. `CollaborationService` checks team membership before returning discussion posts or meeting information. A researcher must be present in `ResearchTeam.memberIds` to post a discussion message. Meeting creation is more restrictive: only the researcher whose ID equals `ResearchTeam.leaderId` can schedule a meeting. Administrators can inspect collaboration information for oversight, but they do not post as a researcher.

This provides two different permissions:

- **Team member:** read/post discussion and view/join scheduled meetings.
- **Team leader:** all member permissions plus schedule meetings.
