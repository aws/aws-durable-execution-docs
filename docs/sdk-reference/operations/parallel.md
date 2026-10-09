# Parallel

## Concurrent branches

Parallel executes multiple operations concurrently. It manages concurrency, collects
results as branches complete, and checkpoints the outcome.

Each branch runs in its own [child context](child-context.md). The default nested mode
checkpoints that context and its result. Flat mode omits the per-branch context checkpoint
to reduce operation overhead.

Use parallel to execute independent tasks concurrently. Use [map](map.md) instead to
execute the same operation concurrently for each item in a collection.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/parallel/simple-parallel.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/parallel/simple-parallel.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/parallel/simple-parallel.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/parallel/simple-parallel.go"
    ```

    All branches must return the same type. For branches of different result
    types, use [`ParallelMixed`](#contextparallel).

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/parallel/simple-parallel.cs"
    ```

## Method signature

### context.parallel

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/parallel/parallel-signature.ts"
    ```

    **Parameters:**

    - `name` (optional) A name for the parallel operation. Pass `undefined` to omit.
    - `branches` An array of `ParallelFunc` or `NamedParallelBranch` objects.
    - `config` (optional) A `ParallelConfig<TOutput>` object.

    **Returns:** `DurablePromise<BatchResult<TOutput>>`. Use `await` to get the result.

    **Throws:** Branch exceptions are captured in the `BatchResult`. Call `throwIfError()`
    to re-throw the first failure.

    **`ParallelFunc` / `NamedParallelBranch`**

    Each element in `branches` is either a plain function or a named branch object:

    ```typescript
    type ParallelFunc<TResult> = (context: DurableContext) => Promise<TResult>

    interface NamedParallelBranch<TResult> {
      name?: string;
      func: ParallelFunc<TResult>;
    }
    ```

    - `name` (optional) A name for this branch.
    - `func` An async function receiving a `DurableContext` and returning
        `Promise<TResult>`.

    Use `NamedParallelBranch` to give an inline lambda a name without defining a named
    function.

=== "Python"

    ```python
    --8<-- "examples/python/operations/parallel/parallel-signature.py"
    ```

    **Parameters:**

    - `functions` A sequence of branches. Each element is either a plain callable
        `(ctx: DurableContext) -> T` or a `ParallelBranch[T]` wrapper that pairs a callable
        with a name.
    - `name` (optional) A name for the parallel operation.
    - `config` (optional) A `ParallelConfig` object.

    **Returns:** `BatchResult[T]`.

    **Raises:** Branch exceptions are captured in the `BatchResult`. Call `throw_if_error()`
    to re-raise the first failure.

    **`ParallelBranch`**

    Wrap a callable in `ParallelBranch` to give a branch a custom name in execution
    history without defining a named function. The `durable_parallel_branch` decorator
    produces the same wrapper from a function.

    ```python
    @dataclass(frozen=True)
    class ParallelBranch(Generic[T]):
        func: Callable          # receives a DurableContext
        name: str | None = None
    ```

=== "Java"

    ```java
    ParallelDurableFuture parallel(String name)
    ParallelDurableFuture parallel(String name, ParallelConfig config)
    ```

    **Parameters:**

    - `name` (required) A name for the parallel operation.
    - `config` (optional) A `ParallelConfig` object.

    **Returns:** `ParallelResult` from `get()`.

    **Throws:** Branch exceptions are captured in `ParallelResult`. Inspect `succeeded` and
    `failed` counts to detect failures.

    **`ParallelDurableFuture`**

    `parallel()` returns a `ParallelDurableFuture`. Call `branch()` to register and
    immediately start each branch, then call `get()` to block until all complete.

    ```java
    interface ParallelDurableFuture extends AutoCloseable, DurableFuture<ParallelResult> {
        <T> DurableFuture<T> branch(String name, Class<T> resultType,
                                    Function<DurableContext, T> func);
        <T> DurableFuture<T> branch(String name, TypeToken<T> resultType,
                                    Function<DurableContext, T> func);
        <T> DurableFuture<T> branch(String name, Class<T> resultType,
                                    Function<DurableContext, T> func,
                                    ParallelBranchConfig config);
        ParallelResult get();   // blocks until all branches complete
        void close();           // calls get() if not already called
    }
    ```

    **Parameters for `branch()`:**

    - `name` (required) A name for this branch.
    - `resultType` `Class<T>` or `TypeToken<T>` for deserialization.
    - `func` `Function<DurableContext, T>` to execute in the branch's child context.
    - `config` (optional) `ParallelBranchConfig` for per-branch serialization.

    **Returns from `branch()`:** `DurableFuture<T>`. `DurableFuture<T>` is the common return
    type for all async Java SDK operations. Call `.get()` on it after `parallel.get()`
    returns to retrieve that branch's individual result.

    !!! tip

        Use try-with-resources to guarantee `get()` is called even if you throw an exception
        before reaching it explicitly.

    **`ParallelBranchConfig`**

    `ParallelBranchConfig` sets a custom `SerDes` for a single branch, overriding the
    handler-level default set on `DurableConfig`.

    ```java
    ParallelBranchConfig.builder()
        .serDes(SerDes)  // optional
        .build()
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/parallel/parallel-signature.go"
    ```

    **Parameters:**

    - `ctx` The durable context.
    - `name` (required) A name for the parallel operation. Pass `""` to omit it.
    - `branches` A `[]Branch[O]`. Each `Branch` has a `Name` (an empty name
        becomes `parallel-branch-<index>`) and a `Func` that receives the
        branch's own `durable.Context`.
    - `opts` (optional) Zero or more `BatchOption` values. See
        [ParallelConfig](#parallelconfig).

    **Returns:** `(BatchResult[O], error)`. Branch failures are captured in the
    `BatchResult`.

    **Errors:** The same rules as `Map`. The batch fails as a unit when its
    `Reason` is `CompletionFailureToleranceExceeded` or `CompletionCustomFailed`.
    `Parallel` then returns the populated `BatchResult` together with a
    `*BatchError`. It returns a `*BatchCompletionError` instead when a custom
    decision failed the batch and no branch failed. A failure within the
    configured tolerance returns a nil error. Any other error (an invalid
    option, suspension, a checkpoint failure) comes with a zero `BatchResult`.
    Return it unchanged. See [Error handling](#error-handling).

    For branches of different result types, use `ParallelMixed` with
    `NewTypedBranch`. Each branch keeps its own result type, and
    `TypedBranch.Result` decodes that branch's value from the returned
    `BatchResult[json.RawMessage]`. For a failed branch it returns the branch's
    error. For a branch that never started or was abandoned it returns a
    `*durable.BranchNotCompletedError`.

=== "C#"

    ```csharp
    Task<IBatchResult<T>> ParallelAsync<T>(
        IReadOnlyList<Func<IDurableContext, CancellationToken, Task<T>>> branches,
        string? name = null,
        ParallelConfig? config = null,
        CancellationToken cancellationToken = default)

    Task<IBatchResult<T>> ParallelAsync<T>(
        IReadOnlyList<DurableBranch<T>> branches,
        string? name = null,
        ParallelConfig? config = null,
        CancellationToken cancellationToken = default)
    ```

    **Parameters:**

    - `branches` The branches to run concurrently, either as plain
        `Func<IDurableContext, CancellationToken, Task<T>>` delegates or as named
        `DurableBranch<T>` records.
    - `name` (optional) A name for the parallel operation. Omit it to infer one from the
        call site.
    - `config` (optional) A `ParallelConfig` object.
    - `cancellationToken` (optional) A token linked with the SDK's workflow-shutdown
        signal, forwarded to each branch.

    **Returns:** `Task<IBatchResult<T>>`. Use `await` to get the result.

    **Throws:** Branch exceptions are captured in the `IBatchResult`. A completion-criteria
    violation surfaces as `ParallelException` when awaited. Call `ThrowIfError()` to
    re-throw the first branch failure explicitly.

    **`DurableBranch<T>`**

    Use the second overload to give each branch a name. `DurableBranch<T>` is a record
    pairing a name with the branch function; the name surfaces on `IBatchItem<T>.Name`.

    ```csharp
    public sealed record DurableBranch<T>(
        string Name,
        Func<IDurableContext, CancellationToken, Task<T>> Func);
    ```

    - `Name` (required) A name for this branch.
    - `Func` An async function receiving the branch's `IDurableContext` and a
        `CancellationToken`, returning `Task<T>`.

### ParallelConfig

=== "TypeScript"

    ```typescript
    interface ParallelConfig<TResult> {
      maxConcurrency?: number;
      completionConfig?: CompletionConfig;
      serdes?: Serdes<BatchResult<TResult>>;
      itemSerdes?: Serdes<TResult>;
      summaryGenerator?: (result: BatchResult<TResult>) => string;
      nesting?: NestingType;
    }
    ```

    **Parameters:**

    - `maxConcurrency` (optional) Maximum branches running at once. Default: unlimited.
    - `completionConfig` (optional) When to stop. Default: wait for all branches.
    - `serdes` (optional) Custom `Serdes` for the `BatchResult`.
    - `itemSerdes` (optional) Custom `Serdes` for individual branch results.
    - `summaryGenerator` (optional) A function invoked when the serialized `BatchResult`
        exceeds 256KB. See [Checkpointing](#checkpointing).
    - `nesting` (optional) `NestingType.NESTED` (default) or `NestingType.FLAT`. See
        [Nesting](#nesting).

=== "Python"

    ```python
    @dataclass(frozen=True)
    class ParallelConfig:
        max_concurrency: int | None = None
        completion_config: CompletionConfig = CompletionConfig.all_successful()
        serdes: SerDes | None = None
        item_serdes: SerDes | None = None
        summary_generator: SummaryGenerator | None = None
        nesting_type: NestingType = NestingType.NESTED
    ```

    **Parameters:**

    - `max_concurrency` (optional) Maximum branches running at once. Default: unlimited.
    - `completion_config` (optional) When to stop. Default:
        `CompletionConfig.all_successful()`.
    - `serdes` (optional) Custom `SerDes` for the `BatchResult`.
    - `item_serdes` (optional) Custom `SerDes` for individual branch results.
    - `summary_generator` (optional) A callable invoked when the serialized `BatchResult`
        exceeds 256KB. See [Checkpointing](#checkpointing).
    - `nesting_type` (optional) `NestingType.NESTED` (default) or `NestingType.FLAT`. See
        [Nesting](#nesting).

=== "Java"

    ```java
    ParallelConfig.builder()
        .maxConcurrency(Integer)      // optional
        .completionConfig(CompletionConfig)  // optional
        .nestingType(NestingType)     // optional
        .build()
    ```

    **Parameters:**

    - `maxConcurrency` (optional) Maximum branches running at once. Default: unlimited.
    - `completionConfig` (optional) When to stop. Default:
        `CompletionConfig.allCompleted()`.
    - `nestingType` (optional) `NestingType.NESTED` (default) or `NestingType.FLAT`. See
        [Nesting](#nesting).

=== "Go"

    Configure `Parallel` with the same `BatchOption` values as `Map`, passed as trailing
    arguments. Name branches with `Branch.Name`.

    ```go
    func WithMaxConcurrency(n int) BatchOption
    func WithCompletion(c CompletionConfig) BatchOption
    func WithItemNamer(namer func(index int) string) BatchOption
    func WithNesting(m NestingMode) BatchOption
    func WithBatchSummary[O any](fn func(result BatchResult[O]) string) BatchOption
    func WithBatchSerdes(s Serdes) BatchOption
    func WithBatchResultSerdes(s Serdes) BatchOption
    ```

    **Parameters:**

    - `WithMaxConcurrency` Maximum branches running at once. Zero or negative is
        invalid. Default: unlimited.
    - `WithCompletion` When to stop. Default: fail-fast. See
        [CompletionConfig](#completionconfig).
    - `WithItemNamer` Names each branch from its zero-based index. It replaces
        every `Branch.Name`. Prefer `Branch.Name`.
    - `WithNesting` `NestingNormal` (default) or `NestingFlat`. See
        [Nesting](#nesting).
    - `WithBatchSummary` A summary for an oversized result. See
        [Checkpointing](#checkpointing).
    - `WithBatchSerdes` Serializer for each branch result.
    - `WithBatchResultSerdes` Serializer for the whole `BatchResult`.

    `ToleratedFailurePercentage` is supported for parallel.

=== "C#"

    ```csharp
    public sealed class ParallelConfig
    {
        public int? MaxConcurrency { get; set; }              // null = unlimited
        public CompletionConfig CompletionConfig { get; set; } // default AllSuccessful()
        public NestingType NestingType { get; set; }          // default Nested
    }
    ```

    **Parameters:**

    - `MaxConcurrency` (optional) Maximum branches running at once. `null` (default) =
        unlimited. Must be at least 1 when set.
    - `CompletionConfig` (optional) When to stop. Default:
        `CompletionConfig.AllSuccessful()`.
    - `NestingType` (optional) `NestingType.Nested` (default) or `NestingType.Flat`. See
        [Nesting](#nesting).

    The SDK serializes results with the `ILambdaSerializer` registered on
    `ILambdaContext.Serializer`; there is no per-operation serializer slot.

### CompletionConfig

See [Completion strategies](#completion-strategies) for how `CompletionConfig` affects
execution and the completion status of the result.

=== "TypeScript"

    ```typescript
    interface CompletionConfig {
      minSuccessful?: number;
      toleratedFailureCount?: number;
      toleratedFailurePercentage?: number;
    }
    ```

=== "Python"

    ```python
    @dataclass(frozen=True)
    class CompletionConfig:
        min_successful: int | None = None
        tolerated_failure_count: int | None = None
        tolerated_failure_percentage: int | float | None = None
        should_complete: Callable[[CompletionStatus], CompletionDecision] | None = None
    ```

    Use the threshold fields for count-based rules, or `should_complete` for a custom
    predicate (see [Completion strategies](#completion-strategies)). `should_complete`
    cannot be combined with the threshold fields.

=== "Java"

    ```java
    CompletionConfig.allCompleted()
    CompletionConfig.allSuccessful()
    CompletionConfig.firstSuccessful()
    CompletionConfig.minSuccessful(int count)
    CompletionConfig.toleratedFailureCount(int count)
    CompletionConfig.shouldComplete(
        Function<CompletionStatus, CompletionDecision> decision)
    ```

=== "Go"

    Set the fields of a `CompletionConfig` and pass it with `WithCompletion`.
    The threshold fields and `ShouldComplete` are mutually exclusive.

    ```go
    type CompletionConfig struct {
    	MinSuccessful              int
    	ToleratedFailureCount      *int
    	ToleratedFailurePercentage *int
    	ShouldComplete             func(BatchProgress) CompletionDecision
    }

    type BatchProgress struct {
    	TotalCount     int
    	CompletedCount int
    	SuccessCount   int
    	FailureCount   int
    	Items          []BatchItemProgress
    }

    type BatchItemProgress struct {
    	Index  int
    	Name   string
    	Status BatchItemStatus
    }

    func ContinueBatch() CompletionDecision
    func CompleteBatch(outcome CompletionOutcome) CompletionDecision

    type CompletionOutcome int

    const (
    	CompletionOutcomeSucceeded CompletionOutcome = 1
    	CompletionOutcomeFailed    CompletionOutcome = 2
    )
    ```

    - `MinSuccessful` Completes the batch once this many branches succeed. Zero
        leaves it unset.
    - `ToleratedFailureCount` Fails the batch once more than this many branches
        fail. Nil leaves it unset. `aws.Int(0)` fails on the first failure.
    - `ToleratedFailurePercentage` Fails the batch once the failure percentage,
        computed against the total branch count, is strictly greater than this
        value. Nil leaves it unset. `aws.Int(0)` fails on the first failure.
    - `ShouldComplete` A custom predicate. It cannot be combined with the
        threshold fields.

    Set the pointer fields with any `*int`, for example `aws.Int` from
    `github.com/aws/aws-sdk-go-v2/aws`. The default (no `WithCompletion`) is
    fail-fast. To run every branch regardless of failures, set
    `ToleratedFailurePercentage: aws.Int(100)`, which is never exceeded. For
    first-successful, set `MinSuccessful: 1`.

=== "C#"

    ```csharp
    CompletionConfig.AllSuccessful()  // tolerate zero failures (the default)
    CompletionConfig.AllCompleted()   // run every branch; never auto-throws
    CompletionConfig.FirstSuccessful() // resolve once one branch succeeds

    // Or set the individual properties directly:
    new CompletionConfig { MinSuccessful = 2 }
    new CompletionConfig { ToleratedFailureCount = 1 }
    new CompletionConfig { ToleratedFailurePercentage = 0.25 } // ratio in [0.0, 1.0]
    ```

    `AllSuccessful()` is equivalent to `ToleratedFailureCount = 0`, and `FirstSuccessful()`
    is equivalent to `MinSuccessful = 1`. Multiple criteria combine: the operation resolves
    as soon as any criterion is met or violated.

### Result types

=== "TypeScript"

    ```typescript
    interface BatchResult<TResult> {
      all: BatchItem<TResult>[];
      status: BatchItemStatus.SUCCEEDED | BatchItemStatus.FAILED;
      completionReason: "ALL_COMPLETED" | "MIN_SUCCESSFUL_REACHED" | "FAILURE_TOLERANCE_EXCEEDED";
      hasFailure: boolean;
      successCount: number;
      failureCount: number;
      startedCount: number;
      totalCount: number;
      getResults(): TResult[];
      getErrors(): ChildContextError[];
      succeeded(): BatchItem<TResult>[];
      failed(): BatchItem<TResult>[];
      started(): BatchItem<TResult>[];
      throwIfError(): void;
    }
    ```

    - **`all`** all `BatchItem` entries, one per branch, in input order. Iterate with
        `item.index` for branch-indexed access when some branches fail.
    - **`getResults()`** results of succeeded branches, preserving input order
    - **`getErrors()`** `ChildContextError[]` for failed branches
    - **`succeeded()` / `failed()` / `started()`** `BatchItem[]` filtered by status
    - **`successCount` / `failureCount` / `startedCount` / `totalCount`** branch counts
    - **`status`** `SUCCEEDED` if no failures, `FAILED` otherwise
    - **`completionReason`** why the operation completed. See
        [Completion strategies](#completion-strategies).
    - **`hasFailure`** `true` if any branch failed
    - **`throwIfError()`** throws the first branch error, if any

    ```typescript
    type CompletionReason =
      | "ALL_COMPLETED"
      | "MIN_SUCCESSFUL_REACHED"
      | "FAILURE_TOLERANCE_EXCEEDED"
    ```

    ```typescript
    interface BatchItem<TResult> {
      index: number;
      status: BatchItemStatus;
      result?: TResult;
      error?: ChildContextError;
    }

    enum BatchItemStatus {
      SUCCEEDED = "SUCCEEDED",
      FAILED    = "FAILED",
      STARTED   = "STARTED",
    }
    ```

    - **`index`** position of this branch in the input array
    - **`status`** `SUCCEEDED`, `FAILED`, or `STARTED` (not yet complete)
    - **`result`** the branch return value, present when `status` is `SUCCEEDED`
    - **`error`** the captured error, present when `status` is `FAILED`

=== "Python"

    ```python
    @dataclass(frozen=True)
    class BatchResult(Generic[R]):
        all: list[BatchItem[R]]
        completion_reason: CompletionReason

        def get_results(self) -> list[R]: ...
        def get_errors(self) -> list[ErrorObject]: ...
        def succeeded(self) -> list[BatchItem[R]]: ...
        def failed(self) -> list[BatchItem[R]]: ...
        def started(self) -> list[BatchItem[R]]: ...
        def throw_if_error(self) -> None: ...
        def to_dict(self) -> dict: ...

        @property
        def status(self) -> BatchItemStatus: ...
        @property
        def has_failure(self) -> bool: ...
        @property
        def success_count(self) -> int: ...
        @property
        def failure_count(self) -> int: ...
        @property
        def started_count(self) -> int: ...
        @property
        def total_count(self) -> int: ...

    class CompletionReason(Enum):
        ALL_COMPLETED             = "ALL_COMPLETED"
        MIN_SUCCESSFUL_REACHED    = "MIN_SUCCESSFUL_REACHED"
        FAILURE_TOLERANCE_EXCEEDED = "FAILURE_TOLERANCE_EXCEEDED"
    ```

    - **`all`** all `BatchItem` entries, one per branch, in input order. Iterate with
        `item.index` for branch-indexed access when some branches fail.
    - **`get_results()`** results of succeeded branches, preserving input order
    - **`get_errors()`** `list[ErrorObject]` for failed branches
    - **`succeeded()` / `failed()` / `started()`** `BatchItem` lists filtered by status
    - **`success_count` / `failure_count` / `started_count` / `total_count`** branch counts
    - **`status`** `BatchItemStatus.SUCCEEDED` if no failures, `FAILED` otherwise
    - **`completion_reason`** why the operation completed. See
        [Completion strategies](#completion-strategies).
    - **`has_failure`** `True` if any branch failed
    - **`throw_if_error()`** raises the first failure: `ChildContextError` for a failed
        branch, `SerDesError` if a branch result failed to serialize, or
        `BatchCompletionError` if a custom predicate failed the batch
    - **`to_dict()`** serializes to a plain dict. Serializability depends on `R`.

    ```python
    @dataclass(frozen=True)
    class BatchItem(Generic[R]):
        index: int
        status: BatchItemStatus
        result: R | None = None
        error: ErrorObject | None = None

        def to_dict(self) -> dict: ...

    class BatchItemStatus(Enum):
        SUCCEEDED = "SUCCEEDED"
        FAILED    = "FAILED"
        STARTED   = "STARTED"
    ```

    - **`index`** position of this branch in the input sequence
    - **`status`** `SUCCEEDED`, `FAILED`, or `STARTED` (not yet complete)
    - **`result`** the branch return value, present when `status` is `SUCCEEDED`
    - **`error`** `ErrorObject` with the captured error, present when `status` is `FAILED`
    - **`to_dict()`** serializes to a plain dict. Serializability of `result` depends on
        `R`.

=== "Java"

    ```java
    record ParallelResult(
        int size,
        int succeeded,
        int failed,
        ConcurrencyCompletionStatus completionStatus
    ) {}

    enum ConcurrencyCompletionStatus {
        ALL_COMPLETED,
        MIN_SUCCESSFUL_REACHED,
        FAILURE_TOLERANCE_EXCEEDED,
        CUSTOM_COMPLETION_SUCCEEDED,
        CUSTOM_COMPLETION_FAILED
    }
    ```

    - **`size`** total number of registered branches
    - **`succeeded`** number of branches that succeeded
    - **`failed`** number of branches that failed
    - **`completionStatus`** why the operation completed. See
        [Completion strategies](#completion-strategies).

    `ConcurrencyCompletionStatus.isSucceeded()` returns `true` for `ALL_COMPLETED`,
    `MIN_SUCCESSFUL_REACHED`, and `CUSTOM_COMPLETION_SUCCEEDED`. To check if any branch
    failed, use `result.failed() > 0` (where `result` is a `ParallelResult`).

    `ParallelResult` contains only aggregate counts. To get individual branch results, hold
    the `DurableFuture<T>` returned by each `branch()` call and call `.get()` on it after
    `parallel.get()` returns. Results are available in the order branches were registered.

=== "Go"

    `Parallel` and `Map` return the same `BatchResult[O]`. It carries each
    branch's result and error.

    ```go
    type BatchResult[O any] struct {
    	Items  []BatchItem[O]
    	Reason CompletionReason
    }

    func (r BatchResult[O]) Results() []O
    func (r BatchResult[O]) Succeeded() []BatchItem[O]
    func (r BatchResult[O]) Failed() []BatchItem[O]
    func (r BatchResult[O]) Started() []BatchItem[O]
    func (r BatchResult[O]) Errors() []error
    func (r BatchResult[O]) HasFailure() bool
    func (r BatchResult[O]) SuccessCount() int
    func (r BatchResult[O]) FailureCount() int
    func (r BatchResult[O]) StartedCount() int
    func (r BatchResult[O]) TotalCount() int
    func (r BatchResult[O]) Status() BatchItemStatus
    func (r BatchResult[O]) Item(name string) *BatchItem[O]
    func (r BatchResult[O]) Result(name string) (value O, ok bool)

    type BatchItem[O any] struct {
    	Index  int
    	Name   string
    	Status BatchItemStatus
    	Result O
    	Err    error
    }

    type BatchItemStatus int

    const (
    	BatchItemNotStarted BatchItemStatus = 0
    	BatchItemSucceeded  BatchItemStatus = 1
    	BatchItemFailed     BatchItemStatus = 2
    	BatchItemStarted    BatchItemStatus = 4
    )

    type CompletionReason int

    const (
    	CompletionAllCompleted             CompletionReason = 1
    	CompletionMinSuccessfulReached     CompletionReason = 2
    	CompletionFailureToleranceExceeded CompletionReason = 3
    	CompletionCustomSucceeded          CompletionReason = 4
    	CompletionCustomFailed             CompletionReason = 5
    )
    ```

    - **`Items`** per-branch outcomes in input order. A branch started and then
        abandoned on early completion is included with `BatchItemStarted`.
        Branches that never started are omitted.
    - **`Results()`** successful results in input order.
    - **`Errors()`** errors of failed branches, in input order. Each is rebuilt
        from its checkpoint record. In `NestingNormal` each is a
        `*durable.ChildContextError` named after the branch. In `NestingFlat`
        each is the rebuilt error the branch function returned. `errors.As`
        matches an SDK error inside, such as `*durable.StepError`, but not your
        own error types. In `NestingNormal`, match your own types on
        `ChildContextError.ErrorType`.
    - **`Succeeded()` / `Failed()` / `Started()`** branches filtered by status.
    - **`SuccessCount()` / `FailureCount()` / `StartedCount()` / `TotalCount()`**
        branch counts. `TotalCount()` excludes branches that never started.
    - **`Status()`** `BatchItemFailed` if any branch failed or the batch failed
        as a unit, else `BatchItemSucceeded`. A custom decision sets it directly.
        A failure within tolerance still gives `BatchItemFailed` with a nil
        error.
    - **`Item(name)` / `Result(name)`** look up one branch by its name.
    - **`Reason`** why the batch completed.

    `BatchItemStatus.String()` and `CompletionReason.String()` return the wire
    forms, such as `SUCCEEDED` and `ALL_COMPLETED`.

    Inspect the returned error and the result.

=== "C#"

    ```csharp
    public interface IBatchResult<T> : IBatchResult
    {
        IReadOnlyList<IBatchItem<T>> All { get; }
        IReadOnlyList<IBatchItem<T>> Succeeded { get; }
        IReadOnlyList<IBatchItem<T>> Failed { get; }
        IReadOnlyList<IBatchItem<T>> Started { get; }
        IReadOnlyList<T> GetResults();
        IReadOnlyList<DurableExecutionException> GetErrors();
        void ThrowIfError();
    }

    public interface IBatchResult
    {
        CompletionReason CompletionReason { get; }
        bool HasFailure { get; }
        int SuccessCount { get; }
        int FailureCount { get; }
        int StartedCount { get; }
        int TotalCount { get; }
    }
    ```

    - **`All`** all `IBatchItem` entries, one per branch, in original index order. Iterate
        with `item.Index` for branch-indexed access when some branches fail.
    - **`Succeeded` / `Failed` / `Started`** `IBatchItem` lists filtered by status, in
        original index order
    - **`GetResults()`** results of succeeded branches, preserving input order. Skips
        failed and started items, so it never throws on partial-failure batches
    - **`GetErrors()`** `DurableExecutionException` list for failed branches
    - **`SuccessCount` / `FailureCount` / `StartedCount` / `TotalCount`** branch counts
    - **`HasFailure`** `true` if any branch failed
    - **`CompletionReason`** why the operation completed. See
        [Completion strategies](#completion-strategies).
    - **`ThrowIfError()`** throws the first failed branch's `Error`, if any

    ```csharp
    public interface IBatchItem<T>
    {
        int Index { get; }
        string? Name { get; }
        BatchItemStatus Status { get; }
        T? Result { get; }                       // set when Status is Succeeded
        DurableExecutionException? Error { get; } // set when Status is Failed
    }

    public enum BatchItemStatus
    {
        Succeeded,
        Failed,
        Started
    }

    public enum CompletionReason
    {
        AllCompleted,
        MinSuccessfulReached,
        FailureToleranceExceeded
    }
    ```

    - **`Index`** zero-based position of this branch in the input list
    - **`Name`** the branch name (from `DurableBranch<T>.Name`), if any
    - **`Status`** `Succeeded`, `Failed`, or `Started` (not dispatched before the operation
        resolved)
    - **`Result`** the branch return value, present when `Status` is `Succeeded`
    - **`Error`** the captured error, present when `Status` is `Failed`

## Branch functions

Each branch receives a `DurableContext` and can use any durable operation such as steps,
waits, child contexts, or nested parallel operations. Branches run in
[child contexts](child-context.md), so they do not share state with each other or with
the parent context.

=== "TypeScript"

    A branch is a `ParallelFunc` (plain async function) or a `NamedParallelBranch` (object
    with `name` and `func`). Use `NamedParallelBranch` to give an inline lambda a name
    without defining a named function.

    ```typescript
    --8<-- "examples/typescript/operations/parallel/named-branches.ts"
    ```

=== "Python"

    A branch is a synchronous callable that receives a `DurableContext` and returns `T`,
    or a `ParallelBranch[T]` wrapping such a callable with a name. Wrap a branch in
    `ParallelBranch` to name it in execution history without defining a named function.

    ```python
    --8<-- "examples/python/operations/parallel/named-branches.py"
    ```

=== "Java"

    Each branch is registered via `ParallelDurableFuture.branch()`. The branch function is a
    synchronous `Function<DurableContext, T>`.

    ```java
    --8<-- "examples/java/operations/parallel/named-branches.java"
    ```

=== "Go"

    A branch is a `Branch[O]` with a `Name` and a `Func`. Set `Name` to name the
    branch. An empty `Name` is named `parallel-branch-<index>`. The `Func`
    receives the branch's own `durable.Context`.

    ```go
    --8<-- "examples/go/operations/parallel/named-branches.go"
    ```

=== "C#"

    A branch is a plain `Func<IDurableContext, CancellationToken, Task<T>>`, or a
    `DurableBranch<T>` record to give it a name. Use the named overload to surface a name
    on `IBatchItem<T>.Name` without defining a named method.

    ```csharp
    --8<-- "examples/csharp/operations/parallel/named-branches.cs"
    ```

### Pass arguments to branches

=== "TypeScript"

    Capture arguments in a closure:

    ```typescript
    --8<-- "examples/typescript/operations/parallel/pass-arguments.ts"
    ```

=== "Python"

    Use a factory function to bind arguments. Avoid using loop variables directly in
    lambdas, as Python closures capture by reference.

    ```python
    --8<-- "examples/python/operations/parallel/pass-arguments.py"
    ```

=== "Java"

    Capture arguments in a lambda. Java lambdas require effectively final variables.

    ```java
    --8<-- "examples/java/operations/parallel/pass-arguments.java"
    ```

=== "Go"

    Capture arguments in the branch closure. In a module that declares Go 1.22
    or later, each loop iteration has its own variable. So a branch built in a
    loop captures its own value.

    ```go
    --8<-- "examples/go/operations/parallel/pass-arguments.go"
    ```

=== "C#"

    Capture arguments in the closure. Copy the loop variable to a local so each branch
    captures its own value.

    ```csharp
    --8<-- "examples/csharp/operations/parallel/pass-arguments.cs"
    ```

## Naming parallel operations

Name your parallel operations to make them easier to identify in logs and tests.

=== "TypeScript"

    The name is the first argument. Pass `undefined` to omit it.

=== "Python"

    Pass `name` as a keyword argument. Omit it or pass `None` to leave it unnamed.

=== "Java"

    The name is always required. Each `branch()` call also requires a name. Pass `null` to
    omit it.

=== "Go"

    The name is the required second argument. Pass `""` to leave it unnamed. Name
    each branch with `Branch.Name`. An empty name becomes
    `parallel-branch-<index>`.

=== "C#"

    The name is the optional `name` argument. Omit it to infer one from the call site. Use
    the `DurableBranch<T>` overload to name each branch.

## Configuration

Configure parallel behavior using `ParallelConfig`:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/parallel/parallel-config.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/parallel/parallel-config.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/parallel/parallel-config.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/parallel/parallel-config.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/parallel/parallel-config.cs"
    ```

