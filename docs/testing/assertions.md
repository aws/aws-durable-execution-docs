# Assertions

After `run()` completes, the test result contains the full operation history. You can
look up operations by name, filter by type, or walk the history to assert on what ran
and how it ran.

## Assert on a step

Look up a step by name and check its status and result.

=== "TypeScript"

    Use `runner.getOperation(name)` to get a handle to the operation, then call
    `getStepDetails()` to access the result.

    ```typescript
    --8<-- "examples/typescript/testing/assertions/assert-step.ts"
    ```

=== "Python"

    Use `result.get_step(name)` to get the `StepOperation`. The `result` attribute holds the
    raw serialized payload.

    ```python
    --8<-- "examples/python/testing/assertions/assert-step.py"
    ```

=== "Java"

    Use `result.getOperation(name)` to get the `TestOperation`, then call
    `getStepResult(Class<T>)` to deserialize the result.

    ```java
    --8<-- "examples/java/testing/assertions/assert-step.java"
    ```

## Assert on a wait

Look up a wait operation and check that it was scheduled with the expected duration.

=== "TypeScript"

    `getWaitDetails()` returns `waitSeconds` and `scheduledEndTimestamp`.

    ```typescript
    --8<-- "examples/typescript/testing/assertions/assert-wait.ts"
    ```

=== "Python"

    Use `result.get_wait(name)`. The `scheduled_end_timestamp` attribute is a `datetime`
    when the wait was scheduled to end.

    ```python
    --8<-- "examples/python/testing/assertions/assert-wait.py"
    ```

=== "Java"

    `getWaitDetails().scheduledEndTimestamp()` returns the scheduled end time as an
    `Instant`.

    ```java
    --8<-- "examples/java/testing/assertions/assert-wait.java"
    ```

## Assert on a callback

For callbacks, the test drives the execution to the point where the callback is waiting,
then completes it from the test.

=== "TypeScript"

    Use `waitForData(WaitingOperationStatus.SUBMITTED)` to wait until the callback submitter
    has run, then call `sendCallbackSuccess()` to complete it.

    ```typescript
    --8<-- "examples/typescript/testing/assertions/assert-callback.ts"
    ```

=== "Python"

    Use `run_async()` to start the execution, `wait_for_callback()` to get the callback ID,
    `send_callback_success()` to complete it, then `wait_for_result()` to get the final
    result. All calls must happen inside the `with runner:` block.

    ```python
    --8<-- "examples/python/testing/assertions/assert-callback.py"
    ```

=== "Java"

    Use `run()` to reach the PENDING state, `getCallbackId()` to get the callback ID,
    `completeCallback()` to complete it, then `run()` again to finish the execution.

    ```java
    --8<-- "examples/java/testing/assertions/assert-callback.java"
    ```

## Assert on a child context

Child contexts appear as `CONTEXT` operations in the result. You can walk their child
operations to assert on what ran inside the context.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing/assertions/assert-child-context.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing/assertions/assert-child-context.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing/assertions/assert-child-context.java"
    ```

## Filter operations by status

When a step retries, the operation history contains one entry per attempt. Filter by
status to count failures and successes separately.

=== "TypeScript"

    Pass `{ status: OperationStatus.FAILED }` to `result.getOperations()` to filter.

    ```typescript
    --8<-- "examples/typescript/testing/assertions/filter-by-status.ts"
    ```

=== "Python"

    Use `result.get_all_operations()` to get a flat list including nested operations, then
    filter by `op.status`.

    ```python
    --8<-- "examples/python/testing/assertions/filter-by-status.py"
    ```

=== "Java"

    `result.getFailedOperations()` and `result.getSucceededOperations()` return pre-filtered
    lists.

    ```java
    --8<-- "examples/java/testing/assertions/filter-by-status.java"
    ```

## See also

- [Authoring](authoring.md) Set up the test runner and write your first test.
- [Cloud Runner](cloud-runner.md) Run tests against a deployed Lambda function.
- [Step](../sdk-reference/operations/step.md)
- [Wait](../sdk-reference/operations/wait.md)
