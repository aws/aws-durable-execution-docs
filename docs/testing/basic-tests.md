# Basic Test Patterns

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Status checking patterns](#status-checking-patterns)
- [Result verification patterns](#result-verification-patterns)
- [Operation-specific assertions](#operation-specific-assertions)
- [Test organization tips](#test-organization-tips)
- [FAQ](#faq)
- [See also](#see-also)

[← Back to main index](../index.md)

## Overview

When you test durable functions, you need to verify that your function executed
successfully, returned the expected result, and that operations like steps or waits ran
correctly. This document shows you common patterns for writing these tests with simple
assertions using the testing SDK.

The testing SDK (`aws-durable-execution-sdk-python-testing`) provides tools to run and
inspect durable functions locally without deploying to AWS. Use these patterns as
building blocks for your own tests, whether you're checking a simple calculation or
inspecting individual operations.

[↑ Back to top](#table-of-contents)

## Prerequisites

To test durable functions, you need both SDKs installed:

```console
# Install the core SDK (for writing durable functions)
pip install aws-durable-execution-sdk-python

# Install the testing SDK (for testing durable functions)
pip install aws-durable-execution-sdk-python-testing

# Install pytest (test framework)
pip install pytest
```

The core SDK provides the decorators and context for writing durable functions. The
testing SDK provides the test runner and assertions for testing them.

[↑ Back to top](#table-of-contents)

## Project structure

Here's a typical project structure for testing durable functions:

```
my-project/
├── src/
│   ├── __init__.py
│   └── my_function.py          # Your durable function
├── test/
│   ├── __init__.py
│   ├── conftest.py             # Pytest configuration and fixtures
│   └── test_my_function.py     # Your tests
├── requirements.txt
└── pytest.ini
```

**Key files:**

- `src/my_function.py` - Contains your durable function with `@durable_execution`
    decorator
- `test/conftest.py` - Configures the `durable_runner` fixture for pytest
- `test/test_my_function.py` - Contains your test cases using the `durable_runner`
    fixture

**Example conftest.py:**

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/conftest-setup.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/conftest-setup.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/conftest-setup.java"
    ```

[↑ Back to top](#table-of-contents)

## Getting started

Here's a simple durable function:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/simple-function.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/simple-function.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/simple-function.java"
    ```

And here's how you test it:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/simple-test.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/simple-test.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/simple-test.java"
    ```

This test:

1. Marks the test with `@pytest.mark.durable_execution` to configure the runner
2. Uses the `durable_runner` fixture to execute the function
3. Checks the execution status
4. Verifies the final result

[↑ Back to top](#table-of-contents)

## Status checking patterns

### Check for successful execution

The most basic pattern verifies that your function completed successfully:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/check-success.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/check-success.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/check-success.java"
    ```

### Check for expected failures

Test that your function fails correctly when given invalid input:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/check-expected-failures.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/check-expected-failures.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/check-expected-failures.java"
    ```

### Check execution with timeout

Verify that your function completes within the expected time:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/check-timeout.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/check-timeout.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/check-timeout.java"
    ```

[↑ Back to top](#table-of-contents)

## Result verification patterns

### Verify simple return values

Check that your function returns the expected value:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/verify-simple-values.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/verify-simple-values.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/verify-simple-values.java"
    ```

### Verify complex return values

Check specific fields in complex return values:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/verify-complex-values.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/verify-complex-values.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/verify-complex-values.java"
    ```

### Verify list results

Check that your function returns the expected list of values:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/verify-list-results.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/verify-list-results.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/verify-list-results.java"
    ```

[↑ Back to top](#table-of-contents)

## Operation-specific assertions

### Verify step operations

Here's a function with a step:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/steps/add-numbers.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/steps/add-numbers.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/steps/add-numbers.java"
    ```

Check that the step executed and produced the expected result:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/verify-step-operations.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/verify-step-operations.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/verify-step-operations.java"
    ```

### Verify wait operations

Here's a function with a wait:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/wait-function.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/wait-function.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/wait-function.java"
    ```

Check that the wait operation was created with correct timing:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/test-wait-operation.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/test-wait-operation.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/test-wait-operation.java"
    ```

### Verify callback operations

Here's a function that creates a callback:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/callback-function.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/callback-function.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/callback-function.java"
    ```

Check that the callback was created with correct configuration:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/verify-callback-operations.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/verify-callback-operations.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/verify-callback-operations.java"
    ```

### Verify child context operations

Here's a function with a child context:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/child-context-function.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/child-context-function.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/child-context-function.java"
    ```

Check that the child context executed correctly:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/verify-child-context.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/verify-child-context.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/verify-child-context.java"
    ```

### Verify parallel operations

Here's a function with parallel operations:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/parallel-function.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/parallel-function.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/parallel-function.java"
    ```

Check that multiple operations executed in parallel:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/verify-parallel-operations.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/verify-parallel-operations.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/verify-parallel-operations.java"
    ```

[↑ Back to top](#table-of-contents)

## Test organization tips

### Use descriptive test names

Name your tests to clearly describe what they verify:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/descriptive-test-names.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/descriptive-test-names.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/descriptive-test-names.java"
    ```

### Group related tests

Organize tests by feature or functionality:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/group-related-tests.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/group-related-tests.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/group-related-tests.java"
    ```

### Use fixtures for common test data

Create fixtures for test data you use across multiple tests:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/use-fixtures.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/use-fixtures.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/use-fixtures.java"
    ```

### Add docstrings to tests

Document what each test verifies:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/add-docstrings.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/add-docstrings.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/add-docstrings.java"
    ```

### Use parametrized tests for similar cases

Test multiple inputs with the same logic using `pytest.mark.parametrize`:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/parametrized-tests.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/parametrized-tests.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/parametrized-tests.java"
    ```

### Keep tests focused

Each test should verify one specific behavior:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing-patterns/basic-tests/keep-tests-focused.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing-patterns/basic-tests/keep-tests-focused.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing-patterns/basic-tests/keep-tests-focused.java"
    ```

[↑ Back to top](#table-of-contents)

## FAQ

**Q: Do I need to deploy my function to test it?**

A: No, the test runner executes your function locally. You only need to deploy for cloud
testing mode.

**Q: How do I test functions with external dependencies?**

A: Mock external dependencies in your test setup. The test runner executes your function
code as-is, so standard Python mocking works.

**Q: Can I test multiple functions in one test file?**

A: Yes, use different `@pytest.mark.durable_execution` markers for each function you
want to test.

**Q: How do I access operation results?**

A: Use `result.get_step(name)` for steps, or iterate through `result.operations` to find
specific operation types.

**Q: What's the difference between result.result and step.result?**

A: `result.result` is the final return value of your handler function. `step.result` is
the return value of a specific step operation.

**Q: How do I test error scenarios?**

A: Check that `result.status is InvocationStatus.FAILED` and inspect `result.error` for
the error message.

**Q: Can I run tests in parallel?**

A: Yes, use pytest-xdist: `pytest -n auto` to run tests in parallel.

**Q: How do I debug failing tests?**

A: Add print statements or use a debugger. The test runner executes your code locally,
so standard debugging tools work.

**Q: What timeout should I use?**

A: Use a timeout slightly longer than your function's expected execution time. For most
tests, 10-30 seconds is sufficient.

**Q: How do I test functions that use environment variables?**

A: Set environment variables in your test setup or use pytest fixtures to manage them.

[↑ Back to top](#table-of-contents)

## See also

- [Complex workflows](complex-workflows.md) - Testing multi-step workflows
- [Best practices](../best-practices.md) - Testing recommendations
- [Testing modes](../advanced/testing-modes.md) - Local and cloud test execution
- [Steps](../core/steps.md) - Testing step operations
- [Wait operations](../core/wait.md) - Testing wait operations
- [Callbacks](../core/callbacks.md) - Testing callback operations

[↑ Back to top](#table-of-contents)
