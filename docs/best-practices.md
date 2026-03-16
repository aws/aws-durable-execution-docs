# Best Practices

## Table of Contents

- [Overview](#overview)
- [Function design](#function-design)
- [Timeout configuration](#timeout-configuration)
- [Naming conventions](#naming-conventions)
- [Performance optimization](#performance-optimization)
- [Serialization](#serialization)
- [Common mistakes](#common-mistakes)
- [Code organization](#code-organization)
- [FAQ](#faq)
- [See also](#see-also)

[← Back to main index](index.md)

## Overview

This guide covers best practices for building reliable, maintainable durable functions. You'll learn how to design functions that are easy to test, debug, and maintain in production.

[↑ Back to top](#table-of-contents)

## Function design

### Keep functions focused

Each durable function should have a single, clear purpose. Focused functions are easier to test, debug, and maintain. They also make it simpler to understand execution flow and identify failures.

**Good:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-focused-function.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-focused-function.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-focused-function.java"
    ```


**Avoid:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-unfocused-function.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-unfocused-function.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-unfocused-function.java"
    ```


### Wrap non-deterministic code in steps

All non-deterministic operations must be wrapped in steps:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/wrap-non-deterministic.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/wrap-non-deterministic.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/wrap-non-deterministic.java"
    ```


**Why:** Non-deterministic code produces different values on replay, breaking state consistency.


### Use @durable_step for reusable functions

Decorate functions with `@durable_step` to get automatic naming, better code organization, and cleaner syntax. This makes your code more maintainable and easier to test.

**Good:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-durable-step.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-durable-step.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-durable-step.java"
    ```


**Avoid:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-inline-step.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-inline-step.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-inline-step.java"
    ```


### Don't share state between steps

Pass data through return values, not global variables or class attributes. Global state breaks on replay because steps return cached results, but global variables reset to their initial values.

**Good:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-pass-data.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-pass-data.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-pass-data.java"
    ```


**Avoid:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-modifying-mutable.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-modifying-mutable.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-modifying-mutable.java"
    ```


### Choose the right execution semantics

Use at-most-once semantics for operations with side effects (payments, emails, database writes) to prevent duplicate execution. Use at-least-once (default) for idempotent operations that are safe to retry.

**At-most-once for side effects:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/at-most-once-semantics.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/at-most-once-semantics.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/at-most-once-semantics.java"
    ```


**At-least-once for idempotent operations:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/at-least-once-semantics.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/at-least-once-semantics.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/at-least-once-semantics.java"
    ```


### Handle errors explicitly

Catch and handle exceptions in your step functions. Distinguish between transient failures (network issues, rate limits) that should retry, and permanent failures (invalid input, not found) that shouldn't.

**Good:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-error-handling.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-error-handling.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-error-handling.java"
    ```


**Avoid:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-generic-errors.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-generic-errors.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-generic-errors.java"
    ```


[↑ Back to top](#table-of-contents)

## Timeout configuration

### Set realistic timeouts

Choose timeout values based on expected execution time plus buffer for retries and network delays. Too short causes unnecessary failures; too long wastes resources waiting for operations that won't complete.

**Good:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-realistic-timeout.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-realistic-timeout.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-realistic-timeout.java"
    ```


**Avoid:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-unrealistic-timeout.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-unrealistic-timeout.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-unrealistic-timeout.java"
    ```


### Use heartbeat timeouts for long operations

Enable heartbeat monitoring for callbacks that take more than a few minutes. Heartbeats detect when external systems stop responding, preventing you from waiting the full timeout period.

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/heartbeat-timeouts.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/heartbeat-timeouts.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/heartbeat-timeouts.java"
    ```


Without heartbeat monitoring, you'd wait the full 24 hours even if the external system crashes after 10 minutes.

### Configure retry delays appropriately

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/retry-delays.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/retry-delays.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/retry-delays.java"
    ```


[↑ Back to top](#table-of-contents)

## Naming conventions

### Use descriptive operation names

Choose names that explain what the operation does, not how it does it. Good names make logs easier to read and help you identify which operation failed.

**Good:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-descriptive-names.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-descriptive-names.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-descriptive-names.java"
    ```


**Avoid:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-vague-names.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-vague-names.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-vague-names.java"
    ```


### Use consistent naming patterns

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/consistent-naming-patterns.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/consistent-naming-patterns.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/consistent-naming-patterns.java"
    ```


### Name dynamic operations with context

Include context when creating operations in loops:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/dynamic-operation-names.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/dynamic-operation-names.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/dynamic-operation-names.java"
    ```


[↑ Back to top](#table-of-contents)

## Performance optimization

### Minimize checkpoint size

Keep operation inputs and results small. Large payloads increase checkpoint overhead, slow down execution, and can hit size limits. Store large data in S3 and pass references instead.

**Good:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-minimize-checkpoint.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-minimize-checkpoint.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-minimize-checkpoint.java"
    ```


**Avoid:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-too-many-steps.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-too-many-steps.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-too-many-steps.java"
    ```


### Batch operations when possible

Group related operations to reduce checkpoint overhead. Each step creates a checkpoint, so batching reduces API calls and speeds up execution.

**Good:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-batch-operations.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-batch-operations.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-batch-operations.java"
    ```


**Avoid:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-individual-operations.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-individual-operations.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-individual-operations.java"
    ```


### Use parallel operations for independent work

Execute independent operations concurrently to reduce total execution time. Use `context.parallel()` to run multiple operations at the same time.

**Good:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-parallel-operations.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-parallel-operations.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-parallel-operations.java"
    ```


**Avoid:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-unnecessary-waits.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-unnecessary-waits.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-unnecessary-waits.java"
    ```


### Avoid unnecessary waits

Only use waits when you need to delay execution:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-unnecessary-waits.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-unnecessary-waits.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-unnecessary-waits.java"
    ```


[↑ Back to top](#table-of-contents)

## Serialization

### Use JSON-serializable types

The SDK uses JSON serialization by default for checkpoints. Stick to JSON-compatible types (dict, list, str, int, float, bool, None) for operation inputs and results.

**Good:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-json-serializable.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-json-serializable.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-json-serializable.java"
    ```


**Avoid:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-non-serializable.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-non-serializable.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-non-serializable.java"
    ```


### Convert non-serializable types

Convert complex types to JSON-compatible formats before returning from steps:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/convert-non-serializable.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/convert-non-serializable.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/convert-non-serializable.java"
    ```


### Use custom serialization for complex types

For complex objects, implement custom serialization or use the SDK's SerDes system:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/custom-serialization.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/custom-serialization.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/custom-serialization.java"
    ```


[↑ Back to top](#table-of-contents)

## Common mistakes

### ⚠️ Modifying mutable objects between steps

**Wrong:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-modifying-mutable.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-modifying-mutable.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-modifying-mutable.java"
    ```


**Right:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-immutable-data.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-immutable-data.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-immutable-data.java"
    ```


### ⚠️ Using context inside its own operations

**Wrong:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-context-in-operation.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-context-in-operation.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-context-in-operation.java"
    ```


**Right:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-nested-step.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-nested-step.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-nested-step.java"
    ```


**Why:** You can't use a context object inside its own operations (like calling `context.step()` inside another `context.step()`). Use child contexts to create isolated execution scopes for nested operations.

### ⚠️ Forgetting to handle callback timeouts

**Wrong:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-no-callback-timeout.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-no-callback-timeout.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-no-callback-timeout.java"
    ```


**Right:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-callback-timeout.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-callback-timeout.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-callback-timeout.java"
    ```


### ⚠️ Creating too many small steps

**Wrong:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/avoid-too-many-steps.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/avoid-too-many-steps.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/avoid-too-many-steps.java"
    ```


**Right:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-calculate-result.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-calculate-result.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-calculate-result.java"
    ```


### ⚠️ Not using retry for transient failures

**Right:**

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/good-retry-transient.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/good-retry-transient.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/good-retry-transient.java"
    ```


[↑ Back to top](#table-of-contents)

## Code organization

### Separate business logic from orchestration

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/separate-business-logic.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/separate-business-logic.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/separate-business-logic.java"
    ```


### Use child contexts for complex workflows

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/use-child-contexts.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/use-child-contexts.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/use-child-contexts.java"
    ```


### Group related configuration

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/best-practices/group-related-config.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/best-practices/group-related-config.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/best-practices/group-related-config.java"
    ```


[↑ Back to top](#table-of-contents)

## FAQ

**Q: How many steps should a durable function have?**

A: There's a limit of 3,000 operations per execution. Keep in mind that more steps mean more API operations and longer execution time. Balance granularity with performance - group related operations when it makes sense, but don't hesitate to break down complex logic into steps.

**Q: Should I create a step for every function call?**

A: No. Only create steps for operations that need checkpointing, retry logic, or isolation.

**Q: Can I use async/await in durable functions?**

A: Functions decorated with `@durable_step` must be synchronous. If you need to call async code, use `asyncio.run()` inside your step to execute it synchronously.

**Q: How do I handle secrets and credentials?**

A: Use AWS Secrets Manager or Parameter Store. Fetch secrets in a step at the beginning of your workflow.

**Q: What's the maximum execution time for a durable function?**

A: Durable functions can run for days or weeks using waits and callbacks. Each individual Lambda invocation is still subject to the 15-minute Lambda timeout.

**Q: How do I test durable functions locally?**

A: Use the testing SDK (`aws-durable-execution-sdk-python-testing`) to run functions locally without AWS credentials. See [Testing patterns](testing-patterns/basic-tests.md) for examples.

**Q: How do I monitor durable functions in production?**

A: Use CloudWatch Logs for execution logs, CloudWatch Metrics for performance metrics, and X-Ray for distributed tracing.

[↑ Back to top](#table-of-contents)

## See also

- [Getting started](getting-started.md) - Build your first durable function
- [Steps](core/steps.md) - Step operations
- [Error handling](advanced/error-handling.md) - Handle failures
- [Testing patterns](testing-patterns/basic-tests.md) - How to test your functions

[↑ Back to top](#table-of-contents)
