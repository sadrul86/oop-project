# OOP Design Notes

## Main classes

- `Researcher`: encapsulates a researcher's identity, department, role, interests, skills and availability.
- `ResearchTeam`: encapsulates team state, leader, members, target size and lifecycle status.
- `JoinRequest`: encapsulates a request to join a team and its approval status.

## Abstraction and interfaces

`ResearchRepository` is an interface. The rest of the application depends on this abstraction instead of a concrete storage class.

`FileResearchRepository` implements the interface and stores objects in a local serialized file. Another implementation such as `PostgresResearchRepository` can replace it later without changing the service classes.

## Encapsulation

Fields are private. State is changed through methods such as `ResearchTeam.addMember()` and setter methods. Team capacity and duplicate-member rules are protected inside domain/service logic.

## Polymorphism

The application uses the `ResearchRepository` interface type in services and in `Main`. Any class implementing that interface can be substituted at runtime.

## Separation of responsibilities

- `model/`: domain objects.
- `repository/`: persistence abstraction and storage.
- `service/`: business rules for profiles, teams and matching.
- `web/`: HTTP routing and HTML rendering.
- `util/`: form parsing, encoding and HTML escaping.

This structure keeps UI code separate from core business rules and demonstrates standard object-oriented design.