## Nesting

Nested mode is the default. The SDK records each branch context as a separate `CONTEXT`
operation and checkpoints the branch result there. Each branch appears separately in the
execution history.

In flat mode, the SDK uses a virtual context for each branch and omits the per-branch
`CONTEXT` operation. Durable operations inside the branch still checkpoint and appear as
children of the parallel operation. The SDK records the branch outcome with the parent
parallel operation.

Use flat mode for parallel operations with many branches when each branch performs few
durable operations and you do not need each branch represented separately in the
execution history. Flat mode removes one checkpointed operation per branch while
preserving checkpoints for durable operations inside each branch.

## Completion strategies

`CompletionConfig` controls when the parallel operation completes. When the operation
reaches the completion criteria, it will abandon branches that have not completed yet.
The abandoned branches will keep running in the background but cannot checkpoint their
results after the parent completes. The SDK makes a best-effort attempt to cancel
ongoing work in abandoned branches, but cancellation is not guaranteed.

=== "TypeScript"

    The `BatchResult`'s `completionReason` indicates the stop condition with which the
    parallel operation completed. Branches that had not started yet do not appear in
    `result.all` at all. Branches that had started but not completed yet appear with status
    `STARTED`.

    | `completionConfig`             | Early exit `completionReason` | Full completion `completionReason` |
    | ------------------------------ | ----------------------------- | ---------------------------------- |
    | `{}` or omitted                | `FAILURE_TOLERANCE_EXCEEDED`  | `ALL_COMPLETED`                    |
    | `toleratedFailureCount=N`      | `FAILURE_TOLERANCE_EXCEEDED`  | `ALL_COMPLETED`                    |
    | `toleratedFailurePercentage=N` | `FAILURE_TOLERANCE_EXCEEDED`  | `ALL_COMPLETED`                    |
    | `minSuccessful=N`              | `MIN_SUCCESSFUL_REACHED`      | `ALL_COMPLETED`                    |

