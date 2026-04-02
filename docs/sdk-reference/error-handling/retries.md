# Retry strategies

Retry strategies configure how the SDK responds to failures in steps. You control the
number of attempts, delay between retries, backoff rate, and which exceptions trigger a
retry. If no retry strategy is configured on a step, any exception propagates
immediately and fails the execution.

## Creating a retry strategy

Use `createRetryStrategy()` (TypeScript/Python) or
`RetryStrategies.exponentialBackoff()` (Java) to build a strategy, then pass it to
`StepConfig`:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/advanced/error-handling/exponential-backoff.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/advanced/error-handling/exponential-backoff.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/advanced/error-handling/exponential-backoff.java"
    ```

## RetryStrategyConfig parameters

**max_attempts / maxAttempts** Maximum number of attempts including the initial attempt.
Default: 3.

**initial_delay / initialDelay** Delay before the first retry. Default: 5 seconds.

**max_delay / maxDelay** Maximum delay between retries. Default: 5 minutes.

**backoff_rate / backoffRate** Multiplier for exponential backoff. Default: 2.0.

**jitter_strategy / jitter** Jitter strategy to spread retries. Default: `FULL`.

**retryable_errors / retryableErrors** Error message patterns to retry (strings or
regex). Default: matches all errors.

**retryable_error_types / retryableErrorTypes** Exception types to retry. Default: empty
(retries all).

## Retry presets

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/advanced/error-handling/retry-presets.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/advanced/error-handling/retry-presets.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/advanced/error-handling/retry-presets.java"
    ```

## Retrying specific exceptions

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/unreliable-operation.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/unreliable-operation.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/unreliable-operation.java"
    ```

## See also

- [Errors](errors.md)
- [Steps](../operations/step.md)
