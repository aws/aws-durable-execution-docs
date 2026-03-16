# Callbacks

## Table of Contents

- [Terminology](#terminology)
- [What are callbacks?](#what-are-callbacks)
- [Key features](#key-features)
- [Getting started](#getting-started)
- [Method signatures](#method-signatures)
- [Configuration](#configuration)
- [Waiting for callbacks](#waiting-for-callbacks)
- [Integration patterns](#integration-patterns)
- [Advanced patterns](#advanced-patterns)
- [Best practices](#best-practices)
- [FAQ](#faq)
- [Testing](#testing)
- [See also](#see-also)

[← Back to main index](../index.md)

## Terminology

**Callback** - A mechanism that pauses execution and waits for an external system to provide a result. Created using `context.create_callback()`.

**Callback ID** - A unique identifier for a callback that you send to external systems. The external system uses this ID to send the result back.

**Callback timeout** - The maximum time to wait for a callback response. If the timeout expires without a response, the callback fails.

**Heartbeat timeout** - The maximum time between heartbeat signals from the external system. Use this to detect when external systems stop responding.

**Wait for callback** - The operation that pauses execution until the callback receives a result. Created using `context.wait_for_callback()`.

[↑ Back to top](#table-of-contents)

## What are callbacks?

Callbacks let your durable function pause and wait for external systems to respond. When you create a callback, you get a unique callback ID that you can send to external systems like approval workflows, payment processors, or third-party APIs. Your function pauses until the external system calls back with a result.

Use callbacks to:
- Wait for human approvals in workflows
- Integrate with external payment systems
- Coordinate with third-party APIs
- Handle long-running external processes
- Implement request-response patterns with external systems

[↑ Back to top](#table-of-contents)

## Key features

- **External system integration** - Pause execution and wait for external responses
- **Unique callback IDs** - Each callback gets a unique identifier for routing
- **Configurable timeouts** - Set maximum wait times and heartbeat intervals
- **Type-safe results** - Callbacks are generic and preserve result types
- **Automatic checkpointing** - Callback results are saved automatically
- **Heartbeat monitoring** - Detect when external systems stop responding

[↑ Back to top](#table-of-contents)

## Getting started

Callbacks let you pause your durable function while waiting for an external system to respond. Think of it like this:

**Your durable function:**
1. Creates a callback and gets a unique `callback_id`
2. Sends the `callback_id` to an external system (payment processor, approval system, etc.)
3. Calls `callback.result()` - execution pauses here ⏸️
4. When the callback is notified, execution resumes ▶️

**Your notification handler** (separate Lambda or service):
1. Receives the result from the external system (via webhook, queue, etc.)
2. Calls AWS Lambda API `SendDurableExecutionCallbackSuccess` with the `callback_id`
3. This wakes up your durable function

The key insight: callbacks need two pieces working together - one that waits, and one that notifies.

### Basic example

Here's a simple example showing the durable function side:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/basic-example.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/basic-example.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/basic-example.java"
    ```


### Notifying the callback

When your external system finishes processing, you need to notify the callback using AWS Lambda APIs:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/notify-success.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/notify-success.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/notify-success.java"
    ```


### Complete example with message broker

Here's a complete example showing both sides of the callback flow:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/message-broker-durable-function.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/message-broker-durable-function.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/message-broker-durable-function.java"
    ```


### Key points

- **Callbacks require two parts**: Your durable function creates the callback, and a separate process notifies the result
- **Use Lambda APIs to notify**: `SendDurableExecutionCallbackSuccess`, `SendDurableExecutionCallbackFailure`, or `SendDurableExecutionCallbackHeartbeat`
- **Execution suspends at `callback.result()`**: Your function stops running and doesn't consume resources while waiting
- **Execution resumes when notified**: When you call the Lambda API with the callback ID, your function resumes from where it suspended
- **Heartbeats keep callbacks alive**: For long operations, send heartbeats to prevent timeout

[↑ Back to top](#table-of-contents)

## Method signatures

### context.create_callback()

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/create-callback-signature.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/create-callback-signature.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/create-callback-signature.java"
    ```


**Parameters:**

- `name` (optional) - A name for the callback, useful for debugging and testing
- `config` (optional) - A `CallbackConfig` object to configure timeout behavior

**Returns:** A `Callback` object with a `callback_id` property

**Type parameter:** `T` - The type of result the callback will receive

### callback.callback_id

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/get-callback-id.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/get-callback-id.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/get-callback-id.java"
    ```


A unique identifier for this callback. Send this ID to external systems so they can return results.

### callback.result()

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/get-callback-result.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/get-callback-result.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/get-callback-result.java"
    ```


Returns the callback result. Blocks until the result is available or the callback times out.

[↑ Back to top](#table-of-contents)

## Configuration

Configure callback behavior using `CallbackConfig`:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/callback-config.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/callback-config.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/callback-config.java"
    ```


### CallbackConfig parameters

**timeout** - Maximum time to wait for the callback response. Use `Duration` helpers to specify:
- `Duration.from_seconds(60)` - 60 seconds
- `Duration.from_minutes(5)` - 5 minutes
- `Duration.from_hours(2)` - 2 hours
- `Duration.from_days(1)` - 1 day

**heartbeat_timeout** - Maximum time between heartbeat signals from the external system. If the external system doesn't send a heartbeat within this interval, the callback fails. Set to 0 or omit to disable heartbeat monitoring.

**serdes** (optional) - Custom serialization/deserialization for the callback result. If not provided, uses JSON serialization.

### Duration helpers

The `Duration` class provides convenient methods for specifying timeouts:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/duration-helpers.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/duration-helpers.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/duration-helpers.java"
    ```


[↑ Back to top](#table-of-contents)

## Waiting for callbacks

After creating a callback, you typically wait for its result. There are two ways to do this:

### Using callback.result()

Call `result()` on the callback object to wait for the response:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/wait-using-result.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/wait-using-result.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/wait-using-result.java"
    ```


### Using context.wait_for_callback()

Alternatively, use `wait_for_callback()` to wait for a callback by its ID:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/wait-using-wait-for-callback.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/wait-using-wait-for-callback.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/wait-using-wait-for-callback.java"
    ```


[↑ Back to top](#table-of-contents)

## Integration patterns

### Human approval workflow

Use callbacks to pause execution while waiting for human approval:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/human-approval-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/human-approval-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/human-approval-workflow.java"
    ```


### Payment processing

Integrate with external payment processors:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/payment-processing.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/payment-processing.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/payment-processing.java"
    ```


### Third-party API integration

Wait for responses from third-party APIs:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/third-party-api-integration.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/third-party-api-integration.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/third-party-api-integration.java"
    ```


### Multiple callbacks

Handle multiple external systems in parallel:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/multiple-callbacks.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/multiple-callbacks.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/multiple-callbacks.java"
    ```


[↑ Back to top](#table-of-contents)

## Advanced patterns

### Callback with retry

Combine callbacks with retry logic for resilient integrations:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/wait-for-external-system.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/wait-for-external-system.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/wait-for-external-system.java"
    ```


### Conditional callback handling

Handle different callback results based on conditions:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/conditional-callback-handling.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/conditional-callback-handling.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/conditional-callback-handling.java"
    ```


### Callback with fallback

Implement fallback logic when callbacks timeout:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/callback-with-fallback.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/callback-with-fallback.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/callback-with-fallback.java"
    ```


[↑ Back to top](#table-of-contents)

## Best practices

**Set appropriate timeouts** - Choose timeout values based on your external system's expected response time. Add buffer for network delays and processing time.

**Use heartbeat timeouts for long operations** - Enable heartbeat monitoring for callbacks that take more than a few minutes. This helps detect when external systems stop responding.

**Send callback IDs securely** - Treat callback IDs as sensitive data. Use HTTPS when sending them to external systems.

**Handle timeout scenarios** - Always handle the case where `callback.result()` returns `None` due to timeout. Implement fallback logic or error handling.

**Name callbacks for debugging** - Use descriptive names to identify callbacks in logs and tests.

**Don't reuse callback IDs** - Each callback gets a unique ID. Don't try to reuse IDs across different operations.

**Validate callback results** - Always validate the structure and content of callback results before using them.

**Use type hints** - Specify the expected result type when creating callbacks: `Callback[dict]`, `Callback[str]`, etc.

**Monitor callback metrics** - Track callback success rates, timeout rates, and response times to identify integration issues.

**Document callback contracts** - Clearly document what data external systems should send back and in what format.

[↑ Back to top](#table-of-contents)

## FAQ

**Q: What happens if a callback times out?**

A: If the timeout expires before receiving a result, `callback.result()` returns `None`. You should handle this case in your code.

**Q: Can I cancel a callback?**

A: No, callbacks can't be cancelled once created. They either receive a result or timeout.

**Q: How do external systems send results back?**

A: External systems use the callback ID to send results through your application's callback endpoint. You need to implement an endpoint that receives the callback ID and result, then forwards it to the durable execution service.

**Q: Can I create multiple callbacks in one function?**

A: Yes, you can create as many callbacks as needed. Each gets a unique callback ID.

**Q: What's the maximum timeout for a callback?**

A: You can set any timeout value using `Duration` helpers. For long-running operations (hours or days), use longer timeouts and enable heartbeat monitoring to detect if external systems stop responding.

**Q: Do I need to wait for a callback immediately after creating it?**

A: No, you can create a callback, send its ID to an external system, perform other operations, and wait for the result later in your function.

**Q: Can callbacks be used with steps?**

A: Yes, you can create and wait for callbacks inside step functions. However, `context.wait_for_callback()` is a convenience method that already wraps the callback in a step with retry logic for you.

**Q: What happens if the external system sends a result after the timeout?**

A: Late results are ignored. The callback has already failed due to timeout.

**Q: How do I test functions with callbacks?**

A: Use the testing SDK to simulate callback responses. See the Testing section below for examples.

**Q: Can I use callbacks in child contexts?**

A: Yes, callbacks work in child contexts just like in the main context.

**Q: What's the difference between timeout and heartbeat_timeout?**

A: `timeout` is the maximum total wait time. `heartbeat_timeout` is the maximum time between heartbeat signals. Use heartbeat timeout to detect when external systems stop responding before the main timeout expires.

[↑ Back to top](#table-of-contents)

## Testing

You can test callbacks using the testing SDK. The test runner lets you simulate callback responses and verify callback behavior.

### Basic callback testing

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/basic-callback-testing.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/basic-callback-testing.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/basic-callback-testing.java"
    ```


### Inspecting callback operations

Use `result.operations` to inspect callback details:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/inspect-callback-operations.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/inspect-callback-operations.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/inspect-callback-operations.java"
    ```


### Testing callback timeouts

Test that callbacks handle timeouts correctly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/test-callback-timeout.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/test-callback-timeout.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/test-callback-timeout.java"
    ```


### Testing callback integration patterns

Test complete integration workflows:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/callbacks/test-integration-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/callbacks/test-integration-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/callbacks/test-integration-workflow.java"
    ```


For more testing patterns, see:
- [Basic tests](../testing-patterns/basic-tests.md) - Simple test examples
- [Complex workflows](../testing-patterns/complex-workflows.md) - Multi-step workflow testing
- [Best practices](../best-practices.md) - Testing recommendations

[↑ Back to top](#table-of-contents)

## See also

- [Steps](steps.md) - Combine callbacks with steps for retry logic
- [Child contexts](child-contexts.md) - Use callbacks in nested contexts
- [Error handling](../advanced/error-handling.md) - Handle callback failures
- [Examples](https://github.com/awslabs/aws-durable-execution-sdk-python/tree/main/examples/src/callback) - More callback examples

[↑ Back to top](#table-of-contents)

## License

See the LICENSE file for our project's licensing.

[↑ Back to top](#table-of-contents)