=== "Python"

    The `BatchResult`'s `completion_reason` indicates the stop condition with which the
    parallel operation completed. Branches that started but did not complete appear in
    `result.all` with status `STARTED`. Branches that never started are omitted from
    `result.all`, so `total_count` counts only the branches that appear.

    | `completion_config`              | Early exit `completion_reason` | Full completion `completion_reason` |
    | -------------------------------- | ------------------------------ | ----------------------------------- |
    | `all_successful()` (default)     | `FAILURE_TOLERANCE_EXCEEDED`   | `ALL_COMPLETED`                     |
    | `first_successful()`             | `MIN_SUCCESSFUL_REACHED`       | `ALL_COMPLETED`                     |
    | `all_completed()`                | n/a                            | `ALL_COMPLETED`                     |
    | `tolerated_failure_count=N`      | `FAILURE_TOLERANCE_EXCEEDED`   | `ALL_COMPLETED`                     |
    | `tolerated_failure_percentage=N` | `FAILURE_TOLERANCE_EXCEEDED`   | `ALL_COMPLETED`                     |
    | `min_successful=N`               | `MIN_SUCCESSFUL_REACHED`       | `ALL_COMPLETED`                     |

    The default `all_successful()` fails the batch on the first branch failure. Use
    `CompletionConfig.all_completed()` to run every branch regardless of failures.

    Set `should_complete` when the threshold fields cannot express the rule. The predicate
    receives a `CompletionStatus` (`success_count`, `failure_count`, `completed_count`,
    `total_count`, and `items`, a per-branch tuple of `CompletionItemStatus`) and returns a
    `CompletionDecision`: `continue_batch()` to keep going, or `complete_batch(outcome)` to
    stop, where `outcome` defaults to `CompletionOutcome.SUCCEEDED`. A
    `CompletionOutcome.FAILED` outcome marks the whole batch failed, and `throw_if_error()`
    then raises `BatchCompletionError` even when no individual branch failed.

    ```python
    --8<-- "examples/python/operations/parallel/custom-completion.py"
    ```

    The predicate runs before any branch is scheduled (`completed_count == 0`) and again on
    each terminal or suspension event, so it must handle the initial zero-progress
    snapshot. It cannot be combined with `min_successful`, `tolerated_failure_count`, or
    `tolerated_failure_percentage`, and must be deterministic, side-effect-free, and
    monotonic. Unscheduled branches report `status=None` in `items`.

