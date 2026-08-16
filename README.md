# Medical Records Management

Centralized medical records platform for patients.
Patients (and their caregivers) can access health history in one place and grant access to healthcare workers when care is needed.

## Status

This repo currently implements a **patient-owned medical record** as plain Java, developed with **TDD**.

What exists today:

- **Domain layer** — `MedicalRecord` aggregate, clinical entries, access grants, typed IDs
- **Application layer** — `MedicalRecordApplicationService` (load → mutate → save)
- **Infrastructure (in-memory)** — `InMemoryMedicalRecordRepository`

There is still **no** database, REST API, or identity service.

| Item | Detail |
|------|--------|
| Architecture style | Microservices (starting with Medical Records) |
| Design | Domain-Driven Design — domain first |
| Language | Java 26 |
| Build | Maven (single module) |
| Tests | JUnit 5 + AssertJ + Mockito |
| Persistence | In-memory repository (swap-ready port) |

## How it works

Each command follows the same pattern:

```text
application service
  → MedicalRecordRepository.findByPatientId(...)
  → MedicalRecord.<domain action>(...)
  → MedicalRecordRepository.save(...)
```

Supported application commands:

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
├── application/                      # Use cases / application service
│   ├── MedicalRecordApplicationService
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

Application-service tests mock `MedicalRecordRepository` with Mockito.
Repository tests exercise the in-memory adapter directly.

## Out of scope (for now)

- Durable persistence (database)
- REST API (HTTP endpoints)
- Messaging, logging, monitoring
- Identity management (authentication / user directory)
- FHIR-typed clinical events / episode graph (timeline model)
- Scoped grants, expiry, and read-audit history
