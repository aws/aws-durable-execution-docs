# Key Concepts

## Durable execution

A durable execution represents the complete lifecycle of a Lambda durable function. The SDK uses a checkpoint and replay mechanism to track progress, suspend execution, and recover from failures. A single execution may span multiple Lambda invocations.

## Durable functions

A durable function is a Lambda function decorated with `@durable_execution` that can be checkpointed and resumed. The function receives a `DurableContext` that provides methods for durable operations.

## Operations

Operations are units of work in a durable execution. Each operation type serves a specific purpose:

- **Steps** - Execute code and checkpoint the result with retry support
- **Waits** - Pause execution for a specified duration without blocking Lambda
- **Callbacks** - Wait for external systems to respond with results
- **Invoke** - Call other durable functions to compose complex workflows
- **Child contexts** - Isolate nested workflows for better organization
- **Parallel** - Execute multiple operations concurrently with completion criteria
- **Map** - Process collections in parallel with batching and failure tolerance

## Checkpoints

Checkpoints are saved states of execution that allow resumption. When your function calls `context.step()` or other operations, the SDK creates a checkpoint and sends it to AWS. If Lambda recycles your environment or your function waits for an external event, execution can resume from the last checkpoint.

## Replay

When your function resumes, completed operations don't re-execute. Instead, they return their checkpointed results instantly. This means your function code runs multiple times, but side effects only happen once per operation.

Because your code runs again on replay, it must be **deterministic** — avoid random values, timestamps, or external API calls outside of steps, as these can produce different values on replay.

## How replay works in practice

Let's trace through a simple workflow:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/getting-started/execution-model.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/getting-started/execution-model.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/getting-started/execution-model.java"
    ```

**First invocation (t=0s):**

1. Lambda invokes your function
2. `fetch_data` executes and calls an external API
3. Result is checkpointed to AWS
4. `context.wait(Duration.from_seconds(30))` is reached
5. Function returns, Lambda can recycle the environment

**Second invocation (t=30s):**

1. Lambda invokes your function again
2. Function code runs from the beginning
3. `fetch_data` returns the checkpointed result instantly (no API call)
4. `context.wait()` is already complete, execution continues
5. `process_data` executes for the first time

## The two SDKs

### Execution SDK (`aws-durable-execution-sdk-python`)

Runs in your Lambda functions. Provides `DurableContext`, operations, decorators, and serialization. Install in your Lambda deployment package.

```console
pip install aws-durable-execution-sdk-python
```

### Testing SDK (`aws-durable-execution-sdk-python-testing`)

A separate SDK for testing your durable functions locally without AWS. Provides `DurableFunctionTestRunner`, pytest integration, and result inspection. Install in your development environment only.

```console
pip install aws-durable-execution-sdk-python-testing
```

## Decorators

The SDK provides decorators to mark functions as durable:

- `@durable_execution` - Marks your Lambda handler as a durable function
- `@durable_step` - Marks a function that can be used with `context.step()`
- `@durable_with_child_context` - Marks a function that receives a child context
