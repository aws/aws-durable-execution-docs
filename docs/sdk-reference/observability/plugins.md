# Plugins

Create plugins for instrumenting durable execution lifecycle events. Each SDK
calls plugin hooks at invocation, operation, and user-function boundaries, so
you can integrate external tooling, such as observability platforms, without
changing your handler logic. For example, you can use a plugin to emit a metric
or trace span each time an operation runs. Errors thrown by a plugin are caught
and logged, and never affect the execution outcome.

!!! note "Experimental feature"

    The plugin interface is currently experimental and may change in a
    future release. Feedback is welcome, please share your input through our
    [GitHub Discussion](https://github.com/aws/aws-durable-execution-docs/discussions/206).

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

=== "Rust"

    A plugin implements `durable::plugin::Plugin`, and a `PluginFactory` returns zero
    or one plugin for each invocation. Every hook has a default implementation, so
    implement only the hooks you need. The operation, attempt, change, and
    user-function hooks are synchronous and return `HookWork`, which is `Ok(None)` or a
    future that does further work. The invocation hooks are asynchronous and return a
    `HookFuture`. An `Err` or a panic from a hook is logged and
    changes nothing else. A binary built with `panic = "abort"` aborts at the panic
    instead.

    ```rust
    --8<-- "examples/rust/sdk-reference/plugins/complete-plugin.rs"
    ```

The hooks fire at three boundaries: invocation, operation, and the user
function. Each hook receives an info object describing that event, including
fields like operation identity, timestamps, or error details.

### Invocation hooks

Invocation hooks run when a Lambda invocation of the execution starts or ends.
The SDK passes an `isFirstInvocation` flag to the start hook, which indicates
whether this is the execution's first invocation or a replay.

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

=== "Rust"

    ```rust
    --8<-- "examples/rust/sdk-reference/plugins/invocation-hooks.rs"
    ```

    The SDK awaits every plugin's `on_invocation_start` before the handler runs. It
    awaits `on_invocation_end` once per invocation, after every checkpoint of the
    invocation is written. `info.status()` is `Succeeded`, `Failed`, `Pending`, or
    `Retrying`, and `wire_str()` gives `SUCCEEDED`, `FAILED`, `PENDING`, or
    `RETRYING`.

### Operation hooks

Operation hooks run when a durable operation, such as a step or wait, starts
or reaches a terminal status.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/plugins/operation-hooks.ts"
    ```

    The `isReplay` field on `OperationInfo` indicates whether the hook is
    firing for the first time or during a replay.

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/plugins/operation-hooks.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/plugins/operation-hooks.java"
    ```

=== "Rust"

    ```rust
    --8<-- "examples/rust/sdk-reference/plugins/operation-hooks.rs"
    ```

    `info.is_replay()` indicates whether the hook is firing for the first time or
    during a replay. A step replayed from a finished record fires no operation hook.
    An operation whose `.spawn()` handle was dropped still fires its end hook if it
    settles before its scope closes or the invocation ends. Returning from the handler
    ends the invocation. An operation the SDK stops fires no end hook. These hooks run
    synchronously and must not block. Put async work in the future they return.

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
    [Wrap hooks](#wrap-hooks).

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

=== "Rust"

    ```rust
    --8<-- "examples/rust/sdk-reference/plugins/user-function-hooks.rs"
    ```

    These hooks fire around step attempts, wait-for-condition checks, child context
    functions, map items and their container, parallel branches and their container,
    the body of a `wait_for_callback`, and the join of each combinator. `outcome` is
    `Ok(())` or the `WireError` that the SDK records, never the value. Start hooks run
    in registration order, and end hooks in reverse order. A user function that panics
    or that the SDK stops before it returns gets no end hook. The hooks run on the task
    that polls the user function.

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

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/plugins/checkpoint-change-hook.java"
    ```

=== "Rust"

    ```rust
    --8<-- "examples/rust/sdk-reference/plugins/checkpoint-change-hook.rs"
    ```

    `info.updated_operations()` is a slice of `OperationInfo`, and
    `info.operations()` maps every operation ID, `EXECUTION` included, to its
    `OperationInfo`.

## Use a plugin

Provide plugins in the durable handler configuration. The SDK calls each plugin
in the order you provide. The [OpenTelemetry](opentelemetry.md) plugins and the
[Durable Execution Telemetry Exporter](telemetry/exporter.md) are ready-made
plugins that you register the same way.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/plugins/use-plugin.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/plugins/use-plugin.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/plugins/use-plugin.java"
    ```

=== "Rust"

    Register each factory with `durable::OptionsBuilder::plugin`, and pass the options
    to `durable::run_with_options`. `ExamplePluginFactory` is the factory from
    [Define a plugin](#define-a-plugin). `build()` returns a
    `durable::OptionsValidationError` when two factories return the same non-empty
    `exclusive_group()`.

    ```rust
    --8<-- "examples/rust/sdk-reference/plugins/use-plugin.rs"
    ```

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

    A plugin can enrich logs by installing a standard `logging.Filter` on the
    root logger, usually in its constructor. The filter runs for every log
    record on the emitting thread, so it can add fields that reflect the
    plugin's current state. See
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

## Wrap hooks

!!! note "SDK support"

    The Python and Java SDKs do not have these hooks.

JavaScript has no thread-local storage to share context between separate start
and end hooks. The TypeScript SDK solves this with hooks that each receive a unit
of work as a function and call it, a common JavaScript callback pattern, so a
plugin can keep context active from the start to the end of the work.

The Python and Java SDKs do not need these hooks. Their start and end hooks run
on the same thread as the work, so a plugin can establish context in the start
hook and tear it down in the end hook.

### Wrap invocation

This hook surrounds the entire handler and every operation it runs.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/plugins/wrap-invocation.ts"
    ```

=== "Rust"

    `wrap_invocation` runs around each poll of the invocation, so keep it cheap and do
    not block in it. Call `next()` once to run the rest of the invocation. A wrap that
    calls `next()` inside its context, for example with a `tracing` span's `in_scope`,
    makes that context current in the handler and in every operation of the invocation.

    ```rust
    --8<-- "examples/rust/sdk-reference/plugins/wrap-invocation.rs"
    ```

### Wrap child context

This hook surrounds a single child context function.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/plugins/wrap-child-context.ts"
    ```

=== "Rust"

    `wrap_user_function` runs around each poll of every user function, so keep it cheap
    and do not block in it. A child context function has the kind
    `durable::OperationKind::Context`.

    ```rust
    --8<-- "examples/rust/sdk-reference/plugins/wrap-child-context.rs"
    ```

### Wrap operation attempt

This hook surrounds a single step or wait-for-condition attempt, running again
for each retry.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/plugins/wrap-operation-attempt.ts"
    ```

=== "Rust"

    `wrap_user_function` runs around each poll of every user function, so keep it cheap
    and do not block in it. A step attempt or a wait-for-condition check has the kind
    `durable::OperationKind::Step` and an attempt number.

    ```rust
    --8<-- "examples/rust/sdk-reference/plugins/wrap-operation-attempt.rs"
    ```
