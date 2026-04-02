# Key Concepts

## Durable execution

A durable execution is the complete lifecycle of an AWS Lambda durable function. It uses
a checkpoint and replay mechanism to track progress, suspend execution, and recover from
failures. When functions resume after suspension or interruptions, previously completed
checkpoints replay and the function continues execution.

The execution lifecycle could include multiple invocations of the Lambda function to
complete, particularly after suspensions or failure recovery. With these replays the
execution can run for extended periods (up to one year) while maintaining reliable
progress despite interruptions.

### Timeouts

The
[execution timeout](https://docs.aws.amazon.com/lambda/latest/api/API_DurableConfig.html#lambda-Type-DurableConfig-ExecutionTimeout)
and Lambda function
[Timeout](https://docs.aws.amazon.com/lambda/latest/api/API_CreateFunction.html#lambda-CreateFunction-request-Timeout)
are different settings. The Lambda function timeout controls how long each individual
invocation can run (maximum 15 minutes). The execution timeout controls the total
elapsed time for the entire durable execution (maximum 1 year).

## Durable functions

A durable function is a Lambda function configured with the
[`DurableConfig`](https://docs.aws.amazon.com/lambda/latest/dg/durable-configuration.html)
object at creation time. Lambda will then apply the checkpoint and replay mechanism to
the function's execution to make it durable at invocation time.

## DurableContext

`DurableContext` is the context object your durable function receives instead of the
standard Lambda `Context`. It exposes all durable operations and provides methods for
creating checkpoints, managing execution flow, and coordinating with external systems.

Your durable function receives a `DurableContext` instead of the default Lambda context:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/getting-started/durable-context.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/getting-started/durable-context.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/getting-started/durable-context.java"
    ```

## Operations

Operations are units of work in a durable execution. Each operation type serves a
specific purpose:

- [Steps](../sdk-reference/operations/step.md) Execute business logic with automatic
    checkpointing and configurable retry
- [Waits](../sdk-reference/operations/wait.md) Suspend execution for a duration without
    consuming compute resources
- [Callbacks](../sdk-reference/operations/callback.md) Suspend execution and wait for an
    external system to submit a result
- [Invoke](../sdk-reference/operations/invoke.md) Invoke another Lambda function and
    checkpoint the result
- [Parallel](../sdk-reference/operations/parallel.md) Execute multiple independent
    operations concurrently
- [Map](../sdk-reference/operations/map.md) Execute an operation on each item in an
    array concurrently with optional concurrency control
- [Child context](../sdk-reference/operations/child-context.md) Group operations into an
    isolated context for sub-workflow organization and concurrent determinism
- [Wait for condition](../sdk-reference/operations/wait-for-condition.md) Poll for a
    condition with automatic checkpointing between attempts

## Checkpoints

A checkpoint is a saved record of a completed durable operation: its type, name, inputs,
result, and timestamp. The SDK creates checkpoints automatically as your function
executes operations. Together, the checkpoints form a log that Lambda uses to resume
execution after a suspension or interruption.

When your code calls a durable operation, the SDK follows this sequence:

1. **Check for an existing checkpoint** if this operation already completed in a
    previous invocation, the SDK returns the stored result without re-executing
2. **Execute the operation** if no checkpoint exists, the SDK runs the operation code
3. **Serialize the result** the SDK serializes the result for storage
4. **Persist the checkpoint** the SDK calls the Lambda checkpoint API to durably store
    the result before continuing
5. **Return the result** execution continues to the next operation

Once the SDK persists a checkpoint, that operation's result is safe. If your function is
interrupted at any point, the SDK can replay up to the last persisted checkpoint on the
next invocation.

## Replay

Lambda keeps a running log of all durable operations as your function executes. When
your function needs to pause or encounters an interruption, Lambda saves this checkpoint
log and stops the execution. When it's time to resume, Lambda invokes your function
again from the beginning and replays the checkpoint log:

1. **Load checkpoint log** the SDK retrieves the checkpoint log for the execution from
    Lambda
2. **Run from beginning** your handler runs from the start, not from where it paused
3. **Skip completed operations** as your code calls durable operations, the SDK checks
    each against the checkpoint log and returns stored results without re-executing the
    operation code
4. **Resume at interruption point** when the SDK reaches an operation without a
    checkpoint, it executes normally and creates new checkpoints from that point
    forward

The SDK enforces determinism by validating that operation names and types match the
checkpoint log during replay. Your orchestration code must make the same sequence of
durable operation calls on every invocation.

## Determinism

Because your code runs again on replay, it must be **deterministic**. Deterministic
means that the code always produces the same results given the same inputs. During
replay, your function runs from the beginning and must follow the same execution path as
the original run. Given the same inputs and checkpoint log, your function must make the
same sequence of durable operation calls. Avoid operations with side effects outside of
steps, as these can produce different values during replay and cause non-deterministic
behavior.

These are some examples of non-deterministic code:

- Random number generation and UUIDs
- Current time or timestamps
- External API calls and database queries
- File system operations

Wrap such non-deterministic code in [steps](../sdk-reference/operations/step.md).

### Rules for deterministic durable operations

1. All durable operations in a context must start sequentially.
2. To run durable operations concurrently, wrap each set of operations in its own child
    context and then run the child contexts concurrently.
3. Only use the child `DurableContext` in the child context scope. Do not use any
    parent's context in a child context scope.

## Replay Walkthrough

Let's trace through a simple workflow:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/getting-started/execution-model.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/getting-started/execution-model.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/getting-started/execution-model.java"
    ```

**First invocation (t=0s):**

1. You start a durable execution by invoking a durable function
2. The durable functions service invokes your durable function handler
3. The fetch step runs and calls an external API
4. The SDK checkpoints the result of the fetch step
5. Execution reaches `context.wait()` and the SDK checkpoints the wait operation
6. The SDK terminates the current Lambda invocation, but the durable execution is still
    active

**Second invocation (t=30s):**

1. The durable functions service invokes your function again
2. The function runs from the ginning
3. The fetch step returns its checkpointed result instantly, it does not re-execute the
    API call
4. The wait has already elapsed, so execution continues
5. The process step runs for the first time
6. The SDK checkpoints the result of the process step
7. The function returns naturally and the invocation ends
8. The durable execution ends
