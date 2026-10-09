# Code organization

Organize your code so that your workflows are composable and testable.

## Separate orchestration from business logic

Business logic is the work a step performs. Orchestration is when and in what order
steps run.

Put your logic in its own testable functions that the durable operation calls, rather
than inline in the operation. Keep `DurableContext` out of your domain logic.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/code-organization/separate-logic.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/code-organization/separate-logic.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/code-organization/separate-logic.java"
    ```

=== "Go"

    Domain functions take plain arguments and return `(T, error)`. Only the step
    body closure receives a `durable.StepContext`, so the domain logic stays free
    of SDK types.

    ```go
    --8<-- "examples/go/patterns/code-organization/separate-logic.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/patterns/code-organization/separate-logic.cs"
    ```

!!! tip

    Simplify your unit testing by not referencing `DurableContext` in your domain logic
    functions. Cover orchestration separately with the
    [durable function testing framework](../../testing/index.md).

## Group operations with child contexts

A child context is a named scope that groups operations in the execution history. Inside
a child context you can call any durable operation, including nested child contexts.

Use a child context when a block of work is one logical unit that is composed of several
operations.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/code-organization/child-context.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/code-organization/child-context.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/code-organization/child-context.java"
    ```

=== "Go"

    The body receives its own child `durable.Context`. Call every durable operation on
    that child. An operation on the outer context from inside the body fails with
    `durable.ErrWrongContext` and records nothing. When you run
    [durablelint](../../sdk-reference/languages/go/index.md#static-analysis-with-durablelint),
    the SDK's static analysis tool, its `durablechildctx` rule reports this call.

    ```go
    --8<-- "examples/go/patterns/code-organization/child-context.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/patterns/code-organization/child-context.cs"
    ```

## Group related configuration

When several steps share the same retry strategy, timeout, or serdes, define the
configuration once and reuse it. Use the name of a configuration object to make its
intent clear.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/code-organization/group-config.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/code-organization/group-config.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/code-organization/group-config.java"
    ```

=== "Go"

    `Step` takes functional options. Collect the options in a `[]durable.StepOption`
    once and pass it to each step with `opts...`.

    ```go
    --8<-- "examples/go/patterns/code-organization/group-config.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/patterns/code-organization/group-config.cs"
    ```

## Run independent work concurrently

Use `parallel` for a fixed number of named branches and `map` to iterate over a
variable-length list. Both checkpoint durable progress and survive Lambda timeouts or
sandbox crashes, unlike language-specific constructs such as `Promise.all`,
`asyncio.gather`, or `CompletableFuture`.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/code-organization/parallelism.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/patterns/code-organization/parallelism.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/patterns/code-organization/parallelism.java"
    ```

=== "Go"

    Use `durable.Parallel` for a fixed set of named branches and `durable.Map`
    for a variable-length list. The map function also receives the item index.

    ```go
    --8<-- "examples/go/patterns/code-organization/parallelism.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/patterns/code-organization/parallelism.cs"
    ```

See the [parallel](../../sdk-reference/operations/parallel.md) and
[map](../../sdk-reference/operations/map.md) references for completion policies,
concurrency limits, and per-item configuration.

## See also

- [Step design](step-design.md)
- [Child context operation](../../sdk-reference/operations/child-context.md)
- [Parallel operation](../../sdk-reference/operations/parallel.md)
- [Map operation](../../sdk-reference/operations/map.md)
