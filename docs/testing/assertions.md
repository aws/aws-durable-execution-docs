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

=== "Go"

    Look up the step with `result.Operation(name)`, read `Status`, and
    deserialize the result with `durabletest.OperationResultAs[T]`.

    ```go
    --8<-- "examples/go/testing/assertions/assert-step.go"
    ```

=== "Rust"

    The local runner, `durable::test_util::LocalRunner`, returns a `TestResult` when the
    run completes. Use `result.operation(name)` to get the first operation with that
    name. `result_json::<T>()` decodes the step's recorded result from JSON into `T`,
    and returns `None` when the operation recorded no result.

    ```rust
    --8<-- "examples/rust/testing/assertions/assert-step.rs"
    ```

=== "C#"

    Use `result.GetStep(name)` to get the `TestStep`, then call `GetResult<T>()` to
    deserialize the result.

    ```csharp
    --8<-- "examples/csharp/testing/assertions/assert-step.cs"
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

=== "Go"

    `op.WaitDetails` carries `WaitSeconds` and `ScheduledEndTimestamp`.

    ```go
    --8<-- "examples/go/testing/assertions/assert-wait.go"
    ```

=== "Rust"

    Match `op.details()` against `TestOperationDetails::Wait` to read the `WaitDetails`.
    `duration()` returns the recorded duration in whole seconds, and
    `scheduled_end_time()` returns the time the wait was scheduled to end as a
    `SystemTime`. The runner's virtual clock completes the wait immediately.

    ```rust
    --8<-- "examples/rust/testing/assertions/assert-wait.rs"
    ```

=== "C#"

    `GetWaitEndsAt()` returns the scheduled end time as a `DateTimeOffset?`. With the
    default `SkipTime` on, the wait completes immediately.

    ```csharp
    --8<-- "examples/csharp/testing/assertions/assert-wait.cs"
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

=== "Go"

    Run to `PENDING`, read the callback ID from `OpenCallbacks()`, complete it
    with `SendCallbackSuccess()`, then run again to finish. `SendCallbackSuccess`
    marshals its payload to JSON. The default callback deserializer, `RawSerdes`,
    returns those bytes unchanged, so the handler receives the raw JSON string and
    the test parses it. To decode the payload into a Go value at the callback,
    pass `durable.WithCallbackSerdes(durable.JSONSerdes)` instead.

    ```go
    --8<-- "examples/go/testing/assertions/assert-callback.go"
    ```

=== "Rust"

    Take a handle with `run.operation("approval")` before you await the run.
    `wait_for_data(WaitingStatus::Submitted)` returns once the submitter has run, and
    `send_callback(CallbackReply::success(&value)?)` completes the callback of that
    `wait_for_callback`. Then await the run to get the result. `CallbackReply::success`
    encodes the value as JSON, and the handler decodes the payload as JSON into a
    `serde_json::Value`.

    ```rust
    --8<-- "examples/rust/testing/assertions/assert-callback.rs"
    ```

=== "C#"

    Use `StartAsync()` to drive the workflow to the callback, `WaitForCallbackAsync()` to
    get the callback ID, `SendCallbackSuccessAsync()` to complete it, then
    `WaitForResultAsync()` to finish the execution.

    ```csharp
    --8<-- "examples/csharp/testing/assertions/assert-callback.cs"
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

=== "Go"

    A child context is a `CONTEXT` operation. Match its children by `ParentID`.

    ```go
    --8<-- "examples/go/testing/assertions/assert-child-context.go"
    ```

=== "Rust"

    A child context is an operation of kind `durable::OperationKind::Context`.
    `result.children(op)` returns the operations directly inside it, in order.

    ```rust
    --8<-- "examples/rust/testing/assertions/assert-child-context.rs"
    ```

=== "C#"

    Child contexts appear as `OperationKind.Context` steps. Walk `TestStep.Children` to
    assert on the operations that ran inside the context.

    ```csharp
    --8<-- "examples/csharp/testing/assertions/assert-child-context.cs"
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

=== "Go"

    The SDK records one operation per step, so a retried step is a single
    record whose `StepDetails.Attempt` counts the failed attempts. Filter by
    iterating `result.Operations` and comparing `Status`.

    ```go
    --8<-- "examples/go/testing/assertions/filter-by-status.go"
    ```

=== "Rust"

    The SDK records one operation per step, so a retried step is a single record whose
    status is its final outcome. `StepDetails::attempt()` counts the completed retries,
    which is 2 after three attempts. Filter `result.operations()` by `op.status()`.

    ```rust
    --8<-- "examples/rust/testing/assertions/filter-by-status.rs"
    ```

=== "C#"

    A retried step reuses a single operation record, so the history holds one entry per
    operation with its terminal status. Pass an `OperationStatus` constant to
    `result.GetStepsByStatus(...)` to filter.

    ```csharp
    --8<-- "examples/csharp/testing/assertions/filter-by-status.cs"
    ```

## See also

- [Authoring](authoring.md) Set up the test runner and write your first test.
- [Cloud Runner](cloud-runner.md) Run tests against a deployed Lambda function.
- [Step](../sdk-reference/operations/step.md)
- [Wait](../sdk-reference/operations/wait.md)
