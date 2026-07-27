# DAG

## Run tasks in a dependency graph

`dag()` runs a set of named tasks whose execution order is derived from the
dependencies you declare between them. You describe the graph once, and the SDK
works out what can run in parallel, what must wait, and what to skip. Use it
when a workflow is a fan out and fan in shape rather than a straight line: two
independent fetches followed by a join, a diamond, or a pipeline where one
branch failing should not stop the others.

A DAG differs from [`map()`](map.md) and [`parallel()`](parallel.md) in what it
lets you express. `map()` applies one function to many items, and `parallel()`
runs a fixed set of branches at the same time. Neither knows about edges between
its units of work. In a DAG each task can depend on any earlier task, read that
task's result, and run only when a condition you supply holds.

!!! note "Experimental feature"

    The DAG interface is currently experimental and may change in a future
    release.

!!! warning "TypeScript, Python, and Java only"

    `dag()` is not yet available in the C# SDK.

The following DAG fetches users and orders at the same time, then joins them
once both finish. `join` declares the two fetches as dependencies, so the SDK
starts it only after both succeed, and passes their results to it.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/dag/simple-dag.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/dag/simple-dag.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/dag/simple-dag.java"
    ```

Nothing runs while you declare tasks. The registration callback only builds the
graph. Execution starts after the callback returns, and the call resolves to a
`DagResult` describing every task.

## Method signature

### context.dag

=== "TypeScript"

    ```typescript
    dag(
      name: string,
      register: (dagCtx: DagContext) => void | Promise<void>,
      config?: DagConfig,
    ): DurablePromise<DagResult>
    ```

    **Parameters:**

    - **name** the DAG's name, used to derive its checkpoint identity
    - **register** declares the tasks. May be sync or async. Runs once per
        invocation, before any task starts.
    - **config** optional [DagConfig](#dagconfig)

    **Returns:** `DurablePromise<DagResult>`, awaited for a
    [DagResult](#dagresult).

    **Throws:** `DagPredicateError` when a `runIf` predicate throws. Registration
    errors surface as `DagCyclicDependencyError`, `DagDuplicateTaskError`,
    `DagInvalidTaskNameError`, or `DagInvalidDependencyError`. A failing task does
    not reject the promise. See [Handle task failures](#handle-task-failures).

=== "Python"

    ```python
    def dag(
        register: Callable[[DagContext], None],
        name: str | None = None,
        config: DagConfig | None = None,
    ) -> DagResult
    ```

    **Parameters:**

    - **register** declares the tasks. Runs once per invocation, before any task
        starts.
    - **name** the DAG's name, used to derive its checkpoint identity
    - **config** optional [DagConfig](#dagconfig)

    **Returns:** `DagResult` directly. Python's DAG API is synchronous, so there
    is nothing to await.

    **Raises:** `DagPredicateError` when a `run_if` predicate raises. Registration
    errors surface as `DagCyclicDependencyError`, `DagDuplicateTaskError`,
    `DagInvalidTaskNameError`, or `DagInvalidDependencyError`. A failing task does
    not raise. See [Handle task failures](#handle-task-failures).

=== "Java"

    ```java
    // sync
    DagResult dag(String name, Consumer<DagContext> register)
    DagResult dag(String name, Consumer<DagContext> register, DagConfig config)

    // async
    DurableFuture<DagResult> dagAsync(String name, Consumer<DagContext> register)
    DurableFuture<DagResult> dagAsync(String name, Consumer<DagContext> register, DagConfig config)
    ```

    **Parameters:**

    - **name** the DAG's name, used to derive its checkpoint identity
    - **register** declares the tasks. Runs once per invocation, before any task
        starts.
    - **config** optional [DagConfig](#dagconfig)

    **Returns:** `DagResult` from the sync form, `DurableFuture<DagResult>` from
    the async form.

    **Throws:** `DagPredicateException` when a `runIf` predicate throws.
    Registration errors surface as `DagCyclicDependencyException`,
    `DagDuplicateTaskException`, `DagInvalidTaskNameException`, or
    `DagInvalidDependencyException`. Every DAG exception is unchecked, so no
    `throws` clause is required. A failing task does not throw. See
    [Handle task failures](#handle-task-failures).

The smallest useful call registers one task and reads its result back.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/dag/dag-signature.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/dag/dag-signature.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/dag/dag-signature.java"
    ```

### Task kinds

A task is not limited to a step. The registration context exposes nine kinds,
and each one returns a handle you can depend on. The names differ slightly by
language.

