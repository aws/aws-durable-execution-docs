# Plugins

Create plugins for instrumenting durable execution lifecycle events. Each SDK
calls plugin hooks at invocation, operation, and user-function boundaries, so
you can integrate external tooling, such as observability platforms, without
changing your handler logic. For example, you can use a plugin to emit a metric
or trace span each time an operation runs. Ordinary hook failures are caught and
logged so instrumentation does not change the execution outcome. Invalid plugin
registrations fail configuration before the handler runs.

This page describes the factory contract in TypeScript, Python, and Java SDK
3.x. Register a factory that creates a fresh plugin for every Lambda invocation,
including each resume and each concurrent request on Lambda Managed Instances.

!!! note "Go support"

    The Go SDK has a plugin API, and it is experimental. For the reference, see
    the Go package documentation for
    [`durable.Plugin`](https://pkg.go.dev/github.com/aws/aws-durable-execution-sdk-go/durable#Plugin)
    and
    [`durable.WithPlugins`](https://pkg.go.dev/github.com/aws/aws-durable-execution-sdk-go/durable#WithPlugins).

## Define a plugin

A plugin implements any of the following lifecycle hooks, and the SDK calls
them as the execution runs. Every hook is optional, so implement only the
ones you need.

The following example plugin shows how to implement every available hook.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/plugins/complete-plugin.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/plugins/complete-plugin.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/plugins/complete-plugin.java"
    ```

The hooks fire at three boundaries: invocation, operation, and the user
function. Each hook receives an info object describing that event, including
fields like operation identity, timestamps, or error details.

### Invocation hooks

Invocation hooks run when a Lambda invocation of the execution starts or ends.
The start hook identifies whether this is the execution's first invocation or
a replay. A resumed execution receives a new plugin instance, so rebuild any
execution-wide state from the checkpointed operations in the hook metadata.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/plugins/invocation-hooks.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/plugins/invocation-hooks.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/plugins/invocation-hooks.java"
    ```

    Invocation-end hooks run in reverse registration order on the handler
    thread after operation work and checkpoints drain. The runner contains
    nonfatal hook failures. For `VirtualMachineError` or `ThreadDeath`, it runs
    the remaining end hooks, then rethrows the first fatal error.

### Operation hooks

Operation hooks run when a durable operation, such as a step or wait, starts
or reaches a terminal status.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/plugins/operation-hooks.ts"
    ```

    The `isReplay` field on `OperationInfo` indicates whether the hook is
    firing for the first time or during a replay. Use `status` to determine
    failure; an operation can fail without error details.

    Operation hooks run their synchronous work in lifecycle order. The SDK
    does not await their returned promises before continuing operations.
    Initialize state needed by later hooks before the first `await`. If
    asynchronous work must finish in order, queue it within the invocation
    and return its promise from the hook.

    Before `onInvocationEnd`, the SDK drains operation-hook promises until
    500 ms before the invocation deadline. It then awaits `onInvocationEnd`
    until 50 ms before the deadline. These limits stop waiting without
    cancelling pending work. Without a usable deadline, both waits continue
    until the work settles. `onInvocationStart` remains awaited.

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/plugins/operation-hooks.py"
    ```

    No hook runs on the checkpoint thread. `on_operation_end` normally runs
    on the thread that observes the terminal result, before returning that
    result to user code. Hooks from concurrent branches can overlap, so
    protect shared mutable state. Slow hooks still delay the thread that
    calls them.

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/plugins/operation-hooks.java"
    ```

### User-function hooks

User-function hooks run when the code you supply for an operation begins or
finishes, on that operation's thread.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/plugins/user-function-hooks.ts"
    ```

    These hooks fire around each step or wait-for-condition attempt. Child
    context functions are instrumented through `wrapChildContextFn`, a
    TypeScript-only hook described in
    [TypeScript-only hooks](#typescript-only-hooks).

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/plugins/user-function-hooks.py"
    ```

    These hooks fire around step attempts, child context functions, and
    wait-for-condition checks.

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/plugins/user-function-hooks.java"
    ```

    These hooks fire around step attempts, child context functions, and
    wait-for-condition checks.

### Checkpoint-change hook

The checkpoint-change hook runs when a checkpoint response reports that
operations changed status. It receives the operations that changed and a
snapshot of all operations.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/plugins/checkpoint-change-hook.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/plugins/checkpoint-change-hook.py"
    ```

    `on_operation_change` runs on a separate plugin change thread. It merges
    notifications that arrive while a hook is running, so one call can cover
    several checkpoint responses. It is not ordered with operation-end
    hooks from those responses.

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/plugins/checkpoint-change-hook.java"
    ```

## Use a plugin

Provide plugin factories in the durable handler configuration. The SDK calls
each factory once per invocation with the same metadata supplied to its
invocation-start hook. Keep invocation identity, spans, and operation buffers on
the returned plugin. Keep shared providers, exporters, and connection pools on
the factory, and make shared resources safe for concurrent use. Do not return a
cached plugin instance. Plugin fields are not checkpointed.

The [OpenTelemetry](opentelemetry.md) plugins and the
[Durable Execution Telemetry Exporter](telemetry/exporter.md) provide factories
that you register the same way.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/plugins/use-plugin.ts"
    ```

    A `DurableInstrumentationPluginFactory` has a synchronous
    `createPlugin(info)` method. The SDK rejects a plugin instance, bare
    function, or old provider with `pluginApiVersion` with `PluginLoadError`.
    Every invocation returns that error as a `FAILED` result before your handler
    runs. Environment-loaded modules still export `durableExecutionPluginProvider`,
    but that export must be a factory.

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/plugins/use-plugin.py"
    ```

    A `DurableInstrumentationPluginFactory` has a synchronous
    `create_plugin(info)` method. Register an instantiated factory. Plugin
    instances and bare callables raise `PluginLoadError` at handler
    initialization. Entry points in `aws_durable_execution.plugins` must
    resolve to factory objects too.

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/plugins/use-plugin.java"
    ```

    `DurableExecutionPluginFactory` is a functional interface with
    `createPlugin(InvocationInfo)`. ServiceLoader providers implement
    `DurableExecutionPluginProvider`, which extends the factory interface with
    `getName()`. `DURABLE_EXECUTION_PLUGINS` selects providers by that name.
    Rebuild provider JARs and Lambda layers against SDK 3.x.
    Use `DurableConfig.toBuilder()` to preserve resolved factories when
    copying configuration, and `getPluginFactories()` to inspect them.

## Logging from a plugin

A plugin can add custom fields to log entries so that logs emitted during the
execution carry extra context.

=== "TypeScript"

    `enrichLogContext` is called for each log entry, and the fields it returns
    are added to that entry. The enrichment applies wherever the SDK logs. See
    [Logging](logging.md) for more about the SDK logger.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/plugins/log-enrichment-hook.ts"
    ```

=== "Python"

    Install a stateless `logging.Filter` on root logger handlers once, in
    the factory constructor. Adding one in every plugin constructor would
    accumulate filters across invocations. The filter below adds a shared
    service name. For invocation-specific fields, read thread-local or
    context-local state instead of storing the active invocation on the
    shared filter. See
    [Logging](logging.md) for more about the SDK logger.

    ```python
    --8<-- "examples/python/sdk-reference/plugins/log-enrichment.py"
    ```

=== "Java"

    The `onUserFunctionStart` and `onUserFunctionEnd` hooks run on the same
    thread as the user function, so a plugin can add fields to the SLF4J MDC in
    those hooks and the logs the function emits include those fields. See
    [Logging](logging.md) for more information.

    ```java
    --8<-- "examples/java/sdk-reference/plugins/log-enrichment.java"
    ```

## TypeScript-only hooks

!!! warning "TypeScript only"

    These hooks exist only in the TypeScript SDK and will not be added to the
    Python or Java SDKs.

JavaScript has no thread-local storage to share context between separate start
and end hooks. The TypeScript SDK solves this with hooks that each receive a unit
of work as a function and call it, a common JavaScript callback pattern, so a
plugin can keep context active from the start to the end of the work.

The Python and Java SDKs do not need these hooks. Their start and end hooks run
on the same thread as the work, so a plugin can establish context in the start
hook and tear it down in the end hook.

### Wrap invocation

This hook surrounds the entire handler and every operation it runs.

```typescript
--8<-- "examples/typescript/sdk-reference/plugins/wrap-invocation.ts"
```

### Wrap child context

This hook surrounds a single child context function.

```typescript
--8<-- "examples/typescript/sdk-reference/plugins/wrap-child-context.ts"
```

### Wrap operation attempt

This hook surrounds a single step or wait-for-condition attempt, running again
for each retry.

```typescript
--8<-- "examples/typescript/sdk-reference/plugins/wrap-operation-attempt.ts"
```
