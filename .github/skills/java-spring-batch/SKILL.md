---
name: java-spring-batch
description: 'Get best practices for designing and implementing Spring Batch jobs, including chunk/tasklet steps, restartability, fault tolerance, partitioning, and production-grade observability.'
---

# Spring Batch Best Practices

Your goal is to help build reliable Spring Batch pipelines that are restartable, idempotent, observable, and easy to operate in production.

## When to Use This Skill

- Building new Spring Batch jobs and step flows
- Refactoring existing batch pipelines for reliability or performance
- Implementing chunk processing (`ItemReader`, `ItemProcessor`, `ItemWriter`)
- Defining retry/skip/restart behavior
- Adding partitioning or multithreaded step execution
- Troubleshooting production batch failures or duplicate processing

## Core Principles

- Design for restartability first, throughput second.
- Keep item processing deterministic and side effects controlled.
- Use explicit transaction boundaries and fault-tolerance policies.
- Prefer observability (metrics/logs/listeners) over ad-hoc debugging.

## Job and Step Design

### JobParameters and JobInstance Behavior

- Define identifying parameters deliberately to avoid accidental duplicate job instances.
- Separate operational metadata (for example, trigger time) from identifying parameters.
- Validate parameters at startup and fail fast with actionable messages.

### Step Type Selection

- Use chunk-oriented steps for record pipelines with read-process-write semantics.
- Use tasklet steps for orchestration, setup/cleanup, or one-off control actions.
- Keep each step focused on one concern; split complex workflows into multiple steps.

### Flow Control

- Use conditional transitions intentionally and document non-happy paths.
- Keep flows explicit for failure/recovery branches.

## Reader / Processor / Writer

### Reader

- Prefer streaming or paging readers for large datasets.
- Set fetch/page sizes based on data shape and memory constraints.
- Guarantee deterministic ordering to support restart correctness.

### Processor

- Keep processors stateless and deterministic.
- Return `null` only for intentional business filtering.
- Avoid I/O-heavy or remote calls in processors unless latency and retries are controlled.

### Writer

- Batch writes and keep writer operations transaction-aware.
- Implement idempotent writes where possible (upsert, key-based deduplication).
- Make failure semantics explicit (rollback vs skip) and test both.

## Fault Tolerance and Transactions

### Chunk and Transaction Tuning

- Choose chunk size based on commit overhead, memory usage, and downstream constraints.
- Avoid very long transactions; tune chunk size and flushing strategy.

### Retry / Skip Policies

- Retry transient exceptions only (network hiccups, lock timeouts).
- Skip malformed records only with bounded thresholds and clear reporting.
- Add backoff when retrying unstable dependencies.

### Restart Semantics

- Persist required progress state in `ExecutionContext`.
- Keep context small and serializable.
- Verify behavior after simulated mid-step failure and restart.

## Scaling Patterns

- Start single-threaded to prove correctness.
- Scale with multi-threaded steps for CPU-bound stages.
- Use partitioning for independent data partitions.
- Consider remote partitioning/chunking for distributed execution.
- Validate thread safety of shared components before enabling concurrency.

## Repository and Metadata

- Use a persistent RDBMS-backed `JobRepository` in production.
- Do not rely on in-memory metadata for real workloads.
- Keep metadata schema migration managed with the rest of the application.

## Observability

- Log job/step start, completion, and failure with execution ids.
- Emit metrics for read/process/write counts, retries, skips, and durations.
- Use listeners (`JobExecutionListener`, `StepExecutionListener`) for lifecycle instrumentation.
- Avoid record-level info logs in high-volume jobs.

## Testing Strategy

- Unit test processors, mappers, and validators as plain classes.
- Use Spring Batch test support for step/job integration tests.
- Add tests for restartability, idempotency, retry, and skip rules.
- Include malformed and edge-case inputs in test fixtures.

## Common Pitfalls

- Using non-deterministic item ordering with restarts enabled
- Putting side effects in processors
- Treating all exceptions as retryable
- Unbounded skip settings with no alerts
- In-memory `JobRepository` in production

## Practical Checklist

1. Parameters validated and identity rules documented
2. Reader/writer are restart-safe and idempotent
3. Chunk size and transaction boundaries tuned
4. Retry/skip/backoff policies configured and tested
5. Metrics/logging/listeners emit operational visibility
6. Failure + restart paths tested with representative data