=== "Java"

    The `ParallelResult`'s `completionStatus` indicates the stop condition with which the
    parallel operation completed. All registered branches (including those never started)
    are counted in `size`.

    | `completionConfig`         | Early exit `completionStatus` | Full completion `completionStatus` |
    | -------------------------- | ----------------------------- | ---------------------------------- |
    | `allCompleted()` (default) | n/a                           | `ALL_COMPLETED`                    |
    | `allSuccessful()`          | `FAILURE_TOLERANCE_EXCEEDED`  | `ALL_COMPLETED`                    |
    | `firstSuccessful()`        | `MIN_SUCCESSFUL_REACHED`      | `ALL_COMPLETED`                    |
    | `minSuccessful(N)`         | `MIN_SUCCESSFUL_REACHED`      | `ALL_COMPLETED`                    |
    | `toleratedFailureCount(N)` | `FAILURE_TOLERANCE_EXCEEDED`  | `ALL_COMPLETED`                    |

    !!! note

        `ParallelConfig` in Java does not support `toleratedFailurePercentage`. Use
        `toleratedFailureCount` instead.

    Use `CompletionConfig.shouldComplete(...)` when the predefined thresholds cannot
    express the completion rule. The SDK evaluates the function as completion state
    changes. It receives a `CompletionStatus` with `successCount`, `failureCount`,
    `completedCount`, `totalCount`, and `allItemsRegistered`. For parallel operations,
    `allItemsRegistered` becomes `true` when `get()` or `close()` joins the operation.

    Return `CompletionDecision.continueExecution()` to keep processing. Return
    `CompletionDecision.complete(...)` with `CUSTOM_COMPLETION_SUCCEEDED` or
    `CUSTOM_COMPLETION_FAILED` to stop and classify the result.

    ```java
    var completion = CompletionConfig.shouldComplete(status -> {
        if (status.successCount() >= requiredSuccesses) {
            return CompletionConfig.CompletionDecision.complete(
                    ConcurrencyCompletionStatus.CUSTOM_COMPLETION_SUCCEEDED);
        }
        if (status.failureCount() >= failureLimit) {
            return CompletionConfig.CompletionDecision.complete(
                    ConcurrencyCompletionStatus.CUSTOM_COMPLETION_FAILED);
        }
        return CompletionConfig.CompletionDecision.continueExecution();
    });
    ```

    A custom completion function is mutually exclusive with `minSuccessful` and
    `toleratedFailureCount`. It must return a non-null decision. Keep it deterministic
    and free of side effects. Registered branches that have not started when it
    completes have status `SKIPPED`. `CUSTOM_COMPLETION_FAILED` does not throw
    automatically. Inspect `result.completionStatus().isSucceeded()` to distinguish
    the custom outcomes.

