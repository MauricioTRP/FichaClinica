# Medical Records Management

Centralized medical records platform for patients.
Patients (and their caregivers) can access health history in one place and grant access to healthcare workers when care is needed.

## Status

This repo currently implements a **patient-owned medical record** as plain Java, developed with **TDD**.

What exists today:

- **Domain layer** — `MedicalRecord` aggregate, clinical entries, access grants, typed IDs
- **Application layer** — UseCase-oriented application layer (see below). For backward compatibility a thin `MedicalRecordApplicationService` façade is kept (it delegates to the UseCases).
- **Infrastructure (in-memory)** — `InMemoryMedicalRecordRepository`

There is still **no** database, REST API, or identity service.

| Item | Detail |
|------|--------|
| Architecture style | Clean Architecture / DDD (UseCase-driven application layer) |
| Design | Domain-Driven Design — domain first; UseCase pattern in application layer |
| Language | Java 26 |
| Build | Maven (single module) |
| Tests | JUnit 5 + AssertJ + Mockito |
| Persistence | In-memory repository (swap-ready port) |

## How it works

The application layer was refactored to follow the UseCase pattern. Each business action is implemented as a dedicated UseCase class that:

- validates input
- loads the aggregate through the repository port
- delegates domain mutations to the aggregate
- persists changes through the repository

A typical flow:

```text
controller / adapter
  → UseCase<Request>.execute(request)
    → MedicalRecordRepository.findByPatientId(...)
    → MedicalRecord.<domain action>(...)
    → MedicalRecordRepository.save(...)
```

To preserve existing code and adapters, `MedicalRecordApplicationService` remains available as a thin façade that simply delegates to the UseCase implementations. Keep this service thin: it should not contain domain logic, only delegation and cross-cutting concerns (transactions, logging) when necessary.

Supported application UseCases (classes under `org.ficha.application.usecase`):

- CreateMedicalRecordUseCase
- GetMedicalRecordUseCase
- GrantCaregiverAccessUseCase
- RevokeCaregiverAccessUseCase
- ShareWithHealthcareWorkerUseCase
- AddProfessionalEntryUseCase
- AddExternalEntryUseCase
- AmendEntryUseCase
- MarkSpecialNeedsUseCase
- ClearSpecialNeedsUseCase

Supported application commands (API surface, still available on `MedicalRecordApplicationService`):

- Create medical record for a patient
- Grant / revoke caregiver access
- Share with a healthcare worker
- Add professional entry
- Add external entry (e.g. uploaded PDF)
- Amend entry (append-only version with reason)
- Mark / clear special needs
- Get medical record by patient ID

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

### Package layout

```text
org.ficha
├── application/                      # Use cases, façade, and application exceptions
│   ├── usecase/                       # Individual UseCase classes (UseCase pattern)
│   ├── MedicalRecordApplicationService # Thin façade kept for backward compatibility
│   └── MedicalRecordNotFoundException
├── domain/
│   ├── model/                        # MedicalRecord, ClinicalEntry, EntryVersion, …
│   ├── model.ids/                    # Typed IDs
│   ├── model.source/                 # ProfessionalSource, ExternalSource
│   ├── repository/                   # MedicalRecordRepository (port)
│   └── exceptions/                   # Domain exceptions
└── infrastructure/
    └── persistence/                  # InMemoryMedicalRecordRepository (adapter)
```

## Running tests

Requires **Java 26** and Maven:

```bash
mvn test
```

Application-layer tests mock `MedicalRecordRepository` with Mockito. Unit tests can target UseCase classes directly — this is recommended because each UseCase implements a single business action and is easy to test in isolation.

## Rationale for the refactor

- UseCase pattern makes the application boundary explicit and gives each business action a single responsibility.
- Easier testing: each UseCase is unit-test friendly (inject repository mocks).
- Better separation: domain logic stays inside aggregates; application layer becomes orchestration only.
- Compatibility: keeping the `MedicalRecordApplicationService` façade avoids breaking existing adapters/controllers while migrating to the UseCase style.

## Out of scope (for now)

- Durable persistence (database)
- REST API (HTTP endpoints)
- Messaging, logging, monitoring
- Identity management (authentication / user directory)
- FHIR-typed clinical events / episode graph (timeline model)
- Scoped grants, expiry, and read-audit history
