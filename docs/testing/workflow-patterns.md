# Workflow patterns

Complete tests for the workflow shapes you'll hit most often: sequential steps, child
contexts, parallel branches, partial failures, long waits, and polling. Each section
shows a handler and the test that exercises it. For the assertion vocabulary used inside
each test, see [Assertions](assertions.md).

## Sequential steps

A sequential workflow runs steps one after another, passing results between them. Verify
that all steps ran and that the final result reflects the full chain.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing/examples/sequential-workflow.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing/examples/sequential-workflow.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing/examples/sequential-workflow.java"
    ```

## Child contexts

Child contexts group operations under a named scope. The test result exposes the child
context as a `CONTEXT` operation. You can inspect its type and walk its child
operations.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing/examples/child-context.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing/examples/child-context.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing/examples/child-context.java"
    ```

## Parallel operations

Parallel branches execute concurrently. Assert on the final result to verify all
branches completed.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing/examples/parallel-workflow.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing/examples/parallel-workflow.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing/examples/parallel-workflow.java"
    ```

## Partial failures

When a step fails after earlier steps have succeeded, the execution fails but the
completed steps remain in the operation history. Assert on the status of individual
steps to verify which ones ran before the failure.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing/examples/partial-failures.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing/examples/partial-failures.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing/examples/partial-failures.java"
    ```

## Long waits

Workflows with long waits (hours or days) would make tests impractical without time
skipping. Each SDK handles this differently.

=== "TypeScript"

    Pass `{ skipTime: true }` to `setupTestEnvironment()` and the runner advances fake
    timers automatically. Waits complete instantly.

    ```typescript
    --8<-- "examples/typescript/testing/examples/long-waits.ts"
    ```

=== "Python"

    Set the `DURABLE_EXECUTION_TIME_SCALE` environment variable to scale `context.wait()`
    durations. Set it to `0.0` for instant waits, or to a small fraction such as `0.01` to
    run waits at 100x speed. The scale does not apply to step retry delays.

    ```python
    --8<-- "examples/python/testing/examples/long-waits.py"
    ```

=== "Java"

    `runUntilComplete()` calls `advanceTime()` automatically, which immediately completes
    STARTED waits without waiting for real time.

    ```java
    --8<-- "examples/java/testing/examples/long-waits.java"
    ```

## Polling with waitForCondition

`waitForCondition` polls a check function until it signals done. The test runner drives
the polling loop the same way it drives retries.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing/examples/polling.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing/examples/polling.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing/examples/polling.java"
    ```

## See also

- [Authoring](authoring.md) Set up the test runner and write your first test.
- [Assertions](assertions.md) Inspect individual operations after a test run.
- [Parallel](../sdk-reference/operations/parallel.md)
- [Child Context](../sdk-reference/operations/child-context.md)
- [Wait for Condition](../sdk-reference/operations/wait-for-condition.md)