=== "TypeScript"

    ```typescript
    step(name, deps, fn, config?)
    invoke(name, funcId, deps, payloadFn, config?)
    callback(name, deps, submitter, config?)
    wait(name, deps, duration, config?)
    waitForCondition(name, deps, check, config)
    runInChildContext(name, deps, fn, config?)
    map(name, deps, items, mapFunc, config?)
    parallel(name, deps, branches, config?)
    dag(name, deps, register, config?)
    ```

    `deps` is always an explicit argument, never inferred. Pass `[]` for a task
    with no dependencies, which also selects a callback shape without the `deps`
    parameter.

    Two exceptions worth knowing. `waitForCondition` requires `config`, while
    every other kind's `config` is optional. `invoke` takes `funcId` before
    `deps`.

=== "Python"

    ```python
    step(func, deps=None, name=None, config=None, *, trigger_rule=..., run_if=None)
    invoke(function_name, payload_fn, deps=None, name=None, config=None, *, ...)
    wait_for_callback(submitter, deps=None, name=None, config=None, *, ...)
    wait(seconds, deps=None, name=None, *, ...)
    wait_for_condition(check, config, deps=None, name=None, *, ...)
    run_in_child_context(func, deps=None, name=None, config=None, *, ...)
    map(inputs, func, deps=None, name=None, config=None, *, ...)
    parallel(functions, deps=None, name=None, config=None, *, ...)
    dag(register, deps=None, name=None, config=None, *, ...)
    ```

    Every kind takes `deps` and `name` as keyword arguments, plus keyword only
    `trigger_rule` and `run_if`.

    A task body takes two positional arguments, the dependency map first and the
    operation context second, as in `lambda deps, sc: ...`. That differs from a
    top level `context.step()` body, which receives only the context.

=== "Java"

    ```java
    <T> TaskHandle<T> step(String name, Class<T> type, DagStepFunction<T> fn)
    <T> TaskHandle<T> invoke(String name, String functionName, Class<T> type, DagPayloadFunction payloadFn)
    <T> TaskHandle<T> callback(String name, Class<T> type, DagCallbackSubmitter submitter)
    TaskHandle<Void> wait(String name, Duration duration)
    <S> TaskHandle<S> waitForCondition(String name, Class<S> type, DagConditionFunction<S> check, WaitForConditionConfig<S> config)
    <T> TaskHandle<T> runInChildContext(String name, Class<T> type, DagChildFunction<T> fn)
    <I,O> TaskHandle<MapResult<O>> map(String name, Collection<I> items, Class<O> type, MapFunction<I,O> fn)
    TaskHandle<ParallelResult> parallel(String name, Consumer<ParallelDurableFuture> branches)
    TaskHandle<DagResult> dag(String name, Consumer<DagContext> register)
    ```

    Each kind also has a `config` overload, and `step` adds positional arity
    sugar for one, two, or three dependencies so the values arrive as typed
    parameters instead of through `Deps`.

    Result types are explicit tokens. Pass `Integer.class` for a simple type, or
    a `TypeToken<List<String>>` for a generic one. Nothing is inferred from the
    lambda.

