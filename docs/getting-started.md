# Getting started

## Table of Contents

- [Overview](#overview)
- [The two SDKs](#the-two-sdks)
- [How durable execution works](#how-durable-execution-works)
- [Your development workflow](#your-development-workflow)
- [Quick start](#quick-start)
- [Next steps](#next-steps)

[← Back to main index](index.md)

## Overview

This guide explains the fundamental concepts behind durable execution and how the SDK works. You'll understand:

- The difference between `aws-durable-execution-sdk-python` and `aws-durable-execution-sdk-python-testing`
- How checkpoints and replay enable reliable workflows
- Why your function code runs multiple times but side effects happen once
- The development workflow from writing to testing to deployment

[↑ Back to top](#table-of-contents)

## The two SDKs

The durable execution ecosystem has two separate packages:

### Execution SDK (aws-durable-execution-sdk-python)

This is the **core SDK** that runs in your Lambda functions. It provides:

- `DurableContext` - The main interface for durable operations
- Operations - Steps, waits, callbacks, parallel, map, child contexts
- Decorators - `@durable_execution`, `@durable_step`, etc.
- Configuration - StepConfig, CallbackConfig, retry strategies
- Serialization - How data is saved in checkpoints

Install it in your Lambda deployment package:

```console
pip install aws-durable-execution-sdk-python
```

### Testing SDK (aws-durable-execution-sdk-python-testing)

This is a **separate SDK** for testing your durable functions. It provides:

- `DurableFunctionTestRunner` - Run functions locally without AWS
- `DurableFunctionCloudTestRunner` - Test deployed Lambda functions
- Pytest integration - Fixtures and markers for writing tests
- Result inspection - Examine execution state and operation results

Install it in your development environment only:

```console
pip install aws-durable-execution-sdk-python-testing
```

**Key distinction:** The execution SDK runs in production Lambda. The testing SDK runs on your laptop or CI/CD. They're separate concerns.

[↑ Back to top](#table-of-contents)

## How durable execution works

Let's trace through a simple workflow to understand the execution model:

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
4. `context.wait(Duration.from_seconds(30))` is already complete, execution continues
5. `process_data` executes for the first time
6. Result is checkpointed
7. Function returns the final result

**Key insights:**

- Your function code runs twice, but `fetch_data` only calls the API once
- The wait doesn't block Lambda - your environment can be recycled
- You write linear code that looks synchronous
- The SDK handles all the complexity of state management

[↑ Back to top](#table-of-contents)

## Your development workflow

```mermaid
flowchart LR
    subgraph dev["Development (Local)"]
        direction LR
        A["1. Write Function<br/>aws-durable-execution-sdk-python"]
        B["2. Write Tests<br/>aws-durable-execution-sdk-python-testing"]
        C["3. Run Tests<br/>pytest"]
    end
    
    subgraph prod["Production (AWS)"]
        direction LR
        D["4. Deploy<br/>SAM/CDK/Terraform"]
        E["5. Test in Cloud<br/>pytest --runner-mode=cloud"]
    end
    
    A --> B --> C --> D --> E
    
    style dev fill:#e3f2fd
    style prod fill:#fff3e0
```

Here's how you build and test durable functions:

### 1. Write your function (execution SDK)

Install the execution SDK and write your Lambda handler:

```console
pip install aws-durable-execution-sdk-python
```

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/getting-started/write-function.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/getting-started/write-function.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/getting-started/write-function.java"
    ```

### 2. Test locally (testing SDK)

Install the testing SDK and write tests:

```console
pip install aws-durable-execution-sdk-python-testing
```

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/getting-started/test-locally.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/getting-started/test-locally.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/getting-started/test-locally.java"
    ```

Run tests without AWS credentials:

```console
pytest test_my_function.py
```

### 3. Deploy to Lambda

Package your function with the execution SDK (not the testing SDK) and deploy using your preferred tool (SAM, CDK, Terraform, etc.).

### 4. Test in the cloud (optional)

Run the same tests against your deployed function:

```console
export AWS_REGION=us-west-2
export QUALIFIED_FUNCTION_NAME="MyFunction:$LATEST"
export LAMBDA_FUNCTION_TEST_NAME="my_function"

pytest --runner-mode=cloud test_my_function.py
```

[↑ Back to top](#table-of-contents)

## Quick start

Ready to build your first durable function? Here's a minimal example:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/getting-started/minimal-example.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/getting-started/minimal-example.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/getting-started/minimal-example.java"
    ```

Deploy this to Lambda and you have a durable function. The `greet_user` step is checkpointed automatically.

### Using a custom boto3 Lambda client

If you need to customize the boto3 Lambda client used for durable execution operations (for example, to configure custom endpoints, retry settings, or credentials), you can pass a `boto3_client` parameter to the decorator. The client must be a boto3 Lambda client:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/getting-started/minimal-example.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/getting-started/minimal-example.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/getting-started/minimal-example.java"
    ```

The custom Lambda client is used for all checkpoint and state management operations. If you don't provide a `boto3_client`, the SDK initializes a default Lambda client from your environment.

[↑ Back to top](#table-of-contents)

## Next steps

Now that you've built your first durable function, explore the core features:

**Learn the operations:**
- [Steps](core/steps.md) - Execute code with retry strategies and checkpointing
- [Wait operations](core/wait.md) - Pause execution for seconds, minutes, or hours
- [Callbacks](core/callbacks.md) - Wait for external systems to respond
- [Child contexts](core/child-contexts.md) - Organize complex workflows
- [Parallel operations](core/parallel.md) - Run multiple operations concurrently
- [Map operations](core/map.md) - Process collections in parallel

**Dive deeper:**
- [Error handling](advanced/error-handling.md) - Handle failures and implement retry strategies
- [Testing patterns](testing-patterns/basic-tests.md) - Write effective tests for your workflows
- [Best practices](best-practices.md) - Avoid common pitfalls

[↑ Back to top](#table-of-contents)

## See also

- [Documentation index](index.md) - Browse all guides and examples
- [Architecture diagrams](architecture.md) - Class diagrams and concurrency flows
- [Logger integration](core/logger.md) - Replay-safe structured logging
- [Examples directory](https://github.com/awslabs/aws-durable-execution-sdk-python/tree/main/examples) - More working examples

[↑ Back to top](#table-of-contents)
