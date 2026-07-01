# Retry Strategies

## Retry suspends invocation

When a step throws an exception, the SDK uses the step's retry strategy to define the
retry behaviour. When the strategy logic requires a retry, the SDK checkpoints the error
and the scheduled resume time, and then ends the Lambda invocation. The backend starts a
new invocation for the execution at the scheduled resume time and the SDK replays the
step body.

Retries do not consume Lambda execution time while waiting for the next retry.

When a step exhausts all retry attempts, the SDK checkpoints the final error and throws
it to your handler. If you configure no retry strategy on a step, the SDK applies a default strategy
with up to 5 retries (6 total attempts). See [Retry presets](#retry-presets).

## Configure a retry strategy

A retry strategy is a function that takes the error and the current attempt number, and
returns a decision. The decision is either to retry with a given delay, or to stop. You
can write a retry strategy directly yourself or use one of the built-in helpers to build
a ready-made strategy from configuration. The SDK ships helpers for exponential backoff
and linear backoff.

### Exponential backoff

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

=== "C#"

    Use `RetryStrategy.Exponential(...)` to build an `IRetryStrategy`, then set it on
    `StepConfig.RetryStrategy`.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/error-handling/exponential-backoff.cs"
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
    the `RetryStrategy` lambda. See [Retrying specific errors](#retry-only-specific-errors).

=== "C#"

    ```csharp
    --8<-- "examples/csharp/sdk-reference/error-handling/retry-strategy-config-signature.cs"
    ```

    **Parameters:**

    - `maxAttempts` (optional) Total attempts including the initial attempt. Default: `3`.
    - `initialDelay` (optional) A `TimeSpan`. Must be positive. Default:
        `TimeSpan.FromSeconds(5)`.
    - `maxDelay` (optional) A `TimeSpan`. Must be positive and `>= initialDelay`. Default:
        `TimeSpan.FromSeconds(300)`.
    - `backoffRate` (optional) Multiplier applied to the delay on each retry. Must be
        `>= 1.0`. Default: `2.0`.
    - `jitter` (optional) A `JitterStrategy` value. Default: `JitterStrategy.Full`.
    - `retryableExceptions` (optional) An array of exception `Type`s. The SDK retries only
        exceptions assignable to one of these types.
    - `retryableMessagePatterns` (optional) An array of regex strings matched against the
        exception message. When you set neither filter, the SDK retries all exceptions.
        When you set both, the SDK retries an exception if it matches either (OR logic).

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

=== "C#"

    ```csharp
    --8<-- "examples/csharp/sdk-reference/error-handling/jitter-strategy-signature.cs"
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

### Linear backoff

Linear backoff grows the delay by a fixed `increment` on each attempt instead of
multiplying by a backoff rate. Use it when you want predictable, bounded growth between
retries rather than the rapid expansion of exponential backoff.

=== "TypeScript"

    Use `createLinearRetryStrategy()` to build a strategy, then pass it as
    `retryStrategy` in `StepConfig`.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/linear-retry-strategy.ts"
    ```

=== "Python"

    Use `create_linear_retry_strategy()` with a `LinearRetryStrategyConfig`, then pass it
    as `retry_strategy` in `StepConfig`.

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/linear-retry-strategy.py"
    ```

=== "Java"

    Use `RetryStrategies.linearBackoff()` to build a strategy, then pass it to
    `StepConfig.builder().retryStrategy()`.

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/linear-retry-strategy.java"
    ```

=== "C#"

    The .NET SDK ships no built-in linear strategy. Use `RetryStrategy.FromDelegate` and
    compute a linearly growing delay yourself, then set it on `StepConfig.RetryStrategy`.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/error-handling/linear-retry-strategy.cs"
    ```

#### LinearRetryStrategyConfig signature

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/linear-retry-strategy-config-signature.ts"
    ```

    **Parameters:**

    - `maxAttempts` (optional) Total attempts including the initial attempt. Default: `6`.
    - `initialDelay` (optional) Delay before the first retry. Default: `{ seconds: 1 }`.
    - `increment` (optional) Amount added to the delay on each retry. Default:
        `{ seconds: 1 }`.
    - `maxDelay` (optional) Maximum delay between retries. Default: `{ minutes: 5 }`.
    - `jitter` (optional) A `JitterStrategy` value. Default: `JitterStrategy.FULL`.
    - `retryableErrors` (optional) Array of strings or `RegExp` patterns matched against
        the error message. The SDK retries all errors when you set neither
        `retryableErrors` nor `retryableErrorTypes`.
    - `retryableErrorTypes` (optional) Array of error classes. The SDK retries only
        errors that are instances of these classes. When you set both filters, the SDK
        retries an error if it matches either (OR logic).

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/linear-retry-strategy-config-signature.py"
    ```

    **Parameters:**

    - `max_attempts` (optional) Total attempts including the initial attempt. Default: `6`.
    - `initial_delay` (optional) A `Duration`. Default: `Duration.from_seconds(1)`.
    - `increment` (optional) Amount added to the delay on each retry. Default:
        `Duration.from_seconds(1)`.
    - `max_delay` (optional) A `Duration`. Default: `Duration.from_minutes(5)`.
    - `jitter_strategy` (optional) A `JitterStrategy` value. Default:
        `JitterStrategy.FULL`.
    - `retryable_errors` (optional) List of strings or compiled `re.Pattern` objects
        matched against the error message. The SDK retries all errors when you set
        neither `retryable_errors` nor `retryable_error_types`.
    - `retryable_error_types` (optional) List of exception classes. The SDK retries only
        exceptions that are instances of these classes. When you set both filters, the
        SDK retries an error if it matches either (OR logic).

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/linear-retry-strategy-config-signature.java"
    ```

    **Parameters:**

    - `maxAttempts` Total attempts including the initial attempt.
    - `initialDelay` A `java.time.Duration`. Minimum 1 second.
    - `maxDelay` A `java.time.Duration`. Minimum 1 second. Caps the calculated delay.
    - `increment` A `java.time.Duration` added to the delay on each retry.
    - `jitter` A `JitterStrategy` value. The three-argument overload omits both
        `maxDelay` and `jitter`.

=== "C#"

    There is no linear config type in .NET. Build the delay from `RetryStrategy.FromDelegate`:

    ```csharp
    --8<-- "examples/csharp/sdk-reference/error-handling/linear-retry-strategy-config-signature.cs"
    ```

    The delegate receives the failing exception and the 1-based attempt number and returns
    a `RetryDecision`. Return `RetryDecision.DoNotRetry()` to stop, or
    `RetryDecision.RetryAfter(delay)` to retry after a computed delay
    (`initialDelay + increment * (attempt - 1)`, capped at your own `maxDelay`).

#### Delay calculation

Linear backoff calculates the delay before each retry as:

```
base_delay = min(initial_delay + increment × (attempt - 1), max_delay)
final_delay = jitter(base_delay), minimum 1 second
```

The same `JitterStrategy` values apply: `FULL`, `HALF`, and `NONE`.

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

=== "C#"

    ```csharp
    --8<-- "examples/csharp/sdk-reference/error-handling/retry-strategy-signature.cs"
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

=== "C#"

    Use `RetryStrategy.FromDelegate((error, attempt) => ...)`. Return
    `RetryDecision.DoNotRetry()` to stop, or `RetryDecision.RetryAfter(delay)` to retry.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/error-handling/custom-retry-strategy.cs"
    ```

## Retry presets

The SDK ships with preset strategies for common cases:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/retry-presets.ts"
    ```

    **`retryPresets.default`** 6 attempts, 5s initial delay, 60s max, 2x backoff, full
    jitter.

    **`retryPresets.linear`** 6 attempts with linear delays of 1s, 2s, 3s, 4s, 5s and no
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

    **`RetryPresets.linear()`** 6 attempts with linear delays of 1s, 2s, 3s, 4s, 5s and no
    jitter.

    **`RetryPresets.fixed(interval)`** 5 attempts at a constant interval. Defaults to a
    5 second interval. Pass a `Duration` to override.

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/retry-presets.java"
    ```

    **`RetryStrategies.Presets.DEFAULT`** 6 attempts, 5s initial delay, 60s max, 2x backoff,
    full jitter.

    **`RetryStrategies.Presets.LINEAR`** 6 attempts with linear delays capped at 5 seconds
    and no jitter.

    **`RetryStrategies.Presets.NO_RETRY`** Fails immediately on first error.

=== "C#"

    ```csharp
    --8<-- "examples/csharp/sdk-reference/error-handling/retry-presets.cs"
    ```

    **`RetryStrategy.Default`** 6 attempts, 5s initial delay, 60s max, 2x backoff, full
    jitter.

    **`RetryStrategy.Transient`** 3 attempts, 1s initial delay, 5s max, 2x backoff, half
    jitter.

    **`RetryStrategy.None`** 1 attempt, fails immediately on error.

## Retry any durable operation

Use the `withRetry` helper to wrap any durable operation in a replay-safe retry loop.
The `withRetry` helper extends the same `RetryStrategy` configuration capability
available to `step` to other operations, such as `invoke`, `waitForCallback`, and
`waitForCondition`.

=== "TypeScript"

    `withRetry(context, name?, func, config)` runs `func` and retries it on failure.
    The function receives the durable context and the 1-based attempt number. By
    default the loop is wrapped in `runInChildContext` so all attempts group under one
    operation in execution history.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/with-retry-helper.ts"
    ```

=== "Python"

    `with_retry(context, func, config, name=None)` runs `func` and retries it on
    failure. The function receives the durable context and the 1-based attempt number.
    By default the loop is wrapped in `run_in_child_context` so all attempts group
    under one operation in execution history.

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/with-retry-helper.py"
    ```

=== "Java"

    `DurableContext.withRetry(name, operation, config)` runs `operation` and retries
    it on failure. The `BiFunction` receives the 1-based attempt number first and the
    durable context second. An async overload, `withRetryAsync`, returns a
    `DurableFuture<T>` for parallel use.

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/with-retry-helper.java"
    ```

=== "C#"

    The .NET SDK has no separate `withRetry` helper, and `InvokeConfig` does not accept a
    retry strategy. To retry another durable operation, wrap it in a `step` and set the
    retry strategy on `StepConfig.RetryStrategy`. The step retries the wrapped operation
    with backoff between failed attempts.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/error-handling/with-retry-helper.cs"
    ```

The `withRetry` helper wraps the retry loop in a child context and uses `context.wait`
between attempts to suspend the invocation while waiting for the retry interval. The
child context, the wait operations, and any operations inside each attempt count toward
the durable operations the execution consumes. See
[AWS Lambda service quotas](https://docs.aws.amazon.com/general/latest/gr/lambda-service.html).

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

=== "C#"

    Pass `retryableExceptions` to `RetryStrategy.Exponential(...)` to retry only those
    exception types (and their subclasses). Every other exception fails immediately.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/error-handling/retry-specific-errors.cs"
    ```

## See also

- [Errors](errors.md)
- [Steps](../operations/step.md)