The following DAG registers four different kinds in one graph.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/dag/task-kinds.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/dag/task-kinds.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/dag/task-kinds.java"
    ```

A `map` task's result is a batch result, and a nested `dag` task's result is
another `DagResult`, so a downstream task reads them through those types.

### DagConfig

Configuration belongs to the container, not to individual tasks. A task keeps
its own operation config, such as a step's retry policy.

=== "TypeScript"

    ```typescript
    interface DagConfig {
      maxConcurrency?: number;
      completionConfig?: DagCompletionConfig;
      defaultRetryStrategy?: (error: Error, attemptCount: number) => RetryDecision;
      defaultTriggerRule?: TriggerRule;
      serdes?: Serdes<DagResult>;
      nesting?: NestingType;
    }
    ```

=== "Python"

    ```python
    @dataclass
    class DagConfig:
        max_concurrency: int | None = None
        completion_config: CompletionConfig | None = None
        default_retry_strategy: Callable[[Exception, int], RetryDecision] | None = None
        default_trigger_rule: TriggerRule = TriggerRule.ALL_SUCCESS
        serdes: SerDes | None = None
    ```

    Leaving `max_concurrency` unset applies the default of 40, which the
    scheduler supplies rather than the dataclass.

=== "Java"

    ```java
    DagConfig.builder()
        .maxConcurrency(int)
        .completionConfig(DagCompletionConfig)
        .defaultRetryStrategy(RetryStrategy)
        .defaultTriggerRule(TriggerRule)
        .serDes(SerDes)
        .build()
    ```

    `maxConcurrency` must be at least 1, and the builder throws
    `IllegalArgumentException` otherwise. `DagCompletionConfig` is a sealed
    interface with static factories: `allCompleted()`, `allSuccessful()`,
    `firstSuccessful()`, `minSuccessful(n)`, `toleratedFailureCount(n)`, and
    `toleratedFailurePercentage(p)`.

`maxConcurrency` defaults to 40. `completionConfig` defaults to draining the
whole reachable graph, so every task that can run does run. Set a completion
config to finish early, for example once a number of tasks succeed or once
failures exceed a tolerance.

### DagResult

`DagResult` reports every task, whether it succeeded, failed, or was skipped.

=== "TypeScript"

    ```typescript
    getResult<TResult>(handle: TaskHandle<string, TResult>): TResult | undefined
    getResult(name: string): unknown
    getStatus(taskNameOrHandle: string | AnyTaskHandle): TaskStatus | undefined
    succeeded(): TaskExecution[]
    failed(): TaskExecution[]
    skipped(): TaskExecution[]
    throwIfError(): void

    readonly results: ReadonlyMap<string, TaskExecution>
    readonly successCount: number
    readonly failureCount: number
    readonly skippedCount: number
    readonly totalCount: number
    readonly completionReason: DagCompletionReason
    ```

    Reading by handle is typed to that task's result. Reading by name returns
    `unknown` and needs a cast.

=== "Python"

    ```python
    def get_result(task: str | TaskHandle) -> Any
    def get_status(task: str | TaskHandle) -> TaskStatus | None
    def succeeded() -> list[TaskExecution]
    def failed() -> list[TaskExecution]
    def skipped() -> list[TaskExecution]
    def throw_if_error() -> None

    results: Mapping[str, TaskExecution]
    success_count: int
    failure_count: int
    skipped_count: int
    total_count: int
    completion_reason: DagCompletionReason
    ```

    Both accessors take either a task name or a handle.

=== "Java"

    ```java
    <T> Optional<T> getResult(TaskHandle<T> handle)
    Optional<Object> getResult(String name)
    Optional<TaskStatus> getStatus(TaskHandle<?> handle)
    Optional<TaskStatus> getStatus(String name)
    List<TaskExecution<?>> succeeded()
    List<TaskExecution<?>> failed()
    List<TaskExecution<?>> skipped()
    Map<String, TaskExecution<?>> results()
    int successCount()
    int failureCount()
    int skippedCount()
    int totalCount()
    DagCompletionReason completionReason()
    void throwIfError()
    ```

    A `TaskHandle` cannot escape the registration lambda, so code after the DAG
    returns reads results by name and casts. Inside a downstream task body the
    handle is in scope, so `deps.get(handle)` there is fully typed.

A task's entry carries its name, status, skip reason, result, error, and
timestamps. Statuses are `SUCCEEDED`, `FAILED`, `SKIPPED`, and `STARTED`, where
`STARTED` is a live state that a finished DAG does not report. A skip reason is
either `TRIGGER_RULE` or `RUN_IF_PREDICATE`.

`completionReason` is `ALL_COMPLETED`, `COMPLETED_WITH_FAILURES`,
`MIN_SUCCESSFUL_REACHED`, or `FAILURE_TOLERANCE_EXCEEDED`.

## Declare dependencies

Declaring a task as a dependency does two things. It orders the graph, and it
passes that task's result to the dependent task. The following diamond seeds a
value, transforms it twice in parallel, then merges both results.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/dag/task-deps.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/dag/task-deps.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/dag/task-deps.java"
    ```

`left` and `right` both depend only on `seed`, so they run at the same time.
`merge` waits for both.

=== "TypeScript"

    Pass dependencies as the handle array, then read them from the first
    callback argument, keyed by task name: `deps["seed"]`. The map is typed from
    the handle array, so each value carries its task's result type.

=== "Python"

    Pass `deps=[handle, ...]` at registration, or chain `.after(handle)` on the
    returned handle. Read a result with either the name or the handle, as
    `deps["seed"]` or `deps[handle]`.

=== "Java"

    Java cannot inspect a lambda body, so inline dependencies are declared with
    `.reads(handle)` and read back with `deps.get(handle)`. The handle carries
    the result type, so the value needs no cast. Calling `deps.get` for a handle
    you did not declare with `.reads(...)` throws `IllegalStateException`.

There are two kinds of edge. A read edge orders the task and hands over the
result. An ordering edge, declared with `after`, makes the task wait without
giving it the result. Use `after` when a task must follow another for
correctness but has no interest in its output.

## Skip tasks conditionally

Two mechanisms decide whether a task runs. A trigger rule looks at the status of
its dependencies. A `runIf` predicate looks at their results.

The default trigger rule is `ALL_SUCCESS`, so a task runs only if every
dependency succeeded. The others are `ALL_FAILED`, `ALL_DONE`, `ANY_SUCCESS`,
`ANY_FAILED`, and `NONE_FAILED`. `ALL_DONE` is how you build a step that runs
whether or not the upstream work succeeded, which suits cleanup and
notification tasks.