=== "Go"

    `BatchResult.Reason` records the stop condition. Branches that never started
    are omitted from `Items`. A branch that started but did not complete appears
    with `BatchItemStarted`. `Parallel` does not leave abandoned branches
    running. An abandoned branch stops at its next durable operation. `Parallel`
    returns only after every started branch has stopped.

    | `CompletionConfig`                       | Early exit `Reason`                                     | Full completion `Reason` |
    | ---------------------------------------- | ------------------------------------------------------- | ------------------------ |
    | zero value (default, fail-fast)          | `CompletionFailureToleranceExceeded`                    | `CompletionAllCompleted` |
    | `ToleratedFailureCount: aws.Int(N)`      | `CompletionFailureToleranceExceeded`                    | `CompletionAllCompleted` |
    | `ToleratedFailurePercentage: aws.Int(N)` | `CompletionFailureToleranceExceeded`                    | `CompletionAllCompleted` |
    | `MinSuccessful: N`                       | `CompletionMinSuccessfulReached`                        | `CompletionAllCompleted` |
    | `ShouldComplete: ...`                    | `CompletionCustomSucceeded` or `CompletionCustomFailed` | `CompletionAllCompleted` |

    The default is fail-fast. Set `ShouldComplete` when the threshold fields cannot
    express the rule. The predicate receives a `BatchProgress` snapshot before the first
    branch and again after each branch reaches a terminal state, so it must handle the
    initial zero-progress snapshot. It must be deterministic. Return `ContinueBatch()`,
    or `CompleteBatch(durable.CompletionOutcomeSucceeded)` or
    `CompleteBatch(durable.CompletionOutcomeFailed)` to stop and classify the result.
    With `ShouldComplete` set, a branch failure does not stop the batch by itself. A
    failed custom outcome returns a `*BatchError`, or a `*BatchCompletionError` when no
    branch failed. A predicate that panics fails the operation with an error that is not
    a `*BatchError`.

    ```go
    --8<-- "examples/go/operations/parallel/custom-completion.go"
    ```

