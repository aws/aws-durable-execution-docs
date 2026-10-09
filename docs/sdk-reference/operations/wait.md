# Wait Operations

## Time-based durable waits

The Wait operation of the `DurableContext` pauses execution for a specified time without
consuming compute. The SDK will checkpoint the start of the wait operation, the function
suspends and Lambda exits, and then the backend automatically resumes execution when the
wait completes. The SDK will replay and resume processing from where it had paused for
the wait.

Unlike language-native sleep functions such as `setTimeout()`, `time.sleep()`, or
`Thread.sleep()`, durable wait operations do not consume Lambda execution time. The
durable function invocation exits cleanly after it checkpoints the start of the wait and
resumes later at the specified time, even if the wait lasts hours or days.

The minimum wait duration is 1 second. The maximum wait duration is the maximum
execution duration of 1 year. There is no cost associated with longer waits.

You cannot cancel a wait after it has started.

## When to use wait

Use `context.wait()` for a time-based delay. For example, use waits between
[steps](step.md) to delay the new next step in multi-step workflows.

### Wait for an event or status change

To wait for an event or status change, rather than just a fixed delay, consider these
alternatives:

- **Polling until a condition is met**
    - [Wait for Condition](wait-for-condition.md) handles the polling loop, state
        tracking, and backoff for you.
- **Waiting for an external system response**
    - [Callbacks](callback.md) suspend your durable function until an external system
        sends a response.

## Wait walkthrough

Here's a simple example of using a wait operation:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/core/wait/basic-wait.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/core/wait/basic-wait.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/core/wait/basic-wait.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/core/wait/basic-wait.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/core/wait/basic-wait.cs"
    ```

When this function runs:

1. The SDK checkpoints the wait operation with a scheduled end time
1. The Lambda function suspends
1. After 5 seconds, the backend automatically invokes your function again
1. Execution resumes after the wait and returns "Wait completed"

## Method signature

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/core/wait/wait-signature.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/core/wait/wait-signature.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/core/wait/wait-signature.java"
    ```

    Set `name` to `null` to omit it.

=== "Go"

    ```go
    --8<-- "examples/go/core/wait/wait-signature.go"
    ```

    Pass `""` to omit the name.

=== "C#"

    ```csharp
    --8<-- "examples/csharp/core/wait/wait-signature.cs"
    ```

    Omit `name` to infer one from the call site.

**Parameters:**

- `duration` (required) - How long to wait. Must be at least 1 second. See
    [Duration](#duration) for how to specify durations in each programming language.
- `name` (optional) - Only used for display, debugging and testing.

**Returns:**

=== "TypeScript"

    `DurablePromise<void>`

=== "Python"

    `None`

=== "Java"

    `Void` (sync)

    `DurableFuture<Void>` (async)

=== "Go"

    `error` (sync, `nil` on success)

    `*durable.Future[durable.Void]` (async)

=== "C#"

    `Task`

**Raises/Throws:**

=== "TypeScript"

    None

=== "Python"

    `ValidationError(DurableExecutionsError)`

=== "Java"

    `IllegalArgumentException`

=== "Go"

    `Wait` returns a plain `error` for a duration under one second, including
    zero and negative durations. It then records no operation and writes no
    checkpoint. `WaitAsync` returns a future that fails with the same error.
    The SDK rounds a duration of one second or more up to whole seconds. It does
    not check the one-year maximum.

=== "C#"

    `ArgumentOutOfRangeException`

## Duration

### Duration signature

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/core/wait/duration-signature.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/core/wait/duration-signature.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/core/wait/duration-signature.java"
    ```

=== "Go"

    The SDK uses the standard library `time.Duration`.

    ```go
    --8<-- "examples/go/core/wait/duration-signature.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/core/wait/duration-signature.cs"
    ```

### Duration usage

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/core/wait/duration-helpers.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/core/wait/duration-helpers.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/core/wait/duration-helpers.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/core/wait/duration-helpers.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/core/wait/duration-helpers.cs"
    ```

## Named wait operations

Name wait operations to make them easier to identify in logs and tests.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/core/wait/named-wait.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/core/wait/named-wait.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/core/wait/named-wait.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/core/wait/named-wait.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/core/wait/named-wait.cs"
    ```

## Scheduled end timestamp

Each wait operation has a scheduled end timestamp that indicates when it completes. This
timestamp uses Unix milliseconds.

The `ScheduledEndTimestamp` field is in the checkpoint's `WaitDetails`. The SDK
calculates the scheduled end time when it first checkpoints the wait operation:

```
{current time} + {wait duration} = {scheduled end timestamp}
```

Wait durations are approximate. The actual resume time depends on system scheduling,
Lambda cold start time, and current system load.

## Concurrency

Waits execute sequentially in the order they appear in your code. You can use waits
inside [`parallel`](parallel.md) or [`map`](map.md) operations. If a branch or iteration
of a parent is ready to suspend due to a wait, the durable function will wait for all
child operations of that parent to complete or suspend before terminating the
invocation.

You can run a wait concurrently with other operations. This is
useful for enforcing a minimum duration — for example, ensuring at least 5 seconds pass
while a step runs in parallel.

=== "TypeScript"

    Don't `await` the wait immediately — use `Promise.all` to run it alongside other
    operations.

    ```typescript
    --8<-- "examples/typescript/core/wait/async-wait.ts"
    ```

=== "Python"

    Python waits are synchronous only. Use [`parallel`](parallel.md) or [`map`](map.md) for
    concurrency.

=== "Java"

    Use `waitAsync()` which returns a `DurableFuture<Void>`, then call `.get()` when you
    need to block.

    ```java
    --8<-- "examples/java/core/wait/async-wait.java"
    ```

=== "Go"

    Await the futures with `durable.Join`, not with one `Result` call after
    another. A sequence of `Result` calls that returns on the first error leaves
    the other futures unawaited. Their progress is then not checkpointed when the
    invocation suspends. `Join` runs in a child context, so it records one more
    operation.

    ```go
    --8<-- "examples/go/core/wait/async-wait.go"
    ```

=== "C#"

    Don't `await` the wait immediately. Capture the `Task` and use `Task.WhenAll` to run
    it alongside other operations.

    ```csharp
    --8<-- "examples/csharp/core/wait/async-wait.cs"
    ```

## Testing

You can verify wait operations in your tests by inspecting the operations list:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/core/wait/test-multiple-waits.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/core/wait/test-multiple-waits.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/core/wait/test-multiple-waits.java"
    ```

=== "Go"

    Save this as `wait_test.go`. It uses the `durabletest` in-memory runner.

    ```go
    --8<-- "examples/go/core/wait/test-multiple-waits.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/core/wait/test-multiple-waits.cs"
    ```

## See also

- [Steps](step.md) - Execute business logic with automatic checkpointing
- [Wait for Condition](wait-for-condition.md) - Poll until a condition is met
- [Callbacks](callback.md) - Wait for external system responses
- [Getting Started](../../getting-started/index.md) - Learn the basics of durable functions

!!! info "Checkpoint consumption"

    Durable operations consume checkpoints. To understand how this operation affects
    your checkpoint usage, see
    [Checkpoint consumption](https://docs.aws.amazon.com/lambda/latest/dg/durable-execution-sdk.html#durable-operations-checkpoint-consumption).
