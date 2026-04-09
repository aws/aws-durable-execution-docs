# Map

## Apply a function to each item in a collection

Map executes a function for each item in a collection concurrently. It manages
concurrency, collects results as items complete, and checkpoints the outcome.

Each item runs in its own [child context](child-context.md) and checkpoints its result
independently as it completes.

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
    - `items` A `Collection<I>` of items to process.
    - `resultType` `Class<O>` or `TypeToken<O>` for deserialization.
    - `function` A `MapFunction<I, O>` called for each item. See
        [Map Function](#map-function).
    - `config` (optional) A `MapConfig` object.

    **Returns:** `MapResult<O>` from `map()`, or `DurableFuture<MapResult<O>>` from
    `mapAsync()`.

    **Throws:** Item exceptions are captured in `MapResult`. Inspect `failed()` to detect
    failures. If the SDK cannot reconstruct the original exception, it throws
    `MapIterationFailedException`.

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

### MapConfig

=== "TypeScript"

    ```typescript
    interface MapConfig<TItem, TResult> {
      maxConcurrency?: number;
      itemNamer?: (item: TItem, index: number) => string;
      completionConfig?: CompletionConfig;
      serdes?: Serdes<BatchResult<TResult>>;
      itemSerdes?: Serdes<TResult>;
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
    - `nesting` (optional) `NestingType.NESTED` (default) or `NestingType.FLAT`. `FLAT`
        reduces operation overhead by ~30% at the cost of lower observability.

=== "Python"

    ```python
    @dataclass(frozen=True)
    class MapConfig:
        max_concurrency: int | None = None
        completion_config: CompletionConfig = CompletionConfig()
        serdes: SerDes | None = None
        item_serdes: SerDes | None = None
        summary_generator: SummaryGenerator | None = None
    ```

    **Parameters:**

    - `max_concurrency` (optional) Maximum items running at once. Default: unlimited.
    - `completion_config` (optional) When to stop. Default: `CompletionConfig()` (lenient,
        all items run regardless of failures).
    - `serdes` (optional) Custom `SerDes` for the `BatchResult`.
    - `item_serdes` (optional) Custom `SerDes` for individual item results.
    - `summary_generator` (optional) A callable invoked when the serialized `BatchResult`
        exceeds 256KB. See [Checkpointing](#checkpointing).

=== "Java"

    ```java
    MapConfig.builder()
        .maxConcurrency(Integer)       // optional
        .completionConfig(CompletionConfig)  // optional
        .serDes(SerDes)                // optional
        .build()
    ```

    **Parameters:**

    - `maxConcurrency` (optional) Maximum items running at once. Default: unlimited.
    - `completionConfig` (optional) When to stop. Default:
        `CompletionConfig.allCompleted()`.
    - `serDes` (optional) Custom `SerDes` for item results and the overall result.

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
    ```

=== "Java"

    ```java
    CompletionConfig.allCompleted()
    CompletionConfig.allSuccessful()
    CompletionConfig.firstSuccessful()
    CompletionConfig.minSuccessful(int count)
    CompletionConfig.toleratedFailureCount(int count)
    CompletionConfig.toleratedFailurePercentage(double percentage)
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
    - **`throw_if_error()`** raises the first item error as a `CallableRuntimeError`
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
        FAILURE_TOLERANCE_EXCEEDED
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

=== "Java"

    ```java
    --8<-- "examples/java/operations/map/named-map.java"
    ```

    The name is always required in Java. The SDK derives each item's name from the operation
    name: `{name}-iteration-{index}`.

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

    The `BatchResult`'s `completion_reason` indicates the stop condition. Items that were
    never started appear in `result.all` with status `STARTED`.

    | `completion_config`              | Early exit `completion_reason` | Full completion `completion_reason` |
    | -------------------------------- | ------------------------------ | ----------------------------------- |
    | `CompletionConfig()` (default)   | `FAILURE_TOLERANCE_EXCEEDED`   | `ALL_COMPLETED`                     |
    | `first_successful()`             | `MIN_SUCCESSFUL_REACHED`       | `ALL_COMPLETED`                     |
    | `tolerated_failure_count=N`      | `FAILURE_TOLERANCE_EXCEEDED`   | `ALL_COMPLETED`                     |
    | `tolerated_failure_percentage=N` | `FAILURE_TOLERANCE_EXCEEDED`   | `ALL_COMPLETED`                     |
    | `min_successful=N`               | `MIN_SUCCESSFUL_REACHED`       | `ALL_COMPLETED`                     |

    !!! warning

        `CompletionConfig.all_completed()` is deprecated. Use the default `CompletionConfig()`
        instead.

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

## Checkpointing

Each item checkpoints its result on completion. Items that have not completed when the
map operation reaches its completion criteria remain with status `STARTED` and will
receive no further checkpoint updates.

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

## See also

- [Parallel operations](parallel.md) execute different functions concurrently
- [Child contexts](child-context.md) understand child context isolation
- [Steps](steps.md) use steps within map functions
- [Error handling](../advanced/error-handling.md) in durable functions
