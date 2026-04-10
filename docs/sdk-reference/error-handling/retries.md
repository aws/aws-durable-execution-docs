# Retry Strategies

## Retry suspends invocation

When a step throws an exception, the SDK uses the step's retry strategy to define the
retry behaviour. When the strategy logic requires a retry, the SDK checkpoints the error
and the scheduled resume time, and then ends the Lambda invocation. The backend starts a
new invocation for the execution at the scheduled resume time and the SDK replays the
step body.

Retries do not consume Lambda execution time while waiting for the next retry.

When a step exhausts all retry attempts, the SDK checkpoints the final error and throws
it to your handler. If you configure no retry strategy on a step, any exception
propagates immediately without retrying.

## Configure a retry strategy

A retry strategy is a function that takes the error and the current attempt number, and
returns a decision. The decision is either to retry with a given delay, or to stop. You
can write a retry strategy directly yourself or use the built-in helper to build a
ready-made retry strategy from configuration.

### RetryStrategy helper

=== "TypeScript"

    Use `createRetryStrategy()` to build a strategy, then pass it as `retryStrategy` in
    `StepConfig`.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/exponential-backoff.ts"
    ```

=== "Python"

    Use `create_retry_strategy()` with a `RetryStrategyConfig`, then pass it as
    `retry_strategy` in `StepConfig`.

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/exponential-backoff.py"
    ```

=== "Java"

    Use `RetryStrategies.exponentialBackoff()` to build a strategy, then pass it to
    `StepConfig.builder().retryStrategy()`.

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/exponential-backoff.java"
    ```

#### RetryStrategyConfig signature

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/retry-strategy-config-signature.ts"
    ```

    **Parameters:**

    - `maxAttempts` (optional) Total attempts including the initial attempt. Default: `3`.
    - `initialDelay` (optional) Delay before the first retry. Default: `{ seconds: 5 }`.
    - `maxDelay` (optional) Maximum delay between retries. Default: `{ minutes: 5 }`.
    - `backoffRate` (optional) Multiplier applied to the delay on each retry. Default: `2`.
    - `jitter` (optional) A `JitterStrategy` value. Default: `JitterStrategy.FULL`.
    - `retryableErrors` (optional) Array of strings or `RegExp` patterns matched against the
        error message. The SDK retries all errors when you set neither `retryableErrors` nor
        `retryableErrorTypes`.
    - `retryableErrorTypes` (optional) Array of error classes. The SDK retries only errors
        that are instances of these classes. When you set both filters, the SDK retries an
        error if it matches either (OR logic).

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/retry-strategy-config-signature.py"
    ```

    **Parameters:**

    - `max_attempts` (optional) Total attempts including the initial attempt. Default: `3`.
    - `initial_delay` (optional) A `Duration`. Default: `Duration.from_seconds(5)`.
    - `max_delay` (optional) A `Duration`. Default: `Duration.from_minutes(5)`.
    - `backoff_rate` (optional) Multiplier applied to the delay on each retry. Default:
        `2.0`.
    - `jitter_strategy` (optional) A `JitterStrategy` value. Default: `JitterStrategy.FULL`.
    - `retryable_errors` (optional) List of strings or compiled `re.Pattern` objects matched
        against the error message. The SDK retries all errors when you set neither
        `retryable_errors` nor `retryable_error_types`.
    - `retryable_error_types` (optional) List of exception classes. The SDK retries only
        exceptions that are instances of these classes. When you set both filters, the SDK
        retries an error if it matches either (OR logic).

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/retry-strategy-config-signature.java"
    ```

    **Parameters:**

    - `maxAttempts` Total attempts including the initial attempt.
    - `initialDelay` A `java.time.Duration`. Minimum 1 second.
    - `maxDelay` A `java.time.Duration`. Minimum 1 second.
    - `backoffRate` Multiplier applied to the delay on each retry.
    - `jitter` A `JitterStrategy` value: `FULL`, `HALF`, or `NONE`.

    Java does not have built-in error type filtering. Filter by error type manually inside
    the `RetryStrategy` lambda. See [Retrying specific errors](#retrying-specific-errors).

#### JitterStrategy

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/jitter-strategy-signature.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/jitter-strategy-signature.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/jitter-strategy-signature.java"
    ```

#### Delay calculation

The SDK calculates the delay before each retry using exponential backoff with jitter:

```
base_delay = min(initial_delay × backoff_rate ^ (attempt - 1), max_delay)
final_delay = jitter(base_delay), minimum 1 second
```

- `JitterStrategy.FULL` randomizes the delay between 0 and `base_delay`. This spreads
    retries across time and avoids many clients retrying simultaneously after a shared
    failure.
- `JitterStrategy.HALF` randomizes between 50% and 100% of `base_delay`.
- `JitterStrategy.NONE` uses the exact calculated delay.

### Write a custom strategy

You can write your own retry strategy directly. The SDK calls it with the error and the
current attempt number after each failure. The attempt number is one-indexed.

#### RetryStrategy signature

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/retry-strategy-signature.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/retry-strategy-signature.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/retry-strategy-signature.java"
    ```

#### Example

=== "TypeScript"

    Return `{ shouldRetry: false }` to stop, or
    `{ shouldRetry: true, delay: { seconds: N } }` to retry.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/custom-retry-strategy.ts"
    ```

=== "Python"

    Use `RetryDecision.retry(Duration)` or `RetryDecision.no_retry()`.

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/custom-retry-strategy.py"
    ```

=== "Java"

    Use `RetryDecision.retry(Duration)` or `RetryDecision.fail()`.

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/custom-retry-strategy.java"
    ```

## Retry presets

The SDK ships with preset strategies for common cases:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/retry-presets.ts"
    ```

    **`retryPresets.default`** 6 attempts, 5s initial delay, 60s max, 2x backoff, full
    jitter.

    **`retryPresets.noRetry`** 1 attempt, fails immediately on error.

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/retry-presets.py"
    ```

    **`RetryPresets.default()`** 6 attempts, 5s initial delay, 60s max, 2x backoff, full
    jitter.

    **`RetryPresets.none()`** 1 attempt, fails immediately on error.

    **`RetryPresets.transient()`** 3 attempts, 2x backoff, half jitter.

    **`RetryPresets.resource_availability()`** 5 attempts, 5s initial delay, 5 min max, 2x
    backoff.

    **`RetryPresets.critical()`** 10 attempts, 1s initial delay, 60s max, 1.5x backoff, no
    jitter.

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/retry-presets.java"
    ```

    **`RetryStrategies.Presets.DEFAULT`** 6 attempts, 5s initial delay, 60s max, 2x backoff,
    full jitter.

    **`RetryStrategies.Presets.NO_RETRY`** Fails immediately on first error.

## Retry only specific errors

You can retry only certain error types and fail immediately on others.

=== "TypeScript"

    Use `retryableErrorTypes` to specify which error classes to retry.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/retry-specific-errors.ts"
    ```

=== "Python"

    Use `retryable_error_types` to specify which exception classes to retry.

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/retry-specific-errors.py"
    ```

=== "Java"

    `RetryStrategy` is a functional interface. Check the error type in the lambda and return
    `RetryDecision.fail()` for errors you do not want to retry.

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/retry-specific-errors.java"
    ```

## See also

- [Errors](errors.md)
- [Steps](../operations/step.md)
