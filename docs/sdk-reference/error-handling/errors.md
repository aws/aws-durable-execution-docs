# Error handling

## Retry behavior

### Errors inside a step

When your code throws an exception from inside a step, the SDK retries the step
according to its [retry strategy](retries.md). The SDK checkpoints each retry attempt,
ends the Lambda invocation and then the backend re-invokes the Lambda function at the
required time. This means you do not consume compute time while waiting between retry
attempts.

### Errors outside a step

When your code throws an exception from outside a step, the SDK marks the execution as
`FAILED` and returns the error immediately. The SDK does not retry it. Wrap error-prone
code in a [step](../operations/step.md) for automatic retry.

## When retries exhaust

When a step exhausts all retry attempts, the SDK checkpoints the final error and throws
it to your handler. You can catch it there and handle it as required.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/basic-error-handling.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/basic-error-handling.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/basic-error-handling.java"
    ```

## Replay throws the same error

When the SDK replays a completed operation from its checkpoint, it returns the
checkpointed result without re-running the step body. If the checkpointed result was an
error, the SDK re-throws that same error at the same point in your code. Your handler
sees the same exception it did on the original execution.

## Exception types

Each SDK wraps step failures in its own exception type. The type carries the original
error and the operation details.

=== "TypeScript"

    ```mermaid
    graph TD
      DOE[DurableOperationError]
      DOE --> StepError
      DOE --> CallbackError
      DOE --> CallbackTimeoutError
      DOE --> CallbackSubmitterError
      DOE --> InvokeError
      DOE --> ChildContextError
      DOE --> WaitForConditionError
      SIE[StepInterruptedError]
    ```

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/exception-hierarchy.ts"
    ```

    `DurableOperationError` is the base class for all operation-level failures. Each
    subclass corresponds to a specific operation type. The `cause` property holds the
    original error your code threw.

    `StepInterruptedError` is not a `DurableOperationError`. The SDK throws it when an
    at-most-once step started but Lambda was interrupted before the SDK checkpointed the
    result. See [Step interrupted](#step-interrupted) below.

=== "Python"

    ```mermaid
    graph TD
      DEE[DurableExecutionsError]
      DEE --> UnrecoverableError
      DEE --> ValidationError
      DEE --> SerDesError
      DEE --> UserlandError
      UnrecoverableError --> ExecutionError
      UnrecoverableError --> InvocationError
      ExecutionError --> CallbackError
      InvocationError --> StepInterruptedError
      UserlandError --> CallableRuntimeError
    ```

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/exception-hierarchy.py"
    ```

    `DurableExecutionsError` is the base class for all SDK exceptions.

    `CallableRuntimeError` wraps any exception your code throws inside a step. Its
    `error_type`, `message`, `data`, and `stack_trace` attributes carry the details of the
    original exception.

    `ExecutionError` fails the execution without retry. `InvocationError` causes Lambda to
    retry the entire invocation. Both carry a `termination_reason` attribute.

    `StepInterruptedError` is a subclass of `InvocationError`. The SDK raises it when an
    at-most-once step started but Lambda was interrupted before the SDK checkpointed the
    result. See [Step interrupted](#step-interrupted) below.

=== "Java"

    ```mermaid
    graph TD
      DEE[DurableExecutionException]
      DEE --> UDEE[UnrecoverableDurableExecutionException]
      DEE --> DOE[DurableOperationException]
      DEE --> SerDesException
      UDEE --> IllegalDurableOperationException
      UDEE --> NonDeterministicExecutionException
      DOE --> StepException
      DOE --> CallbackException
      StepException --> StepFailedException
      StepException --> StepInterruptedException
      CallbackException --> CallbackFailedException
    ```

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/exception-hierarchy.java"
    ```

    `DurableExecutionException` is the base class for all SDK exceptions.

    `DurableOperationException` carries the failed `Operation` and its `ErrorObject`. Use
    `getOperation()`, `getErrorObject()`, and `getOperationStatus()` to inspect the failure.

    `StepFailedException` is thrown when a step exhausts all retry attempts.

    `StepInterruptedException` is thrown when an at-most-once step started but Lambda was
    interrupted before the SDK checkpointed the result. See
    [Step interrupted](#step-interrupted) below.

## Validation errors

The SDK does not retry validation errors. The SDK throws validation errors when you pass
invalid arguments to an SDK operation, such as a negative duration or an empty operation
name.

=== "TypeScript"

    The SDK throws `TypeError` for invalid configuration values.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/validation-error.ts"
    ```

=== "Python"

    The SDK raises `ValidationError` for invalid configuration values.

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/validation-error.py"
    ```

=== "Java"

    The SDK throws `IllegalArgumentException` for invalid configuration values.

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/validation-error.java"
    ```

## Step interrupted

When you [configure a step](../operations/step.md#stepconfig) with at-most-once
semantics, the SDK runs the step body at most once per retry attempt. If Lambda is
interrupted after the step body starts but before the SDK checkpoints the result, the
SDK does not re-run the step on the next invocation. Instead, it throws a
step-interrupted exception.

Use at-most-once semantics for operations with side effects that must not run more than
once, such as charging a payment or sending a notification. When you catch a
step-interrupted exception, check the external system to determine whether the operation
succeeded before deciding how to proceed.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/step-interrupted.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/step-interrupted.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/step-interrupted.java"
    ```

## Serialization errors

The SDK serializes step results to checkpoint storage. The default serializer handles
standard types for each language. Custom serializers should throw the appropriate
exception type when they encounter a value they cannot handle.

=== "TypeScript"

    When a custom `Serdes` implementation throws during `serialize` or `deserialize`, the
    SDK throws `SerdesFailedError` as an unhandled exception. The durable functions backend
    retries the invocation.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/serdes-error.ts"
    ```

=== "Python"

    When serialization or deserialization fails, the SDK raises `SerDesError`, returns a
    `FAILED` status response, and does not retry.

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/serdes-error.py"
    ```

=== "Java"

    When serialization or deserialization fails, the SDK throws `SerDesException`. The
    executor catches it, returns a `FAILED` status response, and does not retry.

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/serdes-error.java"
    ```

## See also

- [Retries](retries.md) Configure retry strategies and backoff
- [Steps](../operations/step.md) Step configuration and semantics
- [Serialization](../state/serialization.md) Custom serializers
