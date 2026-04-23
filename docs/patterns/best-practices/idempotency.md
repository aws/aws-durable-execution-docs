# Idempotency and retries

Replay and retry can each run the same operation more than once. An operation with a
side effect repeats the side effect on every run. Idempotency describes operations where
the effect remains the same regardless of how many times they run.

## At-most-once vs at-least-once

A step has an execution semantic that controls what happens when an attempt fails to
complete, for example when the Lambda sandbox dies, the network drops or the runtime
hits the invocation timeout mid-invocation.

- **At-least-once per retry (default).** The SDK initiates a `START` checkpoint and then
    immediately runs the code inside the step without waiting for the checkpoint
    response. If the attempt does not complete due to an interruption, the step runs
    again on replay. This is safe only for idempotent operations.
- **At-most-once per retry.** The SDK waits for confirmation that the `START` checkpoint
    committed before running the code inside the step. If the attempt does not complete
    due to an interruption, the SDK marks the step as interrupted on replay and raises a
    `StepInterrupted` error instead of re-executing.

Both semantics are per retry attempt. Neither guarantees the step runs exactly once
across the entire workflow. A retry strategy that retries on failure will run the step
again even under at-most-once per retry. To limit a step to a single execution attempt
end-to-end, combine at-most-once with a no-retry strategy.

!!! tip

    At-least-once is the default. Any interruption re-runs the step on replay, so the code
    inside must be safe to run more than once.

## Match the semantic with the side effect

- **At-least-once** is for idempotent operations, such as reads that do not mutate,
    writes to an upsert-capable store, calls that accept an idempotency key and anything
    safe to re-run.
- **At-most-once** is for operations with external side effects such as charging a
    payment card, sending a one-shot SMS or a POST to a non-idempotent API.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/idempotency/choose-semantics.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/idempotency/choose-semantics.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/idempotency/choose-semantics.java"
    ```

!!! warning

    At-most-once applies per attempt, not per workflow. Combine it with a no-retry strategy
    to guarantee the step runs exactly once end-to-end.

## Idempotency tokens

For external services that support an idempotency key, such as most modern payment APIs,
generate a key inside a step once and pass it to every attempt of the side-effecting
step. The external service deduplicates repeated requests with the same key, so even
at-least-once retries are safe.

!!! warning

    Generate the key *inside* a step. A key generated outside a step changes on replay,
    which defeats deduplication and doubles up on retry.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/idempotency/idempotency-tokens.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/idempotency/idempotency-tokens.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/idempotency/idempotency-tokens.java"
    ```

The same pattern applies to any operation that writes to an external store. Include the
key in the write and rely on the store's deduplication.

!!! tip

    When an idempotency-enabled API returns a duplicate-request error on retry, it usually
    means the first attempt already succeeded. Handle that error as success, not failure.

## Database retry patterns

When you own the database, you can make writes idempotent without tokens.

- **Conditional writes.** DynamoDB `PutItem` with `attribute_not_exists`, SQL
    `INSERT ... ON CONFLICT DO NOTHING`, or upserts keyed by a stable ID.
- **Check then write in a single transaction.** Use a transaction that reads the current
    state and writes the new state atomically.
- **Append-only logs.** Write events with a deterministic event ID. Readers deduplicate
    on ID.

Avoid `INSERT` without a uniqueness constraint, and avoid counter increments without a
conditional check if a retry could apply them twice.

## See also

- [Determinism and replay](determinism.md) Why operations can run more than once to
    begin with.
- [Step design](step-design.md) Retry strategy and the boundary between steps.
- [Errors and retries](../../sdk-reference/error-handling/errors.md)
