# Errors

## Table of Contents

- [Overview](#overview)
- [Terminology](#terminology)
- [Getting started](#getting-started)
- [Exception types](#exception-types)
- [Error response formats](#error-response-formats)
- [Common error scenarios](#common-error-scenarios)
- [Troubleshooting](#troubleshooting)
- [Best practices](#best-practices)
- [FAQ](#faq)
- [Testing](#testing)
- [See also](#see-also)

[← Back to main index](../index.md)

## Overview

Error handling in durable functions determines how your code responds to failures. The SDK provides typed exceptions, automatic retry with exponential backoff, and AWS-compliant error responses that help you build resilient workflows.

When errors occur, the SDK can:
- Retry transient failures automatically with configurable backoff
- Checkpoint failures with detailed error information
- Distinguish between recoverable and unrecoverable errors
- Provide clear termination reasons and stack traces for debugging

[↑ Back to top](#table-of-contents)

## Terminology

**Exception** - A Python error that interrupts normal execution flow. The SDK provides specific exception types for different failure scenarios.

**Retry strategy** - A function that determines whether to retry an operation after an exception and how long to wait before retrying.

**Termination reason** - A code indicating why a durable execution terminated, such as `UNHANDLED_ERROR` or `INVOCATION_ERROR`.

**Recoverable error** - An error that can be retried, such as transient network failures or rate limiting.

**Unrecoverable error** - An error that terminates execution immediately without retry, such as validation errors or non-deterministic execution.

**Backoff** - The delay between retry attempts, typically increasing exponentially to avoid overwhelming failing services.

[↑ Back to top](#table-of-contents)

## Getting started

Here's a simple example of handling errors in a durable function:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/basic-error-handling.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/basic-error-handling.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/basic-error-handling.java"
    ```


When this function runs:
1. If `order_id` is missing, `ValueError` is raised from your code
2. The exception is caught and handled gracefully
3. A structured error response is returned to the caller

[↑ Back to top](#table-of-contents)

## Exception types

The SDK provides several exception types for different failure scenarios.

### Exception summary

| Exception | Retryable | Behavior | Use case |
|-----------|-----------|----------|----------|
| `ValidationError` | No | Fails immediately | SDK detects invalid arguments |
| `ExecutionError` | No | Returns FAILED status | Permanent business logic failures |
| `InvocationError` | Yes (by Lambda) | Lambda retries invocation | Transient infrastructure issues |
| `CallbackError` | No | Returns FAILED status | Callback handling failures |
| `StepInterruptedError` | Yes (automatic) | Retries on next invocation | Step interrupted before checkpoint |
| `CheckpointError` | Depends | Retries if 4xx (except invalid token) | Failed to save execution state |
| `SerDesError` | No | Returns FAILED status | Serialization failures |

### Base exceptions

**DurableExecutionsError** - Base class for all SDK exceptions.

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/base-exception.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/base-exception.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/base-exception.java"
    ```


**UnrecoverableError** - Base class for errors that terminate execution. These errors include a `termination_reason` attribute.

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/unrecoverable-error.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/unrecoverable-error.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/unrecoverable-error.java"
    ```


### Validation errors

**ValidationError** - Raised by the SDK when you pass invalid arguments to SDK operations.

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/validation-error.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/validation-error.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/validation-error.java"
    ```


The SDK raises `ValidationError` when:
- Operation arguments are invalid (negative timeouts, empty names)
- Required parameters are missing
- Configuration values are out of range

### Execution errors

**ExecutionError** - Raised when execution fails in a way that shouldn't be retried. Returns `FAILED` status without retry.

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/execution-error.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/execution-error.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/execution-error.java"
    ```


Use `ExecutionError` for:
- Business logic failures
- Invalid data that won't be fixed by retry
- Permanent failures that should fail fast

### Invocation errors

**InvocationError** - Raised when Lambda should retry the entire invocation. Causes Lambda to retry by throwing from the handler.

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/invocation-error.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/invocation-error.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/invocation-error.java"
    ```


Use `InvocationError` for:
- Service unavailability
- Network failures
- Transient infrastructure issues

### Callback errors

**CallbackError** - Raised when callback handling fails.

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/callback-error.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/callback-error.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/callback-error.java"
    ```


### Step interrupted errors

**StepInterruptedError** - Raised when a step is interrupted before checkpointing.

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/step-interrupted-error.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/step-interrupted-error.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/step-interrupted-error.java"
    ```


### Serialization errors

**SerDesError** - Raised when serialization or deserialization fails.

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/serdes-error.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/serdes-error.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/serdes-error.java"
    ```


[↑ Back to top](#table-of-contents)

## Error response formats

The SDK follows AWS service conventions for error responses.

### Error response structure

When a durable function fails, the response includes:

```json
{
  "errorType": "ExecutionError",
  "errorMessage": "Order validation failed",
  "termination_reason": "EXECUTION_ERROR",
  "stackTrace": [
    "  File \"/var/task/handler.py\", line 42, in process_order",
    "    raise ExecutionError(\"Order validation failed\")"
  ]
}
```

### Termination reasons

**UNHANDLED_ERROR** - An unhandled exception occurred in user code.

**INVOCATION_ERROR** - Lambda should retry the invocation.

**EXECUTION_ERROR** - Execution failed and shouldn't be retried.

**CHECKPOINT_FAILED** - Failed to checkpoint execution state.

**NON_DETERMINISTIC_EXECUTION** - Execution produced different results on replay.

**STEP_INTERRUPTED** - A step was interrupted before completing.

**CALLBACK_ERROR** - Callback handling failed.

**SERIALIZATION_ERROR** - Failed to serialize or deserialize data.

### HTTP status codes

When calling durable functions via API Gateway or Lambda URLs:

- **200 OK** - Execution succeeded
- **400 Bad Request** - Validation error or invalid input
- **500 Internal Server Error** - Execution error or unhandled exception
- **503 Service Unavailable** - Invocation error (Lambda will retry)

[↑ Back to top](#table-of-contents)

## Common error scenarios

### Handling input validation

Validate input early and return clear error messages:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/input-validation.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/input-validation.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/input-validation.java"
    ```


### Handling transient failures

Retry transient failures automatically:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/transient-failures.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/transient-failures.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/transient-failures.java"
    ```


### Handling permanent failures

Fail fast for permanent errors:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/permanent-failures.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/permanent-failures.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/permanent-failures.java"
    ```


### Handling multiple error types

Handle different error types appropriately:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/multiple-error-types.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/multiple-error-types.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/multiple-error-types.java"
    ```


[↑ Back to top](#table-of-contents)

## Troubleshooting

### Step retries exhausted

**Problem:** Your step fails after exhausting all retry attempts.

**Cause:** The operation continues to fail, or the error isn't retryable.

**Solution:** Check your retry configuration and error types:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/retries-exhausted.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/retries-exhausted.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/retries-exhausted.java"
    ```


### Checkpoint failed errors

**Problem:** Execution fails with `CheckpointError`.

**Cause:** Failed to save execution state, possibly due to payload size limits or service issues.

**Solution:** Reduce checkpoint payload size or check service health:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/checkpoint-failed.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/checkpoint-failed.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/checkpoint-failed.java"
    ```


### Callback timeout

**Problem:** Callback times out before receiving a response.

**Cause:** External system didn't respond within the timeout period.

**Solution:** Increase callback timeout or implement retry logic:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/callback-timeout.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/callback-timeout.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/callback-timeout.java"
    ```


### Step interrupted errors

**Problem:** Steps are interrupted before completing.

**Cause:** Lambda timeout or memory limit reached during step execution.

**Solution:** Increase Lambda timeout or break large steps into smaller ones:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/step-interrupted.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/step-interrupted.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/step-interrupted.java"
    ```


[↑ Back to top](#table-of-contents)

## Best practices

**Validate input early** - Check for invalid input at the start of your function and return clear error responses or raise appropriate exceptions like `ValueError`.

**Use appropriate exception types** - Choose the right exception type for each failure scenario. Use `ExecutionError` for permanent failures and `InvocationError` for transient issues.

**Configure retry for transient failures** - Use retry strategies for operations that might fail temporarily, such as network calls or rate limits.

**Fail fast for permanent errors** - Don't retry errors that won't be fixed by retrying, such as validation failures or business logic errors.

**Wrap non-deterministic code in steps** - All code that produces different results on replay must be wrapped in steps, including random values, timestamps, and external API calls.

**Handle errors explicitly** - Catch and handle exceptions in your code. Provide meaningful error messages to callers.

**Log errors with context** - Use `context.logger` to log errors with execution context for debugging.

**Keep error messages clear** - Write error messages that help users understand what went wrong and how to fix it.

**Test error scenarios** - Write tests for both success and failure cases to ensure your error handling works correctly.

**Monitor error rates** - Track error rates and termination reasons to identify issues in production.

[↑ Back to top](#table-of-contents)

## FAQ

**Q: What's the difference between ExecutionError and InvocationError?**

A: `ExecutionError` fails the execution without retry (returns FAILED status). `InvocationError` triggers Lambda to retry the entire invocation. Use `ExecutionError` for permanent failures and `InvocationError` for transient issues.

**Q: How do I retry only specific exceptions?**

A: Use `retryable_error_types` in `RetryStrategyConfig`:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/faq-retry-specific.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/faq-retry-specific.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/faq-retry-specific.java"
    ```


**Q: Can I customize the backoff strategy?**

A: Yes, configure `initial_delay_seconds`, `max_delay_seconds`, `backoff_rate`, and `jitter_strategy` in `RetryStrategyConfig`.

**Q: What happens when retries are exhausted?**

A: The step checkpoints the error and the exception propagates to your handler. You can catch and handle it there.

**Q: How do I prevent duplicate operations on retry?**

A: Use at-most-once semantics for operations with side effects:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/faq-parallel-errors.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/faq-parallel-errors.py"
    ```

=== "Java">

    ``` java
    --8<-- "examples/java/advanced/error-handling/faq-parallel-errors.java"
    ```


**Q: Can I access error details in my code?**

A: Yes, catch the exception and access its attributes:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/faq-error-details.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/faq-error-details.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/faq-error-details.java"
    ```


**Q: How do I handle errors in parallel operations?**

A: Wrap each parallel operation in a try-except block or let errors propagate to fail the entire execution:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/faq-parallel-errors.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/faq-parallel-errors.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/faq-parallel-errors.java"
    ```


**Q: What's the maximum number of retry attempts?**

A: You can configure any number of attempts, but consider Lambda timeout limits. The default is 6 attempts.

[↑ Back to top](#table-of-contents)

## Testing

You can test error handling using the testing SDK. The test runner executes your function and lets you inspect errors.

### Testing successful execution

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/test-successful-execution.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/test-successful-execution.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/test-successful-execution.java"
    ```


### Testing error conditions

Test that your function handles errors correctly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/test-error-conditions.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/test-error-conditions.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/test-error-conditions.java"
    ```


### Testing SDK validation errors

Test that the SDK catches invalid configuration:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/test-sdk-validation.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/test-sdk-validation.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/test-sdk-validation.java"
    ```


### Testing retry behavior

Test that steps retry correctly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/test-retry-behavior.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/test-retry-behavior.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/test-retry-behavior.java"
    ```


### Testing retry exhaustion

Test that execution fails when retries are exhausted:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/test-retry-exhaustion.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/test-retry-exhaustion.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/test-retry-exhaustion.java"
    ```


### Inspecting error details

Inspect error details in test results:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/test-inspect-error-details.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/test-inspect-error-details.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/test-inspect-error-details.java"
    ```


For more testing patterns, see:
- [Basic tests](../testing/basic-tests.md) - Simple test examples
- [Complex workflows](../testing/complex-workflows.md) - Multi-step workflow testing
- [Best practices](../patterns/best-practices.md) - Testing recommendations

[↑ Back to top](#table-of-contents)

## See also

- [Retries](retries.md) - Retry strategies and backoff configuration
- [Step](../operations/step.md) - Configure retry for steps
- [Callback](../operations/callback.md) - Handle callback errors
- [Child context](../operations/child-context.md) - Error handling in nested contexts
- [Examples](https://github.com/awslabs/aws-durable-execution-sdk-python/tree/main/examples/src/step) - Error handling examples

[↑ Back to top](#table-of-contents)

[↑ Back to top](#table-of-contents)
