# Team Discussion and Meeting Features

## Team Discussion

- Discussion content is private to approved team members.
- A team member can post research updates, questions and coordination messages.
- The backend gets the author identity from the authenticated session; the browser cannot choose another researcher ID.
- Non-members cannot read or post discussion content.
- Administrators can inspect the workspace in read-only mode.

Key classes:

- `TeamDiscussionPost` - discussion domain object
- `CollaborationService` - membership and posting rules
- `ResearchRepository` / `FileResearchRepository` - persistence
- `ResearchWebServer` - HTTP request handling
- `HtmlPages` - workspace UI

## Team Meetings

- Approved members can view scheduled meetings.
- Only the team leader can schedule a meeting.
- A meeting contains a title, date/time, optional agenda and optional HTTP/HTTPS meeting URL.
- The backend rejects past meeting times and invalid meeting URLs.
- Upcoming meetings with a URL display a **Join meeting** button.

Key class: `TeamMeeting`.

## Permission Matrix

| Action | Visitor | Logged-in non-member | Team member | Team leader | Admin |
|---|---:|---:|---:|---:|---:|
| View public team information | Yes | Yes | Yes | Yes | Yes |
| Read team discussion | No | No | Yes | Yes | Yes (read-only) |
| Post discussion message | No | No | Yes | Yes | No |
| View meeting schedule | No | No | Yes | Yes | Yes (read-only) |
| Schedule meeting | No | No | No | Yes | No |
| Open meeting link | No | No | Yes | Yes | Yes |

## Viva Explanation

A useful short answer is:

> After a join request is approved, the researcher becomes a member of the team. Membership unlocks a private collaboration workspace. All approved members can discuss research and view meetings, while only the team leader can schedule meetings. These permissions are checked again in the Java service layer, so hiding a button is not the security mechanism.
