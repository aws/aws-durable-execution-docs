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
[steps](steps.md) to delay the new next step in multi-step workflows.

### Wait for an event or status change

To wait for an event or status change, rather than just a fixed delay, consider these
alternatives:

- **Polling until a condition is met**
    - [Wait for Condition](wait-for-condition.md) handles the polling loop, state
        tracking, and backoff for you.
- **Waiting for an external system response**
    - [Callbacks](callbacks.md) suspend your durable function until an external system
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

**Raises/Throws:**

=== "TypeScript"

    None

=== "Python"

    `ValidationError(DurableExecutionsError)`

=== "Java"

    `IllegalArgumentException`

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

In TypeScript and Java, you can run a wait concurrently with other operations. This is
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

## See also

- [Steps](steps.md) - Execute business logic with automatic checkpointing
- [Wait for Condition](wait-for-condition.md) - Poll until a condition is met
- [Callbacks](callbacks.md) - Wait for external system responses
- [Getting Started](../getting-started.md) - Learn the basics of durable functions
