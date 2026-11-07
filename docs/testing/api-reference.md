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

=== "Go"

    Construction is the whole lifecycle. The `opts` are the production handler's
    `durable.HandlerOption` values, such as `durable.WithSerdes`. The handler has type
    `durable.Handler[I, O]`, that is `func(durable.Context, I) (O, error)`.

    ```go
    func NewLocalRunner[I, O any](handler durable.Handler[I, O], opts ...durable.HandlerOption) *LocalRunner[I, O]
    ```

    **Parameters:**

    - `handler` (required) The durable handler under test.
    - `opts` (optional) `durable.HandlerOption` values, applied as in production. The
        runner supplies its own in-memory execution client.

    **Returns:** `*LocalRunner[I, O]`

=== "Rust"

    `durable::test_util::LocalRunner` runs the handler in process. The `test_util`
    module requires the SDK's `test-util` cargo feature, which you enable on a
    dev-dependency as [Install the testing SDK](authoring.md#install-the-testing-sdk)
    shows.

    ```rust
    impl LocalRunner {
        // ...
        pub fn new() -> Self;
        // ...
        pub fn options(mut self, options: crate::Options) -> Self;
        // ...
    }
    ```

    `LocalRunner::default()` returns the same runner as `LocalRunner::new()`. Each
    setting is a builder method that takes the runner and returns it (see
    [LocalDurableTestRunnerSetupParameters](#localdurabletestrunnersetupparameters)).
    You pass the handler to `run`, so one runner can run several handlers. The runner
    needs no setup or teardown.

    **Parameters:**

    - `options` (optional) The production handler's `durable::Options`. Every setting
        applies as in production except the transport. The runner ignores `sdk_config`,
        `lambda_client`, and `execution_client`, and uses its in-memory backend.
        Defaults to `Options::default()`.

    **Returns:** `LocalRunner`

=== "C#"

    The local runner type is `DurableTestRunner<TInput, TOutput>`. Construct it with the
    workflow delegate and dispose it with `await using`:

    ```csharp
    DurableTestRunner(
        Func<TInput, IDurableContext, Task<TOutput>> handler,
        TestRunnerOptions? options = null)
    ```

    ```csharp
    await using var runner = new DurableTestRunner<object?, string>(
        Workflow,
        new TestRunnerOptions { SkipTime = true });
    ```

    There is no static setup/teardown. The `await using` declaration and `DisposeAsync()`
    replace the TypeScript `setupTestEnvironment`/`teardownTestEnvironment` lifecycle.

    **Constructor parameters:**

    - `handler` (required) A `Func<TInput, IDurableContext, Task<TOutput>>`, either a method
        group or an inline `(input, ctx) => ...` async lambda.
    - `options` (optional) A [`TestRunnerOptions`](#localdurabletestrunnersetupparameters)
        record. Defaults to a new instance with `SkipTime = true`.

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

=== "Go"

    `Run` performs a single invocation and may return `PENDING`.
    `RunUntilComplete` loops, advancing timers and running registered invoke
    targets between invocations. It returns at a terminal status, when the
    execution is blocked on a callback or chained invoke, or at the invocation
    cap.

    ```go
    func (r *LocalRunner[I, O]) Run(event I) (*TestResult, error)
    func (r *LocalRunner[I, O]) RunUntilComplete(event I, opts ...RunnerOption) (*TestResult, error)
    ```

    **Parameters:**

    - `event` (required) The handler input, of the runner's input type `I`. The runner
        encodes it as JSON.
    - `opts` (optional) `RunnerOption` values. `WithMaxInvocations` sets the invocation cap,
        which defaults to `DefaultMaxInvocations` (100).

    **Returns:** `*TestResult` and an `error`. When `RunUntilComplete` reaches the cap,
    the result holds the last state, usually `PENDING`, with `CapReached` set to `true`.

    ```go
    CapReached bool
    ```

    **Errors:** The error is non-nil, and the result nil, only when the runner itself
    fails. That happens when the event does not encode as JSON, the invocation returns
    an error that indicates an SDK or runner bug, or the response does not parse. A
    handler that returns an error produces a nil error and a result with status
    `FAILED`.

=== "Rust"

    ```rust
    pub fn run<E, O, F, Fut>(&self, handler: F, event: E) -> RunHandle<O>
    where
        E: Serialize + DeserializeOwned + Send + 'static,
        O: Serialize + DeserializeOwned + Send + 'static,
        F: Fn(E, DurableContext) -> Fut + Send + Sync + 'static,
        Fut: Future<Output = Result<O, BoxError>> + Send;
    // ...
    pub fn max_invocations(mut self, max: usize) -> Self;
    ```

    `run` returns a `durable::test_util::RunHandle<O>` at once. `RunHandle` is a future.
    Awaiting it drives the full replay loop until the execution succeeds or fails,
    nothing can advance it, or the invocation cap is reached. The run starts when the
    test first awaits a future of the run, such as the `RunHandle`.

    **Parameters:**

    - `handler` (required) The durable handler, the function you pass to `durable::run`.
        Its event and output types need `Serialize` and `DeserializeOwned`.
    - `event` (required) The handler input. The runner encodes it as JSON.
    - `max` (optional) The invocation cap, set with `max_invocations`. Defaults to 100.
        A value of 0 counts as 1.

    **Returns:** `RunHandle<O>`. Awaiting it returns
    `Result<TestResult<O>, TestRunnerError>`. When nothing can advance the execution, or
    the cap is reached, the result has status `ExecutionStatus::Running` and
    `error_message()` says why the run ended.

    **Errors:** Awaiting the handle returns `Err(TestRunnerError)` only when the runner
    itself fails. That happens when the event does not encode as JSON
    (`EventSerialization`), the output of a succeeded execution does not decode into `O`
    (`OutputDeserialization`), or a response envelope has a status other than
    `SUCCEEDED`, `FAILED`, and `PENDING` (`UnexpectedEnvelope`). A handler that returns
    an error produces `Ok` with a result whose status is `ExecutionStatus::Failed`.

=== "C#"

    ```csharp
    Task<TestResult<TOutput>> RunAsync(
        TInput input,
        TimeSpan? timeout = null,
        CancellationToken cancellationToken = default)
    ```

    `RunAsync` drives the full replay loop until the workflow reaches a terminal state. It
    throws `InvalidOperationException` if the workflow suspends on a callback. Use the
    `StartAsync` + `WaitForCallbackAsync` two-call pattern for callback workflows.

    ```csharp
    TestResult<string> result = await runner.RunAsync(null);
    ```

    **Parameters:**

    - `input` (required) The handler input.
    - `timeout` (optional) Wall-clock timeout for the call. Defaults to
        `TestRunnerOptions.DefaultTimeout` (30 seconds).
    - `cancellationToken` (optional) A `CancellationToken`.

    **Returns:** `Task<TestResult<TOutput>>`

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

=== "Go"

    For a callback or chained invoke, run to `PENDING`, resolve the pending operation,
    then call `RunUntilComplete` again.

=== "Rust"

    `run` returns the `RunHandle` at once and drives nothing until the test awaits a
    future of the run. Before you await the `RunHandle`, take operation handles from it
    and await their controls, such as `wait_for_data` and `send_callback` (see [Drive a
    callback from an operation](#drive-a-callback-from-an-operation)). The run moves on
    only while the test awaits a future of the run, such as the `RunHandle` or
    `wait_for_data`. So await the operation controls first and the `RunHandle` last.

=== "C#"

    The local runner supports the async pattern, primarily for callback workflows:

    ```csharp
    // Start the workflow and return the durable execution ARN
    Task<string> StartAsync(
        TInput input, TimeSpan? timeout = null, CancellationToken cancellationToken = default)

    // Drive a started workflow to a terminal state
    Task<TestResult<TOutput>> WaitForResultAsync(
        string durableExecutionArn, TimeSpan? timeout = null, CancellationToken cancellationToken = default)
    ```

    `StartAsync` drives the workflow to its first suspension point and returns the ARN.
    `WaitForResultAsync` must be preceded by a `StartAsync` on the same runner; it throws
    `InvalidOperationException` otherwise. Use these together with `WaitForCallbackAsync`
    and the `SendCallback*` methods to drive callback workflows.

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

=== "Go"

    Inspect operations through the result.

    ```go
    func (r *TestResult) Operation(name string) *TestOperation
    func (r *TestResult) OperationByNameAndIndex(name string, index int) *TestOperation
    func (r *TestResult) OperationByIndex(index int) *TestOperation
    func (r *TestResult) OperationByID(id string) *TestOperation
    func (r *TestResult) OperationsByType(opType string) []TestOperation
    ```

    **Parameters:**

    - `name` (required) The operation name. `Operation` returns the first match.
    - `index` (required) A zero-based index. For `OperationByNameAndIndex` it counts
        operations with that name. For `OperationByIndex` it counts all operations.
    - `id` (required) The operation's wire ID.
    - `opType` (required) An operation type string, such as `"STEP"`.

    **Returns:** A `*TestOperation`, or nil if not found. `OperationsByType` returns a
    `[]TestOperation`.

=== "Rust"

    Take operation handles from the `RunHandle` that `run` returns. A handle names an
    operation by name, index, or ID, and finds it each time you use the handle. So you
    can take a handle before the operation exists. After the run,
    `result.operation(name)` and `result.operations_named(name)` find operations by
    name, `result.children(parent)` finds the children of an operation, and
    `result.operations()` returns the whole history in first-checkpoint order (see
    [Operations](#operations)).

    ```rust
    pub fn operation(&self, name: &str) -> OperationHandle;
    // ...
    pub fn operation_by_index(&self, index: usize) -> OperationHandle;
    // ...
    pub fn operation_by_name_and_index(&self, name: &str, index: usize) -> OperationHandle;
    // ...
    pub fn operation_by_id(&self, id: &str) -> OperationHandle;
    // ...
    pub fn snapshot(&self) -> Option<TestOperation>;
    ```

    **Parameters:**

    - `name` (required) The operation name, matched exactly. `operation` names the first
        operation with that name.
    - `index` (required) A zero-based index, in `TestResult::operations` order. For
        `operation_by_name_and_index` it counts operations with that name. For
        `operation_by_index` it counts all operations.
    - `id` (required) The unique operation ID.

    **Returns:** An `OperationHandle`. Its `snapshot()` returns the operation as it is
    now, as a `TestOperation`, or `None` while no operation matches.

=== "C#"

    The .NET runner does not expose operation lookups; inspect operations through the
    returned [`TestResult<TOutput>`](#testresult) instead:

    ```csharp
    TestStep GetStep(string name)      // throws if not found
    TestStep? FindStep(string name)    // null if not found
    IReadOnlyList<TestStep> GetSteps(string name)
    TestStep GetStepById(string operationId)
    IReadOnlyList<TestStep> GetStepsByStatus(string status)
    IReadOnlyList<TestStep> GetChildren(TestStep parent)
    ```

    `result.Steps` also exposes the full list of recorded operations (excluding the
    top-level execution). See [TestResult](#testresult).

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

=== "Go"

    Drive callbacks from the runner by callback ID. Enumerate the pending callbacks with
    `OpenCallbacks`, then resolve one. A failure takes an `errorType` and an
    `errorMessage` string.

    ```go
    func (r *LocalRunner[I, O]) OpenCallbacks() []OpenCallback
    func (r *LocalRunner[I, O]) SendCallbackSuccess(callbackID string, payload any) error
    func (r *LocalRunner[I, O]) SendCallbackFailure(callbackID, errorType, errorMessage string) error
    func (r *LocalRunner[I, O]) SendCallbackHeartbeat(callbackID string) error
    func (r *LocalRunner[I, O]) TimeoutCallback(callbackID string) error

    type OpenCallback struct {
    	CallbackID string
    	Name       string
    }
    ```

    `SendCallbackSuccess` encodes `payload` as JSON. So a Go string `"approved"` arrives
    as `"approved"` with its quote characters.
    `OpenCallback.Name` is the callback operation's name. A `WaitForCallback` records an
    unnamed inner callback, so its `Name` is empty. The `WaitForCallback` name is on the
    callback's parent operation.

=== "Rust"

    Answer a callback in one of two ways. Queue a reply on the runner before the run
    with `callback` or `callback_for`, or send one during the run from an operation
    handle (see [Drive a callback from an
    operation](#drive-a-callback-from-an-operation)). Both take a
    `durable::test_util::CallbackReply`.

    ```rust
    pub fn callback(mut self, reply: CallbackReply) -> Self;
    // ...
    pub fn callback_for(mut self, name: impl Into<String>, reply: CallbackReply) -> Self;
    ```

    A pending callback is a callback that waits for a reply. Between invocations, the
    runner visits the pending callbacks in the order they were first checkpointed. It
    gives each one the first queued `callback_for` reply that names it, else the first
    queued `callback` reply, else nothing. A name matches the callback's own name, or
    the name of the `wait_for_callback` that contains it. Queued replies arrive before
    the virtual clock moves. When a `callback_for` reply matches no callback by the end
    of the run, the result's `error_message()` names it.

    ```rust
    impl CallbackReply {
        pub fn success<T: Serialize>(value: &T) -> Result<Self, BoxError>;
        // ...
        pub fn raw(payload: impl Into<String>) -> Self;
        // ...
        pub fn failure(failure: CallbackFailure) -> Self;
        // ...
        pub fn timeout() -> Self;
        // ...
        pub fn heartbeat_timeout() -> Self;
        // ...
    }
    ```

    - `success(value)` The callback succeeds with the JSON encoding of `value`. Returns
        an error when `value` does not encode as JSON.
    - `raw(payload)` The callback succeeds with `payload` unchanged. The handler decodes
        it with the callback's [serdes](../sdk-reference/state/serialization.md).
    - `failure(failure)` The callback fails with the error fields of `failure`.
    - `timeout()` The callback times out with the message `Callback timed out`.
    - `heartbeat_timeout()` The callback times out with the error type
        `Callback.Heartbeat` and the message `Callback timed out on heartbeat`.

    ```rust
    impl CallbackFailure {
        pub fn new(error_type: impl Into<String>, error_message: impl Into<String>) -> Self;
        // ...
        pub fn with_error_data(mut self, error_data: impl Into<String>) -> Self;
        // ...
        pub fn with_stack_trace(mut self, frames: Vec<String>) -> Self;
        // ...
    }
    ```

    `CallbackFailure::new` sets the error type and message. `with_error_data` and
    `with_stack_trace` add the error data and the stack trace frames.
    `CallbackFailure::default()` leaves every field unset.

=== "C#"

    Callbacks are driven from the runner. After `StartAsync`, wait for the callback ID, then
    send success, failure, or a heartbeat:

    ```csharp
    // Wait for a pending callback and return its ID
    Task<string> WaitForCallbackAsync(
        string durableExecutionArn, string? name = null,
        TimeSpan? timeout = null, CancellationToken cancellationToken = default)

    // Complete a callback with a success result
    Task SendCallbackSuccessAsync<TResult>(
        string callbackId, TResult result, CancellationToken cancellationToken = default)

    // Fail a callback
    Task SendCallbackFailureAsync(
        string callbackId, ErrorObject? error = null, CancellationToken cancellationToken = default)

    // Keep a callback alive
    Task SendCallbackHeartbeatAsync(
        string callbackId, CancellationToken cancellationToken = default)
    ```

    There is no separate "time out" method: to simulate a timeout, fail the callback with an
    `ErrorObject` or let the configured callback timeout elapse.

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

=== "Go"

    Resolve a chained invoke by its operation name, or register the target so the invoke
    runs for real (see the next section).

    ```go
    func (r *LocalRunner[I, O]) CompleteChainedInvoke(name string, payload any) error
    func (r *LocalRunner[I, O]) FailChainedInvoke(name, errorType, errorMessage string) error
    func (r *LocalRunner[I, O]) TimeoutChainedInvoke(name string) error
    ```

=== "Rust"

    Register a target for the invoked function. See [Register mock handlers for
    invoke](#register-mock-handlers-for-invoke). An invoke of a function name with no
    registered target fails the execution, and the result's `error_message()` names the
    function.

=== "C#"

    The .NET SDK does not expose per-invoke completion methods. Instead, register the
    invoked function's handler on the runner and let the real invoke path run against it. See
    [Register mock handlers for invoke](#register-mock-handlers-for-invoke). To make an
    invoke fail, throw from the registered handler.

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

=== "Go"

    Register the function identifier the handler passes to `durable.Invoke`.
    Build the target with `DurableFunction` for a durable handler or
    `PlainFunction` for a one-shot handler. Registered targets may invoke other
    registered identifiers up to `MaxInvokeDepth` levels deep.

    ```go
    func (r *LocalRunner[I, O]) RegisterFunction(functionID string, fn Function)
    func DurableFunction[I, O any](handler durable.Handler[I, O], opts ...durable.HandlerOption) Function
    func PlainFunction[I, O any](handler func(context.Context, I) (O, error)) Function
    const MaxInvokeDepth = 10
    ```

=== "Rust"

    ```rust
    pub fn register_function<I, O, F, Fut>(mut self, name: impl Into<String>, function: F) -> Self
    where
        I: DeserializeOwned + Send + 'static,
        O: Serialize + Send + 'static,
        F: Fn(I) -> Fut + Send + Sync + 'static,
        Fut: Future<Output = Result<O, BoxError>> + Send + 'static;
    // ...
    pub fn register_durable_function<E, O, F, Fut>(
        mut self,
        name: impl Into<String>,
        handler: F,
    ) -> Self
    where
        E: Serialize + DeserializeOwned + Send + 'static,
        O: Serialize + DeserializeOwned + Send + 'static,
        F: Fn(E, DurableContext) -> Fut + Send + Sync + 'static,
        Fut: Future<Output = Result<O, BoxError>> + Send;
    ```

    Both methods return the runner for chaining. `name` is the function name the handler
    passes to `ctx.invoke`, matched exactly. A later registration under the same name
    replaces the earlier one. When an invocation suspends, the runner runs the target of
    each pending invoke once, before the virtual clock moves.

    `register_function` registers a plain function. The runner decodes its input from
    the invoke payload and encodes its output as JSON. An `Err` fails the invoke.
    `register_durable_function` runs the handler as its own durable execution, with a
    fresh in-memory backend and a copy of the runner's settings. The runner gives that
    execution none of its registered functions, queued callback replies, or injected
    checkpoint failures. So a registered durable target cannot invoke another registered
    target.

=== "C#"

    ```csharp
    // Register a durable sibling that the workflow invokes with context.InvokeAsync()
    DurableTestRunner<TInput, TOutput> RegisterDurableFunction<TPayload, TResult>(
        string functionNameOrArn,
        Func<TPayload, IDurableContext, Task<TResult>> handler)

    // Register a plain (non-durable) Lambda sibling, also invoked with context.InvokeAsync()
    DurableTestRunner<TInput, TOutput> RegisterFunction<TPayload, TResult>(
        string functionNameOrArn,
        Func<TPayload, ILambdaContext, Task<TResult>> handler)
    ```

    Both methods return the runner for chaining.

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

=== "Go"

    To exercise failure and retry, return an error from a step. `OmitTokenOnCheckpoint`
    withholds the checkpoint token on the n-th checkpoint call, so the invocation ends
    `PENDING` and resumes on the next run. `n` counts calls from now, starting at 1. A
    value below 1 panics.

    ```go
    func (r *LocalRunner[I, O]) OmitTokenOnCheckpoint(n int)
    ```

=== "Rust"

    These builder methods make the in-memory backend fail or delay checkpoint calls.

    ```rust
    pub fn fail_next_checkpoints(mut self, count: usize) -> Self;
    // ...
    pub fn fail_checkpoints_after(mut self, skip: usize, count: usize) -> Self;
    // ...
    pub fn fail_checkpoints_after_retryable(mut self, skip: usize, count: usize) -> Self;
    // ...
    pub fn withdraw_checkpoints_after(mut self, n: usize) -> Self;
    // ...
    pub fn checkpoint_latency(mut self, latency: std::time::Duration) -> Self;
    ```

    - `fail_next_checkpoints(count)` Rejects the next `count` checkpoint calls with a
        non-retryable error and stores nothing for them. The SDK sends no further
        checkpoint and fails the execution with the error type
        `CheckpointUnrecoverableExecutionError`.
    - `fail_checkpoints_after(skip, count)` Lets `skip` checkpoint calls through, then
        rejects the next `count` calls in the same way.
    - `fail_checkpoints_after_retryable(skip, count)` Lets `skip` checkpoint calls
        through, then rejects the next `count` calls with a retryable error. The
        invocation fails, the runner invokes again, and replay resumes from the stored
        state.
    - `withdraw_checkpoints_after(n)` Answers checkpoint request `n + 1` without a
        checkpoint token, once. Requests count from 1 across invocations. The backend
        applies that request, the SDK sends nothing more in that invocation, and the
        runner invokes again at once.
    - `checkpoint_latency(latency)` Delays each checkpoint response by `latency` in
        Tokio time, to expose races. Defaults to zero. It does not move the virtual
        clock.

    The three `fail_*` methods set one shared failure plan, so call only one of them on
    a runner.

=== "C#"

    The .NET SDK does not expose checkpoint-manipulation methods. To exercise failure and
    retry paths, throw from inside a step and assert on `TestStep.Attempt` and the recorded
    operation status. See the retry example in [Authoring](authoring.md).

### Control time

=== "TypeScript"

    Pass `{ skipTime: true }` to `setupTestEnvironment()`. Both `context.wait()` delays and
    step retry delays complete in zero wall-clock time.

=== "Python"

    Pass `skip_time=True` to `DurableFunctionTestRunner`. The runner uses a virtual clock
    that skips modeled delays instantly. Both `context.wait()` durations and step retry
    delays complete in zero wall-clock time. `skip_time` defaults to `True`.

    ```python
    runner = DurableFunctionTestRunner(handler=handler, skip_time=True)
    ```

    Set `skip_time=False` to assert on real wait durations.

=== "Java"

    ```java
    void advanceTime()
    ```

    `runUntilComplete()` calls `advanceTime()` after each invocation. When you call `run()`
    directly, call `advanceTime()` yourself between invocations. `advanceTime()` marks
    PENDING step retries as READY and completes STARTED waits without real-time sleeps.

=== "Go"

    Time is virtual and always skipped. `RunUntilComplete` advances timer-blocked
    operations between invocations. `CompletePendingTimers` fires every pending retry
    and wait timer at once and reports whether any fired. To check a wait's duration,
    read `WaitDetails.WaitSeconds`.

    ```go
    func (r *LocalRunner[I, O]) CompletePendingTimers() bool
    ```

=== "Rust"

    Time is virtual and always skipped. The runner keeps a virtual clock in whole
    seconds that starts at 0 for each run. Between invocations, it moves the clock to
    the earliest pending wait or retry end and fires every wait and retry due by then,
    so waits and retries end in time order. Time moves only between invocations. To
    check a wait's duration, read `WaitDetails::duration` (see [Wait
    details](#wait-details)).

=== "C#"

    Set `SkipTime` on `TestRunnerOptions`. When true (the default), both `WaitAsync` delays
    and step/`WaitForConditionAsync` retry backoffs complete immediately instead of sleeping
    for real wall-clock time. There is no manual `advanceTime()` call. `RunAsync` advances
    time internally.

    ```csharp
    await using var runner = new DurableTestRunner<object, string>(
        Workflow,
        new TestRunnerOptions { SkipTime = true });
    ```

    Set `SkipTime = false` to assert on real wait durations.

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

=== "Go"

    `Reset` discards the checkpoint log, the recorded events, and every open
    callback and invoke, while keeping the handler, its options, and the
    registered functions, so one runner serves several cases.

    ```go
    func (r *LocalRunner[I, O]) Reset()
    ```

=== "Rust"

    A runner holds only settings, and `run` borrows it. Each run starts from an empty
    history and gets its own copy of the queued callback replies, so one runner serves
    several cases with no reset.

=== "C#"

    There is no `reset()`. A runner is single-use and not thread-safe: create a new
    `DurableTestRunner<TInput, TOutput>` per test and dispose it with `await using`.

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

    ```python
    DurableFunctionTestRunner(handler, skip_time=True, poll_interval=1.0)
    ```

    **Fields:**

    - `skip_time` (optional) Use a virtual clock that skips modeled delays instantly.
        Defaults to `True`.
    - `poll_interval` (optional) Seconds between internal polling cycles. Defaults to `1.0`.

=== "Java"

    Not applicable. Configure with `withDurableConfig`, `withOutputType`, and
    `advanceTime()` on the runner instance.

=== "Go"

    The only run option is `WithMaxInvocations` on `RunUntilComplete`. Handler options
    come from `durable.HandlerOption` at construction. At the cap, `RunUntilComplete`
    returns its last result with `CapReached` set to `true`.

    ```go
    type RunnerOption func(*runnerConfig)
    func WithMaxInvocations(n int) RunnerOption
    const DefaultMaxInvocations = 100
    ```

=== "Rust"

    Configure the runner with builder methods on `LocalRunner`. Each takes the runner
    and returns it. These methods set how the in-memory backend pages the execution
    state:

    ```rust
    pub fn single_page(mut self) -> Self;
    // ...
    pub fn initial_page_size(mut self, size: usize) -> Self;
    // ...
    pub fn checkpoint_page_size(mut self, size: usize) -> Self;
    ```

    **Methods:**

    - `options(options)` The production handler's `durable::Options`. Defaults to
        `Options::default()`. See [Create a runner](#create-a-runner).
    - `max_invocations(max)` The invocation cap. Defaults to 100. See [Run the
        handler](#run-the-handler).
    - `single_page()` Serves the whole execution state in one page. By default the
        backend pages the state, as the service does.
    - `initial_page_size(size)` The number of operations in the first page of state that
        each invocation receives. Defaults to 1. A value of 0 counts as 1.
    - `checkpoint_page_size(size)` The number of stored operations above which a
        checkpoint response is paged. Defaults to 1. A value of 0 counts as 1.
    - `checkpoint_latency(latency)` The delay of each checkpoint response, in Tokio
        time. Defaults to zero. See [Simulate failures](#simulate-failures).
    - `fail_next_checkpoints`, `fail_checkpoints_after`,
        `fail_checkpoints_after_retryable`, and `withdraw_checkpoints_after` Injected
        checkpoint failures. None by default. See [Simulate
        failures](#simulate-failures).
    - `callback` and `callback_for` Queued callback replies. None by default. See [Drive
        callbacks](#drive-callbacks).
    - `register_function` and `register_durable_function` Invoke targets. None by
        default. See [Register mock handlers for
        invoke](#register-mock-handlers-for-invoke).

    Time is always virtual. See [Control time](#control-time).

=== "C#"

    Configuration lives in the `TestRunnerOptions` record passed to the constructor:

    ```csharp
    public sealed record TestRunnerOptions
    {
        public bool SkipTime { get; init; } = true;
        public int MaxInvocations { get; init; } = 100;
        public TimeSpan DefaultTimeout { get; init; } = TimeSpan.FromSeconds(30);
        public ILambdaSerializer? Serializer { get; init; }
        public ILoggerFactory? LoggerFactory { get; init; }
        public string DurableExecutionArn { get; init; } // synthetic test ARN by default
    }
    ```

    **Fields:**

    - `SkipTime` (optional) Complete waits and retry backoffs instantly. Defaults to `true`
        (the opposite of the JavaScript SDK, where time-skipping is opt-in).
    - `MaxInvocations` (optional) Maximum handler invocations before throwing
        `TestExecutionLimitException`. Defaults to `100`.
    - `DefaultTimeout` (optional) Wall-clock timeout per `RunAsync`/`WaitForResultAsync`
        call. Defaults to 30 seconds.
    - `Serializer` (optional) An `ILambdaSerializer` for result deserialization. Defaults to
        `DefaultLambdaJsonSerializer`.
    - `LoggerFactory` (optional) An `ILoggerFactory` for runtime logging.
    - `DurableExecutionArn` (optional) The ARN used in the test context. Defaults to a
        synthetic test ARN.

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

=== "Go"

    Read the `Status` field. It is one of three `ExecutionStatus` values. Compare it
    against the constants, for example `result.Status == durabletest.Succeeded`.

    ```go
    Status ExecutionStatus
    ```

=== "Rust"

    ```rust
    impl<O> TestResult<O> {
        // ...
        pub fn status(&self) -> ExecutionStatus;
        // ...
        pub fn is_success(&self) -> bool;
        // ...
        pub fn is_failure(&self) -> bool;
        // ...
        pub fn is_suspended(&self) -> bool;
        // ...
    }
    ```

    **Returns:** `status()` returns a `durable::test_util::ExecutionStatus` (see
    [Execution status](#execution-status)). `is_success()` is `true` for `Succeeded`.
    `is_failure()` is `true` for `Failed`, `TimedOut`, and `Stopped`. `is_suspended()`
    is `true` for `Running`. Under `LocalRunner`, a run that ends because nothing can
    advance the execution, or because it reached the invocation cap, has status
    `Running`.

=== "C#"

    ```csharp
    InvocationStatus Status { get; }
    bool IsSucceeded { get; }   // Status == InvocationStatus.Succeeded
    bool IsFailed { get; }      // Status == InvocationStatus.Failed
    ```

    **Type:** `InvocationStatus` (`Succeeded`, `Failed`, `Pending`). There is no separate
    execution-status enum in .NET; the cloud runner maps the service's finer terminal states
    (`FAILED`, `TIMED_OUT`, `STOPPED`) onto `InvocationStatus.Failed`.

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

=== "Go"

    `RawResult` holds the JSON result when the status is `SUCCEEDED`. Deserialize
    it with the package generic `ResultAs[O]`, which returns an error if the
    execution did not succeed.

    ```go
    RawResult string
    func ResultAs[O any](r *TestResult) (O, error)
    ```

=== "Rust"

    ```rust
    pub fn output(&self) -> Option<&O>;
    // ...
    pub fn into_output(self) -> Option<O>;
    ```

    **Returns:** The handler output, already decoded into the handler's output type `O`,
    when the status is `Succeeded`, and `None` otherwise. `output()` borrows it and
    `into_output()` takes it.

    **Errors:** When the output of a succeeded execution does not decode into `O`,
    awaiting the `RunHandle` returns `Err(TestRunnerError::OutputDeserialization)`.

=== "C#"

    ```csharp
    TOutput? Result { get; }
    ```

    The deserialized execution output when `Status` is `InvocationStatus.Succeeded`;
    otherwise the default value for `TOutput`. The result type is fixed at the runner's
    `TOutput` type parameter, so no per-call type argument is needed. Call
    `EnsureSucceeded()` first if you want a failed run to throw before you read `Result`.

    ```csharp
    result.EnsureSucceeded();
    Assert.Equal("hello", result.Result);
    ```

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

=== "Go"

    `Error` holds a `*TestError` when the status is `FAILED`. This is
    durabletest's own error type, not the SDK's `durable.ErrorObject`.

    ```go
    Error *TestError
    ```

=== "Rust"

    ```rust
    pub fn error(&self) -> Option<&crate::WireError>;
    // ...
    pub fn error_type(&self) -> Option<&str>;
    // ...
    pub fn error_message(&self) -> Option<&str>;
    ```

    **Returns:** `error()` returns the error as a `durable::WireError` (see
    [TestResultError](#testresulterror)), or `None` when the execution succeeded. When
    the execution failed, it is the recorded error. While the status is `Running`, it
    says why the run ended. `error_type()` and `error_message()` read the same record.
    Under `LocalRunner`, a failure recorded with no error type reads `Error`.

=== "C#"

    ```csharp
    ErrorObject? Error { get; }
    ```

    The error when `Status` is `InvocationStatus.Failed`; otherwise `null`. `ErrorObject`
    comes from `Amazon.Lambda.DurableExecution` and exposes `ErrorType`, `ErrorMessage`,
    `ErrorData`, and `StackTrace`.

    ```csharp
    Assert.True(result.IsFailed);
    Assert.NotNull(result.Error);
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

=== "Go"

    `Operations` lists every operation the execution checkpointed, in checkpoint order.
    It includes nested operations and excludes the execution operation. A nested
    operation's `ParentID` names its parent. To filter by status, iterate and compare
    `Status`. `OperationsByType` filters by type string.

    ```go
    Operations []TestOperation
    func (r *TestResult) OperationsByType(opType string) []TestOperation
    ```

=== "Rust"

    ```rust
    pub fn operations(&self) -> &[TestOperation];
    // ...
    pub fn operation(&self, name: &str) -> Option<&TestOperation>;
    // ...
    pub fn operations_named<'a>(
        &'a self,
        name: &'a str,
    ) -> impl Iterator<Item = &'a TestOperation> + 'a;
    // ...
    pub fn children<'a>(
        &'a self,
        parent: &'a TestOperation,
    ) -> impl Iterator<Item = &'a TestOperation> + 'a;
    ```

    `operations()` lists every operation the execution checkpointed, in first-checkpoint
    order. It includes nested operations and excludes the execution operation. To filter
    by status, filter the slice, for example
    `result.operations().iter().filter(|op| op.status() == durable::OperationStatus::Succeeded)`.

    **Parameters:**

    - `name` (required) The operation name, matched exactly. `operation` returns the
        first match, and `operations_named` returns every match.
    - `parent` (required) An operation of this result. `children` returns the operations
        whose parent is `parent`.

    **Returns:** A slice or an iterator of `TestOperation`. `operation` returns `None`
    when no operation has the name.

=== "C#"

    ```csharp
    IReadOnlyList<TestStep> Steps { get; }   // all operations except the top-level execution

    TestStep GetStep(string name)                 // throws if not found
    TestStep? FindStep(string name)               // null if not found
    IReadOnlyList<TestStep> GetSteps(string name) // all matches (parallel branches, map items)
    TestStep GetStepById(string operationId)
    IReadOnlyList<TestStep> GetStepsByStatus(string status)  // pass OperationStatus constants
    IReadOnlyList<TestStep> GetChildren(TestStep parent)
    ```

    Filter `Steps` by kind with LINQ, e.g.
    `result.Steps.Where(s => s.Kind == OperationKind.Step)`. Use the `OperationStatus`
    string constants (e.g. `OperationStatus.Succeeded`) with `GetStepsByStatus`.

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

=== "Go"

    `Events` is the execution's history event sequence. `EventTypes` returns the
    event type names in order. The local runner synthesizes the events and leaves
    fields it has no source for unset.

    ```go
    Events []types.Event
    func (r *TestResult) EventTypes() []string
    ```

=== "Rust"

    ```rust
    pub fn history_events(&self) -> &[Event];
    ```

    `Event` is the service model type `durable::lambda_types::Event`, and
    `event.event_type()` returns its `durable::lambda_types::EventType`. `CloudRunner`
    returns the events of `GetDurableExecutionHistory` unchanged. `LocalRunner`
    synthesizes the events with the same service model types, with one
    `InvocationCompleted` event for each invocation. Its event sequence can differ from
    the history Lambda records. For example, a `wait_for_condition` check that asks to
    continue is `StepFailed` under `LocalRunner` and `StepSucceeded` on Lambda, so assert
    an event sequence only for the runner that produced it. `TestOperation::events()`
    returns the events of one operation (see [Common accessors](#common-accessors)).

=== "C#"

    The .NET `TestResult<TOutput>` does not expose raw history events. Assert on the folded
    operations through `Steps` and the `TestStep` accessors instead. (Internally the cloud
    runner reconstructs operations from the history event stream, but that stream is not
    surfaced on the result.)

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

=== "Go"

    `Invocations` records one entry per completed invocation. Both runners
    populate it, so a cross-backend test can assert on `len(result.Invocations)`.

    ```go
    Invocations []TestInvocation

    type TestInvocation struct {
    	RequestID string
    	StartTime time.Time
    	EndTime   time.Time
    	Error     *TestError
    }
    ```

=== "Rust"

    ```rust
    pub fn invocation_count(&self) -> usize;
    // ...
    pub fn invocations(&self) -> &[TestInvocation];
    ```

    `invocations()` holds one `TestInvocation` for each `InvocationCompleted` history
    event, in history order. Under `LocalRunner`, invocation `n`, counting from 1, has
    the request ID `local-invocation-{n}`, and the list has `invocation_count()`
    records. Under `CloudRunner`, `invocation_count()` counts the `InvocationCompleted`
    events, or the number of polls when the history holds no such event.

    ```rust
    impl TestInvocation {
        // ...
        pub fn start_time(&self) -> Option<std::time::SystemTime>;
        // ...
        pub fn end_time(&self) -> Option<std::time::SystemTime>;
        // ...
        pub fn request_id(&self) -> Option<&str>;
        // ...
        pub fn error(&self) -> Option<&crate::WireError>;
        // ...
    }
    ```

    When an `InvocationCompleted` event omits its details, `start_time()`, `end_time()`,
    and `request_id()` return `None`. A time also returns `None` when its timestamp lies
    outside the range of `SystemTime`. `error()` is `None` for an invocation that
    returned a `SUCCEEDED` or `PENDING` result. For a `FAILED` result it holds the
    result's error.

=== "C#"

    ```csharp
    int? InvocationCount { get; }
    ```

    The number of handler invocations the local runner used to drive the workflow. It is
    `null` when not tracked. The cloud runner never tracks it, so do not assert on
    `InvocationCount` in tests intended to run against both backends.

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

=== "Go"

    `FormatTree` renders the operations as an indented tree. `WriteTree` writes the same
    text to an `io.Writer`. Both take optional columns, and `DefaultTreeColumns` returns
    the default set.

    ```go
    func (r *TestResult) FormatTree(columns ...TreeColumn) string
    func (r *TestResult) WriteTree(w io.Writer, columns ...TreeColumn) error
    func DefaultTreeColumns() []TreeColumn
    ```

=== "Rust"

    ```rust
    pub fn display_with(&self, columns: PrintColumns) -> impl std::fmt::Display + '_;
    ```

    `TestResult` implements `Display`, which prints a table of the operations with every
    column, so `println!("{result}")` writes it to stdout. `display_with` prints only
    the columns that `columns` enables.

    ```rust
    pub struct PrintColumns {
        pub parent_id: bool,
        pub name: bool,
        pub kind: bool,
        pub sub_type: bool,
        pub status: bool,
        pub start_time: bool,
        pub end_time: bool,
        pub duration: bool,
    }
    ```

    `PrintColumns::default()` enables every column. Set fields to `false` to hide
    columns. The struct is `#[non_exhaustive]`, so code outside the SDK builds it with
    `default()`. The columns, in order, are `parentId`, `name`, `type`, `subType`,
    `status`, `startTime`, `endTime`, and `duration`. The fields `parent_id`, `name`,
    `kind`, `sub_type`, `status`, `start_time`, `end_time`, and `duration` enable them.

    The table has a header line and one line per operation, with the cells separated by
    `|` and a space on each side. Times print as ISO 8601 UTC with three fractional
    digits. The duration prints in whole milliseconds, such as `1000ms`. An absent value
    prints `-`. A result with no operations prints `No operations found.`

=== "C#"

    The .NET SDK does not expose a pretty-print method on the result. Iterate `result.Steps`
    and format them yourself, e.g. with a `foreach` over `Name`, `Kind`, and `Status`.

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

=== "Go"

    The result error is `TestError`. It carries `Type`, `Message`, `ErrorData`,
    and `StackTrace`. It is distinct from the SDK's `durable.ErrorObject`.

    ```go
    type TestError struct {
    	Type       string
    	Message    string
    	ErrorData  string
    	StackTrace []string
    }
    ```

=== "Rust"

    ```rust
    impl WireError {
        // ...
        pub fn error_type(&self) -> Option<&str>;
        // ...
        pub fn error_message(&self) -> Option<&str>;
        // ...
        pub fn error_data(&self) -> Option<&str>;
        // ...
        pub fn stack_trace(&self) -> &[String];
        // ...
    }
    ```

    The result error is `durable::WireError`, the type the SDK also returns from
    `OperationError::wire()`. Its fields are private, so a test compares the accessors.
    `stack_trace()` is an empty slice when no frames were recorded. `Display` writes the
    type and the message as `type: message`, either one alone when the other is absent,
    or `unknown error` when both are absent.

=== "C#"

    Uses `ErrorObject` from `Amazon.Lambda.DurableExecution`:

    ```csharp
    public sealed class ErrorObject
    {
        public string? ErrorType { get; set; }
        public string? ErrorMessage { get; set; }
        public string? ErrorData { get; set; }
        public IReadOnlyList<string>? StackTrace { get; set; }
    }
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

=== "Go"

    `TestOperation` exposes public fields and an `IsTerminal` method. `Status`, `Type`,
    and `SubType` are strings. A detail pointer is non-nil only when the operation is of
    that kind. Child nesting is through `ParentID`.

    ```go
    type TestOperation struct {
    	ID        string
    	Name      string
    	Status    string
    	Type      string
    	SubType   string
    	ParentID  string
    	StartTime time.Time
    	EndTime   time.Time
    	StepDetails     *TestStepDetails
    	CallbackDetails *TestCallbackDetails
    	InvokeDetails   *TestInvokeDetails
    	ContextDetails  *TestContextDetails
    	WaitDetails     *TestWaitDetails
    }
    func (o *TestOperation) IsTerminal() bool
    ```

=== "Rust"

    `durable::test_util::TestOperation` is a copy of one operation record, read through
    accessor methods.

    ```rust
    impl TestOperation {
        // ...
        pub fn id(&self) -> &str;
        // ...
        pub fn events(&self) -> &[Event];
        // ...
        pub fn kind(&self) -> OperationKind;
        // ...
        pub fn sub_type(&self) -> Option<&str>;
        // ...
        pub fn status(&self) -> OperationStatus;
        // ...
        pub fn succeeded(&self) -> bool;
        // ...
        pub fn failed(&self) -> bool;
        // ...
        pub fn result(&self) -> Option<&str>;
        // ...
        pub fn error(&self) -> Option<&crate::WireError>;
        // ...
        pub fn error_type(&self) -> Option<&str>;
        // ...
        pub fn error_message(&self) -> Option<&str>;
        // ...
        pub fn name(&self) -> Option<&str>;
        // ...
        pub fn parent_id(&self) -> Option<&str>;
        // ...
        pub fn start_time(&self) -> Option<std::time::SystemTime>;
        // ...
        pub fn end_time(&self) -> Option<std::time::SystemTime>;
        // ...
        pub fn details(&self) -> TestOperationDetails<'_>;
        // ...
    }
    ```

    - `id()` The unique operation ID.
    - `events()` The history events of this operation, in history order.
    - `kind()` The `durable::OperationKind` (see [Operation type](#operation-type)).
    - `sub_type()` The sub-type, such as `"Step"` or `"WaitForCallback"`, or `None`. The
        constants of `durable::OperationSubType` hold the built-in values.
    - `status()` The `durable::OperationStatus` (see [Operation
        status](#operation-status)). `succeeded()` is `true` for `Succeeded`, and
        `failed()` is `true` for `Failed`.
    - `result()` The serialized result, JSON with the default serdes. Decode it with
        `serde_json::from_str`. Under `CloudRunner`, a result over the history's inline
        size limit is `None`.
    - `error()`, `error_type()`, and `error_message()` The recorded error (see
        [TestResultError](#testresulterror)).
    - `name()` The operation name, or `None` for an unnamed operation.
    - `parent_id()` The ID of the operation that contains this one, or `None` for a
        top-level operation. `TestResult::children` returns an operation's children.
    - `start_time()` and `end_time()` The start and end times. `end_time()` is `None`
        until the operation ends. Under `LocalRunner`, each time is the run's start time
        plus virtual seconds.
    - `details()` The details of the operation's kind.

    To test for a callback, compare `op.kind() == durable::OperationKind::Callback`. To
    test for a `wait_for_callback`, compare
    `op.sub_type() == Some(durable::OperationSubType::WAIT_FOR_CALLBACK.as_str())`.

    `details()` returns the variant of `TestOperationDetails` that matches `kind()`:

    ```rust
    pub enum TestOperationDetails<'a> {
        Execution,
        Context(&'a ContextDetails),
        Step(&'a StepDetails),
        Wait(&'a WaitDetails),
        Callback(&'a CallbackDetails),
        ChainedInvoke,
    }
    ```

=== "C#"

    The docs "Operation" type maps to `TestStep` in .NET. Common accessors:

    ```csharp
    string Id { get; }
    string? Name { get; }
    string? ParentId { get; }
    OperationKind Kind { get; }              // Step, Wait, Callback, ChainedInvoke, Context, Execution
    string? SubKind { get; }                 // e.g. "Parallel", "Map", "WaitForCallback"
    string Status { get; }                   // compare against OperationStatus constants
    int Attempt { get; }                     // 1-based for steps, 0 for other kinds
    DateTimeOffset? StartedAt { get; }
    DateTimeOffset? EndedAt { get; }
    TimeSpan? Duration { get; }
    IReadOnlyList<TestStep> Children { get; }
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

    // Retry attempt number (1-based)
    int getAttempt()
    ```

=== "Go"

    `op.StepDetails` carries the step's recorded state. `Attempt` counts the
    attempts that failed. A step that succeeds on its first attempt records 0. A
    step that fails twice and then succeeds records 2. A step that fails on its
    only attempt records 1. The cloud runner reads the value from the service's
    `RetryDetails.CurrentAttempt`. `Attempt` differs from the 1-based
    `StepContext.Attempt()` that the step body reads. Deserialize the step result
    with `OperationResultAs[T]`.

    ```go
    type TestStepDetails struct {
    	Attempt              int32
    	Result               string
    	ErrorType            string
    	ErrorMessage         string
    	NextAttemptTimestamp time.Time
    	ErrorData            string
    	StackTrace           []string
    }
    func OperationResultAs[O any](op *TestOperation) (O, error)
    ```

=== "Rust"

    ```rust
    impl StepDetails {
        pub fn attempt(&self) -> u32;
        // ...
        pub fn next_attempt_time(&self) -> Option<std::time::SystemTime>;
        // ...
    }
    ```

    Match the `TestOperationDetails::Step` variant of `details()`, for example
    `if let TestOperationDetails::Step(step) = op.details()`. Read the step's result and
    error on the operation (see [Common accessors](#common-accessors)).

    - `attempt()` The number of completed retries, counting from 0. A step that succeeds
        on its first attempt records 0. A step that fails on its only attempt records 0.
        A step that fails twice and then succeeds records 2. Both runners report the
        same value. Inside the step body, `StepContext::attempt()` counts attempts from
        1, so the third attempt reads 3 there and the record reads 2.
    - `next_attempt_time()` The time the next attempt was scheduled for after the latest
        retry, or `None` for a step that was never retried.

=== "C#"

    `TestStep` exposes step details through kind-aware accessors rather than a separate
    details object:

    ```csharp
    T? GetResult<T>()          // deserializes the step result
    ErrorObject? GetError()    // the step error, or null
    int Attempt { get; }       // retry attempt (1-based)
    ```

    `GetResult<T>()` routes to the right details block based on `Kind`, so it also works for
    chained-invoke, context, and callback operations.

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

=== "Go"

    `op.WaitDetails` carries `WaitSeconds` and `ScheduledEndTimestamp`.
    `WaitSeconds` comes from the wait's `WaitStarted` history event and is 0 when
    the history has no such event.

    ```go
    type TestWaitDetails struct {
    	WaitSeconds           int
    	ScheduledEndTimestamp time.Time
    }
    ```

=== "Rust"

    ```rust
    impl WaitDetails {
        pub fn duration(&self) -> Option<std::time::Duration>;
        // ...
        pub fn scheduled_end_time(&self) -> Option<std::time::SystemTime>;
        // ...
    }
    ```

    Match the `TestOperationDetails::Wait` variant of `details()`. `duration()` is the
    recorded wait duration in whole seconds. `scheduled_end_time()` is the time the wait
    was scheduled to end. Under `LocalRunner`, it is the virtual time the wait started
    plus its duration. Each is `None` when the history recorded no value.

=== "C#"

    ```csharp
    DateTimeOffset? GetWaitEndsAt()
    ```

    Returns the scheduled end time for a wait operation, or `null` for non-wait kinds.

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

=== "Go"

    `op.CallbackDetails` carries the callback ID, result, and error fields.

    ```go
    type TestCallbackDetails struct {
    	CallbackID   string
    	Result       string
    	ErrorType    string
    	ErrorMessage string
    	ErrorData    string
    	StackTrace   []string
    }
    ```

=== "Rust"

    ```rust
    impl CallbackDetails {
        pub fn callback_id(&self) -> Option<&str>;
        // ...
    }
    ```

    Match the `TestOperationDetails::Callback` variant of `details()`. `callback_id()`
    is the ID the handler sees. Read the callback's result and error on the operation. A
    `wait_for_callback` records a named `Context` operation with the sub-type
    `WaitForCallback`, whose child is an unnamed callback operation. Find that callback
    with `result.children(context)`.

=== "C#"

    ```csharp
    string? GetCallbackId()   // the callback ID for a callback operation, or null
    T? GetResult<T>()         // the delivered callback result
    ErrorObject? GetError()   // the callback error, or null
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

=== "Go"

    `op.InvokeDetails` carries the invoke result and error fields.

    ```go
    type TestInvokeDetails struct {
    	Result       string
    	ErrorType    string
    	ErrorMessage string
    	ErrorData    string
    	StackTrace   []string
    }
    ```

=== "Rust"

    A chained invoke has the `TestOperationDetails::ChainedInvoke` variant, which
    carries no data. Read the invoke's result and error on the operation with `result()`
    and `error()`.

=== "C#"

    ```csharp
    string? GetChainedInvokeFunctionName()   // invoked function name, or null
    T? GetResult<T>()                        // the invoke result
    ErrorObject? GetError()                  // the invoke error, or null
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

=== "Go"

    `op.ContextDetails` carries the context result and error fields. Find the context's
    children through `ParentID`.

    ```go
    type TestContextDetails struct {
    	Result         string
    	ReplayChildren bool
    	ErrorType      string
    	ErrorMessage   string
    	ErrorData      string
    	StackTrace     []string
    }
    ```

=== "Rust"

    ```rust
    impl ContextDetails {
        pub fn replay_children(&self) -> bool;
        // ...
    }
    ```

    Match the `TestOperationDetails::Context` variant of `details()`.
    `replay_children()` is `true` when the context closed with the `ReplayChildren`
    option. Only `LocalRunner` reports it, and `CloudRunner` reports `false`. Read the
    context's result and error on the operation, and its child operations with
    `TestResult::children`.

=== "C#"

    A child context is a `TestStep` with `Kind == OperationKind.Context`. Read its result
    and children through the shared accessors:

    ```csharp
    T? GetResult<T>()                    // the context result
    ErrorObject? GetError()              // the context error, or null
    IReadOnlyList<TestStep> Children { get; }   // steps recorded inside the context
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

=== "Go"

    The execution outcome is on the result as `Status`, `RawResult`, and `Error`.
    `Operations` excludes the execution operation.

=== "Rust"

    The execution outcome is on the result as `status()`, `output()`, and `error()`.
    `operations()` excludes the execution operation.

=== "C#"

    The .NET SDK does not expose execution details on a step. The top-level execution
    operation is excluded from `result.Steps`, and the execution-level outcome is surfaced
    on the result itself via `Status`, `Result`, and `Error`.

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

=== "Go"

    Drive callbacks from the runner. See [Drive callbacks](#drive-callbacks).

=== "Rust"

    Take an `OperationHandle` from the `RunHandle` (see [Inspect
    operations](#inspect-operations)). Await its controls first and the `RunHandle`
    last.

    ```rust
    impl OperationHandle {
        pub fn snapshot(&self) -> Option<TestOperation>;
        // ...
        pub async fn wait_for_data(
            &self,
            status: WaitingStatus,
        ) -> Result<TestOperation, TestRunnerError>;
        // ...
        pub async fn send_callback(&self, reply: CallbackReply) -> Result<(), TestRunnerError>;
        // ...
        pub async fn send_callback_heartbeat(&self) -> Result<(), TestRunnerError>;
        // ...
    }
    ```

    - `snapshot()` The operation as it is now, or `None` while no operation matches.
    - `wait_for_data(status)` Waits until the operation reaches `status` (see [Waiting
        operation status](#waiting-operation-status)), and returns the operation at that
        moment. It returns at once when the operation already reached the status.
    - `send_callback(reply)` Sends a `CallbackReply` (see [Drive
        callbacks](#drive-callbacks)). The target is the operation when it is a
        callback, or the callback inside it when it is a `wait_for_callback` context.
        Under `LocalRunner`, the reply applies at once.
    - `send_callback_heartbeat()` Sends a heartbeat to the same target. `LocalRunner`
        runs no heartbeat timer, so a heartbeat changes nothing there. It returns
        `TestRunnerError::NoHeartbeatTimeout` when the handler set no heartbeat timeout
        on the callback.

    **Errors:** `wait_for_data` returns `TestRunnerError::OperationNotReached` when the
    execution ends before the operation reaches the status. `send_callback` returns
    `OperationNotFound`, `NotACallback`, or `CallbackNotPending` when the handle names no
    pending callback. `send_callback_heartbeat` returns those errors when the handle
    cannot resolve a callback ID. Under `LocalRunner`, it also returns
    `CallbackNotPending` after the callback stops waiting and `NoHeartbeatTimeout` when
    the handler set no heartbeat timeout. Under `CloudRunner`, it sends the heartbeat
    without checking the operation status, and a service rejection returns `Aws`. Each
    async control also returns the run's own runner error.

=== "C#"

    Driven from the runner, not from the `TestStep`. Use `WaitForCallbackAsync` to get the
    callback ID and the `SendCallback*` methods to deliver a result. See
    [LocalDurableTestRunner: Drive callbacks](#drive-callbacks).

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

=== "Go"

    `ExecutionStatus` is a string type with three values. The cloud runner reports every
    terminal status other than `SUCCEEDED` as `Failed`.

    ```go
    type ExecutionStatus string
    const (
    	Succeeded ExecutionStatus = "SUCCEEDED"
    	Failed    ExecutionStatus = "FAILED"
    	Pending   ExecutionStatus = "PENDING"
    )
    ```

=== "Rust"

    `durable::test_util::ExecutionStatus`, which `TestResult::status()` returns:

    ```rust
    pub enum ExecutionStatus {
        Running,
        Succeeded,
        Failed,
        TimedOut,
        Stopped,
    }
    ```

    | Value       | Meaning                                                                     |
    | ----------- | --------------------------------------------------------------------------- |
    | `Running`   | The execution is still in progress                                          |
    | `Succeeded` | The handler returned a value                                                |
    | `Failed`    | The execution failed                                                        |
    | `TimedOut`  | The execution exceeded its execution timeout. Only `CloudRunner` reports it |
    | `Stopped`   | The execution was stopped. Only `CloudRunner` reports it                    |

    The enum is `#[non_exhaustive]`, so a `match` on it needs a `_` arm.

=== "C#"

    `InvocationStatus` from `Amazon.Lambda.DurableExecution`:

    | Value       | Meaning                          |
    | ----------- | -------------------------------- |
    | `Succeeded` | Execution completed successfully |
    | `Failed`    | Execution failed                 |
    | `Pending`   | Execution is waiting             |

    There is no separate execution-status enum. The cloud runner maps the service's
    `TIMED_OUT` and `STOPPED` terminal states onto `Failed`.

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

=== "Go"

    `TestOperation.Status` is a plain string. Compare it against the literal, for
    example `op.Status == "SUCCEEDED"`. `IsTerminal` recognizes the terminal set
    `SUCCEEDED`, `FAILED`, `CANCELLED`, `TIMED_OUT`, and `STOPPED`. Other observed
    values include `STARTED`, `PENDING`, and `READY`.

    ```go
    func (o *TestOperation) IsTerminal() bool
    ```

=== "Rust"

    `durable::OperationStatus`, which `TestOperation::status()` returns:

    ```rust
    pub enum OperationStatus {
        Started,
        Pending,
        Ready,
        Succeeded,
        Failed,
        Cancelled,
        TimedOut,
        Stopped,
    }

    impl OperationStatus {
        pub fn wire_str(self) -> &'static str;
        // ...
    }
    ```

    | Value       | `wire_str()` | Meaning                                                                             |
    | ----------- | ------------ | ----------------------------------------------------------------------------------- |
    | `Started`   | `STARTED`    | The operation started and is still in progress                                      |
    | `Pending`   | `PENDING`    | The operation waits for a retry delay to elapse                                     |
    | `Ready`     | `READY`      | The retry delay elapsed and the next attempt can run. Only `LocalRunner` reports it |
    | `Succeeded` | `SUCCEEDED`  | The operation completed successfully                                                |
    | `Failed`    | `FAILED`     | The operation failed                                                                |
    | `Cancelled` | `CANCELLED`  | The operation was cancelled                                                         |
    | `TimedOut`  | `TIMED_OUT`  | The operation reached its timeout                                                   |
    | `Stopped`   | `STOPPED`    | The operation was stopped                                                           |

    `wire_str()` returns the service's string for the status.
    `durable::lambda_types::OperationStatus` is the service model type of the same name,
    and `TestOperation` does not use it.

=== "C#"

    `OperationStatus` from `Amazon.Lambda.DurableExecution.Testing` is a static class of
    string constants (compare against `TestStep.Status`), not an enum:

    | Constant                    | Meaning                          |
    | --------------------------- | -------------------------------- |
    | `OperationStatus.Started`   | Operation is running             |
    | `OperationStatus.Succeeded` | Operation completed successfully |
    | `OperationStatus.Failed`    | Operation failed                 |
    | `OperationStatus.Pending`   | Operation is queued              |
    | `OperationStatus.Cancelled` | Operation was cancelled          |
    | `OperationStatus.TimedOut`  | Operation exceeded its timeout   |
    | `OperationStatus.Stopped`   | Operation was stopped            |
    | `OperationStatus.Ready`     | Operation is ready to resume     |

    ```csharp
    Assert.Equal(OperationStatus.Succeeded, result.GetStep("step-1").Status);
    ```

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

=== "Go"

    `TestOperation.Type` and `SubType` are strings, and `OperationsByType` takes a
    string. The type strings are `STEP`, `WAIT`, `CALLBACK`, `CHAINED_INVOKE`,
    `CONTEXT`, and `EXECUTION`.

    ```go
    func (r *TestResult) OperationsByType(opType string) []TestOperation
    ```

=== "Rust"

    `durable::OperationKind`, which `TestOperation::kind()` returns:

    ```rust
    pub enum OperationKind {
        Step,
        Wait,
        Context,
        Callback,
        ChainedInvoke,
        Execution,
    }

    impl OperationKind {
        pub fn wire_str(self) -> &'static str;
        // ...
    }
    ```

    | Value           | `wire_str()`     | Meaning                                                                              |
    | --------------- | ---------------- | ------------------------------------------------------------------------------------ |
    | `Step`          | `STEP`           | A step operation                                                                     |
    | `Wait`          | `WAIT`           | A wait operation                                                                     |
    | `Context`       | `CONTEXT`        | A child context, such as a `map` item, a `parallel` branch, or a `wait_for_callback` |
    | `Callback`      | `CALLBACK`       | A callback operation                                                                 |
    | `ChainedInvoke` | `CHAINED_INVOKE` | An invoke operation                                                                  |
    | `Execution`     | `EXECUTION`      | The root execution operation. `TestResult::operations()` never contains it           |

=== "C#"

    `OperationKind` from `Amazon.Lambda.DurableExecution.Testing` (read via `TestStep.Kind`):

    | Value                         | Meaning                      |
    | ----------------------------- | ---------------------------- |
    | `OperationKind.Step`          | A step operation             |
    | `OperationKind.Wait`          | A wait operation             |
    | `OperationKind.Callback`      | A callback operation         |
    | `OperationKind.ChainedInvoke` | An invoke operation          |
    | `OperationKind.Context`       | A child context              |
    | `OperationKind.Execution`     | The root execution operation |

    ```csharp
    var stepOps = result.Steps.Where(s => s.Kind == OperationKind.Step).ToList();
    ```

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

=== "Go"

    The test drives callbacks through the runner by callback ID. Call `OpenCallbacks`
    after `RunUntilComplete` returns `PENDING`.

=== "Rust"

    `durable::test_util::WaitingStatus`, which `OperationHandle::wait_for_data` takes:

    ```rust
    pub enum WaitingStatus {
        #[default]
        Started,
        Submitted,
        Completed,
    }
    ```

    | Value       | An operation reaches it when | A `wait_for_callback` context reaches it when             |
    | ----------- | ---------------------------- | --------------------------------------------------------- |
    | `Started`   | it exists                    | it has a step or callback child                           |
    | `Submitted` | it has a terminal status     | a step or callback child has a terminal status            |
    | `Completed` | it has a terminal status     | its callback child and the context have a terminal status |

    The terminal statuses are `Succeeded`, `Failed`, `Cancelled`, `TimedOut`, and
    `Stopped`. The default is `Started`.

=== "C#"

    The .NET SDK does not expose a waiting-operation-status enum. After `StartAsync`, call
    `WaitForCallbackAsync(arn, name)` to obtain the callback ID; the `OperationStatus`
    constants cover the operation lifecycle states.

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

=== "Go"

    `CloudRunner` is not generic. You choose the output type when you call `ResultAs`.
    It takes a caller-supplied `DurableExecutionAPI` client, typically a real
    `*lambda.Client`, so a test can pass a fake. `functionName` must be a qualified
    identifier. Build the client with `config.LoadDefaultConfig` and
    `lambda.NewFromConfig`.

    ```go
    func NewCloudRunner(api DurableExecutionAPI, functionName string, opts ...CloudRunnerOption) *CloudRunner
    ```

    **Parameters:**

    - `api` (required) A `DurableExecutionAPI`, typically a `*lambda.Client`.
    - `functionName` (required) The function name, ARN, or partial ARN, with a version or
        alias qualifier.
    - `opts` (optional) The `CloudRunnerOption` values `WithPollInterval` and
        `WithTimeout`.

    **Returns:** `*CloudRunner`

=== "Rust"

    `durable::test_util::CloudRunner` invokes a deployed durable function. Each setting
    is a builder method that takes the runner and returns it.

    ```rust
    impl CloudRunner {
        // ...
        pub fn new(function_name: impl Into<String>) -> Self;
        // ...
        pub fn region(mut self, region: impl Into<String>) -> Self;
        // ...
        pub fn qualifier(mut self, qualifier: impl Into<String>) -> Self;
        // ...
        pub fn lambda_client(mut self, client: crate::LambdaClient) -> Self;
        // ...
    }
    ```

    **Parameters:**

    - `function_name` (required) The function name or ARN.
    - `qualifier` (optional) The version or alias to invoke. Defaults to `$LATEST`.
    - `region` (optional) The AWS Region of the default Lambda client. Defaults to the
        Region of the ambient AWS configuration, such as `AWS_REGION`. The runner
        ignores it when you set `lambda_client`.
    - `lambda_client` (optional) A configured `durable::LambdaClient`, used for every
        call. Without it, the runner builds a client from the ambient AWS configuration
        when the run starts.

    **Returns:** `CloudRunner`

=== "C#"

    ```csharp
    CloudDurableTestRunner(
        string functionArn,
        IAmazonLambda? lambdaClient = null,
        CloudTestRunnerOptions? options = null)
    ```

    ```csharp
    await using var runner = new CloudDurableTestRunner<OrderInput, OrderResult>(
        "arn:aws:lambda:us-east-1:123456789012:function:my-fn:live");
    ```

    **Parameters:**

    - `functionArn` (required) The qualified function ARN (with alias, version, or
        `$LATEST`). A qualifier is required for durable functions.
    - `lambdaClient` (optional) An `IAmazonLambda` client. When null, the runner creates and
        owns an `AmazonLambdaClient` using the default credential chain and disposes it on
        `DisposeAsync`.
    - `options` (optional) A [`CloudTestRunnerOptions`](#clouddurabletestrunnerconfig)
        record for poll intervals, timeout, and serializer.

    The input and output types are the runner's type parameters, so no separate `inputType`
    or `outputType` argument is needed.

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

=== "Go"

    `Run` invokes the function and polls to a terminal status. It takes a
    `context.Context` and an `any` event. `RunWithArn` polls an execution started
    elsewhere. The timeout and poll interval are set at construction.

    ```go
    func (r *CloudRunner) Run(ctx context.Context, event any) (*TestResult, error)
    func (r *CloudRunner) RunWithArn(ctx context.Context, executionArn string) (*TestResult, error)
    ```

    **Parameters:**

    - `ctx` (required) A `context.Context`. Its deadline or cancellation ends the run.
    - `event` (required) The handler input. The runner encodes it as JSON.
    - `executionArn` (required) The ARN of an execution that is already running.

    **Returns:** `*TestResult` and an `error`.

    **Errors:** The error is non-nil, and the result nil, only when the runner itself
    fails. That happens when the event does not encode as JSON, the `Invoke` call fails,
    the invoke response has no `DurableExecutionArn`, polling fails or exceeds the
    `WithTimeout` limit, or `ctx` ends. An execution that fails produces a nil error and
    a result with status `FAILED`.

=== "Rust"

    ```rust
    pub fn run<E, O>(&self, event: E) -> RunHandle<O>
    where
        E: Serialize,
        O: DeserializeOwned + Send + 'static;
    ```

    `run` returns a `RunHandle<O>` at once. Awaiting it invokes the function, polls
    `GetDurableExecution` until the execution ends or the configured poll budget is
    spent, and reads the whole history into the result. A spent budget produces a result
    with status `ExecutionStatus::Running`. [Configure polling and
    timeouts](#configure-polling-and-timeouts) gives the budget and the attempts of each
    Lambda call. The runner ignores the invoke response's payload. No handler gives the
    output type, so name it at the call, for example `runner.run::<_, String>(event)`.

    **Parameters:**

    - `event` (required) The handler input. The runner encodes it as JSON and sends it
        as the invoke payload.

    **Returns:** `RunHandle<O>`. Awaiting it returns
    `Result<TestResult<O>, TestRunnerError>`.

    **Errors:** Awaiting the handle returns `Err(TestRunnerError)` only when the runner
    itself fails. That happens when the event does not encode as JSON
    (`EventSerialization`), a Lambda call fails on its last attempt (`Aws`), the invoke
    response carries no `DurableExecutionArn` (`NoExecutionArn`), or the output of a
    succeeded execution does not decode into `O` (`OutputDeserialization`). An execution
    that fails, times out, or stops produces `Ok` with a result of that status.

=== "C#"

    ```csharp
    Task<TestResult<TOutput>> RunAsync(
        TInput input,
        TimeSpan? timeout = null,
        CancellationToken cancellationToken = default)
    ```

    Invokes the deployed function (Event invocation), polls the durable-execution history
    until the execution reaches a terminal state, and returns the folded result. This is the
    same signature as the local runner, so tests written against
    `IDurableTestRunner<TInput, TOutput>` run unchanged on either backend.

    **Parameters:**

    - `input` (required) The handler input.
    - `timeout` (optional) Wall-clock timeout. Defaults to
        `CloudTestRunnerOptions.DefaultTimeout` (5 minutes).
    - `cancellationToken` (optional) A `CancellationToken`.

    **Returns:** `Task<TestResult<TOutput>>`

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

=== "Go"

    To poll an execution started elsewhere, for example an async invoke, use
    `RunWithArn`.

    ```go
    func (r *CloudRunner) RunWithArn(ctx context.Context, executionArn string) (*TestResult, error)
    ```

=== "Rust"

    `run` returns the `RunHandle` at once. To act on the execution before it ends, set
    `InvocationType::Event` (see
    [CloudDurableTestRunnerConfig](#clouddurabletestrunnerconfig)) and use the operation
    handles, as on `LocalRunner` (see [Drive a callback from an
    operation](#drive-a-callback-from-an-operation)). Await the operation controls first
    and the `RunHandle` last.

=== "C#"

    The .NET SDK does not return a dedicated async-execution handle. Use the same
    `StartAsync` + `WaitForResultAsync` pair as the local runner:

    ```csharp
    Task<string> StartAsync(
        TInput input, TimeSpan? timeout = null, CancellationToken cancellationToken = default)

    Task<TestResult<TOutput>> WaitForResultAsync(
        string durableExecutionArn, TimeSpan? timeout = null, CancellationToken cancellationToken = default)
    ```

    `StartAsync` fires an Event invocation and resolves the durable execution ARN by
    listing executions. Drive callbacks with `WaitForCallbackAsync` and the `SendCallback*`
    methods between the two calls.

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

=== "Go"

    Inspect operations through the result, the same accessors as the local
    runner. See [TestResult](#testresult).

=== "Rust"

    Take operation handles from the `RunHandle` with `operation`, `operation_by_index`,
    `operation_by_name_and_index`, and `operation_by_id`. See [Inspect
    operations](#inspect-operations) for their selectors and return type. Under
    `CloudRunner`, `snapshot()` returns the operation in the last history the runner read.
    After the run, use the result's lookups. See [Operations](#operations).

=== "C#"

    Inspect operations through the returned [`TestResult<TOutput>`](#testresult). The cloud
    result exposes the same `Steps` list and `GetStep`/`FindStep`/`GetSteps` accessors as the
    local runner. See [TestResult](#testresult).

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

=== "Go"

    Drive callbacks from the runner, with a callback ID from the execution's operations
    or history. These methods make the real `SendDurableExecutionCallback*` API calls.

    ```go
    func (r *CloudRunner) SendCallbackSuccess(callbackID string, payload any) error
    func (r *CloudRunner) SendCallbackFailure(callbackID, errorType, errorMessage string) error
    func (r *CloudRunner) SendCallbackHeartbeat(callbackID string) error
    ```

=== "Rust"

    Drive callbacks from an operation handle, as on `LocalRunner`. See [Drive a callback
    from an operation](#drive-a-callback-from-an-operation). The controls need
    `InvocationType::Event`. Under the default `RequestResponse`, `wait_for_data`,
    `send_callback`, and `send_callback_heartbeat` return `TestRunnerError::Unsupported`
    and make no Lambda call.

    `send_callback` reads the history again, checks that the callback is still
    `Started`, and calls `SendDurableExecutionCallbackSuccess` or
    `SendDurableExecutionCallbackFailure`. `send_callback_heartbeat` reads the history
    again to find the callback ID and calls `SendDurableExecutionCallbackHeartbeat`.
    When the poll budget is spent, both controls use the last history the runner read
    instead. `CallbackReply::timeout()` and `CallbackReply::heartbeat_timeout()` return
    `TestRunnerError::Unsupported`.

=== "C#"

    Callbacks are driven from the runner, same as the local runner. After `StartAsync`, call
    `WaitForCallbackAsync` and then `SendCallbackSuccessAsync`, `SendCallbackFailureAsync`,
    or `SendCallbackHeartbeatAsync`. On the cloud runner these issue the corresponding
    `SendDurableExecutionCallback*` Lambda API calls.

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

=== "Go"

    Configure with the constructor options `WithPollInterval` (default 2 seconds) and
    `WithTimeout` (default 5 minutes).

    ```go
    type CloudRunnerOption func(*cloudRunnerConfig)
    func WithPollInterval(d time.Duration) CloudRunnerOption
    func WithTimeout(d time.Duration) CloudRunnerOption
    ```

=== "Rust"

    ```rust
    pub fn poll_interval(mut self, interval: std::time::Duration) -> Self;
    // ...
    pub fn max_poll_attempts(mut self, max: usize) -> Self;
    // ...
    pub fn invocation_type(mut self, invocation_type: InvocationType) -> Self;
    ```

    - `poll_interval(interval)` The wait before each `GetDurableExecution` poll, before
        each history read of `wait_for_data`, and before each history page that follows
        a page with a next marker. Defaults to 1 second.
    - `max_poll_attempts(max)` The shared poll budget for `GetDurableExecution` polls
        and operation-control history reads. A poll counts once however many attempts it
        makes. A control's history read counts once however many pages and attempts it
        makes. The history read that builds the final result does not count. When the
        runner spends the budget while the execution still runs, the result has status
        `ExecutionStatus::Running`. A value of 0 counts as 1. By default the budget is
        unlimited, so the runner polls until the execution ends or a Lambda call fails
        on its last attempt.
    - `invocation_type(invocation_type)` How the runner invokes the function. Defaults
        to `InvocationType::RequestResponse`. See
        [CloudDurableTestRunnerConfig](#clouddurabletestrunnerconfig).

    Each `GetDurableExecution` call and each history page request makes up to three
    attempts on any error. The second attempt starts 1000 ms after the first fails, and
    the third starts 1450 ms after the second fails. The error of the third attempt is
    the error of the call. `Invoke` and the callback calls make one attempt.

    `max_poll_attempts` limits polls by count. To impose a wall-clock deadline, await the
    `RunHandle` under `tokio::time::timeout`.

=== "C#"

    Configure polling and timeout through the `CloudTestRunnerOptions` record passed to the
    constructor. Per-call timeouts can also be passed to `RunAsync`/`WaitForResultAsync`:

    ```csharp
    await using var runner = new CloudDurableTestRunner<OrderInput, OrderResult>(
        functionArn,
        options: new CloudTestRunnerOptions
        {
            InitialPollInterval = TimeSpan.FromMilliseconds(200),
            PollInterval = TimeSpan.FromSeconds(2),
            DefaultTimeout = TimeSpan.FromMinutes(5),
        });
    ```

    The invocation type is fixed to `Event` (fire-and-forget) so callback workflows do not
    deadlock; there is no `invocationType` knob.

### Reset between runs {#cloud-reset-between-runs}

=== "TypeScript"

    ```typescript
    reset(): void
    ```

=== "Python"

    Create a new runner instance per test.

=== "Java"

    Create a new runner instance per test.

=== "Go"

    Create a new `CloudRunner` per test.

=== "Rust"

    A runner holds only settings, and `run` borrows it. Each run starts a new execution,
    so one runner serves several cases with no reset.

=== "C#"

    There is no `reset()`. Create a new `CloudDurableTestRunner<TInput, TOutput>` per test
    and dispose it with `await using` (which disposes a runner-owned Lambda client).

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

=== "Go"

    Configure the runner with the `NewCloudRunner` options `WithPollInterval` and
    `WithTimeout`, shown under [Configure polling and
    timeouts](#configure-polling-and-timeouts). The cloud runner calls `Invoke` without
    an invocation type, so Lambda uses its default, `RequestResponse`.

=== "Rust"

    Configure the runner with the builder methods under [Create a
    runner](#cloud-create-a-runner) and [Configure polling and
    timeouts](#configure-polling-and-timeouts). `invocation_type` takes a
    `durable::test_util::InvocationType`:

    ```rust
    pub enum InvocationType {
        #[default]
        RequestResponse,
        Event,
    }
    ```

    - `RequestResponse` (default) A synchronous invoke. It returns after the execution
        ended or the invocation reached its timeout.
    - `Event` An asynchronous invoke. It returns once the execution started. The
        operation controls need it.

=== "C#"

    The .NET equivalent is the `CloudTestRunnerOptions` record:

    ```csharp
    public sealed record CloudTestRunnerOptions
    {
        public TimeSpan InitialPollInterval { get; init; } = TimeSpan.FromMilliseconds(200);
        public TimeSpan PollInterval { get; init; } = TimeSpan.FromSeconds(2);
        public TimeSpan DefaultTimeout { get; init; } = TimeSpan.FromMinutes(5);
        public ILambdaSerializer? Serializer { get; init; }
    }
    ```

    **Fields:**

    - `InitialPollInterval` (optional) Delay before the first poll retry; subsequent delays
        grow exponentially up to `PollInterval`. Defaults to 200 milliseconds.
    - `PollInterval` (optional) Maximum steady-state interval between polls. Defaults to
        2 seconds.
    - `DefaultTimeout` (optional) Wall-clock timeout for polling. Defaults to 5 minutes.
    - `Serializer` (optional) An `ILambdaSerializer` for payload and result serialization.
        Defaults to `DefaultLambdaJsonSerializer`.

## See also

- [Authoring](authoring.md) Set up the local runner and write your first test.
- [Assertions](assertions.md) Inspect steps, waits, and callbacks after a test run.
- [Workflow patterns](workflow-patterns.md) Complete tests for common workflow shapes.
- [Cloud Runner](cloud-runner.md) Run tests against a deployed Lambda function.
- [Runner](runner.md) How the replay loop and checkpointing work.
