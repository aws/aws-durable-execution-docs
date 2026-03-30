# Get Started

## Installation

Install the SDK using pip:

```console
pip install aws-durable-execution-sdk-python
```

## Quick example

Here's a simple durable function that processes an order:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/index/quick-example.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/index/quick-example.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/index/quick-example.java"
    ```

Each `context.step()` call is checkpointed automatically. If Lambda recycles your execution environment, the function resumes from the last completed step.

## Next steps

- [Key Concepts](key-concepts.md) - Understand the mental model behind durable execution
- [Quick Start](quick-start.md) - Build and test your first durable function
