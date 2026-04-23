# Pause and resume

Durable functions can pause for seconds, hours, or days without keeping a Lambda
invocation running. The SDK suspends execution, checkpoints the wait, and resumes the
handler when the wait condition is met. This turns traditional polling loops and
long-timeout wait-for-reply code into structured, cost-free pauses.

The durable wait operations cover delayed-work scenarios:

- [wait](../../sdk-reference/operations/wait.md) for a fixed duration.
- [waitForCallback](../../sdk-reference/operations/callback.md) for an external system
    signalling completion.
- [waitForCondition](../../sdk-reference/operations/wait-for-condition.md) for polling a
    check function on a schedule.

## Prefer `wait` over `sleep`

Do not call language-specific methods like `setTimeout`, `time.sleep`, or `Thread.sleep`
to pause a durable function. Those keep the invocation running and reset to zero on
replay. The durable waits suspend the execution and do not incur compute cost while
suspended.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/pause-resume/wait-vs-sleep.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/pause-resume/wait-vs-sleep.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/pause-resume/wait-vs-sleep.java"
    ```

!!! tip

    Name every wait. Named waits show up in the operation history and CloudWatch, which
    keeps timelines legible in logs and test assertions.

## Always set a callback timeout

`waitForCallback` suspends the execution until an external system calls the SDK's
callback success or failure endpoints. Without a timeout the execution waits
up to the execution timeout, holding the resource slot until an operator intervenes.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/pause-resume/callback-timeout.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/pause-resume/callback-timeout.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/pause-resume/callback-timeout.java"
    ```

!!! danger

    Always set a timeout on `waitForCallback` to avoid stalled executions.

When the timeout elapses, the SDK raises a callback timeout error. Either let it
propagate to mark the step and the execution as failed, or catch it and handle it with
compensatory actions.

## Use heartbeats for long external operations

A heartbeat timeout fails the callback if the external system stops checking in, even if
the overall timeout has not elapsed.

The external system has to call the SDK's heartbeat endpoint periodically while the work
is in progress.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/pause-resume/heartbeat-timeout.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/pause-resume/heartbeat-timeout.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/pause-resume/heartbeat-timeout.java"
    ```

!!! warning

    A 24-hour timeout means a 24-hour outage when the external worker crashes. Set a
    heartbeat timeout comfortably longer than the expected interval between heartbeats, but
    shorter than the overall operation timeout.

## Poll external services with `waitForCondition`

Use `waitForCondition` for external systems where you have to poll rather than create a
callback.

The SDK runs your check function, applies a wait strategy between polls, and resumes
when the check signals completion. Each poll is a step. The wait between polls suspends
the execution.

Use an exponential backoff wait strategy so an unresponsive downstream system does not
create a retry storm.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/pause-resume/wait-for-condition.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/pause-resume/wait-for-condition.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/pause-resume/wait-for-condition.java"
    ```

!!! tip

    Durable waits round up to a minimum of one second. Don't use wait for condition to poll
    for sub-second low-latency state changes.

## See also

- [Wait operation](../../sdk-reference/operations/wait.md)
- [Wait-for-callback](../../sdk-reference/operations/callback.md)
- [Wait-for-condition](../../sdk-reference/operations/wait-for-condition.md)
- [Testing](../../testing/index.md)
