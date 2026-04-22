# Testing API Reference

Reference for the test runners, the test result, operation accessors, and the enums used
across the testing SDKs. For an onboarding walkthrough, start with
[Authoring](authoring.md).

## LocalDurableTestRunner

Runs a durable handler in-process against an in-memory checkpoint store. No AWS
credentials, no deployment, and no containers are required. Use it for unit tests and
CI.

### Create a runner

=== "TypeScript"

    ```typescript
    new LocalDurableTestRunner({ handlerFunction: handler })
    ```

    Call `LocalDurableTestRunner.setupTestEnvironment()` in `beforeAll` and
    `LocalDurableTestRunner.teardownTestEnvironment()` in `afterAll`.

    ```typescript
    static setupTestEnvironment(params?: LocalDurableTestRunnerSetupParameters): Promise<void>
    static teardownTestEnvironment(): Promise<void>
    ```

    **Constructor parameters:**

    - `handlerFunction` (required) The handler created with `withDurableExecution`.

=== "Python"

    ```python
    DurableFunctionTestRunner(handler=handler, poll_interval=1.0)
    ```

    Use as a context manager. The context manager starts the scheduler thread on entry and
    stops it on exit.

    **Parameters:**

    - `handler` (required) The handler decorated with `@durable_execution`.
    - `poll_interval` (optional) Seconds between internal polling cycles. Defaults to `1.0`.

=== "Java"

    ```java
    // Class-based input type
    LocalDurableTestRunner.create(Class<I> inputType, BiFunction<I, DurableContext, O> handlerFn)

    // TypeToken-based input type (for generic types)
    LocalDurableTestRunner.create(TypeToken<I> inputType, BiFunction<I, DurableContext, O> handlerFn)

    // With a custom DurableConfig
    LocalDurableTestRunner.create(Class<I> inputType, BiFunction<I, DurableContext, O> handlerFn, DurableConfig config)

    // From a DurableHandler instance (extracts config automatically)
    LocalDurableTestRunner.create(Class<I> inputType, DurableHandler<I, O> handler)
    ```

    Override config or the output type on an existing runner:

    ```java
    LocalDurableTestRunner<I, O> withDurableConfig(DurableConfig config)
    LocalDurableTestRunner<I, O> withOutputType(Class<O> outputType)
    LocalDurableTestRunner<I, O> withOutputType(TypeToken<O> outputType)
    ```

    **Parameters:**

    - `inputType` (required) The input type class or `TypeToken`.
    - `handlerFn` (required) A `BiFunction<I, DurableContext, O>` implementing the handler
        logic.
    - `config` (optional) A `DurableConfig`. The runner overrides the
        `DurableExecutionClient` with its in-memory implementation but preserves all other
        settings such as custom `SerDes`.
    - `handler` (optional) A `DurableHandler` instance. The runner extracts its
        configuration automatically.

### Run the handler

=== "TypeScript"

    ```typescript
    run(params?: InvokeRequest): Promise<TestResult>
    ```

    `run()` drives the full replay loop until the execution completes or fails.

    **Parameters:**

    - `params` (optional) An `InvokeRequest` object.
        - `payload` (optional) The input payload to pass to the handler.

    **Returns:** `Promise<TestResult>`

=== "Python"

    ```python
    run(
        input: str | None = None,
        timeout: int = 900,
        function_name: str = "test-function",
        execution_name: str = "execution-name",
        account_id: str = "123456789012",
    ) -> DurableFunctionTestResult
    ```

    **Parameters:**

    - `input` (optional) JSON-encoded input string. Defaults to `None`.
    - `timeout` (optional) Maximum seconds to wait for completion. Defaults to `900`.
    - `function_name` (optional) Function name used internally. Defaults to
        `"test-function"`.
    - `execution_name` (optional) Execution name used internally. Defaults to
        `"execution-name"`.
    - `account_id` (optional) Account ID used internally. Defaults to `"123456789012"`.

    **Returns:** `DurableFunctionTestResult`

    **Raises:** `TimeoutError` if the execution does not complete within `timeout`.

