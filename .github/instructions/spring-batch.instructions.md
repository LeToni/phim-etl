---
description: 'Best practices for building robust Spring Batch jobs with restartability, idempotency, observability, and scalable chunk/tasklet processing.'
applyTo: '**/*.java, **/*.kt, **/application*.yml, **/application*.yaml'
---

# Spring Batch Development

## General Instructions

- Make only high-confidence suggestions and keep behavior changes explicit.
- Favor maintainable job design: small steps, clear boundaries, and restart-safe logic.
- Treat batch jobs as production workloads: observable, fault-tolerant, and repeatable.

## Job Design

### Job Parameters and Identity

- Design jobs with explicit `JobParameters` and stable identifying parameters.
- Keep non-identifying runtime metadata separate to avoid creating accidental new `JobInstance`s.
- Validate parameters at job start and fail fast with actionable error messages.

### Idempotency and Restartability

- Make `ItemReader`, `ItemProcessor`, and `ItemWriter` idempotent whenever possible.
- Ensure restartability by persisting state needed for `ExecutionContext` recovery.
- Use deterministic input ordering to avoid duplicate or skipped records on restart.
- Avoid side effects in processors; keep external writes in writers and make them retry-safe.

### Step Boundaries

- Prefer chunk-oriented processing for high-volume record pipelines.
- Use tasklet steps for control-flow actions, orchestration, or one-off operations.
- Keep each step focused on one responsibility; split complex logic into multiple steps.

## Reader / Processor / Writer Guidance

### ItemReader

- Use streaming/paging readers that fit source characteristics and dataset size.
- Configure fetch/page size intentionally and document why that value was chosen.
- Ensure resource cleanup and predictable cursor behavior.

### ItemProcessor

- Keep processors pure and deterministic.
- Use explicit filtering by returning `null` only when business rules require dropping items.
- Avoid network calls in processors unless latency, retries, and circuit-breaking are handled.

### ItemWriter

- Batch writes in chunks and use transaction-aware writers.
- Make writes idempotent (upsert, natural keys, dedup markers) where feasible.
- Handle partial failure semantics clearly (skip/retry/rollback) and log decisions.

## Transactions, Fault Tolerance, and Throughput

### Transactions

- Define transaction boundaries at step/chunk level with explicit transaction managers.
- Keep chunk size aligned with memory profile and downstream commit costs.
- Avoid long-running transactions; tune chunk size and flush strategy instead.

### Retry / Skip / Backoff

- Configure retries only for transient exceptions; avoid retrying permanent validation failures.
- Use skip policies with clear maximum skip thresholds and alerting.
- Add backoff for unstable downstream dependencies.

### Scaling

- Start with single-threaded correctness, then scale with:
  - Multi-threaded steps for CPU-bound work.
  - Partitioning for large independent datasets.
  - Remote chunking/partitioning for distributed execution.
- When parallelizing, verify thread safety of readers/writers and shared state.

## State, Metadata, and Repository

- Use a persistent `JobRepository` (RDBMS) in production.
- Never use in-memory repository for critical jobs beyond local development.
- Keep `ExecutionContext` small, serializable, and version-tolerant.
- Version schemas and migration scripts for batch metadata tables.

## Observability and Operations

### Logging

- Use structured, parameterized logs for job/step lifecycle events.
- Include correlation identifiers (job name, instance id, execution id, step name).
- Avoid per-record info logs in large jobs; aggregate metrics and sample debug logs.

### Metrics and Tracing

- Expose counts: read, processed, filtered, written, skipped, retried, failed.
- Emit step duration and throughput metrics.
- Integrate Micrometer/Actuator for dashboards and alerting.

### Listeners

- Use `JobExecutionListener` and `StepExecutionListener` for lifecycle hooks.
- Keep listener logic lightweight and non-blocking.
- Avoid business-critical writes in listeners unless idempotent and monitored.

## Data and File Handling

- For flat files, define explicit encoding, delimiter, quoting, and header/trailer strategy.
- Validate input schema early; quarantine malformed records with clear diagnostics.
- Archive and checksum input/output artifacts when auditability is required.

## Testing Strategy

- Unit test processors and mappers as plain components.
- Use Spring Batch test support for step/job integration tests.
- Cover restart behavior, retry/skip policies, and idempotency in tests.
- Use representative test datasets that include malformed and boundary records.

## Security and Compliance

- Avoid logging PII/secrets; mask sensitive fields.
- Externalize credentials and endpoint secrets through environment configuration.
- Apply least privilege for batch service accounts and storage/database access.

## Build and Verification

- After modifying batch logic, run a full test suite and at least one end-to-end job path.
- Validate both first-run and restart scenarios before considering changes complete.

## Useful Commands

| Gradle Command      | Maven Command     | Description |
|:--------------------|:------------------|:------------|
| `./gradlew test`    | `./mvnw test`     | Run automated tests. |
| `./gradlew build`   | `./mvnw package`  | Build and run all checks. |
| `./gradlew bootRun` | `./mvnw spring-boot:run` | Run app locally (including batch launcher endpoints if present). |
