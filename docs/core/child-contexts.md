# Child Contexts

## Table of Contents

- [Terminology](#terminology)
- [What are child contexts?](#what-are-child-contexts)
- [Key features](#key-features)
- [Getting started](#getting-started)
- [Method signatures](#method-signatures)
- [Using the @durable_with_child_context decorator](#using-the-durable_with_child_context-decorator)
- [Naming child contexts](#naming-child-contexts)
- [Use cases for isolation](#use-cases-for-isolation)
- [Advanced patterns](#advanced-patterns)
- [Best practices](#best-practices)
- [FAQ](#faq)
- [Testing](#testing)
- [See also](#see-also)

[← Back to main index](../index.md)

## Terminology

**Child context** - An isolated execution scope within a durable function. Created using `context.run_in_child_context()`.

**Parent context** - The main durable function context that creates child contexts.

**Context function** - A function decorated with `@durable_with_child_context` that receives a `DurableContext` and can execute operations.

**Context isolation** - Child contexts have their own operation namespace, preventing naming conflicts with the parent context.

**Context result** - The return value from a child context function, which is checkpointed as a single unit in the parent context.

[↑ Back to top](#table-of-contents)

## What are child contexts?

A child context creates a scope in which you can nest durable operations. It creates an isolated execution scope with its own set of operations, checkpoints, and state. This is often useful as a unit of concurrency that lets you run concurrent operations within your durable function. You can also use child contexts to wrap large chunks of durable logic into a single piece - once completed, that logic won't run or replay again.

Use child contexts to:
- Run concurrent operations (steps, waits, callbacks) in parallel
- Wrap large blocks of logic that should execute as a single unit
- Handle large data that exceeds individual step limits
- Isolate groups of related operations
- Create reusable components
- Improve code organization and maintainability

[↑ Back to top](#table-of-contents)

## Key features

- **Concurrency unit** - Run multiple operations concurrently within your function
- **Execution isolation** - Child contexts have their own operation namespace
- **Single-unit checkpointing** - Completed child contexts never replay
- **Large data handling** - Process data that exceeds individual step limits
- **Named contexts** - Identify contexts by name for debugging and testing

[↑ Back to top](#table-of-contents)

## Getting started

Here's an example showing why child contexts are useful - they let you group multiple operations that execute as a single unit:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/validate-order.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/validate-order.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/validate-order.java"
    ```


**Why use a child context here?**

Child contexts let you group related operations into a logical unit. Once `process_order` completes, its result is saved just like a step - everything inside won't replay even if the function continues or restarts. This provides organizational benefits and a small optimization by avoiding unnecessary replays.

**Key benefits:**

- **Organization**: Group related operations together for better code structure and readability
- **Reusability**: Call `process_order` multiple times in the same function, and each execution is tracked independently
- **Isolation**: Child contexts act like checkpointed functions - once done, they're done

[↑ Back to top](#table-of-contents)

## Method signatures

### context.run_in_child_context()

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/run-in-child-context-signature.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/run-in-child-context-signature.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/run-in-child-context-signature.java"
    ```


**Parameters:**

- `func` - A callable that receives a `DurableContext` and returns a result. Use the `@durable_with_child_context` decorator to create context functions.
- `name` (optional) - A name for the child context, useful for debugging and testing

**Returns:** The result of executing the context function.

**Raises:** Any exception raised by the context function.

### @durable_with_child_context decorator

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/decorator-example.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/decorator-example.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/decorator-example.java"
    ```


The decorator wraps your function so it can be called with arguments and passed to `context.run_in_child_context()`.

[↑ Back to top](#table-of-contents)

## Using the @durable_with_child_context decorator

The `@durable_with_child_context` decorator marks a function as a context function. Context functions receive a `DurableContext` as their first parameter and can execute any durable operations:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/process-order.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/process-order.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/process-order.java"
    ```


**Why use @durable_with_child_context?**

The decorator wraps your function so it can be called with arguments and passed to `context.run_in_child_context()`. It provides a convenient way to define reusable workflow components.

[↑ Back to top](#table-of-contents)

## Naming child contexts

You can name child contexts explicitly using the `name` parameter. Named contexts are easier to identify in logs and tests:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/data-processing.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/data-processing.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/data-processing.java"
    ```


**Naming best practices:**

- Use descriptive names that explain what the context does
- Keep names consistent across your codebase
- Use names when you need to inspect specific contexts in tests
- Names help with debugging and monitoring

[↑ Back to top](#table-of-contents)

## Use cases for isolation

### Organizing complex workflows

Use child contexts to organize complex workflows into logical units:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/inventory-check.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/inventory-check.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/inventory-check.java"
    ```


### Creating reusable components

Child contexts make it easy to create reusable workflow components:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/send-notifications.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/send-notifications.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/send-notifications.java"
    ```


[↑ Back to top](#table-of-contents)

## Advanced patterns

### Conditional child contexts

Execute child contexts based on conditions:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/standard-processing.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/standard-processing.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/standard-processing.java"
    ```


### Error handling in child contexts

Handle errors within child contexts:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/risky-operation.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/risky-operation.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/risky-operation.java"
    ```


### Sequential child contexts

Execute multiple child contexts sequentially:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/process-region-a.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/process-region-a.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/process-region-a.java"
    ```


For parallel execution, use `context.parallel()` instead. See [Parallel operations](parallel.md) for details.

[↑ Back to top](#table-of-contents)

## Best practices

**Use child contexts for logical grouping** - Group related operations together in a child context to improve code organization and readability.

**Name contexts descriptively** - Use clear names that explain what the context does. This helps with debugging and testing.

**Keep context functions focused** - Each context function should have a single, well-defined purpose. Don't create overly complex context functions.

**Use child contexts for large data** - When processing data that exceeds step size limits, break it into multiple steps within a child context.

**Create reusable components** - Design context functions that can be reused across different workflows.

**Handle errors appropriately** - Decide whether to handle errors within the child context or let them propagate to the parent.

**Pass data through parameters** - Pass data to child contexts through function parameters, not global variables.

**Document context functions** - Add docstrings explaining what the context does and what it returns.

**Test context functions independently** - Write tests for individual context functions to ensure they work correctly in isolation.

[↑ Back to top](#table-of-contents)

## FAQ

**Q: What's the difference between a child context and a step?**

A: A step is a single operation that checkpoints its result. A child context is a collection of operations (steps, waits, callbacks, etc.) that execute in an isolated scope. The entire child context result is checkpointed as a single unit in the parent context.

**Q: Can I use steps inside child contexts?**

A: Yes, child contexts can contain any durable operations: steps, waits, and callbacks.

**Q: When should I use a child context vs multiple steps?**

A: Use child contexts when you want to:
- Group related operations logically
- Create reusable workflow components
- Handle data larger than step size limits
- Isolate operations from the parent context

Use multiple steps when operations are independent and don't need isolation.

**Q: Can child contexts access the parent context?**

A: No, child contexts receive their own `DurableContext` instance. They can't access the parent context directly. Pass data through function parameters.

**Q: What happens if a child context fails?**

A: If an operation within a child context raises an exception, the exception propagates to the parent context unless you handle it within the child context.

**Q: Can I create multiple child contexts in one function?**

A: Yes, you can create as many child contexts as needed. They execute sequentially by default. For parallel execution, use `context.parallel()` instead.

**Q: Can I use callbacks in child contexts?**

A: Yes, child contexts support all durable operations including callbacks, waits, and steps.

**Q: Can I pass large data to child contexts?**

A: Yes, but be mindful of Lambda payload limits. If data is very large, consider storing it externally (S3, DynamoDB) and passing references.

**Q: Do child contexts share the same logger?**

A: Yes, the logger is inherited from the parent context, but you can access it through the child context's `ctx.logger`.

[↑ Back to top](#table-of-contents)

## Testing

You can test child contexts using the testing SDK. The test runner executes your function and lets you inspect child context results.

### Basic child context testing

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/basic-child-context-testing.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/basic-child-context-testing.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/basic-child-context-testing.java"
    ```


### Inspecting child context operations

Use `result.get_context()` to inspect child context results:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/inspect-child-context-operations.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/inspect-child-context-operations.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/inspect-child-context-operations.java"
    ```


### Testing large data handling

Test that child contexts handle large data correctly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/test-large-data-handling.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/test-large-data-handling.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/test-large-data-handling.java"
    ```




### Testing error handling

Test that child contexts handle errors correctly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/child-contexts/test-error-handling.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/child-contexts/test-error-handling.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/child-contexts/test-error-handling.java"
    ```


For more testing patterns, see:
- [Basic tests](../testing-patterns/basic-tests.md) - Simple test examples
- [Complex workflows](../testing-patterns/complex-workflows.md) - Multi-step workflow testing
- [Best practices](../best-practices.md) - Testing recommendations

[↑ Back to top](#table-of-contents)

## See also

- [Steps](steps.md) - Use steps within child contexts
- [Wait operations](wait.md) - Use waits within child contexts
- [Callbacks](callbacks.md) - Use callbacks within child contexts
- [Parallel operations](parallel.md) - Execute child contexts in parallel
- [Examples](https://github.com/awslabs/aws-durable-execution-sdk-python/tree/main/examples/src/run_in_child_context) - More child context examples

[↑ Back to top](#table-of-contents)

## License

See the LICENSE file for our project's licensing.

[↑ Back to top](#table-of-contents)
