# Invoke Operations

## Table of Contents

- [What are invoke operations?](#what-are-invoke-operations)
- [Terminology](#terminology)
- [Key features](#key-features)
- [Getting started](#getting-started)
- [Method signature](#method-signature)
- [Function composition patterns](#function-composition-patterns)
- [Configuration](#configuration)
- [Error handling](#error-handling)
- [Advanced patterns](#advanced-patterns)
- [Best practices](#best-practices)
- [FAQ](#faq)
- [Testing](#testing)
- [See also](#see-also)

[← Back to main index](../index.md)

## Terminology

**Invoke operation** - A durable operation that calls another durable function and waits for its result. Created using `context.invoke()`.

**Chained invocation** - The process of one durable function calling another durable function. The calling function suspends while the invoked function executes.

**Function composition** - Building complex workflows by combining multiple durable functions, where each function handles a specific part of the overall process.

**Payload** - The input data sent to the invoked function. Can be any JSON-serializable value or use custom serialization.

**Timeout** - The maximum time to wait for an invoked function to complete. If exceeded, the invoke operation fails with a timeout error.

[↑ Back to top](#table-of-contents)

## What are invoke operations?

Invoke operations let you call other Lambda functions from within your durable function. You can invoke both durable functions and regular on-demand Lambda functions. This enables function composition, where you break complex workflows into smaller, reusable functions. The calling function suspends while the invoked function executes, and resumes when the result is available.

Use invoke operations to:
- Modularize complex workflows into manageable functions
- Call existing Lambda functions (durable or on-demand) from your workflow
- Isolate different parts of your business logic
- Build hierarchical execution patterns
- Coordinate multiple Lambda functions durably
- Integrate with existing Lambda-based services

When you invoke a function, the SDK:
1. Checkpoints the invoke operation
2. Triggers the target function asynchronously
3. Suspends the calling function
4. Resumes the calling function when the result is ready
5. Returns the result or propagates any errors

[↑ Back to top](#table-of-contents)

## Key features

- **Automatic checkpointing** - Invoke operations are checkpointed before execution
- **Asynchronous execution** - Invoked functions run independently without blocking resources
- **Result handling** - Results are automatically deserialized and returned
- **Error propagation** - Errors from invoked functions propagate to the caller
- **Timeout support** - Configure maximum wait time for invoked functions
- **Custom serialization** - Control how payloads and results are serialized
- **Named operations** - Identify invoke operations by name for debugging

[↑ Back to top](#table-of-contents)

## Getting started

Here's a simple example of invoking another durable function:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/process-order.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/process-order.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/process-order.java"
    ```


When this function runs:
1. It invokes the `validate-order` function and waits for the result
2. If validation succeeds, it invokes the `process-payment` function
3. Each invoke operation is checkpointed automatically
4. If the function is interrupted, it resumes from the last completed invoke

[↑ Back to top](#table-of-contents)

## Method signature

### context.invoke()

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/invoke-method-signature.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/invoke-method-signature.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/invoke-method-signature.java"
    ```


**Parameters:**

- `function_name` - The name of the Lambda function to invoke. This should be the function name, not the ARN.
- `payload` - The input data to send to the invoked function. Can be any JSON-serializable value.
- `name` (optional) - A name for the invoke operation, useful for debugging and testing.
- `config` (optional) - An `InvokeConfig` object to configure timeout and serialization.

**Returns:** The result returned by the invoked function.

**Raises:** 
- `CallableRuntimeError` - If the invoked function fails or times out

[↑ Back to top](#table-of-contents)

## Function composition patterns

### Sequential invocations

Call multiple functions in sequence, where each depends on the previous result:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/orchestrate-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/orchestrate-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/orchestrate-workflow.java"
    ```


### Conditional invocations

Invoke different functions based on conditions:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/process-document.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/process-document.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/process-document.java"
    ```


### Hierarchical workflows

Build hierarchical workflows where parent functions coordinate child functions:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/parent-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/parent-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/parent-workflow.java"
    ```


### Invoking on-demand functions

You can invoke regular Lambda functions (non-durable) from your durable workflow:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/invoke-ondemand-function.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/invoke-ondemand-function.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/invoke-ondemand-function.java"
    ```


[↑ Back to top](#table-of-contents)

## Configuration

Configure invoke behavior using `InvokeConfig`:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/invoke-with-config.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/invoke-with-config.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/invoke-with-config.java"
    ```


### InvokeConfig parameters

**timeout** - Maximum duration to wait for the invoked function to complete. Default is no timeout. Use this to prevent long-running invocations from blocking execution indefinitely.

**serdes_payload** - Custom serialization/deserialization for the payload sent to the invoked function. If None, uses default JSON serialization.

**serdes_result** - Custom serialization/deserialization for the result returned from the invoked function. If None, uses default JSON serialization.

**tenant_id** - Optional tenant identifier for multi-tenant isolation. If provided, the invocation will be scoped to this tenant.

### Setting timeouts

Use the `Duration` class to set timeouts:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/set-timeout.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/set-timeout.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/set-timeout.java"
    ```


[↑ Back to top](#table-of-contents)

## Error handling

### Handling invocation errors

Errors from invoked functions propagate to the calling function. Catch and handle them as needed:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/handle-invocation-error.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/handle-invocation-error.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/handle-invocation-error.java"
    ```


### Timeout handling

Handle timeout errors specifically:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/handle-timeout-error.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/handle-timeout-error.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/handle-timeout-error.java"
    ```


### Retry patterns

Implement retry logic for failed invocations:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/retry-example-10.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/retry-example-10.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/retry-example-10.java"
    ```


[↑ Back to top](#table-of-contents)

## Advanced patterns

### Custom serialization

Use custom serialization for complex data types:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/serialize.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/serialize.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/serialize.java"
    ```


### Fan-out pattern with parallel invocations

Invoke multiple functions in parallel using steps:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/invoke-service.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/invoke-service.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/invoke-service.java"
    ```


### Passing context between invocations

Pass data between invoked functions:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/pass-context-between-invocations.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/pass-context-between-invocations.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/pass-context-between-invocations.java"
    ```


[↑ Back to top](#table-of-contents)

## Best practices

**Use descriptive function names** - Choose clear, descriptive names for the functions you invoke to make workflows easier to understand.

**Name invoke operations** - Use the `name` parameter to identify invoke operations in logs and tests.

**Set appropriate timeouts** - Configure timeouts based on expected execution time. Don't set them too short or too long.

**Handle errors explicitly** - Catch and handle errors from invoked functions. Don't let them propagate unexpectedly.

**Keep payloads small** - Large payloads increase serialization overhead. Consider passing references instead of large data.

**Design for idempotency** - Invoked functions should be idempotent since they might be retried.

**Use hierarchical composition** - Break complex workflows into layers of functions, where each layer handles a specific level of abstraction.

**Avoid deep nesting** - Don't create deeply nested invocation chains. Keep hierarchies shallow for better observability.

**Log invocation boundaries** - Log when invoking functions and when receiving results for better debugging.

**Consider cost implications** - Each invoke operation triggers a separate Lambda invocation, which has cost implications.

**Mix durable and on-demand functions** - You can invoke both durable and regular Lambda functions. The orchestrator can be durable and compose regular on-demand functions. The orchestrator provides durability for the results of the invoked on-demand functions without needing to provide durability on the invoked functions themselves. Use durable functions for complex workflows and on-demand functions for simple operations.

[↑ Back to top](#table-of-contents)

## FAQ

**Q: What's the difference between invoke and step?**

A: `invoke()` calls another durable function (Lambda), while `step()` executes code within the current function. Use invoke for function composition, use step for checkpointing operations within a function.

**Q: Can I invoke non-durable functions?**

A: Yes, `context.invoke()` can call both durable functions and regular on-demand Lambda functions. The invoke operation works with any Lambda function that accepts and returns JSON-serializable data.


**Q: How do I pass the result from one invoke to another?**

A: Simply use the return value. The type of the return value is governed by the `serdes_result` configuration:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/pass-result-between-invokes.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/pass-result-between-invokes.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/pass-result-between-invokes.java"
    ```


**Q: What happens if an invoked function fails?**

A: The error propagates to the calling function as a `CallableRuntimeError`. You can catch and handle it.

**Q: Can I invoke the same function multiple times?**

A: Yes, you can invoke the same function multiple times with different payloads or names.

**Q: How do I invoke a function in a different AWS account?**

A: The `function_name` parameter accepts function names in the same account. For cross-account invocations, you need appropriate IAM permissions and may need to use function ARNs (check AWS documentation for cross-account Lambda invocations).

**Q: What's the maximum timeout I can set?**

A: The timeout is limited by Lambda's maximum execution time (15 minutes). However, durable functions can run longer by suspending and resuming.

**Q: Can I invoke functions in parallel?**

A: Not directly with `context.invoke()`. For parallel execution, consider using `context.parallel()` with steps that perform invocations, or invoke multiple functions sequentially.

**Q: How do I debug invoke operations?**

A: Use the `name` parameter to identify operations in logs. Check CloudWatch logs for both the calling and invoked functions.

**Q: What happens if I don't set a timeout?**

A: The invoke operation waits indefinitely for the invoked function to complete. It's recommended to set timeouts for better error handling.

**Q: What's the difference between context.invoke() and using boto3's Lambda client to invoke functions?**

A: When you use `context.invoke()`, the SDK suspends your durable function's execution while waiting for the result. This means you don't pay for Lambda compute time while waiting. With boto3's Lambda client, your function stays active and consumes billable compute time while waiting for the response. Additionally, `context.invoke()` automatically checkpoints the operation, handles errors durably, and integrates with the durable execution lifecycle.

[↑ Back to top](#table-of-contents)

## Testing

You can test invoke operations using the testing SDK. The test runner executes your function and lets you inspect invoke operations.

### Basic invoke testing

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/test-basic-invoke.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/test-basic-invoke.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/test-basic-invoke.java"
    ```


### Inspecting invoke operations

Use the result object to inspect invoke operations:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/test-inspect-invoke.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/test-inspect-invoke.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/test-inspect-invoke.java"
    ```


### Testing error handling

Test that invoke errors are handled correctly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/test-error-handling.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/test-error-handling.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/test-error-handling.java"
    ```


### Testing timeouts

Test that timeouts are handled correctly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/test-timeout-handling.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/test-timeout-handling.py"
    ```

=== "Java">

    ``` java
    --8<-- "examples/java/core/invoke/test-timeout-handling.java"
    ```


### Mocking invoked functions

When testing, you can mock the invoked functions to control their behavior:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/invoke/test-mock-invoked-functions.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/invoke/test-mock-invoked-functions.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/invoke/test-mock-invoked-functions.java"
    ```


For more testing patterns, see:
- [Basic tests](../testing-patterns/basic-tests.md) - Simple test examples
- [Complex workflows](../testing-patterns/complex-workflows.md) - Multi-step workflow testing
- [Best practices](../best-practices.md) - Testing recommendations

[↑ Back to top](#table-of-contents)

## See also

- [Steps](steps.md) - Execute code with checkpointing
- [Child contexts](child-contexts.md) - Organize operations hierarchically
- [Parallel operations](parallel.md) - Execute multiple operations concurrently
- [Error handling](../advanced/error-handling.md) - Handle errors in durable functions

[↑ Back to top](#table-of-contents)

[↑ Back to top](#table-of-contents)
