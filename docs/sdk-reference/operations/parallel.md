# Parallel

## Concurrent branches

Parallel executes multiple operations concurrently. It manages concurrency, collects
results as branches complete, and checkpoints the outcome.

Each branch runs in its own [child context](child-context.md) and checkpoints its result
independently as it completes.

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

    - `functions` A sequence of callables, each receiving a `DurableContext` and returning
        `T`.
    - `name` (optional) A name for the parallel operation.
    - `config` (optional) A `ParallelConfig` object.

    **Returns:** `BatchResult[T]`.

    **Raises:** Branch exceptions are captured in the `BatchResult`. Call `throw_if_error()`
    to re-raise the first failure.

    Each element in `functions` is a plain callable `(ctx: DurableContext) -> T`. Python has
    no named-branch wrapper type.

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

### ParallelConfig

=== "TypeScript"

    ```typescript
    interface ParallelConfig<TResult> {
      maxConcurrency?: number;
      completionConfig?: CompletionConfig;
      serdes?: Serdes<BatchResult<TResult>>;
      itemSerdes?: Serdes<TResult>;
      nesting?: NestingType;
    }
    ```

    **Parameters:**

    - `maxConcurrency` (optional) Maximum branches running at once. Default: unlimited.
    - `completionConfig` (optional) When to stop. Default: wait for all branches.
    - `serdes` (optional) Custom `Serdes` for the `BatchResult`.
    - `itemSerdes` (optional) Custom `Serdes` for individual branch results.
    - `nesting` (optional) `NestingType.NESTED` (default) or `NestingType.FLAT`. `FLAT`
        reduces operation overhead by ~30% at the cost of lower observability.

=== "Python"

    ```python
    @dataclass(frozen=True)
    class ParallelConfig:
        max_concurrency: int | None = None
        completion_config: CompletionConfig = CompletionConfig.all_successful()
        serdes: SerDes | None = None
        item_serdes: SerDes | None = None
        summary_generator: SummaryGenerator | None = None
    ```

    **Parameters:**

    - `max_concurrency` (optional) Maximum branches running at once. Default: unlimited.
    - `completion_config` (optional) When to stop. Default:
        `CompletionConfig.all_successful()`.
    - `serdes` (optional) Custom `SerDes` for the `BatchResult`.
    - `item_serdes` (optional) Custom `SerDes` for individual branch results.
    - `summary_generator` (optional) A callable invoked when the serialized `BatchResult`
        exceeds 256KB. See [Checkpointing](#checkpointing).

=== "Java"

    ```java
    ParallelConfig.builder()
        .maxConcurrency(Integer)      // optional
        .completionConfig(CompletionConfig)  // optional
        .build()
    ```

    **Parameters:**

    - `maxConcurrency` (optional) Maximum branches running at once. Default: unlimited.
    - `completionConfig` (optional) When to stop. Default:
        `CompletionConfig.allCompleted()`.

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
    ```

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
    - **`throw_if_error()`** raises the first branch error as a `CallableRuntimeError`
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
        FAILURE_TOLERANCE_EXCEEDED
    }
    ```

    - **`size`** total number of registered branches
    - **`succeeded`** number of branches that succeeded
    - **`failed`** number of branches that failed
    - **`completionStatus`** why the operation completed. See
        [Completion strategies](#completion-strategies).

    `ConcurrencyCompletionStatus.isSucceeded()` returns `true` for both `ALL_COMPLETED` and
    `MIN_SUCCESSFUL_REACHED`. To check if any branch failed, use `result.failed() > 0`
    (where `result` is a `ParallelResult`).

    `ParallelResult` contains only aggregate counts. To get individual branch results, hold
    the `DurableFuture<T>` returned by each `branch()` call and call `.get()` on it after
    `parallel.get()` returns. Results are available in the order branches were registered.

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

    Branch functions are synchronous callables that receive a `DurableContext` and return
    `T`.

    ```python
    --8<-- "examples/python/operations/parallel/named-branches.py"
    ```

=== "Java"

    Each branch is registered via `ParallelDurableFuture.branch()`. The branch function is a
    synchronous `Function<DurableContext, T>`.

    ```java
    --8<-- "examples/java/operations/parallel/named-branches.java"
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

## Naming parallel operations

Name your parallel operations to make them easier to identify in logs and tests.

=== "TypeScript"

    The name is the first argument. Pass `undefined` to omit it.

=== "Python"

    Pass `name` as a keyword argument. Omit it or pass `None` to leave it unnamed.

=== "Java"

    The name is always required. Each `branch()` call also requires a name. Pass `null` to
    omit it.

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
    parallel operation completed. Branches that were never started appear in `result.all`
    with status `STARTED`.

    | `completion_config`              | Early exit `completion_reason` | Full completion `completion_reason` |
    | -------------------------------- | ------------------------------ | ----------------------------------- |
    | `all_successful()` (default)     | `FAILURE_TOLERANCE_EXCEEDED`   | `ALL_COMPLETED`                     |
    | `first_successful()`             | `MIN_SUCCESSFUL_REACHED`       | `ALL_COMPLETED`                     |
    | `tolerated_failure_count=N`      | `FAILURE_TOLERANCE_EXCEEDED`   | `ALL_COMPLETED`                     |
    | `tolerated_failure_percentage=N` | `FAILURE_TOLERANCE_EXCEEDED`   | `ALL_COMPLETED`                     |
    | `min_successful=N`               | `MIN_SUCCESSFUL_REACHED`       | `ALL_COMPLETED`                     |

    !!! warning

        `CompletionConfig.all_completed()` is deprecated. Use
        `CompletionConfig.all_successful()` instead.

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

## Checkpointing

Each branch checkpoints its result on completion. Branches that have not completed yet
when the parallel operation reaches its completion criteria remain with status `STARTED`
and will receive no further checkpoint updates.

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

## See also

- [Map operations](map.md) run the same function concurrently on a collection
- [Child contexts](child-context.md) understand child context isolation
- [Steps](step.md) use steps within parallel branches
- [Error handling](../error-handling/errors.md) in durable functions