A predicate runs after the trigger rule passes. Returning false skips the task
with reason `RUN_IF_PREDICATE`.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/dag/run-if.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/dag/run-if.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/dag/run-if.java"
    ```

The order total is zero, so `issue-refund` skips. `notify` depends on it under
the default `ALL_SUCCESS` rule, so it skips too. Skipping cascades down the
graph, and a skipped task counts as neither a success nor a failure.

!!! warning "A predicate that throws aborts the DAG"

    Returning false is a skip. Throwing is not. A predicate runs during
    scheduling, so the SDK treats a throw as a defect in deterministic code
    rather than a task failure. The DAG aborts: the offending task never reaches
    a terminal state, no further tasks start, and the call fails with
    `DagPredicateError`, or `DagPredicateException` in Java. Keep predicates
    pure, and put anything that can fail inside a task body instead, where a
    throw is an ordinary failure you can inspect and retry.

## Limit concurrency

`maxConcurrency` bounds how many of a DAG's tasks run at once. The following DAG
has three independent tasks and a limit of two, so the third starts only after
one of the first two finishes.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/dag/dag-config.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/dag/dag-config.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/dag/dag-config.java"
    ```

The default is 40. Raise it when tasks are IO bound and the downstream services
tolerate the load, and lower it to protect a rate limited dependency.

The bound applies only to the DAG's own tasks. A `map` or `parallel` task inside
the DAG keeps its own concurrency setting for its internal fan out, so a DAG
limit of 2 does not stop one `map` task from processing many items at once. A
nested DAG gets its own budget, independent of its parent's.

## Handle task failures

A failing task does not fail the DAG. Failure is a terminal state for that task,
the scheduler keeps going with everything still runnable, and the container
completes with a `completionReason` of `COMPLETED_WITH_FAILURES`. That is what
makes a DAG useful for work where partial progress is worth keeping.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/dag/error-handling.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/dag/error-handling.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/dag/error-handling.java"
    ```

`charge-card` fails, so `ship-order` skips under the default trigger rule.
`record-metrics` depends on nothing and succeeds regardless. Inspecting the
failures gives you each task's name and error.

To convert failures into a thrown error instead, call `throwIfError()`. It
raises `DagExecutionError`, or `DagExecutionException` in Java, carrying the
first failure as its cause. To stop scheduling early once failures pile up, set
a completion config with a failure tolerance.

Retries are per task. A task's own config controls its retry policy, and
`defaultRetryStrategy` on the DAG supplies a policy for tasks that do not set
one.

## Nest DAGs

A task can be a DAG. Nesting groups a subgraph so it is scheduled, checkpointed,
and reported as one unit of the parent.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/dag/nested-dag.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/dag/nested-dag.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/dag/nested-dag.java"
    ```

The parent sees `inner` as a single task, and its result is the inner DAG's
`DagResult`, so a downstream task can read individual inner task results
through it. The inner DAG's own concurrency limit governs its tasks.

## Naming tasks

A task's name is its identity. The SDK derives each task's checkpoint identity
from the DAG name and the task name, which is what lets a resumed invocation
match a stored result to the task that produced it.

Names must therefore be stable across replays. Use literal names, and do not
build them from anything that can differ between invocations, such as a
timestamp, a random value, or the position of an item in a collection that might
be reordered. A name that changes between invocations looks like a new task, so
its earlier result is not found and the work runs again.

Names must be unique within one DAG. A duplicate is a registration error.

## Checkpointing and replay

Each task checkpoints its own result. When an invocation resumes, a task that
already succeeded is not run again. Its stored result is returned instead, which
is what makes an expensive or non idempotent task safe inside a DAG.

The container checkpoints one aggregate envelope beside the task checkpoints.
The envelope carries the task counts, the completion reason, and the per task
detail. When it fits under the checkpoint payload limit the SDK stores it whole,
and a resumed invocation reads every result straight back from it without
touching the task checkpoints.

When the envelope exceeds the limit the SDK drops the per task detail and keeps
the counts and the completion reason. On replay it rebuilds that detail by re
running the deterministic registration graph and reading each task's result from
its own retained checkpoint. The task bodies do not re run, so a task's side
effects still happen exactly once. The set of in flight tasks is preserved in
the envelope, so a task that had started but not finished resumes rather than
starting again.

## See also

- [Map](map.md) applies one function across a collection
- [Parallel](parallel.md) runs a fixed set of branches at once
- [Child Context](child-context.md) groups operations without declaring edges
- [Errors](../error-handling/errors.md) covers the error hierarchy
- [Retries](../error-handling/retries.md) covers retry strategies
