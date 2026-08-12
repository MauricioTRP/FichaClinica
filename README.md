# Medical Records Management

Cetralized medical records platforms for patiences.
Patients (and their caregivers) can access health history in one place and grant access to healthcare workers when care is needed.

## Status

This repo currently contains the **Medical Records Domain** as plain Java, developed using **TDD**.

There's no persistence, REST API, or application/infrastructure layer yet.

| Item | Detail                                                                                           |
|------|--------------------------------------------------------------------------------------------------|
| Architecture style | Microservices (starting with Medical Records)                                                    |
| Design | Domain-Driven Design — domain first                                                              |
| Language | Java 21                                                                                          |
| Build | Maven (multi-module)                                                                             |
| Tests | JUnit 5 + AssertJ + Mockito                                                                      |
| Coverage | JaCoCo (minimum **80%** line coverage on `verify`), enforces **100%** of Critical Domain Methods |

## Domain Model (Medical Record)

A **Medical Record** is one longitudinal record per patient (a folder of clinical entries over time).

### Roles (external identity IDs)

Identities live outside this service (future Identity Service). The domain only references typed IDs:

- `PatientId` — owner of the record
- `CaregiverId` — can read and share the record
- `HealthcareWorkerId` — can add professional entries and amend them when granted access

### Core Behaviors

- **Ownership** — each `MedicalRecord` belongs to one patient
- **Caregiver access** — the patient grants/revokes caregiver access
- **Sharing** — a caregiver with access can share the record with a healthcare worker
- **Professional entries** — authorized healthcare workers add clinical entries
- **External entries** — patient or caregiver can add an entry from documents provided by an unregistered professional (e.g. printed discharge papers, PDF)
- **Amendments** — append-only versions; every amendment requires a non-blank reason; prior versions remain readable
- **Special needs marker** — lightweight hook for future analytics (e.g. atypical baseline lab parameters)

### Main packages

```
org.ficha.domain
|--- model/           # MedicalRecord, ClinicalEntry, EntryVersion, EntryContent, …
|--- model.ids/       # Typed IDs
|--- model.source/    # ProfessionalSource, ExternalSource
|--- exception/       # Domain exceptions
```

## Out of scope (for now)

- Persistence (database, file system, etc.)
- REST API (HTTP endpoints)
- Application layer (services, use cases)
- Infrastructure layer (messaging, logging, monitoring, etc.)
- Identity management (authentication, authorization, user management)