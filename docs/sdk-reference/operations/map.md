# Map

## Apply a function to each item in a collection

Map executes a function for each item in a collection concurrently. It manages
concurrency, collects results as items complete, and checkpoints the outcome.

Each item runs in its own [child context](child-context.md). The default nested mode
checkpoints that context and its result. Flat mode omits the per-item context checkpoint to
reduce operation overhead.

Use map to apply the same operation to every item in a collection. Use
[parallel](parallel.md) instead to execute different operations concurrently.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/map/simple-map.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/map/simple-map.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/map/simple-map.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/map/simple-map.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/map/simple-map.cs"
    ```

## Method signature

### context.map

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/map/map-signature.ts"
    ```

    **Parameters:**

    - `name` (optional) A name for the map operation. Pass `undefined` to omit.
    - `items` An array of items to process.
    - `mapFunc` A `MapFunc` called for each item. See [Map Function](#map-function).
    - `config` (optional) A `MapConfig<TInput, TOutput>` object.

    **Returns:** `DurablePromise<BatchResult<TOutput>>`. Use `await` to get the result.

    **Throws:** Item exceptions are captured in the `BatchResult`. Call `throwIfError()` to
    re-throw the first failure.

=== "Python"

    ```python
    --8<-- "examples/python/operations/map/map-signature.py"
    ```

    **Parameters:**

    - `inputs` A sequence of items to process.
    - `func` A callable called for each item. See [Map Function](#map-function).
    - `name` (optional) A name for the map operation.
    - `config` (optional) A `MapConfig` object.

    **Returns:** `BatchResult[T]`.

    **Raises:** Item exceptions are captured in the `BatchResult`. Call `throw_if_error()`
    to re-raise the first failure.

=== "Java"

    ```java
    --8<-- "examples/java/operations/map/map-signature.java"
    ```

    **Parameters:**

    - `name` (required) A name for the map operation.
    - `items` A `Collection<I>` of items to process. Its iteration order must remain
        stable during replay so item indexes match their checkpoints. Use an ordered
        collection such as `List`, `LinkedHashSet`, or `TreeSet`. The SDK rejects
        `HashSet` and the `keySet()`, `values()`, and `entrySet()` views of known
        unordered maps. Copy or sort unordered inputs into a `List` before calling
        `map()` or `mapAsync()`.
    - `resultType` `Class<O>` or `TypeToken<O>` for deserialization.
    - `function` A `MapFunction<I, O>` called for each item. See
        [Map Function](#map-function).
    - `config` (optional) A `MapConfig` object.

    **Returns:** `MapResult<O>` from `map()`, or `DurableFuture<MapResult<O>>` from
    `mapAsync()`.

    **Throws:** `IllegalArgumentException` if `items` is null or has a known
    non-deterministic iteration order. Item exceptions are captured in `MapResult`.
    Inspect `failed()` to detect failures. If the SDK cannot reconstruct the original
    exception, it throws `MapIterationFailedException`.

=== "Go"

    ```go
    --8<-- "examples/go/operations/map/map-signature.go"
    ```

    **Parameters:**

    - `ctx` The durable context.
    - `name` (required) A name for the map operation. Pass `""` to omit it.
    - `items` The `[]I` slice of items to process.
    - `fn` The item function called for each item. See
        [Map Function](#map-function).
    - `opts` (optional) Zero or more `BatchOption` values. See
        [MapConfig](#mapconfig).

    **Returns:** `(BatchResult[O], error)`. Item failures are captured in the
    `BatchResult`.

    **Errors:** The batch fails as a unit when its `Reason` is
    `CompletionFailureToleranceExceeded` or `CompletionCustomFailed`. `Map` then
    returns the populated `BatchResult` together with a `*BatchError`. It returns
    a `*BatchCompletionError` instead when a custom decision failed the batch and
    no item failed. A failure within the configured tolerance returns a nil
    error. Any other error (an invalid option, suspension, a checkpoint failure)
    comes with a zero `BatchResult`. Return it unchanged. See
    [Error handling](#error-handling).

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/map/map-signature.cs"
    ```

    **Parameters:**

    - `items` An `IReadOnlyList<TItem>` of items to process.
    - `func` A function called for each item. See [Map Function](#map-function).
    - `name` (optional) A name for the map operation. Omit it to infer one from the
        call site.
    - `config` (optional) A `MapConfig<TItem>` object.
    - `cancellationToken` (optional) A token linked with the SDK's workflow-shutdown
        signal, forwarded to `func`.

    **Returns:** `Task<IBatchResult<TResult>>`. Use `await` to get the result.

    **Throws:** Item exceptions are captured in the `IBatchResult`. Inspect `Failed` to
    detect failures, or call `ThrowIfError()` to re-throw the first failure. The map
    throws `MapException` only when the `CompletionConfig` criteria are violated.

### Map Function

=== "TypeScript"

    ```typescript
    type MapFunc<TInput, TOutput> = (
      context: DurableContext,
      item: TInput,
      index: number,
      array: TInput[],
    ) => Promise<TOutput>
    ```

    **Parameters:**

    - `context` The child `DurableContext` for this item's execution.
    - `item` The current item being processed.
    - `index` The zero-based index of the item in the input array.
    - `array` The full input array.

    **Returns:** `Promise<TOutput>`.

=== "Python"

    ```python
    Callable[[DurableContext, T, int, Sequence[T]], R]
    ```

    **Parameters:**

    - `ctx` The child `DurableContext` for this item's execution.
    - `item` The current item being processed.
    - `index` The zero-based index of the item in the input sequence.
    - `items` The full input sequence.

    **Returns:** `R`.

=== "Java"

    ```java
    @FunctionalInterface
    interface MapFunction<I, O> {
        O apply(I item, int index, DurableContext context);
    }
    ```

    **Parameters:**

    - `item` The current item being processed.
    - `index` The zero-based index of the item in the input collection.
    - `context` The child `DurableContext` for this item's execution.

    **Returns:** `O`.

=== "Go"

    The item function is the `fn` parameter of [`durable.Map`](#contextmap), with the
    signature `func(ctx durable.Context, item I, index int) (O, error)`.

    **Parameters:**

    - `ctx` The child `durable.Context` for this item's execution.
    - `item` The current item.
    - `index` The zero-based index of the item.

    **Returns:** `(O, error)`.

    The function takes no fourth argument for the full collection. The slice is
    in scope at the call site, so close over it when an item depends on the rest
    of the collection.

=== "C#"

    ```csharp
    Func<IDurableContext, TItem, int, IReadOnlyList<TItem>, CancellationToken, Task<TResult>>
    ```

    **Parameters:**

    - `context` The child `IDurableContext` for this item's execution.
    - `item` The current item being processed.
    - `index` The zero-based index of the item in the input list.
    - `items` The full input list.
    - `cancellationToken` A token linked with the SDK's workflow-shutdown signal. It is
        also tripped when a sibling item satisfies the `CompletionConfig` and the map
        short-circuits.

    **Returns:** `Task<TResult>`.

### MapConfig

=== "TypeScript"

    ```typescript
    interface MapConfig<TItem, TResult> {
      maxConcurrency?: number;
      itemNamer?: (item: TItem, index: number) => string;
      completionConfig?: CompletionConfig;
      serdes?: Serdes<BatchResult<TResult>>;
      itemSerdes?: Serdes<TResult>;
      summaryGenerator?: (result: BatchResult<TResult>) => string;
      nesting?: NestingType;
    }
    ```

    **Parameters:**

    - `maxConcurrency` (optional) Maximum items running at once. Default: unlimited.
    - `itemNamer` (optional) A function that returns a custom name for each item, used in
        logs and tests.
    - `completionConfig` (optional) When to stop. Default: wait for all items.
    - `serdes` (optional) Custom `Serdes` for the `BatchResult`.
    - `itemSerdes` (optional) Custom `Serdes` for individual item results.
    - `summaryGenerator` (optional) A function invoked when the serialized `BatchResult`
        exceeds 256KB. See [Checkpointing](#checkpointing).
    - `nesting` (optional) `NestingType.NESTED` (default) or `NestingType.FLAT`. See
        [Nesting](#nesting).

=== "Python"

    ```python
    @dataclass(frozen=True)
    class MapConfig(Generic[T]):
        max_concurrency: int | None = None
        completion_config: CompletionConfig = CompletionConfig()
        serdes: SerDes | None = None
        item_serdes: SerDes | None = None
        summary_generator: SummaryGenerator | None = None
        nesting_type: NestingType = NestingType.NESTED
        item_namer: Callable[[T, int], str] | None = None
    ```

    **Parameters:**

    - `max_concurrency` (optional) Maximum items running at once. Default: unlimited.
    - `completion_config` (optional) When to stop. Default: `CompletionConfig()` (lenient,
        all items run regardless of failures).
    - `serdes` (optional) Custom `SerDes` for the `BatchResult`.
    - `item_serdes` (optional) Custom `SerDes` for individual item results.
    - `summary_generator` (optional) A callable invoked when the serialized `BatchResult`
        exceeds 256KB. See [Checkpointing](#checkpointing).
    - `nesting_type` (optional) `NestingType.NESTED` (default) or `NestingType.FLAT`. See
        [Nesting](#nesting).
    - `item_namer` (optional) A deterministic callable that returns a custom name for each
        item from the item and its zero-based index.

=== "Java"

    ```java
    MapConfig.builder()
        .maxConcurrency(Integer)       // optional
        .completionConfig(CompletionConfig)  // optional
        .serDes(SerDes)                // optional
        .nestingType(NestingType)      // optional
        .itemNamer(BiFunction<Object, Integer, String>)  // optional
        .itemNamer(Class<I>, BiFunction<? super I, Integer, String>)  // optional
        .build()
    ```

    **Parameters:**

    - `maxConcurrency` (optional) Maximum items running at once. Default: unlimited.
    - `completionConfig` (optional) When to stop. Default:
        `CompletionConfig.allCompleted()`.
    - `serDes` (optional) Custom `SerDes` for item results and the overall result.
    - `nestingType` (optional) `NestingType.NESTED` (default) or `NestingType.FLAT`. See
        [Nesting](#nesting).
    - `itemNamer` (optional) A function that returns a custom name for each item from the
        item and its zero-based index. Pass the item type as the first argument to receive
        the item strongly typed instead of as `Object`. Java does not support `itemNamer`
        with `NestingType.FLAT`.

=== "Go"

    Configure `Map` with `BatchOption` values passed as trailing arguments. The same
    options configure `Parallel`.

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

    - `WithMaxConcurrency` Maximum items running at once. Zero or negative is
        invalid. Default: unlimited.
    - `WithCompletion` When to stop. Default: fail-fast. See
        [CompletionConfig](#completionconfig).
    - `WithItemNamer` Names each item from its zero-based index. The namer receives only
        the index. An index whose namer returns `""` is named `map-item-<index>`. The
        namer must be deterministic.
    - `WithNesting` `NestingNormal` (default) or `NestingFlat`. See
        [Nesting](#nesting).
    - `WithBatchSummary` A summary for an oversized result. See
        [Checkpointing](#checkpointing).
    - `WithBatchSerdes` Serializer for each item result.
    - `WithBatchResultSerdes` Serializer for the whole `BatchResult`.

=== "C#"

    ```csharp
    public sealed class MapConfig<TItem>
    {
        public int? MaxConcurrency { get; set; }               // null = unlimited
        public CompletionConfig CompletionConfig { get; set; } // default AllSuccessful()
        public NestingType NestingType { get; set; }           // default Nested
        public Func<TItem, int, string>? ItemNamer { get; set; }
    }
    ```

    **Parameters:**

    - `MaxConcurrency` (optional) Maximum items running at once. `null` (default) is
        unlimited; must be at least 1 when set.
    - `CompletionConfig` (optional) When to stop. Default: `CompletionConfig.AllSuccessful()`.
        Any item failure completes the map with `FailureToleranceExceeded`. Set
        `CompletionConfig.AllCompleted()` to run every item regardless of failures.
    - `NestingType` (optional) `NestingType.Nested` (default) or `NestingType.Flat`. See
        [Nesting](#nesting).
    - `ItemNamer` (optional) A function that returns a custom name for each item, given the
        item and its zero-based index. Used in logs and traces. When `null` (default),
        items are named by index.

    The `BatchResult` and per-item results are serialized with the `ILambdaSerializer`
    registered on `ILambdaContext.Serializer`; there is no per-item serializer slot. See
    [Serialization](../state/serialization.md).

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
    CompletionConfig.toleratedFailurePercentage(double percentage)
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

    - `MinSuccessful` Completes the batch once this many items succeed. Zero
        leaves it unset.
    - `ToleratedFailureCount` Fails the batch once more than this many items
        fail. Nil leaves it unset. `aws.Int(0)` fails on the first failure.
    - `ToleratedFailurePercentage` Fails the batch once the failure percentage,
        computed against the total item count, is strictly greater than this
        value. Nil leaves it unset. `aws.Int(0)` fails on the first failure.
    - `ShouldComplete` A custom predicate. It cannot be combined with the
        threshold fields.

    Set the pointer fields with any `*int`, for example `aws.Int` from
    `github.com/aws/aws-sdk-go-v2/aws`. The default (no `WithCompletion`) is fail-fast.
    For all-completed, set `ToleratedFailurePercentage: aws.Int(100)`, which is never
    exceeded. For first-successful, set `MinSuccessful: 1`.

=== "C#"

    Use the static factories or set the properties directly:

    ```csharp
    CompletionConfig.AllSuccessful()   // default for map: ToleratedFailureCount = 0
    CompletionConfig.AllCompleted()    // every item runs regardless of failures
    CompletionConfig.FirstSuccessful() // MinSuccessful = 1

    new CompletionConfig { MinSuccessful = count }
    new CompletionConfig { ToleratedFailureCount = count }
    new CompletionConfig { ToleratedFailurePercentage = ratio } // ratio in [0.0, 1.0]
    ```

### Result types

=== "TypeScript"

    Map returns the same `BatchResult<TResult>` type as parallel.

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

    - **`all`** all `BatchItem` entries, one per item, in input order
    - **`getResults()`** results of succeeded items, preserving input order
    - **`getErrors()`** `ChildContextError[]` for failed items
    - **`succeeded()` / `failed()` / `started()`** `BatchItem[]` filtered by status
    - **`successCount` / `failureCount` / `startedCount` / `totalCount`** item counts
    - **`status`** `SUCCEEDED` if no failures, `FAILED` otherwise
    - **`completionReason`** why the operation completed. See
        [Completion strategies](#completion-strategies).
    - **`hasFailure`** `true` if any item failed
    - **`throwIfError()`** throws the first item error, if any

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

=== "Python"

    Map returns the same `BatchResult[R]` type as parallel.

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
    ```

    - **`all`** all `BatchItem` entries, one per item, in input order
    - **`get_results()`** results of succeeded items, preserving input order
    - **`get_errors()`** `list[ErrorObject]` for failed items
    - **`succeeded()` / `failed()` / `started()`** `BatchItem` lists filtered by status
    - **`success_count` / `failure_count` / `started_count` / `total_count`** item counts
    - **`status`** `BatchItemStatus.SUCCEEDED` if no failures, `FAILED` otherwise
    - **`completion_reason`** why the operation completed. See
        [Completion strategies](#completion-strategies).
    - **`has_failure`** `True` if any item failed
    - **`throw_if_error()`** raises the first failure: `ChildContextError` for a failed
        item, `SerDesError` if an item result failed to serialize, or
        `BatchCompletionError` if a custom predicate failed the batch
    - **`to_dict()`** serializes to a plain dict. Serializability depends on `R`.

=== "Java"

    Map returns `MapResult<O>`, which differs from `ParallelResult`. It holds per-item
    results with individual status, result, and error fields.

    ```java
    record MapResult<T>(
        List<MapResultItem<T>> items,
        ConcurrencyCompletionStatus completionReason
    ) {
        MapResultItem<T> getItem(int index)
        T getResult(int index)
        MapError getError(int index)
        boolean allSucceeded()
        int size()
        List<T> results()        // all results, nulls for failed/skipped items
        List<T> succeeded()      // results of succeeded items only
        List<MapError> failed()  // errors of failed items only
    }

    record MapResultItem<T>(Status status, T result, MapError error) {
        enum Status { SUCCEEDED, FAILED, SKIPPED }
    }

    record MapError(String errorType, String errorMessage, List<String> stackTrace) {}

    enum ConcurrencyCompletionStatus {
        ALL_COMPLETED,
        MIN_SUCCESSFUL_REACHED,
        FAILURE_TOLERANCE_EXCEEDED,
        CUSTOM_COMPLETION_SUCCEEDED,
        CUSTOM_COMPLETION_FAILED
    }
    ```

    - **`items`** ordered list of `MapResultItem`, one per input item
    - **`getItem(index)`** the `MapResultItem` at the given index
    - **`getResult(index)`** the result at the given index, or `null` if failed or skipped
    - **`getError(index)`** the `MapError` at the given index, or `null` if succeeded or
        skipped
    - **`allSucceeded()`** `true` if every item has status `SUCCEEDED`
    - **`size()`** total number of items
    - **`results()`** all results as a list, with `null` for failed or skipped items
    - **`succeeded()`** results of items with status `SUCCEEDED`
    - **`failed()`** `MapError` objects for items with status `FAILED`
    - **`completionReason`** why the operation completed. See
        [Completion strategies](#completion-strategies).

    Items that did not start before the operation reached its completion criteria have
    status `SKIPPED` (not `STARTED` as in TypeScript and Python).

=== "Go"

    `Map` and `Parallel` return the same `BatchResult[O]`.

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

    - **`Items`** per-item outcomes in input order. An item started and then
        abandoned on early completion is included with `BatchItemStarted`.
        Items that never started are omitted.
    - **`Results()`** successful results in input order.
    - **`Errors()`** errors of failed items, in input order. Each is rebuilt
        from its checkpoint record. In `NestingNormal` each is a
        `*durable.ChildContextError` named after the item. In `NestingFlat` each
        is the rebuilt error the item function returned. `errors.As` matches an
        SDK error inside, such as `*durable.StepError`, but not your own error
        types. In `NestingNormal`, match your own types on
        `ChildContextError.ErrorType`.
    - **`Succeeded()` / `Failed()` / `Started()`** items filtered by status.
    - **`SuccessCount()` / `FailureCount()` / `StartedCount()` / `TotalCount()`**
        item counts. `TotalCount()` excludes items that never started.
    - **`Status()`** `BatchItemFailed` if any item failed or the batch failed as
        a unit, else `BatchItemSucceeded`. A custom decision sets it directly. A
        failure within tolerance still gives `BatchItemFailed` with a nil error.
    - **`Item(name)` / `Result(name)`** look up one item by its recorded name.
    - **`Reason`** why the batch completed.

    `BatchItemStatus.String()` and `CompletionReason.String()` return the wire
    forms, such as `SUCCEEDED` and `ALL_COMPLETED`.

    Inspect the returned error and the result. Each `BatchItem` holds `Result` (set when
    `Status` is `BatchItemSucceeded`) and `Err` (set when `Status` is
    `BatchItemFailed`).

=== "C#"

    Map returns the same `IBatchResult<TResult>` type as parallel. It holds per-item
    results with individual status, result, and error.

    ```csharp
    public interface IBatchResult<T> : IBatchResult
    {
        IReadOnlyList<IBatchItem<T>> All { get; }        // one per item, index order
        IReadOnlyList<IBatchItem<T>> Succeeded { get; }
        IReadOnlyList<IBatchItem<T>> Failed { get; }
        IReadOnlyList<IBatchItem<T>> Started { get; }
        IReadOnlyList<T> GetResults();                   // succeeded results, index order
        IReadOnlyList<DurableExecutionException> GetErrors();
        void ThrowIfError();                             // throws first item error, if any
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

    - **`All`** all `IBatchItem` entries, one per item, in original index order
    - **`GetResults()`** results of succeeded items, preserving index order
    - **`GetErrors()`** `DurableExecutionException` for failed items, in index order
    - **`Succeeded` / `Failed` / `Started`** `IBatchItem` lists filtered by status
    - **`SuccessCount` / `FailureCount` / `StartedCount` / `TotalCount`** item counts
    - **`CompletionReason`** why the operation completed. See
        [Completion strategies](#completion-strategies).
    - **`HasFailure`** `true` if any item failed
    - **`ThrowIfError()`** throws the first item error, if any

    ```csharp
    public interface IBatchItem<T>
    {
        int Index { get; }
        string? Name { get; }
        BatchItemStatus Status { get; }
        T? Result { get; }                       // set when Status == Succeeded
        DurableExecutionException? Error { get; } // set when Status == Failed
    }

    public enum BatchItemStatus
    {
        Succeeded,
        Failed,
        Started
    }
    ```

    Items that did not start before the operation reached its completion criteria have
    status `Started`.

## The map function

The map function can use any durable operation such as steps, waits, or nested map and
parallel operations. Each item runs in its own child context, so items do not share
state with each other or with the parent context.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/map/map-function.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/map/map-function.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/map/map-function.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/map/map-function.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/map/map-function.cs"
    ```

## Naming map operations

Name your map operations to make them easier to identify in logs and tests.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/map/named-map.ts"
    ```

    The name is the first argument. Pass `undefined` to omit it.

    Use `itemNamer` in `MapConfig` to give each item a custom name:

    ```typescript
    context.map("process-orders", orders, processOrder, {
      itemNamer: (order, index) => `order-${order.id}`,
    });
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/map/named-map.py"
    ```

    Pass `name` as a keyword argument. Omit it or pass `None` to leave it unnamed.

    Use `item_namer` in `MapConfig` to give each item a custom name:

    ```python
    config = MapConfig(item_namer=lambda order, index: f"order-{order['id']}")

    context.map(orders, process_order, name="process-orders", config=config)
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/map/named-map.java"
    ```

    The name is always required in Java. The SDK derives each item's name from the operation
    name: `{name}-iteration-{index}`.

    Use `itemNamer` in `MapConfig` to give each item a custom name:

    ```java
    var config = MapConfig.builder()
            .itemNamer(Order.class, (order, index) -> "order-" + order.id())
            .build();

    context.map("process-orders", orders, ProcessedOrder.class, this::processOrder, config);
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/map/named-map.go"
    ```

    The name is the required second argument. Pass `""` to leave it unnamed.

    Use `WithItemNamer` to give each item a custom name. The namer receives only
    the item's zero-based index, so close over the input slice to name from the
    item value:

    ```go
    durable.Map(ctx, "process-orders", orders, processOrder,
    	durable.WithItemNamer(func(i int) string { return "order-" + orders[i].ID }))
    ```

    The namer must be deterministic. An index whose namer returns `""` is named
    `map-item-<index>`.

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/map/named-map.cs"
    ```

    The name is the optional trailing argument. Omit it to infer one from the call site.

    Use `ItemNamer` in `MapConfig` to give each item a custom name:

    ```csharp
    var config = new MapConfig<Order>
    {
        ItemNamer = (order, index) => $"order-{order.Id}",
    };
    ```

## Configuration

Configure map behavior using `MapConfig`:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/map/map-config.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/map/map-config.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/map/map-config.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/map/map-config.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/map/map-config.cs"
    ```

## Nesting

Nested mode is the default. The SDK records each item context as a separate `CONTEXT`
operation and checkpoints the item result there. Each item appears separately in the
execution history.

In flat mode, the SDK uses a virtual context for each item and omits the per-item
`CONTEXT` operation. Durable operations inside the map function still checkpoint and
appear as children of the map operation. The SDK records the item outcome with the parent
map operation.

Use flat mode for maps with many items when each item performs few durable operations and
you do not need each item represented separately in the execution history. Flat mode
removes one checkpointed operation per item while preserving checkpoints for durable
operations inside each item.

## Completion strategies

`CompletionConfig` controls when the map operation completes. When the operation reaches
the completion criteria, it abandons items that have not completed yet. The abandoned
items will keep running in the background but cannot checkpoint their results after the
parent completes. The SDK makes a best-effort attempt to cancel ongoing work in
abandoned items, but cancellation is not guaranteed.

=== "TypeScript"

    The `BatchResult`'s `completionReason` indicates the stop condition. Items that had not
    started yet do not appear in `result.all`. Items that had started but not completed
    appear with status `STARTED`.

    | `completionConfig`             | Early exit `completionReason` | Full completion `completionReason` |
    | ------------------------------ | ----------------------------- | ---------------------------------- |
    | `{}` or omitted                | `FAILURE_TOLERANCE_EXCEEDED`  | `ALL_COMPLETED`                    |
    | `toleratedFailureCount=N`      | `FAILURE_TOLERANCE_EXCEEDED`  | `ALL_COMPLETED`                    |
    | `toleratedFailurePercentage=N` | `FAILURE_TOLERANCE_EXCEEDED`  | `ALL_COMPLETED`                    |
    | `minSuccessful=N`              | `MIN_SUCCESSFUL_REACHED`      | `ALL_COMPLETED`                    |

=== "Python"

    The `BatchResult`'s `completion_reason` indicates the stop condition. Items that
    started but did not complete appear in `result.all` with status `STARTED`. Items that
    never started are omitted from `result.all`, so `total_count` counts only the items
    that appear.

    | `completion_config`              | Early exit `completion_reason` | Full completion `completion_reason` |
    | -------------------------------- | ------------------------------ | ----------------------------------- |
    | `CompletionConfig()` (default)   | `FAILURE_TOLERANCE_EXCEEDED`   | `ALL_COMPLETED`                     |
    | `first_successful()`             | `MIN_SUCCESSFUL_REACHED`       | `ALL_COMPLETED`                     |
    | `all_completed()`                | n/a                            | `ALL_COMPLETED`                     |
    | `tolerated_failure_count=N`      | `FAILURE_TOLERANCE_EXCEEDED`   | `ALL_COMPLETED`                     |
    | `tolerated_failure_percentage=N` | `FAILURE_TOLERANCE_EXCEEDED`   | `ALL_COMPLETED`                     |
    | `min_successful=N`               | `MIN_SUCCESSFUL_REACHED`       | `ALL_COMPLETED`                     |

    The default `CompletionConfig()` is fail-fast: any item failure exceeds the tolerance
    and completes the batch early. Use `CompletionConfig.all_completed()` to run every item
    regardless of failures.

    Set `should_complete` when the threshold fields cannot express the rule. The predicate
    receives a `CompletionStatus` (`success_count`, `failure_count`, `completed_count`,
    `total_count`, and `items`, a per-item tuple of `CompletionItemStatus`) and returns a
    `CompletionDecision`: `continue_batch()` to keep going, or
    `complete_batch(outcome)` to stop, where `outcome` defaults to
    `CompletionOutcome.SUCCEEDED`. A `CompletionOutcome.FAILED` outcome marks the whole
    batch failed, and `throw_if_error()` then raises `BatchCompletionError` even when no
    individual item failed.

    ```python
    --8<-- "examples/python/operations/map/custom-completion.py"
    ```

    The predicate runs before any item is scheduled (`completed_count == 0`) and again on
    each terminal or suspension event, so it must handle the initial zero-progress
    snapshot. It cannot be combined with `min_successful`, `tolerated_failure_count`, or
    `tolerated_failure_percentage`, and must be deterministic, side-effect-free, and
    monotonic. Unscheduled items report `status=None` in `items`.

=== "Java"

    The `MapResult`'s `completionReason` indicates the stop condition. Items that did not
    start before the operation completed have status `SKIPPED`.

    | `completionConfig`              | Early exit `completionReason` | Full completion `completionReason` |
    | ------------------------------- | ----------------------------- | ---------------------------------- |
    | `allCompleted()` (default)      | n/a                           | `ALL_COMPLETED`                    |
    | `allSuccessful()`               | `FAILURE_TOLERANCE_EXCEEDED`  | `ALL_COMPLETED`                    |
    | `firstSuccessful()`             | `MIN_SUCCESSFUL_REACHED`      | `ALL_COMPLETED`                    |
    | `minSuccessful(N)`              | `MIN_SUCCESSFUL_REACHED`      | `ALL_COMPLETED`                    |
    | `toleratedFailureCount(N)`      | `FAILURE_TOLERANCE_EXCEEDED`  | `ALL_COMPLETED`                    |
    | `toleratedFailurePercentage(p)` | `FAILURE_TOLERANCE_EXCEEDED`  | `ALL_COMPLETED`                    |

    Use `CompletionConfig.shouldComplete(...)` when the predefined thresholds cannot
    express the completion rule. The SDK evaluates the function as completion state
    changes. It receives a `CompletionStatus` with `successCount`, `failureCount`,
    `completedCount`, `totalCount`, and `allItemsRegistered`. Map registers all items
    before processing begins.

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

    A custom completion function is mutually exclusive with `minSuccessful`,
    `toleratedFailureCount`, and `toleratedFailurePercentage`. It must return a
    non-null decision. Keep it deterministic and free of side effects. Items that have
    not started when it completes have status `SKIPPED`.
    `CUSTOM_COMPLETION_FAILED` does not throw automatically. Inspect
    `result.completionReason().isSucceeded()` to distinguish the custom outcomes.

=== "Go"

    `BatchResult.Reason` records the stop condition. Items that never started are
    omitted from `Items`. An item that started but did not complete appears with
    `BatchItemStarted`. `Map` does not leave abandoned items running. An abandoned
    item stops at its next durable operation. `Map` returns only after every
    started item has stopped.

    | `CompletionConfig`                       | Early exit `Reason`                                     | Full completion `Reason` |
    | ---------------------------------------- | ------------------------------------------------------- | ------------------------ |
    | zero value (default, fail-fast)          | `CompletionFailureToleranceExceeded`                    | `CompletionAllCompleted` |
    | `ToleratedFailureCount: aws.Int(N)`      | `CompletionFailureToleranceExceeded`                    | `CompletionAllCompleted` |
    | `ToleratedFailurePercentage: aws.Int(N)` | `CompletionFailureToleranceExceeded`                    | `CompletionAllCompleted` |
    | `MinSuccessful: N`                       | `CompletionMinSuccessfulReached`                        | `CompletionAllCompleted` |
    | `ShouldComplete: ...`                    | `CompletionCustomSucceeded` or `CompletionCustomFailed` | `CompletionAllCompleted` |

    The default is fail-fast. Set `ToleratedFailurePercentage: aws.Int(100)` to run
    every item regardless of failures.

    Set `ShouldComplete` when the threshold fields cannot express the rule. The
    predicate receives a `BatchProgress` snapshot before the first item and again
    after each item reaches a terminal state, so it must handle the initial
    zero-progress snapshot. It must be deterministic. Return `ContinueBatch()` to
    keep going, or `CompleteBatch(durable.CompletionOutcomeSucceeded)` or
    `CompleteBatch(durable.CompletionOutcomeFailed)` to stop and classify the
    result. With `ShouldComplete` set, an item failure does not stop the batch by
    itself. A failed custom outcome returns a `*BatchError`, or a
    `*BatchCompletionError` when no item failed. A predicate that panics fails the
    operation with an error that is not a `*BatchError`.

    ```go
    --8<-- "examples/go/operations/map/custom-completion.go"
    ```

=== "C#"

    The `IBatchResult`'s `CompletionReason` indicates the stop condition. Items that were
    not dispatched before the operation completed have status `Started`.

    | `CompletionConfig`                   | Early exit `CompletionReason` | Full completion `CompletionReason` |
    | ------------------------------------ | ----------------------------- | ---------------------------------- |
    | `AllSuccessful()` (default)          | `FailureToleranceExceeded`    | `AllCompleted`                     |
    | `AllCompleted()`                     | n/a                           | `AllCompleted`                     |
    | `FirstSuccessful()`                  | `MinSuccessfulReached`        | `AllCompleted`                     |
    | `MinSuccessful = N`                  | `MinSuccessfulReached`        | `AllCompleted`                     |
    | `ToleratedFailureCount = N`          | `FailureToleranceExceeded`    | `AllCompleted`                     |
    | `ToleratedFailurePercentage = ratio` | `FailureToleranceExceeded`    | `AllCompleted`                     |

!!! note

    When using a `minSuccessful` strategy, failures do not trigger early exit. If all items
    fail before the success threshold is reached, the operation completes with
    `ALL_COMPLETED`.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/map/completion-config.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/map/completion-config.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/map/completion-config.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/map/completion-config.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/map/completion-config.cs"
    ```

## Error handling

When an item throws an error, map captures the error in the result rather than
propagating it immediately. Other items continue running.

=== "TypeScript"

    `BatchResult.status` is `FAILED` if any item failed. Call `throwIfError()` to propagate
    the first item error as an exception, or inspect `getErrors()` to handle errors
    individually.

    ```typescript
    --8<-- "examples/typescript/operations/map/error-handling.ts"
    ```

=== "Python"

    `BatchResult.status` is `FAILED` if any item failed. Call `throw_if_error()` to
    propagate the first item error as an exception, or inspect `get_errors()` to handle
    errors individually.

    ```python
    --8<-- "examples/python/operations/map/error-handling.py"
    ```

=== "Java"

    Check `result.failed()` to detect item failures. Each `MapError` contains `errorType`,
    `errorMessage`, and `stackTrace` as plain strings. If the SDK cannot reconstruct the
    original exception, it throws `MapIterationFailedException`.

    ```java
    --8<-- "examples/java/operations/map/error-handling.java"
    ```

=== "Go"

    `Map` captures each item error in the result. By default (fail-fast) the
    first item failure completes the batch. Items still running are abandoned,
    and items not yet started never run. `Map` then returns the populated
    result together with a `*durable.BatchError`, whose `Errors` holds the
    per-item errors. Match it with `errors.As` and read the failures from
    `BatchResult.Errors()` and `BatchResult.Failed()`. Return any other error
    unchanged. To run every item despite failures, set a tolerance with
    `WithCompletion`.

    ```go
    --8<-- "examples/go/operations/map/error-handling.go"
    ```

=== "C#"

    `IBatchResult.HasFailure` is `true` if any item failed. Call `ThrowIfError()` to
    propagate the first item error as an exception, or inspect `GetErrors()` (which returns
    `DurableExecutionException` objects) to handle errors individually.

    ```csharp
    --8<-- "examples/csharp/operations/map/error-handling.cs"
    ```

## Checkpointing

Checkpoint behavior depends on the nesting type. In nested mode, each item checkpoints
its result in a per-item `CONTEXT` operation. In flat mode, the SDK omits that context
checkpoint and records the item outcome with the parent map operation. Durable operations
inside an item still checkpoint in both modes.

Items that have not completed when the map operation reaches its completion criteria
receive no further checkpoint updates. Unless noted otherwise, the language-specific
details below describe nested mode.

=== "TypeScript"

    The parent map operation also checkpoints the serialized `BatchResult` for
    observability. On replay, the SDK deserializes the `BatchResult` directly from that
    checkpoint.

    For results over 256KB, the SDK cannot store the full `BatchResult` in the checkpoint.
    Instead, the SDK reconstructs the `BatchResult` from the checkpointed results of the
    individual items. In that case, the checkpoint stores a compact JSON summary, which is
    for observability only.

    The default summary generator produces:

    ```json
    {
      "type": "MapResult",
      "totalCount": 5,
      "successCount": 4,
      "failureCount": 1,
      "completionReason": "ALL_COMPLETED",
      "status": "FAILED"
    }
    ```

=== "Python"

    The parent map operation also checkpoints the serialized `BatchResult` for
    observability. On replay, the SDK deserializes the `BatchResult` directly from that
    checkpoint.

    For results over 256KB, the SDK cannot store the full `BatchResult` in the checkpoint,
    so it re-executes the items to reconstruct it instead. In that case, the checkpoint
    stores the output of `summary_generator`, which is for observability only.

    The default summary generator produces:

    ```json
    {
      "type": "MapResult",
      "totalCount": 5,
      "successCount": 4,
      "failureCount": 1,
      "completionReason": "ALL_COMPLETED",
      "status": "FAILED"
    }
    ```

    When you pass a custom `MapConfig` without setting `summary_generator`, the SDK
    checkpoints an empty string for large payloads.

    `SummaryGenerator` is a callable protocol you can pass by setting `summary_generator` on
    [`MapConfig`](#mapconfig):

    ```python
    class SummaryGenerator(Protocol[T]):
        def __call__(self, result: T) -> str: ...
    ```

=== "Java"

    For results under 256KB, the SDK checkpoints the serialized `MapResult` payload. On
    replay, the SDK deserializes the `MapResult` directly from that checkpoint without
    re-executing items.

    For results over 256KB, the SDK checkpoints with an empty payload and a `replayChildren`
    flag. On replay, the SDK re-executes the items to reconstruct the `MapResult` from their
    individual checkpoints.

=== "Go"

    `WithNesting` selects the checkpoint shape. In `NestingNormal` (the default)
    each item checkpoints its result in its own child context. In `NestingFlat`
    the SDK omits the per-item context and records the item outcome with the
    parent map. Durable operations inside an item still checkpoint in both modes.

    The SDK checkpoints the whole `BatchResult` when it is at most 256KB
    serialized. On replay, the SDK decodes that `BatchResult` without running the
    items. A larger result is not stored. The checkpoint then keeps the child
    operations plus a compact record, and replay rebuilds each item from that
    item's own checkpoint. The record holds `type`, `totalCount`, `successCount`,
    `failureCount`, `completionReason`, `status`, and `itemStatuses`, one
    character per started item. Pass `WithBatchSummary` to add your own string
    under the `summary` key. `WithBatchSerdes` sets the per-item serializer and
    `WithBatchResultSerdes` the whole-result serializer.

=== "C#"

    In nested mode, the SDK reconstructs `IBatchResult` from the per-item child-context
    checkpoints without re-executing completed items. In flat mode, the SDK records item
    results and errors inline on the parent map operation instead.

    The SDK serializes results with the `ILambdaSerializer` registered on
    `ILambdaContext.Serializer`; there is no per-item summary generator to configure.

## Nesting map operations

A map function can call `context.map()` or `context.parallel()` to create nested
operations. Each nested map creates its own set of child contexts.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/map/nested-map.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/map/nested-map.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/map/nested-map.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/map/nested-map.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/map/nested-map.cs"
    ```

## See also

- [Parallel operations](parallel.md) execute different functions concurrently
- [Child contexts](child-context.md) understand child context isolation
- [Steps](step.md) use steps within map functions
- [Error handling](../error-handling/errors.md) in durable functions

!!! info "Checkpoint consumption"

    Durable operations consume checkpoints. To understand how this operation affects
    your checkpoint usage, see
    [Checkpoint consumption](https://docs.aws.amazon.com/lambda/latest/dg/durable-execution-sdk.html#durable-operations-checkpoint-consumption).
