# Authoring

## Install the testing SDK

=== "TypeScript"

    ```bash
    npm install --save-dev @aws/durable-execution-sdk-js-testing
    ```

=== "Python"

    ```bash
    pip install aws-durable-execution-sdk-python-testing
    ```

=== "Java"

    Add the testing dependency to your build file with `test` scope:

    ```xml
    <dependency>
      <groupId>software.amazon.lambda</groupId>
      <artifactId>aws-durable-execution-sdk-java-testing</artifactId>
      <scope>test</scope>
    </dependency>
    ```

=== "C#"

    Add the testing package to your test project:

    ```bash
    dotnet add package Amazon.Lambda.DurableExecution.Testing
    ```

## Write a minimal test

Create a runner with your handler, call `run()`, and assert on the result.

=== "TypeScript"

    Call `setupTestEnvironment()` in `beforeAll` and `teardownTestEnvironment()` in
    `afterAll`. Create a new runner instance in `beforeEach` so each test starts with a
    clean state.

    ```typescript
    --8<-- "examples/typescript/testing/authoring/minimal-test.ts"
    ```

=== "Python"

    Use `DurableFunctionTestRunner` as a context manager. The context manager starts the
    scheduler thread on entry and stops it on exit.

    ```python
    --8<-- "examples/python/testing/authoring/minimal-test.py"
    ```

=== "Java"

    Use `LocalDurableTestRunner.create()` with the input type and a handler function. Call
    `runUntilComplete()` to drive the full replay loop until the execution completes or
    fails.

    ```java
    --8<-- "examples/java/testing/authoring/minimal-test.java"
    ```

=== "C#"

    Construct `DurableTestRunner<TIn, TOut>` with your workflow delegate and
    `new TestRunnerOptions { SkipTime = true }`, call `RunAsync()`, then
    `EnsureSucceeded()` and assert on `Result`.

    ```csharp
    --8<-- "examples/csharp/testing/authoring/minimal-test.cs"
    ```

## Test a failed execution

When your handler throws outside a step, or a step exhausts all retries, the execution
fails. Assert on the status and inspect the error.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing/authoring/test-failure.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing/authoring/test-failure.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing/authoring/test-failure.java"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/testing/authoring/test-failure.cs"
    ```

## Test retries

The test runner drives the full retry loop via replay. Configure a retry strategy on the
step, and the runner re-invokes the handler as many times as needed.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing/authoring/test-retries.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing/authoring/test-retries.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing/authoring/test-retries.java"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/testing/authoring/test-retries.cs"
    ```

## Skip time in tests

Retry backoffs and `context.wait()` durations use real time in production. The test
runner collapses these delays so tests finish in milliseconds.

=== "TypeScript"

    Pass `{ skipTime: true }` to `setupTestEnvironment()`. The runner swaps its real timer
    for a queue that fires immediately. Both `context.wait()` delays and step retry delays
    complete in zero wall-clock time.

    ```typescript
    await LocalDurableTestRunner.setupTestEnvironment({ skipTime: true });
    ```

=== "Python"

    Pass `skip_time=True` to `DurableFunctionTestRunner` (the default). The runner uses a
    virtual clock that advances instantly through modeled delays. Both `context.wait()`
    durations and step retry delays complete in zero wall-clock time.

    ```python
    runner = DurableFunctionTestRunner(handler=handler, skip_time=True)
    ```

    Set `skip_time=False` to assert on real wait durations.

=== "Java"

    `runUntilComplete()` calls `advanceTime()` after each invocation. `advanceTime()`
    immediately marks PENDING step retries as READY and completes STARTED waits without
    real-time sleeps. If you call `run()` directly, call `advanceTime()` yourself between
    invocations.

    ```java
    runner.runUntilComplete(input); // advanceTime() runs after each invocation
    ```

=== "C#"

    `TestRunnerOptions.SkipTime` defaults to `true`, so `RunAsync()` completes both
    `WaitAsync` timers and step retry backoffs immediately without real-time sleeps.
    Set it to `false` to assert on real wait durations.

    ```csharp
    await using var runner = new DurableTestRunner<TIn, TOut>(
        Workflow, new TestRunnerOptions { SkipTime = true });
    await runner.RunAsync(input); // waits and retry backoffs complete instantly
    ```

See [Workflow patterns: Long waits](workflow-patterns.md#long-waits) for a worked
example.

## Test branching logic

Run the same handler with different inputs to cover each branch. Each test case gets its
own runner instance.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing/authoring/test-branching.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing/authoring/test-branching.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing/authoring/test-branching.java"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/testing/authoring/test-branching.cs"
    ```

## See also

- [API Reference](api-reference.md) Full reference for the runner, result, and operation
    classes.
- [Workflow patterns](workflow-patterns.md) Complete tests for common workflow shapes.
- [Assertions](assertions.md) Inspect steps, waits, and callbacks after a test run.
- [Cloud Runner](cloud-runner.md) Run the same tests against a deployed Lambda function.
- [SAM CLI](sam-cli.md) Local and remote invocation with SAM CLI.
