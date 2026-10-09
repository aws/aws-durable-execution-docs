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

=== "Go"

    The name is the second argument, after the context. A name derived from the input is
    deterministic. A name built from `time.Now()` breaks replay identity. When you run
    [durablelint](../../sdk-reference/languages/go/index.md#static-analysis-with-durablelint),
    the SDK's static analysis tool, its `durablenondeterminism` rule reports the
    `time.Now()` call.

    ```go
    --8<-- "examples/go/patterns/step-design/step-names-wrong.go"
    ```

    ```go
    --8<-- "examples/go/patterns/step-design/step-names.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/patterns/step-design/step-names.cs"
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

=== "Go"

    Give each side effect its own step so each one checkpoints independently. A
    single step that performs all three repeats all three when it retries.

    ```go
    --8<-- "examples/go/patterns/step-design/one-thing-per-step-wrong.go"
    ```

    ```go
    --8<-- "examples/go/patterns/step-design/one-thing-per-step.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/patterns/step-design/one-thing-per-step.cs"
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

=== "Go"

    Define the function once, free of SDK types, and wrap it in a `durable.Step` closure
    wherever you reuse it.

    ```go
    --8<-- "examples/go/patterns/step-design/reusable-step.go"
    ```

=== "C#"

    Define a method returning `Task<T>` and reference it as the step body.

    ```csharp
    --8<-- "examples/csharp/patterns/step-design/reusable-step.cs"
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

=== "Go"

    A step body receives a `durable.StepContext`, which cannot start an operation. A
    body that captures the outer `durable.Context` and starts an operation on it
    compiles. At run time that operation fails with `durable.ErrWrongContext` and
    records nothing. When you run
    [durablelint](../../sdk-reference/languages/go/index.md#static-analysis-with-durablelint),
    its `durablenestedop` rule reports the operation. Group operations with
    `durable.RunInChildContext`.

    ```go
    --8<-- "examples/go/patterns/step-design/step-boundary-wrong.go"
    ```

    ```go
    --8<-- "examples/go/patterns/step-design/step-boundary.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/patterns/step-design/step-boundary.cs"
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

=== "Go"

    List the retryable errors in `RetryConfig.RetryableErrors` as matchers.
    `durable.ErrorAs` matches an error type under `errors.As`, and
    `durable.ErrorIs` matches a sentinel value under `errors.Is`. A step fails
    on the first attempt whose error no matcher accepts.

    ```go
    --8<-- "examples/go/patterns/step-design/handle-errors-in-step.go"
    ```

=== "C#"

    List the retryable exception types in the retry strategy configuration.

    ```csharp
    --8<-- "examples/csharp/patterns/step-design/handle-errors-in-step.cs"
    ```

!!! warning

    Swallowing an exception inside a step hides the failure from the retry strategy and the
    caller. Let the error propagate and configure the retry strategy to decide.

## See also

- [Idempotency and retries](idempotency.md) Retry interactions with side effects.
- [Code organization](code-organization.md) Child contexts, grouping.
- [Step operation reference](../../sdk-reference/operations/step.md)
- [Errors and retries](../../sdk-reference/error-handling/errors.md)
