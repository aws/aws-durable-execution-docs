# Complex Workflow Testing

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Multi-step workflows](#multi-step-workflows)
- [Nested child contexts](#nested-child-contexts)
- [Parallel operations](#parallel-operations)
- [Error scenarios](#error-scenarios)
- [Timeout handling](#timeout-handling)
- [Polling patterns](#polling-patterns)
- [FAQ](#faq)
- [See also](#see-also)

[← Back to main index](../index.md)

## Overview

When your workflows involve multiple steps, nested contexts, or parallel operations, you need to verify more than just the final result. You'll want to check intermediate states, operation ordering, error handling, and timeout behavior.

This guide shows you how to test workflows that chain operations together, handle errors gracefully, and implement polling patterns.

[↑ Back to top](#table-of-contents)

## Prerequisites

You need both SDKs installed:

```console
pip install aws-durable-execution-sdk-python
pip install aws-durable-execution-sdk-python-testing
pip install pytest
```

If you're new to testing durable functions, start with [Basic test patterns](basic-tests.md) first.

[↑ Back to top](#table-of-contents)

## Multi-step workflows

### Sequential operations


Here's a workflow that processes an order through validation, payment, and fulfillment:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/sequential-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/sequential-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/sequential-workflow.java"
    ```


Verify all steps execute in order:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-sequential-operations.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-sequential-operations.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-sequential-operations.java"
    ```


[↑ Back to top](#table-of-contents)

### Conditional branching

Test different execution paths based on input:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/conditional-branching-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/conditional-branching-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/conditional-branching-workflow.java"
    ```


Test both paths separately:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-conditional-branching.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-conditional-branching.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-conditional-branching.java"
    ```


[↑ Back to top](#table-of-contents)

## Nested child contexts


### Single child context

Child contexts isolate operations:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/single-child-context-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/single-child-context-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/single-child-context-workflow.java"
    ```


Verify the child context executes:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-single-child-context.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-single-child-context.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-single-child-context.java"
    ```


[↑ Back to top](#table-of-contents)

### Multiple child contexts

Use multiple child contexts to organize operations:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/multiple-child-contexts-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/multiple-child-contexts-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/multiple-child-contexts-workflow.java"
    ```


Verify both contexts execute:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-multiple-child-contexts.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-multiple-child-contexts.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-multiple-child-contexts.java"
    ```


[↑ Back to top](#table-of-contents)

## Parallel operations

### Basic parallel execution

Multiple operations execute concurrently:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/basic-parallel-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/basic-parallel-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/basic-parallel-workflow.java"
    ```


Verify all operations execute:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-basic-parallel.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-basic-parallel.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-basic-parallel.java"
    ```


[↑ Back to top](#table-of-contents)

### Processing collections


Process collection items in parallel:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/processing-collections-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/processing-collections-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/processing-collections-workflow.java"
    ```


Verify collection processing:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-processing-collections.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-processing-collections.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-processing-collections.java"
    ```


[↑ Back to top](#table-of-contents)

## Error scenarios

### Expected failures

Test that your workflow fails correctly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/expected-failures-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/expected-failures-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/expected-failures-workflow.java"
    ```


Verify validation failures:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-expected-failures.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-expected-failures.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-expected-failures.java"
    ```


[↑ Back to top](#table-of-contents)

### Retry behavior

Test operations that retry on failure:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/retry-behavior-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/retry-behavior-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/retry-behavior-workflow.java"
    ```


Verify retry succeeds:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-retry-behavior.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-retry-behavior.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-retry-behavior.java"
    ```


[↑ Back to top](#table-of-contents)

### Partial failures

Test workflows where some operations succeed before failure:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/partial-failures-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/partial-failures-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/partial-failures-workflow.java"
    ```


Verify partial execution:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-partial-failures.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-partial-failures.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-partial-failures.java"
    ```


[↑ Back to top](#table-of-contents)

## Timeout handling

### Callback timeouts


Verify callback timeout configuration:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-callback-timeouts.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-callback-timeouts.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-callback-timeouts.java"
    ```


Test callback configuration:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/callback-timeouts-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/callback-timeouts-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/callback-timeouts-workflow.java"
    ```


[↑ Back to top](#table-of-contents)

### Long waits

For workflows with long waits, verify configuration without actually waiting:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/long-waits-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/long-waits-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/long-waits-workflow.java"
    ```


Test completes quickly:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-long-waits.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-long-waits.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-long-waits.java"
    ```


[↑ Back to top](#table-of-contents)

## Polling patterns

### Wait-for-condition

Poll until a condition is met:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/wait-for-condition-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/wait-for-condition-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/wait-for-condition-workflow.java"
    ```


Verify polling behavior:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-wait-for-condition.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-wait-for-condition.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-wait-for-condition.java"
    ```


[↑ Back to top](#table-of-contents)

### Maximum attempts

Test polling respects attempt limits:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/maximum-attempts-workflow.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/maximum-attempts-workflow.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/maximum-attempts-workflow.java"
    ```


Test with unreachable target:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/testing-patterns/complex-workflows/test-maximum-attempts.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/testing-patterns/complex-workflows/test-maximum-attempts.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/testing-patterns/complex-workflows/test-maximum-attempts.java"
    ```


[↑ Back to top](#table-of-contents)

## FAQ

**Q: How do I test workflows with long waits?**

A: The test runner doesn't actually wait. You can verify wait operations are configured correctly without waiting for them to complete.

**Q: Can I test workflows with external API calls?**

A: Yes, but mock external dependencies in your tests. The test runner executes your code locally, so standard Python mocking works.

**Q: What's the best way to test conditional logic?**

A: Write separate tests for each execution path. Use descriptive test names and verify the specific operations that should execute in each path.

**Q: How do I verify operation ordering?**

A: Iterate through `result.operations` and check the order. You can also use operation names to verify specific sequences.

**Q: What timeout should I use?**

A: Use a timeout slightly longer than expected execution time. For most tests, 30-60 seconds is sufficient.

**Q: How do I test error recovery?**

A: Test both the failure case (verify the error is raised) and the recovery case (verify retry succeeds). Use separate tests for each scenario.

[↑ Back to top](#table-of-contents)

## See also

- [Basic test patterns](basic-tests.md) - Simple testing patterns
- [Best practices](../best-practices.md) - Testing recommendations
- [Steps](../core/steps.md) - Step operations
- [Wait operations](../core/wait.md) - Wait operations
- [Callbacks](../core/callbacks.md) - Callback operations
- [Child contexts](../core/child-contexts.md) - Child context operations
- [Parallel operations](../core/parallel.md) - Parallel execution

[↑ Back to top](#table-of-contents)
