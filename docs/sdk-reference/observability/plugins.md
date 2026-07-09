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

    This hook is a work in progress and is not yet available in the Python SDK.

=== "Java"

    This hook is a work in progress and is not yet available in the Java SDK.

## Use a plugin

Provide plugins in the durable handler configuration. The SDK calls each plugin
in the order you provide.

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
