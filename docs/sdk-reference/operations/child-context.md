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

    **Raises:** `CallableRuntimeError` wrapping the original exception if the child context
    function raises.

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
    - `item_serdes` (optional) Custom `SerDes` for individual items within the child
        context.
    - `sub_type` (optional) An internal subtype identifier. Used by `map` and `parallel`
        internally; not needed for direct use.
    - `summary_generator` (optional) A function that generates a compact summary when the
        result exceeds the checkpoint size limit. Used internally by `map` and `parallel`.

=== "Java"

    ```java
    --8<-- "examples/java/operations/child-contexts/child-config-signature.java"
    ```

    **Parameters:**

    - `serDes` (optional) Custom `SerDes` for the child context result. See
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
2. To run durable operations concurrently, enclose each set of operations in its own
    child context.
3. Start each child context serially. You do not have to wait for the previous child
    context to complete before starting the next.
4. Inside the child function you must use the child context argument, not the parent
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

## See also

- [Steps](step.md) Run a single function with automatic checkpointing
- [Parallel operations](parallel.md) Execute operations concurrently
- [Map operations](map.md) Run operation for each item in a collection