=== "C#"

    The `IBatchResult`'s `CompletionReason` indicates the stop condition with which the
    parallel operation completed. Branches that were not dispatched before the operation
    resolved appear in `result.All` with status `Started`.

    | `CompletionConfig`               | Early exit `CompletionReason` | Full completion `CompletionReason` |
    | -------------------------------- | ----------------------------- | ---------------------------------- |
    | `AllSuccessful()` (default)      | `FailureToleranceExceeded`    | `AllCompleted`                     |
    | `AllCompleted()`                 | n/a                           | `AllCompleted`                     |
    | `FirstSuccessful()`              | `MinSuccessfulReached`        | `AllCompleted`                     |
    | `MinSuccessful = N`              | `MinSuccessfulReached`        | `AllCompleted`                     |
    | `ToleratedFailureCount = N`      | `FailureToleranceExceeded`    | `AllCompleted`                     |
    | `ToleratedFailurePercentage = N` | `FailureToleranceExceeded`    | `AllCompleted`                     |

    When the operation resolves with `FailureToleranceExceeded`, awaiting it throws
    `ParallelException`, which carries the aggregate result on `Result`.

!!! note

    When using a `minSuccessful` strategy, failures do not trigger early exit. If all
    branches fail before the success threshold is reached, the operation completes with
    `ALL_COMPLETED`.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/parallel/completion-config.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/parallel/completion-config.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/parallel/completion-config.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/parallel/completion-config.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/parallel/completion-config.cs"
    ```

## Error handling

When a branch throws an error, parallel captures the error in the result rather than
propagating it immediately. Other branches continue running.

=== "TypeScript"

    `BatchResult.status` is `FAILED` if any branch failed. Call `throwIfError()` to
    propagate the first branch error as an exception, or inspect `getErrors()` to handle
    errors individually.

    ```typescript
    --8<-- "examples/typescript/operations/parallel/error-handling.ts"
    ```

=== "Python"

    `BatchResult.status` is `FAILED` if any branch failed. Call `throw_if_error()` to
    propagate the first branch error as an exception, or inspect `get_errors()` to handle
    errors individually.

    ```python
    --8<-- "examples/python/operations/parallel/error-handling.py"
    ```

=== "Java"

    Check `result.failed() > 0` (where `result` is a `ParallelResult`) to detect branch
    failures. To propagate a branch error, call `.get()` on the `DurableFuture<T>` for that
    branch to rethrow the original exception. This will throw
    `ParallelBranchFailedException` if the SDK cannot reconstruct the original.

    ```java
    --8<-- "examples/java/operations/parallel/error-handling.java"
    ```

=== "Go"

    `Parallel` captures each branch error in the result. By default (fail-fast)
    the first branch failure completes the batch. Branches still running are
    abandoned, and branches not yet started never run. `Parallel` then returns
    the populated result together with a `*durable.BatchError`. Match it with
    `errors.As`. Tolerate failures to keep every branch running, then read the
    failures from `BatchResult.Errors()` and `BatchResult.Failed()`.

    ```go
    --8<-- "examples/go/operations/parallel/error-handling.go"
    ```

=== "C#"

    `IBatchResult.HasFailure` is `true` if any branch failed. Call `ThrowIfError()` to
    propagate the first branch error as a `DurableExecutionException`, or inspect
    `GetErrors()` to handle errors individually.

    ```csharp
    --8<-- "examples/csharp/operations/parallel/error-handling.cs"
    ```

## Checkpointing

Checkpoint behavior depends on the nesting type. In nested mode, each branch checkpoints
its result in a per-branch `CONTEXT` operation. In flat mode, the SDK omits that context
checkpoint and records the branch outcome with the parent parallel operation. Durable
operations inside a branch still checkpoint in both modes.

Branches that have not completed when the parallel operation reaches its completion
criteria receive no further checkpoint updates. Unless noted otherwise, the
language-specific details below describe nested mode.

=== "TypeScript"

    The parent parallel operation also checkpoints the serialized `BatchResult` for
    observability. On replay, the SDK deserializes the `BatchResult` directly from that
    checkpoint.

    For results over 256KB, the SDK cannot store the full `BatchResult` in the checkpoint.
    Instead, the SDK reconstructs the `BatchResult` from the checkpointed results of the
    individual branches. In that case, the checkpoint stores a compact JSON summary, which
    is for observability only.

    The default summary generator produces:

    ```json
    {
      "type": "ParallelResult",
      "totalCount": 3,
      "successCount": 2,
      "failureCount": 1,
      "startedCount": 0,
      "completionReason": "ALL_COMPLETED",
      "status": "FAILED"
    }
    ```

=== "Python"

    The parent parallel operation also checkpoints the serialized `BatchResult` for
    observability. On replay, the SDK deserializes the `BatchResult` directly from that
    checkpoint.

    For results over 256KB, the SDK cannot store the full `BatchResult` in the checkpoint,
    so it re-executes the branches to reconstruct it instead. In that case, the checkpoint
    stores the output of `summary_generator`, which is for observability only.

    The default summary generator produces:

    ```json
    {
      "type": "ParallelResult",
      "totalCount": 3,
      "successCount": 2,
      "failureCount": 1,
      "startedCount": 0,
      "completionReason": "ALL_COMPLETED",
      "status": "FAILED"
    }
    ```

    When you pass a custom `ParallelConfig` without setting `summary_generator`, the SDK
    checkpoints an empty string for large payloads.

    `SummaryGenerator` is a callable protocol you can pass by setting `summary_generator` on
    [`ParallelConfig`](#parallelconfig):

    ```python
    class SummaryGenerator(Protocol[T]):
        def __call__(self, result: T) -> str: ...
    ```

=== "Java"

    The parent parallel operation checkpoints no result payload. On replay, the SDK always
    re-executes the branches to reconstruct the `ParallelResult` from their individual
    checkpoints.

=== "Go"

    `WithNesting` selects the checkpoint shape, as for `Map`. The SDK checkpoints
    the whole `BatchResult` when it is at most 256KB serialized. On replay, the
    SDK decodes that `BatchResult` without running the branches. A larger result
    is not stored. The checkpoint then keeps the child operations plus a compact
    record, and replay rebuilds each branch from that branch's own checkpoint.
    The record holds `type`, `totalCount`, `successCount`, `failureCount`,
    `startedCount`, `completionReason`, `status`, and `itemStatuses`, one
    character per started branch. Pass `WithBatchSummary` to add your own string
    under the `summary` key.

=== "C#"

    In nested mode, the SDK reconstructs `IBatchResult` from per-branch checkpoints.
    In flat mode, the SDK records branch results and errors inline on the parent parallel
    operation instead. The SDK serializes results with the `ILambdaSerializer` registered
    on `ILambdaContext.Serializer`.

## Nesting parallel operations

A branch function can call `context.parallel()` to create nested parallel operations.
Each nested parallel creates its own set of child contexts.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/parallel/nested-parallel.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/parallel/nested-parallel.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/parallel/nested-parallel.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/parallel/nested-parallel.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/parallel/nested-parallel.cs"
    ```

## See also

- [Map operations](map.md) run the same function concurrently on a collection
- [Child contexts](child-context.md) understand child context isolation
- [Steps](step.md) use steps within parallel branches
- [Error handling](../error-handling/errors.md) in durable functions

!!! info "Checkpoint consumption"

    Durable operations consume checkpoints. To understand how this operation affects
    your checkpoint usage, see
    [Checkpoint consumption](https://docs.aws.amazon.com/lambda/latest/dg/durable-execution-sdk.html#durable-operations-checkpoint-consumption).
