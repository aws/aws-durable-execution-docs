# Child Context

## Isolate execution scope

A child context creates an isolated execution scope within a durable function for
grouping operations. It has its own operation namespace and its own set of checkpoints.
Unlike a [step](step.md), which wraps a single function call, a child context can
contain multiple durable operations, such as steps, waits, and other operations.

When the child context completes, the SDK checkpoints the result as a single unit in the
parent context. On replay, the SDK returns the checkpointed result without re-running
the operations inside the child context. If the result exceeds the checkpoint size limit
the child context will reconstruct the result in memory from the checkpointed results of
its child operations without rerunning the child operations.

Use child contexts to group multiple durable operations. This is useful to organize
complex workflows, implement sub-workflows and maintain determinism when running
multiple child contexts concurrently.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/child-contexts/basic-child-context.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/child-contexts/basic-child-context.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/child-contexts/basic-child-context.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/child-contexts/basic-child-context.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/child-contexts/basic-child-context.cs"
    ```

## Method signature

### Run in ChildContext

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/child-contexts/run-in-child-context-signature.ts"
    ```

    **Parameters:**

    - `name` (optional) A name for the child context. Pass `undefined` to omit.
    - `fn` An async function that receives a `DurableContext` and returns `Promise<T>`.
    - `config` (optional) A `ChildConfig<T>` object.

    **Returns:** `DurablePromise<T>`. Use `await` to get the result.

    **Throws:** `ChildContextError` wrapping the original error if the child context
    function throws.

=== "Python"

    ```python
    --8<-- "examples/python/operations/child-contexts/run-in-child-context-signature.py"
    ```

    **Parameters:**

    - `func` A callable that receives a `DurableContext` and returns `T`. Use the
        `@durable_with_child_context` decorator to create one.
    - `name` (optional) A name for the child context. Defaults to the function's name when
        using `@durable_with_child_context`.
    - `config` (optional) A `ChildConfig` object.

    **Returns:** `T`, the return value of `func`.

    **Raises:** `ChildContextError` (a `DurableOperationError` subclass) wrapping the
    original exception if the child context function raises.

=== "Java"

    ```java
    --8<-- "examples/java/operations/child-contexts/run-in-child-context-signature.java"
    ```

    The name is always required. Pass `null` to omit it.

    **Parameters:**

    - `name` (required) A name for the child context. Pass `null` to omit.
    - `resultType` The `Class<T>` or `TypeToken<T>` for deserialization.
    - `func` A `Function<DurableContext, T>` to execute.
    - `config` (optional) A `RunInChildContextConfig` object.

    **Returns:** `T` (sync) or `DurableFuture<T>` (async via `runInChildContextAsync()`).

    **Throws:** The original exception re-thrown after deserialization if possible,
    otherwise `ChildContextFailedException`.

