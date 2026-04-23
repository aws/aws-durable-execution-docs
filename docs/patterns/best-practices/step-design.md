# Step design

A step is the smallest unit of durability. It runs your code, checkpoints the return
value, and retrieves that value for every subsequent replay. How you divide work into
steps influences how many checkpoints your execution has, how retries behave and how you
read the workflow history in logs and errors.

## Name steps meaningfully

The step name shows up in checkpoints, execution history, CloudWatch logs, and errors.
Names like `step1`, `process-data`, or `do-stuff` make failures hard to triage. Prefer
names like `validate-order`, `charge-payment`, and `notify-customer` that describe the
encapsulated logic.

Keep names static. Names are part of the deterministic identity of the step. Including a
timestamp or a random ID in the name breaks replay because the same step resolves to a
different name on subsequent invocations.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/step-design/step-names.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/step-design/step-names.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/step-design/step-names.java"
    ```

!!! warning

    A step name with a timestamp or random value resolves to a different name on replay.
    Keep names static.

## Single responsibility

Operations that must succeed or fail together belong in the same step. Split unrelated
operations into separate steps so each one has a single logical intent. Keep one
external API call per step when that call has side effects.

A step that batches three unrelated side effects reruns all three on retry. If the
second call fails, the first runs again.

Related reads against the same resource can batch into one step because they are safe to
re-run.

!!! info

    Pure computation rarely benefits from its own step. Deriving a value from data that is
    already in memory does not need durability, it does not need retries, and each extra
    step is an unnecessary checkpoint.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/step-design/one-thing-per-step.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/step-design/one-thing-per-step.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/step-design/one-thing-per-step.java"
    ```

## Reuse step logic

Define a reusable step function once and reference it repeatedly from the workflow.

=== "TypeScript"

    Wrap the core logic in a named function, pass it to `context.step`.

    ```typescript
    --8<-- "examples/typescript/patterns/step-design/reusable-step.ts"
    ```

=== "Python"

    `@durable_step` wraps a callable so that calling it with arguments returns a
    `(StepContext) -> T` that `context.step` can run.

    ```python
    --8<-- "examples/python/patterns/step-design/reusable-step.py"
    ```

=== "Java"

    Define a method and reference it with a lambda.

    ```java
    --8<-- "examples/java/patterns/step-design/reusable-step.java"
    ```

## Step nesting

A step receives a `StepContext`, not the full `DurableContext`. A step is the atomic
unit the SDK checkpoints. You cannot call other durable operations such as `step` or
`wait` inside another step. If you need to group several durable operations, use
`runInChildContext` as described in [Code organization](code-organization.md) instead.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/step-design/step-boundary.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/step-design/step-boundary.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/step-design/step-boundary.java"
    ```

## Handle errors explicitly

Code inside a step runs under the step's retry strategy. An unhandled error triggers the
strategy's decision function. A returned value checkpoints the result.

Let errors propagate and use the retry strategy's configuration to decide which error
types are retryable. Retry transient failures such as network timeouts, rate limits and
503s with backoff. Fail the step immediately for permanent failures such as invalid
input, 404s and authentication errors.

Match the retry strategy to the work. Fast idempotent calls get tight retries, meaning a
handful of attempts with only a few seconds of backoff. Long-running calls to third
parties get wide retries (many attempts, minutes of backoff). See
[Retries](../../sdk-reference/error-handling/retries.md) for presets and configuration
options.

=== "TypeScript"

    List the retryable error classes in the retry strategy configuration.

    ```typescript
    --8<-- "examples/typescript/patterns/step-design/handle-errors-in-step.ts"
    ```

=== "Python"

    List the retryable error classes in the retry strategy configuration.

    ```python
    --8<-- "examples/python/patterns/step-design/handle-errors-in-step.py"
    ```

=== "Java"

    Write a `RetryStrategy` lambda that checks the error type before delegating to a preset
    for the delay decision.

    ```java
    --8<-- "examples/java/patterns/step-design/handle-errors-in-step.java"
    ```

!!! warning

    Swallowing an exception inside a step hides the failure from the retry strategy and the
    caller. Let the error propagate and configure the retry strategy to decide.

## See also

- [Idempotency and retries](idempotency.md) Retry interactions with side effects.
- [Code organization](code-organization.md) Child contexts, grouping.
- [Step operation reference](../../sdk-reference/operations/step.md)
- [Errors and retries](../../sdk-reference/error-handling/errors.md)
