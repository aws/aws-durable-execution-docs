# Determinism during replay

The Durable Execution SDK checkpoints your code so that it can terminate the current
invocation and not consume compute while it waits for a timed duration or processing
result to be ready. The AWS Lambda backend re-invokes the function when it is ready to
resume processing.

Durable functions run your handler from the top on every invocation. A step that
completed in an earlier invocation returns its checkpointed result on replay without
re-executing the code inside the step. Anything outside a step or other durable
operation runs every time the function replays.

For replay to follow the same path, the code that runs every time has to produce the
same values. That is determinism. Non-deterministic code inside your handler body can
send control flow down a different branch on replay, so a downstream step runs with the
wrong inputs.

## Handler code must be deterministic

Any code that is not inside a durable operation must be a pure function of the handler
inputs and the results of completed operations. Anything that depends on wall-clock
time, a random source, an external service, the local file system, or mutable global
state is non-deterministic and must run inside a durable operation.

Concrete examples of code that is not deterministic:

- **Time and identity** `Date.now()`, `time.time()`, `Instant.now()`, `UUID` generation,
    or anything that returns a different value each call.
- **External I/O** HTTP calls, database reads, AWS SDK calls, reading files.
- **Random numbers** `Math.random()`, `random.random()`, `Random`.

## Non-deterministic code must be in a durable operation

A step checkpoints its return value. On replay the step returns the checkpointed value
instead of running the underlying code. Wrapping a non-deterministic call in a step
means the value will always be the result of the first successful completion of that
code.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/determinism/non-deterministic-in-step.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/determinism/non-deterministic-in-step.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/determinism/non-deterministic-in-step.java"
    ```

=== "Go"

    When you run
    [durablelint](../../sdk-reference/languages/go/index.md#static-analysis-with-durablelint),
    the SDK's static analysis tool, its `durablenondeterminism` rule reports a
    non-deterministic call outside a step, such as `time.Now` or a read from `math/rand`
    or `crypto/rand`. Wrapping the call in a step checkpoints the value and silences the
    rule.

    ```go
    --8<-- "examples/go/patterns/determinism/non-deterministic-in-step.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/patterns/determinism/non-deterministic-in-step.cs"
    ```

Because the SDK checkpoints the result of `generate-transaction-id`, every replay sees
the same `transactionId` and the charge step receives the same argument. Without the
wrapper, `UUID.randomUUID()` would produce a new value on every replay and the
downstream step would either double-charge or hit an idempotency error from the payment
service.

!!! tip

    Wrap every non-deterministic call inside a step.

## Pass data through return values, not closures

Steps checkpoint their return value. On replay the SDK returns the cached value in
place of running the step body. Replay discards any state a step body writes to
variables outside itself through a closure, because the write happens inside the body
and the body does not run. The first invocation succeeds. Replay leaves the outer state
at its initial value.

Return the value from the step. Read the return value in the handler and update outer
state there.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/determinism/return-value-passing.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/determinism/return-value-passing.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/determinism/return-value-passing.java"
    ```

=== "Go"

    When you run
    [durablelint](../../sdk-reference/languages/go/index.md#static-analysis-with-durablelint),
    its `durableclosure` rule reports a write to a variable captured from outside the
    step body. Replay skips the body, so the write never happens again. Return the value
    instead.

    ```go
    --8<-- "examples/go/patterns/determinism/return-value-passing-wrong.go"
    ```

    ```go
    --8<-- "examples/go/patterns/determinism/return-value-passing.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/patterns/determinism/return-value-passing.cs"
    ```

For processing a list of independent items,
[map](/durable-execution/sdk-reference/operations/map/) is a simpler choice than an
explicit loop. It runs the per-item operation in parallel, checkpoints each result, and
returns a `BatchResult` you can reduce. Use an explicit loop when items depend on each
other, such as a running total or chained transformations. Keep the loop deterministic.
Each step must produce the same result on replay.

!!! danger

    Side effects inside a step body that write to outer state do not re-run on replay.
    The first invocation looks correct because the body runs and the write lands. Replay
    returns the cached result and skips the body, so the outer state stays at its initial
    value.

## Keep branches stable across replay

Control flow decisions made outside steps must depend only on deterministic inputs. If
an `if` or `switch` depends on something non-deterministic, replay can walk a different
branch and attempt to return results from operations that never ran. Wrap the
non-deterministic decision into a step and branch on the step's return value.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/determinism/stable-branches.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/determinism/stable-branches.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/determinism/stable-branches.java"
    ```

=== "Go"

    When you run
    [durablelint](../../sdk-reference/languages/go/index.md#static-analysis-with-durablelint),
    its `durablenondeterminism` rule reports a branch on `time.Now()` outside a step.
    Capture the decision in a step, or read `durable.ExecutionStartTime(ctx)` when a
    stable timestamp is all you need.

    ```go
    --8<-- "examples/go/patterns/determinism/stable-branches-wrong.go"
    ```

    ```go
    --8<-- "examples/go/patterns/determinism/stable-branches.go"
    ```

    Map iteration and the `go` statement are two more sources of non-determinism. When
    you run
    [durablelint](../../sdk-reference/languages/go/index.md#static-analysis-with-durablelint),
    its `durablenondeterminism` rule reports a range over a map that starts durable
    operations, because map iteration order is randomized. Sort the keys and range over
    the slice. The `durablegoroutine` rule reports a durable operation inside a `go`
    statement. At run time that call fails with `durable.ErrWrongGoroutine` and records
    nothing. Use `durable.Go`, which gives the goroutine its own context.

    A `//durable:ignore` comment suppresses a durablelint diagnostic. The
    `durablenocheck` build tag is a separate mechanism. It compiles out the run-time
    goroutine and context checks. See [Goroutines and
    `durable.Go`](../../sdk-reference/languages/go/index.md#goroutines-and-durablego).

=== "C#"

    ```csharp
    --8<-- "examples/csharp/patterns/determinism/stable-branches.cs"
    ```

The same rule applies to reading from external services. Fetching a flag from a database
outside a step risks replaying against a changed value. Fetch inside a step, branch on
the returned value.

!!! warning

    Feature flags, environment variables read at runtime, and configuration pulled from a
    remote store could all change between the first invocation and a replay. Capture the
    value inside a step so the evaluation criteria are stable.

## See also

- [Idempotency and retries](idempotency.md) How retry behavior interacts with
    determinism.
- [Step design](step-design.md) Naming, granularity, and the step boundary.
- [Step operation reference](../../sdk-reference/operations/step.md)
