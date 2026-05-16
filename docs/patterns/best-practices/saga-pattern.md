# Saga Pattern

The Saga pattern is used in multi-step workflows to ensure a graceful rollback to a consistent state in the event of a workflow step failure. 

The core idea is to keep a record of compensating steps for each successful forward step. If there is failure at any step in the workflow, execute compensating steps for all previously completed steps in reverse order. This makes sure that the system can return to a consistent state even when something goes wrong.

Durable functions are well-suited for sagas because:

- Each step is automatically retried on transient failures
- When compensating actions are made durable steps, they are checkpointed and retried
- If Lambda crashes mid-compensation, execution resumes from the last checkpoint

## Example

Consider an order processing workflow with three steps: reserve inventory, charge payment, and fulfill shipment. If the shipment fulfilment step fails, we need to refund the payment and cancel the inventory reservation to avoid holding stock indefinitely.

```
Forward:      reserve inventory → charge payment → fulfill shipment
Failure at fulfill shipment:
Compensate:   refund payment → cancel reservation
```

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/saga-pattern/saga-pattern.ts"
    ```
=== "Python"

    ```python
    --8<-- "examples/python/patterns/saga-pattern/saga-pattern.py"
    ```
=== "Java"

    ```java
    --8<-- "examples/java/patterns/saga-pattern/saga-pattern.java"
    ```

## Parallel Steps

If steps are independent, they can be executed concurrently using the `context.parallel()` method.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/saga-pattern/saga-pattern-parallel.ts"
    ```
=== "Python"

    ```python
    --8<-- "examples/python/patterns/saga-pattern/saga-pattern-parallel.py"
    ```
=== "Java"

    ```java
    --8<-- "examples/java/patterns/saga-pattern/saga-pattern-parallel.java"
    ```

## Idempotency of Compensating Steps

Compensating steps must be idempotent. The SDK retries a durable compensation step if it 
fails due to a transient error, so the step can execute more than once. Design 
compensation logic to handle duplicate execution gracefully.

For example, a step that cancels an inventory reservation should succeed even if the reservation was already cancelled.

!!! warning
    Durable compensation steps can execute more than once hence design them to be idempotent. Cancelling an already-cancelled forward step must not error or produce side effects.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/saga-pattern/idempotent-compensation.ts"
    ```
=== "Python"

    ```python
    --8<-- "examples/python/patterns/saga-pattern/idempotent-compensation.py"
    ```
=== "Java"

    ```java
    --8<-- "examples/java/patterns/saga-pattern/idempotent-compensation.java"
    ```


## Common Pitfalls

**1. Non-durable compensations**

Always wrap compensations in a durable step. Without it, a compensation that fails will not be retried and the failure will be silently lost.

!!! warning
    If a compensation fails outside a durable step, the exception aborts the loop. The failed
    compensation is not retried and all remaining compensations are skipped, leaving
    the system partially rolled back.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/saga-pattern/pitfall1-non-durable.ts"
    ```
=== "Python"

    ```python
    --8<-- "examples/python/patterns/saga-pattern/pitfall1-non-durable.py"
    ```
=== "Java"

    ```java
    --8<-- "examples/java/patterns/saga-pattern/pitfall1-non-durable.java"
    ```

**2. Compensations that depend on non-deterministic data**

The compensations list is rebuilt on every replay because it lives outside any step. The list is always rebuilt in the same order because the steps that add to it always replay in the same order. However, never store non-deterministic data (values computed outside a step like timestamps or random IDs) in the compensation closure.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/patterns/saga-pattern/pitfall2-in-memory-state.ts"
    ```
=== "Python"

    ```python
    --8<-- "examples/python/patterns/saga-pattern/pitfall2-in-memory-state.py"
    ```
=== "Java"

    ```java
    --8<-- "examples/java/patterns/saga-pattern/pitfall2-in-memory-state.java"
    ```

## See Also

- [Steps](../../sdk-reference/operations/step.md)
- [Parallel](../../sdk-reference/operations/parallel.md)
- [Error Handling](../../sdk-reference/error-handling/errors.md)
- [Determinism and Replay](./determinism.md)
- [Idempotency and Retries](./idempotency.md)