=== "Java"

    ```java
    // Runs a single invocation (may return PENDING)
    TestResult<O> run(I input)

    // Drives the full replay loop until SUCCEEDED, FAILED, or PENDING with no
    // auto-advanceable operations
    TestResult<O> runUntilComplete(I input)
    ```

    **Parameters:**

    - `input` (required) The handler input.

    **Returns:** `TestResult<O>`

### Run asynchronously

=== "TypeScript"

    Not applicable. `run()` already drives the full replay loop.

=== "Python"

    ```python
    # Start execution without waiting for completion
    run_async(
        input: str | None = None,
        timeout: int = 900,
        function_name: str = "test-function",
        execution_name: str = "execution-name",
        account_id: str = "123456789012",
    ) -> str  # returns execution_arn

    # Wait for a running execution to complete
    wait_for_result(execution_arn: str, timeout: int = 60) -> DurableFunctionTestResult
    ```

=== "Java"

    Not applicable on the local runner. `run()` runs a single invocation,
    `runUntilComplete()` drives the full loop. The cloud runner exposes `startAsync()`, see
    [CloudDurableTestRunner](#clouddurabletestrunner).

### Inspect operations

=== "TypeScript"

    ```typescript
    getOperation(name: string, index?: number): DurableOperation
    getOperationByIndex(index: number): DurableOperation
    getOperationByNameAndIndex(name: string, index: number): DurableOperation
    getOperationById(id: string): DurableOperation
    ```

    **Parameters:**

    - `name` (required) The operation name.
    - `index` (optional) Zero-based index when multiple operations share the same name.
        Defaults to `0`.
    - `id` (required) The unique operation ID.

    **Returns:** `DurableOperation`

=== "Python"

    Not available on the runner. Inspect operations through the result:
    `result.get_step(name)`, `result.get_wait(name)`, `result.get_callback(name)`,
    `result.get_context(name)`, `result.get_invoke(name)`,
    `result.get_operation_by_name(name)`, and `result.get_all_operations()`. See
    [TestResult](#testresult).

=== "Java"

    ```java
    TestOperation getOperation(String name)
    ```

    **Parameters:**

    - `name` (required) The operation name.

    **Returns:** `TestOperation`, or `null` if not found.

### Drive callbacks

=== "TypeScript"

    Callback interaction is on the `DurableOperation` object. Get a handle with
    `getOperation()` and then call `waitForData()` and `sendCallback*()` on it. See
    [Drive a callback from an operation](#drive-a-callback-from-an-operation).

=== "Python"

    ```python
    # Wait for a callback to become available and return its ID
    wait_for_callback(execution_arn: str, name: str | None = None, timeout: int = 60) -> str

    # Send a successful callback result
    send_callback_success(callback_id: str, result: bytes | None = None) -> None

    # Send a callback failure
    send_callback_failure(callback_id: str, error: ErrorObject | None = None) -> None

    # Send a callback heartbeat
    send_callback_heartbeat(callback_id: str) -> None
    ```

=== "Java"

    ```java
    // Get the callback ID for a named callback operation
    String getCallbackId(String operationName)

    // Complete a callback with a success result
    void completeCallback(String callbackId, String result)

    // Fail a callback
    void failCallback(String callbackId, ErrorObject error)

    // Time out a callback
    void timeoutCallback(String callbackId)
    ```

### Drive chained invokes

=== "TypeScript"

    Not applicable. Register a mock handler for the invoked function and let the test drive
    the real handler path. See
    [Register mock handlers for invoke](#register-mock-handlers-for-invoke).

=== "Python"

    Not applicable.

=== "Java"

    ```java
    // Complete a chained invoke with a success result
    void completeChainedInvoke(String name, String result)

    // Fail a chained invoke
    void failChainedInvoke(String name, ErrorObject error)

    // Time out a chained invoke
    void timeoutChainedInvoke(String name)

    // Stop a chained invoke
    void stopChainedInvoke(String name, ErrorObject error)
    ```

### Register mock handlers for invoke

=== "TypeScript"

    ```typescript
    // Register a durable function for context.invoke() calls
    registerDurableFunction(functionName: string, handler: DurableLambdaHandler): this

    // Register a standard Lambda function for context.invoke() calls
    registerFunction(functionName: string, handler: Handler): this
    ```

    Both methods return the runner for chaining.

=== "Python"

    Not applicable.

=== "Java"

    Not applicable.

### Simulate failures

=== "TypeScript"

    Not applicable.

=== "Python"

    Not applicable.

=== "Java"

    ```java
    // Reset a step checkpoint to STARTED (simulates checkpoint failure)
    void resetCheckpointToStarted(String stepName)

    // Remove a step checkpoint (simulates fire-and-forget loss)
    void simulateFireAndForgetCheckpointLoss(String stepName)
    ```

### Control time

=== "TypeScript"

    Pass `{ skipTime: true }` to `setupTestEnvironment()`. Both `context.wait()` delays and
    step retry delays complete in zero wall-clock time.

=== "Python"

    Set the `DURABLE_EXECUTION_TIME_SCALE` environment variable to a float that multiplies
    `context.wait()` durations. Step retry delays use the configured
    `next_attempt_delay_seconds` at real wall-clock time and the scale does not apply to
    them.

=== "Java"

    ```java
    void advanceTime()
    ```

    `runUntilComplete()` calls `advanceTime()` after each invocation. When you call `run()`
    directly, call `advanceTime()` yourself between invocations. `advanceTime()` marks
    PENDING step retries as READY and completes STARTED waits without real-time sleeps.

See [Authoring: Skip time in tests](authoring.md#skip-time-in-tests) for an overview.

### Reset between runs

=== "TypeScript"

    ```typescript
    reset(): void
    ```

    Clears the operation index, wait manager, and operation storage so the runner can be
    reused across tests.

=== "Python"

    Create a new `DurableFunctionTestRunner` instance per test.

=== "Java"

    Create a new `LocalDurableTestRunner` instance per test.

### Reference types

#### LocalDurableTestRunnerSetupParameters

=== "TypeScript"

    ```typescript
    interface LocalDurableTestRunnerSetupParameters {
      skipTime?: boolean;
      checkpointDelay?: number;
    }
    ```

    **Fields:**

    - `skipTime` (optional) Install fake timers so retry delays and waits complete
        instantly. Defaults to `false`.
    - `checkpointDelay` (optional) Simulated delay in milliseconds on checkpoint API calls.
        Useful for surfacing race conditions.

=== "Python"

    Not applicable. Configure with the `DURABLE_EXECUTION_TIME_SCALE` environment variable
    and the `poll_interval` constructor argument.

=== "Java"

    Not applicable. Configure with `withDurableConfig`, `withOutputType`, and
    `advanceTime()` on the runner instance.

## TestResult

The object returned by `run()`. Exposes the execution status, the final result, any
error, and the full operation history.

### Status

=== "TypeScript"

    ```typescript
    getStatus(): ExecutionStatus | undefined
    ```

    **Returns:** `ExecutionStatus` (`SUCCEEDED`, `FAILED`, `PENDING`, or `undefined`).

=== "Python"

    ```python
    result.status  # InvocationStatus
    ```

    **Type:** `InvocationStatus` (`SUCCEEDED`, `FAILED`, `PENDING`, `TIMED_OUT`, `STOPPED`).

=== "Java"

    ```java
    ExecutionStatus getStatus()
    boolean isSucceeded()
    boolean isFailed()
    ```

    **Returns:** `ExecutionStatus` (`SUCCEEDED`, `FAILED`, `PENDING`).

### Result

=== "TypeScript"

    ```typescript
    getResult(): TResult | undefined
    ```

    **Returns:** The deserialized execution output, or `undefined` if not available.

    **Throws:** The execution error if the execution failed.

=== "Python"

    ```python
    result.result  # str | None
    ```

    The raw JSON-encoded result payload.

=== "Java"

    ```java
    <T> T getResult(Class<T> resultType)
    <T> T getResult(TypeToken<T> resultType)
    O getResult()  // requires withOutputType() to be set
    ```

    **Returns:** The deserialized execution output.

    **Throws:** `IllegalStateException` if the execution did not succeed.

### Error

=== "TypeScript"

    ```typescript
    getError(): TestResultError
    ```

    **Returns:** A `TestResultError` object.

    **Throws:** If the execution succeeded.

=== "Python"

    ```python
    result.error  # ErrorObject | None
    ```

=== "Java"

    ```java
    Optional<ErrorObject> getError()
    ```

### Operations

=== "TypeScript"

    ```typescript
    getOperations(params?: { status: OperationStatus }): DurableOperation[]
    ```

    **Parameters:**

    - `params` (optional) Filter by `OperationStatus`.

    **Returns:** Array of `DurableOperation`.

=== "Python"

    ```python
    result.operations               # list[Operation], top-level only
    result.get_all_operations()     # list[Operation], including nested

    # Typed lookup by name
    result.get_step(name: str) -> StepOperation
    result.get_wait(name: str) -> WaitOperation
    result.get_context(name: str) -> ContextOperation
    result.get_callback(name: str) -> CallbackOperation
    result.get_invoke(name: str) -> InvokeOperation
    result.get_execution(name: str) -> ExecutionOperation
    result.get_operation_by_name(name: str) -> Operation
    ```

    **Raises:** `DurableFunctionsTestError` if the operation is not found.

=== "Java"

    ```java
    List<TestOperation> getOperations()
    List<TestOperation> getSucceededOperations()
    List<TestOperation> getFailedOperations()
    TestOperation getOperation(String name)
    ```

    `getOperation(name)` returns `null` if not found.

### History events

=== "TypeScript"

    ```typescript
    getHistoryEvents(): Event[]
    ```

=== "Python"

    Not available on the test result.

=== "Java"

    ```java
    List<Event> getHistoryEvents()
    List<Event> getEventsForOperation(String operationName)
    ```

### Invocations

=== "TypeScript"

    ```typescript
    getInvocations(): Invocation[]
    ```

    Useful for asserting on the number of Lambda re-invocations the test runner drove.

=== "Python"

    Not available on the test result.

=== "Java"

    Not available on the test result.

### Pretty-print

=== "TypeScript"

    ```typescript
    print(config?: { parentId?: boolean; name?: boolean; type?: boolean; ... }): void
    ```

    Writes a formatted table of operations to stdout.

=== "Python"

    Not applicable.

=== "Java"

    Not applicable.

### Reference types {#testresult-reference-types}

#### TestResultError

=== "TypeScript"

    ```typescript
    interface TestResultError {
      errorMessage: string | undefined;
      errorType: string | undefined;
      errorData: string | undefined;
      stackTrace: string[] | undefined;
    }
    ```

=== "Python"

    Uses `ErrorObject` from the main SDK:

    ```python
    @dataclass
    class ErrorObject:
        error_message: str | None
        error_type: str | None
    ```

=== "Java"

    Uses `ErrorObject` from `software.amazon.awssdk.services.lambda.model`:

    ```java
    ErrorObject.errorMessage()  // String
    ErrorObject.errorType()     // String
    ```

## Operation

Represents a single entry in the operation history. Each type of operation exposes its
own details block (step, wait, callback, chained invoke, context, execution) on top of a
shared set of accessors.

### Common accessors

=== "TypeScript"

    ```typescript
    getName(): string | undefined
    getType(): OperationType | undefined
    getSubType(): OperationSubType | undefined
    getStatus(): OperationStatus | undefined
    getStartTimestamp(): Date | undefined
    getEndTimestamp(): Date | undefined
    getId(): string | undefined
    getParentId(): string | undefined
    getOperationData(): Operation | undefined
    getEvents(): Event[] | undefined
    getChildOperations(): DurableOperation[] | undefined
    isWaitForCallback(): boolean
    isCallback(): boolean
    ```

=== "Python"

    Every operation type inherits these fields from the base `Operation` dataclass:

    ```python
    operation.operation_id: str
    operation.operation_type: OperationType
    operation.status: OperationStatus
    operation.name: str | None
    operation.parent_id: str | None
    operation.sub_type: OperationSubType | None
    operation.start_timestamp: datetime | None
    operation.end_timestamp: datetime | None
    ```

=== "Java"

    ```java
    String getName()
    OperationType getType()
    String getSubtype()
    OperationStatus getStatus()
    Duration getDuration()
    boolean isCompleted()
    List<Event> getEvents()
    String getId()
    ```

### Step details

=== "TypeScript"

    ```typescript
    getStepDetails(): StepDetails | undefined
    ```

=== "Python"

    ```python
    step.result: OperationPayload | None
    step.error: ErrorObject | None
    step.attempt: int
    step.next_attempt_timestamp: datetime | None
    step.child_operations: list[Operation]
    ```

=== "Java"

    ```java
    StepDetails getStepDetails()

    // Convenience deserialization for step results
    <T> T getStepResult(Class<T> type)
    <T> T getStepResult(TypeToken<T> type)

    // Step error
    ErrorObject getError()

    // Retry attempt number (0-based)
    int getAttempt()
    ```

### Wait details

=== "TypeScript"

    ```typescript
    getWaitDetails(): WaitResultDetails | undefined
    ```

    `WaitResultDetails` exposes `waitSeconds` and `scheduledEndTimestamp`.

=== "Python"

    ```python
    wait.scheduled_end_timestamp: datetime | None
    ```

=== "Java"

    ```java
    WaitDetails getWaitDetails()
    ```

    `WaitDetails.scheduledEndTimestamp()` returns an `Instant`.

### Callback details

=== "TypeScript"

    ```typescript
    getCallbackDetails(): CallbackDetails | undefined
    ```

=== "Python"

    ```python
    callback.callback_id: str | None
    callback.result: OperationPayload | None
    callback.error: ErrorObject | None
    callback.child_operations: list[Operation]
    ```

=== "Java"

    ```java
    CallbackDetails getCallbackDetails()
    ```

### Chained invoke details

=== "TypeScript"

    ```typescript
    getChainedInvokeDetails(): ChainedInvokeDetails | undefined
    ```

=== "Python"

    ```python
    invoke.result: OperationPayload | None
    invoke.error: ErrorObject | None
    ```

=== "Java"

    ```java
    ChainedInvokeDetails getChainedInvokeDetails()
    ```

### Context details

=== "TypeScript"

    ```typescript
    getContextDetails(): ContextDetails | undefined
    ```

=== "Python"

    ```python
    ctx.result: OperationPayload | None
    ctx.error: ErrorObject | None
    ctx.child_operations: list[Operation]

    # Typed lookup among the context's children
    ctx.get_step(name) -> StepOperation
    ctx.get_wait(name) -> WaitOperation
    ctx.get_context(name) -> ContextOperation
    ctx.get_callback(name) -> CallbackOperation
    ctx.get_invoke(name) -> InvokeOperation
    ```

=== "Java"

    ```java
    ContextDetails getContextDetails()
    ```

### Execution details

=== "TypeScript"

    Not applicable.

=== "Python"

    Not applicable.

=== "Java"

    ```java
    ExecutionDetails getExecutionDetails()
    ```

### Drive a callback from an operation

=== "TypeScript"

    ```typescript
    waitForData(status?: WaitingOperationStatus): Promise<DurableOperation>
    sendCallbackSuccess(result?: string): Promise<SendDurableExecutionCallbackSuccessResponse>
    sendCallbackFailure(error?: ErrorObject): Promise<SendDurableExecutionCallbackFailureResponse>
    sendCallbackHeartbeat(): Promise<SendDurableExecutionCallbackHeartbeatResponse>
    ```

=== "Python"

    Driven from the runner. See [LocalDurableTestRunner: Drive callbacks](#drive-callbacks).

=== "Java"

    Driven from the runner. See [LocalDurableTestRunner: Drive callbacks](#drive-callbacks).

## Enums

### Execution status

The terminal status of a durable execution.

=== "TypeScript"

    `ExecutionStatus` from `@aws-sdk/client-lambda`:

    | Value       | Meaning                                       |
    | ----------- | --------------------------------------------- |
    | `SUCCEEDED` | Execution completed successfully              |
    | `FAILED`    | Execution failed                              |
    | `PENDING`   | Execution is waiting (callback, invoke, etc.) |
    | `TIMED_OUT` | Execution exceeded its timeout                |
    | `STOPPED`   | Execution was stopped                         |

=== "Python"

    `InvocationStatus` from `aws_durable_execution_sdk_python.execution`:

    | Value       | Meaning                          |
    | ----------- | -------------------------------- |
    | `SUCCEEDED` | Execution completed successfully |
    | `FAILED`    | Execution failed                 |
    | `PENDING`   | Execution is waiting             |
    | `TIMED_OUT` | Execution exceeded its timeout   |
    | `STOPPED`   | Execution was stopped            |

=== "Java"

    `ExecutionStatus` from `software.amazon.lambda.durable.model`:

    | Value       | Meaning                          |
    | ----------- | -------------------------------- |
    | `SUCCEEDED` | Execution completed successfully |
    | `FAILED`    | Execution failed                 |
    | `PENDING`   | Execution is waiting             |

### Operation status

The status of an individual operation.

=== "TypeScript"

    `OperationStatus` from `@aws-sdk/client-lambda`:

    | Value       | Meaning                          |
    | ----------- | -------------------------------- |
    | `STARTED`   | Operation is running             |
    | `SUCCEEDED` | Operation completed successfully |
    | `FAILED`    | Operation failed                 |
    | `PENDING`   | Operation is queued              |
    | `CANCELLED` | Operation was cancelled          |
    | `TIMED_OUT` | Operation exceeded its timeout   |
    | `STOPPED`   | Operation was stopped            |

=== "Python"

    `OperationStatus` from `aws_durable_execution_sdk_python.lambda_service`. Same values as
    TypeScript.

=== "Java"

    `OperationStatus` from `software.amazon.awssdk.services.lambda.model`. Same values as
    TypeScript.

### Operation type

=== "TypeScript"

    `OperationType` from `@aws-sdk/client-lambda`:

    | Value            | Meaning                      |
    | ---------------- | ---------------------------- |
    | `STEP`           | A step operation             |
    | `WAIT`           | A wait operation             |
    | `CALLBACK`       | A callback operation         |
    | `CHAINED_INVOKE` | An invoke operation          |
    | `CONTEXT`        | A child context              |
    | `EXECUTION`      | The root execution operation |

=== "Python"

    `OperationType` from `aws_durable_execution_sdk_python.lambda_service`. Same values as
    TypeScript.

=== "Java"

    `OperationType` from `software.amazon.awssdk.services.lambda.model`. Same values as
    TypeScript.

### Waiting operation status

=== "TypeScript"

    `WaitingOperationStatus` from `@aws/durable-execution-sdk-js-testing`:

    | Value       | Meaning                                                                                               |
    | ----------- | ----------------------------------------------------------------------------------------------------- |
    | `STARTED`   | Fires when the operation starts                                                                       |
    | `SUBMITTED` | For callbacks: fires when the submitter function completes. For other operations: same as `COMPLETED` |
    | `COMPLETED` | Fires when the operation reaches a terminal status                                                    |

=== "Python"

    Not applicable. Use `runner.wait_for_callback()` to wait for a callback to become
    available.

=== "Java"

    Not applicable. Use `runner.getCallbackId()` after `run()` returns `PENDING`.

## CloudDurableTestRunner

Invokes a deployed Lambda function, polls for completion, and retrieves the full
operation history for assertions. Use it to validate deployment, IAM permissions, and
real service integrations. Both runners share the same `TestResult` and `Operation`
types, so tests written against the local runner run unchanged against the cloud runner.

### Create a runner {#cloud-create-a-runner}

=== "TypeScript"

    ```typescript
    new CloudDurableTestRunner({
      functionName: string,
      client?: LambdaClient,
      config?: CloudDurableTestRunnerConfig,
    })
    ```

    **Parameters:**

    - `functionName` (required) The function name or ARN. A qualifier is required for
        durable functions.
    - `client` (optional) A configured `LambdaClient`. Defaults to `new LambdaClient()`.
    - `config` (optional) A `CloudDurableTestRunnerConfig` object.

=== "Python"

    ```python
    DurableFunctionCloudTestRunner(
        function_name: str,
        region: str = "us-west-2",
        lambda_endpoint: str | None = None,
        poll_interval: float = 1.0,
    )
    ```

    **Parameters:**

    - `function_name` (required) The function name or ARN.
    - `region` (optional) AWS region. Defaults to `"us-west-2"`.
    - `lambda_endpoint` (optional) Custom Lambda endpoint URL. Defaults to `None`.
    - `poll_interval` (optional) Seconds between status polls. Defaults to `1.0`.

=== "Java"

    ```java
    // Class-based types
    CloudDurableTestRunner.create(String functionArn, Class<I> inputType, Class<O> outputType)

    // TypeToken-based types
    CloudDurableTestRunner.create(String functionArn, TypeToken<I> inputType, TypeToken<O> outputType)

    // With custom LambdaClient
    CloudDurableTestRunner.create(String functionArn, Class<I> inputType, Class<O> outputType, LambdaClient lambdaClient)
    ```

    Override settings on an existing runner:

    ```java
    CloudDurableTestRunner<I, O> withLambdaClient(LambdaClient lambdaClient)
    CloudDurableTestRunner<I, O> withPollInterval(Duration interval)
    CloudDurableTestRunner<I, O> withTimeout(Duration timeout)
    CloudDurableTestRunner<I, O> withInvocationType(InvocationType type)
    CloudDurableTestRunner<I, O> withSerDes(SerDes serDes)
    ```

    **Parameters:**

    - `functionArn` (required) The function ARN. A qualifier is required for durable
        functions.
    - `inputType` (required) The input type class or `TypeToken`.
    - `outputType` (required) The output type class or `TypeToken`.
    - `lambdaClient` (optional) A configured `LambdaClient`. Defaults to a client using
        `DefaultCredentialsProvider`.

### Run the handler {#cloud-run-the-handler}

=== "TypeScript"

    ```typescript
    run(params?: InvokeRequest): Promise<TestResult>
    ```

    **Parameters:**

    - `params` (optional) An `InvokeRequest` object.
        - `payload` (optional) The input payload.

    **Returns:** `Promise<TestResult>`

=== "Python"

    ```python
    run(input: str | None = None, timeout: int = 60) -> DurableFunctionTestResult
    ```

    **Parameters:**

    - `input` (optional) JSON-encoded input string.
    - `timeout` (optional) Maximum seconds to wait. Defaults to `60`.

    **Returns:** `DurableFunctionTestResult`

    **Raises:** `TimeoutError` if the execution does not complete within `timeout`.

=== "Java"

    ```java
    TestResult<O> run(I input)
    TestResult<O> runUntilComplete(I input)
    ```

    **Parameters:**

    - `input` (required) The handler input.

    **Returns:** `TestResult<O>`

### Run asynchronously {#cloud-run-asynchronously}

=== "TypeScript"

    Not applicable. `run()` already drives the full replay loop.

=== "Python"

    ```python
    # Start execution without waiting
    run_async(input: str | None = None, timeout: int = 60) -> str  # returns execution_arn

    # Wait for a running execution to complete
    wait_for_result(execution_arn: str, timeout: int = 60) -> DurableFunctionTestResult
    ```

=== "Java"

    ```java
    AsyncExecution<O> startAsync(I input)
    ```

    The returned `AsyncExecution<O>` exposes `pollUntil()`, `pollUntilComplete()`,
    `isComplete()`, `hasOperation()`, `hasCallback()`, `getCallbackId()`, `getOperation()`,
    `getOperations()`, `getStatus()`, `getExecutionArn()`, `completeCallback()`,
    `failCallback()`, and `heartbeatCallback()`.

### Inspect operations {#cloud-inspect-operations}

=== "TypeScript"

    ```typescript
    getOperation(name: string): DurableOperation
    getOperationByIndex(index: number): DurableOperation
    getOperationByNameAndIndex(name: string, index: number): DurableOperation
    getOperationById(id: string): DurableOperation
    ```

=== "Python"

    Inspect operations through the test result. See [TestResult](#testresult).

=== "Java"

    ```java
    TestOperation getOperation(String name)
    ```

### Drive callbacks {#cloud-drive-callbacks}

=== "TypeScript"

    Callback interaction is on the `DurableOperation` object. See
    [Drive a callback from an operation](#drive-a-callback-from-an-operation).

=== "Python"

    ```python
    wait_for_callback(execution_arn: str, name: str | None = None, timeout: int = 60) -> str
    send_callback_success(callback_id: str, result: bytes | None = None) -> None
    send_callback_failure(callback_id: str, error: ErrorObject | None = None) -> None
    send_callback_heartbeat(callback_id: str) -> None
    ```

=== "Java"

    Drive callbacks through the `AsyncExecution<O>` returned by `startAsync()`. Use
    `getCallbackId()`, `completeCallback()`, `failCallback()`, and `heartbeatCallback()` on
    the async handle.

### Configure polling and timeouts

=== "TypeScript"

    Pass `config: { pollInterval, invocationType }` to the constructor. See
    [CloudDurableTestRunnerConfig](#clouddurabletestrunnerconfig).

=== "Python"

    Pass `poll_interval` to the constructor. Pass `timeout` to `run()` or `run_async()` to
    set the maximum wait.

=== "Java"

    Use `withPollInterval(Duration)`, `withTimeout(Duration)`, and
    `withInvocationType(InvocationType)` on the runner.

### Reset between runs {#cloud-reset-between-runs}

=== "TypeScript"

    ```typescript
    reset(): void
    ```

=== "Python"

    Create a new runner instance per test.

=== "Java"

    Create a new runner instance per test.

### Reference types {#cloud-reference-types}

#### CloudDurableTestRunnerConfig

=== "TypeScript"

    ```typescript
    interface CloudDurableTestRunnerConfig {
      pollInterval?: number;
      invocationType?: InvocationType;
    }
    ```

    **Fields:**

    - `pollInterval` (optional) Milliseconds between history polls. Defaults to `1000`.
    - `invocationType` (optional) Lambda invocation type. Defaults to
        `InvocationType.RequestResponse`.

=== "Python"

    Not a separate config object. Pass `poll_interval` directly to the constructor.

=== "Java"

    Not a separate config object. Use the `with*` builder methods on the runner.

## See also

- [Authoring](authoring.md) Set up the local runner and write your first test.
- [Assertions](assertions.md) Inspect steps, waits, and callbacks after a test run.
- [Workflow patterns](workflow-patterns.md) Complete tests for common workflow shapes.
- [Cloud Runner](cloud-runner.md) Run tests against a deployed Lambda function.
- [Runner](runner.md) How the replay loop and checkpointing work.
