# What Changed in This Version

This update fixes the unrealistic workflow in the first version.

## Previous problem

The old interface allowed a visitor to select any researcher when sending a join request and showed approval controls without proving that the current visitor was the correct team leader.

## Updated solution

- Added account registration, login and logout.
- Added Student, Faculty and Admin roles.
- Added `UserAccount` as an OOP domain class.
- Added `AuthenticationService` and `SessionManager`.
- Each normal account is linked to one `Researcher` profile.
- Users update only their own profile.
- A team creator automatically becomes its leader.
- Team creation no longer asks the browser to choose a leader.
- Join requests no longer ask the browser to choose a researcher.
- The backend gets the requester ID from the login session.
- Students can see their outgoing request status but cannot approve it.
- Only the destination team's leader can approve/reject its request.
- Admin can manage all requests.
- Backend service rules block direct unauthorized HTTP submissions.
- Added salted PBKDF2 password hashing.
- Added dashboard pages and role-aware navigation.

## Recommended viva demonstration

1. Log in as `amina@example.edu` / `student123`.
2. Open **Secure Research Collaboration** and send a join request.
3. Open **Requests** and show that Amina can see the pending status but no approval buttons.
4. Log out.
5. Log in as `nabil@example.edu` / `leader123`.
6. Open **Requests to teams I lead** and approve Amina's request.
7. Log out and log back in as Amina.
8. Show the request status as APPROVED and show Amina in the team member list.


## Team collaboration update

This version also adds two post-formation collaboration features:

- **Private Team Discussion**: approved members can post research progress, questions and coordination updates. Outsiders can see that a workspace exists but cannot read its content.
- **Team Meeting System**: approved members can view meeting details and use an online meeting link. Only the team leader can schedule a meeting. The backend validates the leader identity, future date/time and HTTP/HTTPS meeting link.
- Discussion posts and meetings use new OOP domain classes (`TeamDiscussionPost`, `TeamMeeting`) and a dedicated `CollaborationService`.
- Both features are stored through the existing `ResearchRepository` abstraction, so they are persisted in `research-data.bin` in the current prototype.

### Updated viva demonstration

After Amina is approved into a team, open that team again and show that the private discussion and meetings area becomes available. Post a discussion update as Amina. Then log in as the team leader and schedule a meeting. Log back in as a normal team member and show that the meeting is visible but the scheduling form is not.