=== "Go"

    ```go
    --8<-- "examples/go/operations/child-contexts/run-in-child-context-signature.go"
    ```

    The three child-context functions are the blocking `RunInChildContext`, the async
    `RunInChildContextAsync`, and `Go`, a shorthand for `RunInChildContextAsync`. `Go`
    provides replay-safe concurrency.

    **Parameters:**

    - `ctx` (required) The durable context, always the first argument.
    - `name` (required) A name for the child context. Pass `""` to omit it.
    - `fn` (required) `func(durable.Context) (O, error)`. It receives the child
        context and must run its operations on that context.
    - `opts` (optional) Variadic `ChildOption` values. See [Child Config](#child-config).

    **Returns:** `(O, error)` for `RunInChildContext`, or `*Future[O]` for
    `RunInChildContextAsync` and `Go`. Read an async result with `future.Result(ctx)`.
    The SDK does not store a result whose serialized form exceeds 256KB. On replay
    it runs `fn` again to rebuild that result, and the operations inside return
    their checkpointed results.

    **Errors:** `*ChildContextError` wrapping the failure that escaped the body, or
    the error a `WithChildErrorMapper` mapper derives from it. Its `ErrorType` names
    the escaping error, for example `"StepError"`. `errors.As` against an inner SDK
    error type such as `*StepError` succeeds. Your own error types do not match, so
    match them on `ErrorType`.

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/child-contexts/run-in-child-context-signature.cs"
    ```

    **Parameters:**

    - `func` A function that receives an `IDurableContext` and a `CancellationToken`
        and returns a `Task<T>` (or `Task` for the no-value overload).
    - `name` (optional) A name for the child context. Omit it to infer one from the
        call site.
    - `config` (optional) A `ChildContextConfig` object.
    - `cancellationToken` (optional) A token linked with the SDK's workflow-shutdown
        signal, forwarded to `func`.

    **Returns:** `Task<T>`, or `Task` for the no-value overload. Use `await` to get the
    result.

    **Throws:** `ChildContextException` if the child context function throws. Supply
    `ChildContextConfig.ErrorMapping` to remap it into a domain-specific exception.

### Child Config

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/child-contexts/child-config-signature.ts"
    ```

    **Parameters:**

    - `serdes` (optional) Custom `Serdes<T>` for the child context result. See
        [Serialization](../state/serialization.md).
    - `subType` (optional) An internal subtype identifier. Used by `map` and `parallel`
        internally; not needed for direct use.
    - `summaryGenerator` (optional) A function that generates a compact summary when the
        result exceeds the checkpoint size limit. Used internally by `map` and `parallel`.
    - `errorMapper` (optional) A function that maps child context errors to custom error
        types.
    - `virtualContext` (optional) If `true`, skips checkpointing and uses the parent's ID
        for child operations.

=== "Python"

    ```python
    --8<-- "examples/python/operations/child-contexts/child-config-signature.py"
    ```

    **Parameters:**

    - `serdes` (optional) Custom `SerDes` for the child context result. See
        [Serialization](../state/serialization.md).
    - `sub_type` (optional) An internal subtype identifier. Used by `map` and `parallel`
        internally; not needed for direct use.
    - `summary_generator` (optional) A function that generates a compact summary when the
        result exceeds the checkpoint size limit. Used internally by `map` and `parallel`.
    - `is_virtual` (optional) When `True`, skips checkpointing for the child context and
        propagates the parent's ID to its operations. Used internally by `map` and
        `parallel` for flat nesting; not needed for direct use.

=== "Java"

    ```java
    --8<-- "examples/java/operations/child-contexts/child-config-signature.java"
    ```

    **Parameters:**

    - `serDes` (optional) Custom `SerDes` for the child context result. See
        [Serialization](../state/serialization.md).

=== "Go"

    ```go
    --8<-- "examples/go/operations/child-contexts/child-config-signature.go"
    ```

    Pass variadic `ChildOption` values.

    **Parameters:**

    - `WithChildSerdes` Custom `Serdes` for the child context result.
    - `WithChildErrorMapper` Maps the child's `*ChildContextError` to another error
        before the operation returns. The mapper must be deterministic.
    - `WithChildSummary` A summary function the SDK calls only when the result
        exceeds the 256KB checkpoint limit. The SDK stores the summary as the
        checkpoint payload and never reads it back.
    - `WithChildSubType` An operation subtype for observability. It takes 1 to 32
        characters from `A-Z`, `a-z`, `0-9`, `-`, and `_`, and the SDK's own
        subtypes are rejected. The subtype is part of the operation's identity on
        replay, so it must not change between invocations.
    - `WithChildVirtual` Makes the child virtual. The SDK records no operation for the
        wrapper, and the operations inside it record the nearest checkpointed ancestor
        as their parent. Replay runs the body again on every invocation that reaches it.
        `Map` and `Parallel` use the same mechanism for `NestingFlat` items.

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/child-contexts/child-config-signature.cs"
    ```

    **Parameters:**

    - `SubType` (optional) An operation sub-type label for observability. Used
        internally by `map` and `parallel`; not needed for direct use.
    - `ErrorMapping` (optional) A function that maps exceptions thrown by the child
        context (surfaced as `ChildContextException`) into a domain-specific exception.

    The child context result is serialized with the `ILambdaSerializer` registered on
    `ILambdaContext.Serializer`; there is no per-context serializer. See
    [Serialization](../state/serialization.md).

## The child context's function

The child context function receives a `DurableContext` as its argument. This is the
child context.

Code inside the child context can can call any durable operation on that child context,
such as steps, waits, callbacks and further nested child contexts.

Do not use the parent context inside the child context via closure because it will
corrupt execution state and cause non-deterministic behaviour.

=== "TypeScript"

    Pass any async function directly. The function receives a `DurableContext` and must
    return a `Promise<T>`.

    ```typescript
    --8<-- "examples/typescript/operations/child-contexts/context-function.ts"
    ```

=== "Python"

    Use the `@durable_with_child_context` decorator. It wraps your function so it can be
    called with arguments and passed to `context.run_in_child_context()`. The decorator
    automatically uses the function's name as the child context name.

    ```python
    --8<-- "examples/python/operations/child-contexts/context-function.py"
    ```

=== "Java"

    Pass a lambda or method reference directly. The function receives a `DurableContext` and
    returns `T`.

    ```java
    --8<-- "examples/java/operations/child-contexts/context-function.java"
    ```

=== "Go"

    Pass the function directly. It receives its own child `durable.Context` and must run
    operations on that context. Using the captured parent context returns
    `ErrWrongContext`. When you run
    [durablelint](../languages/go/index.md#static-analysis-with-durablelint), the SDK's
    static analysis tool, its `durablechildctx` rule reports that call.

    ```go
    --8<-- "examples/go/operations/child-contexts/context-function.go"
    ```

=== "C#"

    Pass an `async (child, ct) => ...` lambda directly. The function receives its own
    `IDurableContext` and must return a `Task<T>`.

    ```csharp
    --8<-- "examples/csharp/operations/child-contexts/context-function.cs"
    ```

### Pass arguments to the child context

=== "TypeScript"

    Capture arguments in a closure:

    ```typescript
    --8<-- "examples/typescript/operations/child-contexts/pass-arguments.ts"
    ```

=== "Python"

    Pass arguments when calling the decorated function:

    ```python
    --8<-- "examples/python/operations/child-contexts/pass-arguments.py"
    ```

=== "Java"

    Capture arguments in a lambda:

    ```java
    --8<-- "examples/java/operations/child-contexts/pass-arguments.java"
    ```

=== "Go"

    Capture read-only arguments in the closure. Do not write back to captured variables.
    The result must flow through the return value, which the SDK checkpoints. When you
    run [durablelint](../languages/go/index.md#static-analysis-with-durablelint), its
    `durableclosure` rule reports writes to captured variables.

    ```go
    --8<-- "examples/go/operations/child-contexts/pass-arguments.go"
    ```

=== "C#"

    Capture arguments in the closure:

    ```csharp
    --8<-- "examples/csharp/operations/child-contexts/pass-arguments.cs"
    ```

## Naming child contexts

Name child contexts to make them easier to identify in logs and tests.

=== "TypeScript"

    The name is the first argument. Pass `undefined` to omit it.

    ```typescript
    --8<-- "examples/typescript/operations/child-contexts/named-child-context.ts"
    ```

=== "Python"

    The `@durable_with_child_context` decorator uses the function's name automatically.
    Override it with the `name` keyword argument to `run_in_child_context()`.

    ```python
    --8<-- "examples/python/operations/child-contexts/named-child-context.py"
    ```

=== "Java"

    The name is always the first argument. Pass `null` to omit it.

    ```java
    --8<-- "examples/java/operations/child-contexts/named-child-context.java"
    ```

=== "Go"

    The name is the second argument, after the context. Pass `""` to omit it.

    ```go
    --8<-- "examples/go/operations/child-contexts/named-child-context.go"
    ```

=== "C#"

    The name is the optional `name` argument. Omit it to infer one from the call site.

    ```csharp
    --8<-- "examples/csharp/operations/child-contexts/named-child-context.cs"
    ```

## Concurrency

!!! note

    [Parallel](parallel.md) and [map](map.md) operations manage the complexity of
    concurrency for you with concurrency control and completion policies, so you don't have
    to code it yourself using child contexts.

It is not deterministic to run durable operations concurrently without wrapping each
concurrent branch in its own child context. The reason for this is that to ensure
deterministic replay, each durable operation gets an incrementing ID from a sequential
counter. If two operations start concurrently, the counter increments in whatever order
they happen to execute. On replay that order could differ, which could result in
unexpected behaviour. For example, an operation could receive a different operation ID
on replay and then retrieve a different operation's result.

A child context has its own isolated operation ID counter, so internal operations do not
affect the parent's checkpoint state. You must still start each child context
sequentially in the parent.

### Concurrency rules

1. All durable operations inside a context must start sequentially.
1. To run durable operations concurrently, enclose each set of operations in its own
    child context.
1. Start each child context serially. You do not have to wait for the previous child
    context to complete before starting the next.
1. Inside the child function you must use the child context argument, not the parent
    context.

=== "TypeScript"

    Don't `await` each child context immediately. Start them all, then await together.

    ```typescript
    --8<-- "examples/typescript/operations/child-contexts/concurrent-child-contexts.ts"
    ```

=== "Python"

    Use [parallel](parallel.md) or [map](map.md) to run code concurrently.

    !!! warning

        In Python, `ThreadPoolExecutor.submit` and `Thread.start` do not guarantee the order in
        which the interpreter actually runs the handler functions.

=== "Java"

    Use `runInChildContextAsync()` to get a `DurableFuture<T>`, then block with
    `DurableFuture.allOf()`.

    ```java
    --8<-- "examples/java/operations/child-contexts/concurrent-child-contexts.java"
    ```

=== "Go"

    Use `durable.Go` (or `RunInChildContextAsync`) for each branch, then await each
    future. Do not use a bare `go` statement. A durable operation on a context from
    another goroutine returns `ErrWrongGoroutine`. When you run
    [durablelint](../languages/go/index.md#static-analysis-with-durablelint), its
    `durablegoroutine` rule reports a durable operation inside a `go` statement.

    ```go
    --8<-- "examples/go/operations/child-contexts/concurrent-child-contexts.go"
    ```

=== "C#"

    Don't `await` each child context immediately. Start them all, then await together.

    ```csharp
    --8<-- "examples/csharp/operations/child-contexts/concurrent-child-contexts.cs"
    ```

## Testing

The testing SDK records child context operations as `CONTEXT` type operations. Inspect
them to verify the child context ran and produced the expected result.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/child-contexts/test-child-context.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/child-contexts/test-child-context.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/child-contexts/test-child-context.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/operations/child-contexts/test-child-context.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/operations/child-contexts/test-child-context.cs"
    ```

## See also

- [Steps](step.md) Run a single function with automatic checkpointing
- [Parallel operations](parallel.md) Execute operations concurrently
- [Map operations](map.md) Run operation for each item in a collection

!!! info "Checkpoint consumption"

    Durable operations consume checkpoints. To understand how this operation affects
    your checkpoint usage, see
    [Checkpoint consumption](https://docs.aws.amazon.com/lambda/latest/dg/durable-execution-sdk.html#durable-operations-checkpoint-consumption).
