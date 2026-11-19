# Retry Strategies

## Retry suspends invocation

When a step throws an exception, the SDK uses the step's retry strategy to define the
retry behaviour. When the strategy logic requires a retry, the SDK checkpoints the error
and the scheduled resume time, and then ends the Lambda invocation. The backend starts a
new invocation for the execution at the scheduled resume time and the SDK replays the
step body.

Retries do not consume Lambda execution time while waiting for the next retry.

A retry decision can also pause the durable execution. Lambda then starts the next
attempt only after an operator resumes the execution. See
[Pause after a failed attempt](#pause-after-a-failed-attempt).

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

=== "Go"

    Build a strategy with `MustNewRetryStrategy` and a `RetryConfig`, then pass it with
    the `WithRetry` step option. `MustNewRetryStrategy` panics for an invalid config.
    `NewRetryStrategy` returns an error instead. The default strategy is
    `ExponentialBackoff`.

    ```go
    --8<-- "examples/go/sdk-reference/error-handling/exponential-backoff.go"
    ```

=== "Rust"

    Build a `durable::builders::RetryStrategyConfig` with
    `RetryStrategyConfig::builder()`, then pass it to the step with
    `.retry_strategy_config(..)`. `build()` cannot fail. The invocation suspends during
    a retry delay when all remaining SDK work is blocked. A retry that becomes due
    while SDK work continues starts in the same invocation.

    ```rust
    --8<-- "examples/rust/sdk-reference/error-handling/exponential-backoff.rs"
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
    - `pauseEvery` (optional) Pauses the durable execution after every
        `pauseEvery`th failed attempt that the strategy retries. Use a positive integer.
        Default: never pauses. See
        [Pause after a failed attempt](#pause-after-a-failed-attempt).

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
    - `pause_every` (optional, keyword-only) Pauses the durable execution
        after every `pause_every`th failed attempt that the strategy retries. Use a
        positive integer. Default: `None`, which never pauses. See
        [Pause after a failed attempt](#pause-after-a-failed-attempt).

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

=== "Go"

    The type is `RetryConfig`. The zero value of each field selects its default. The
    zero value of the whole struct is not the `ExponentialBackoff` preset.

    ```go
    --8<-- "examples/go/sdk-reference/error-handling/retry-strategy-config-signature.go"
    ```

    **Fields:**

    - `MaxAttempts` Total attempts including the first. Default: `3`. Must not be
        negative. The zero value selects the default. Use `NoRetry()` or
        `MaxAttempts: 1` for a single attempt.
    - `InitialDelay` A `time.Duration`. Default: `5 * time.Second`. At least 1 second
        when set.
    - `MaxDelay` A `time.Duration`. Default: `5 * time.Minute`. At least 1 second when
        set.
    - `BackoffRate` Multiplier applied to the delay on each retry. Default: `2`. Must be
        finite and not negative.
    - `Jitter` A `JitterStrategy` value. Default: `JitterFull`. When set, it must be one
        of the defined constants.
    - `RetryableErrors` A slice of `ErrorMatcher`. When empty, every error is retryable.
        Entries must not be nil. An error that no matcher accepts fails the step on
        that attempt. See [Retrying specific errors](#retry-only-specific-errors).
    - `PauseEvery` Pauses the durable execution after every `PauseEvery`th failed
        attempt that the strategy retries. Default: `0`, which never pauses. The SDK
        does not validate it. See
        [Pause after a failed attempt](#pause-after-a-failed-attempt).

    `NewRetryStrategy()` returns one joined error that names every invalid field. The
    strategy rounds each delay to the nearest whole second, with a minimum of 1 second.

=== "Rust"

    Set each field with a method of `RetryStrategyConfigBuilder`, which
    `RetryStrategyConfig::builder()` returns. `build()` fills every field you leave
    unset with its default.

    ```rust
    --8<-- "examples/rust/sdk-reference/error-handling/retry-strategy-config-signature.rs"
    ```

    **Methods:**

    - `max_attempts` Total attempts including the initial attempt. Default: `3`, or `6`
        with a linear backoff.
    - `initial_delay` A `std::time::Duration`. Default: 5 seconds, or 1 second with a
        linear backoff.
    - `max_delay` A `Duration` that caps the delay before jitter. Default: 5 minutes.
    - `backoff_rate` Multiplier applied to the delay on each retry. It sets
        `Backoff::exponential(rate)`. Default: `2.0`.
    - `backoff` A `Backoff` value, exponential or linear. `backoff` and `backoff_rate`
        set the same value, so the last call wins. Default:
        `Backoff::exponential(2.0)`.
    - `jitter` A `JitterStrategy` value. Default: `JitterStrategy::Full`.
    - `retry_if` A predicate over the `durable::StepError`. The SDK retries only the
        errors for which it returns `true`. Without it, the SDK retries every error.
        See [Retry only specific errors](#retry-only-specific-errors).
    - `pause_every` Pauses the durable execution after every `n`th failed attempt
        that the strategy retries. Pass 1 or more. Default: never pauses.
        `pause_every(0)` also never pauses. See
        [Pause after a failed attempt](#pause-after-a-failed-attempt).

    `build()` accepts any value. The strategy rounds each delay to the nearest whole
    second, with a minimum of 1 second. `RetryStrategyConfig::default_preset()`
    returns the default preset from [Retry presets](#retry-presets), which differs
    from `RetryStrategyConfig::builder().build()`.

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

=== "Go"

    ```go
    --8<-- "examples/go/sdk-reference/error-handling/jitter-strategy-signature.go"
    ```

    `RetryConfig` and `WaitConfig` default to `JitterFull`. `LinearRetryConfig` defaults
    to `JitterNone`.

=== "Rust"

    ```rust
    --8<-- "examples/rust/sdk-reference/error-handling/jitter-strategy-signature.rs"
    ```

    A config from `RetryStrategyConfig::builder()` defaults to `JitterStrategy::Full`.
    The `RetryStrategyConfig::linear()` preset uses `JitterStrategy::None`.

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

=== "Go"

    Build a strategy with `MustLinearBackoff` and a `LinearRetryConfig`, then pass it
    with `WithRetry`. `MustLinearBackoff` panics for an invalid config. `LinearBackoff`
    returns an error instead. `LinearBackoff` defaults to no jitter, so this example
    sets `JitterFull`.

    ```go
    --8<-- "examples/go/sdk-reference/error-handling/linear-retry-strategy.go"
    ```

=== "Rust"

    Pass `Backoff::linear(increment)` to the builder's `.backoff(..)`, then pass the
    config to the step with `.retry_strategy_config(..)`.

    ```rust
    --8<-- "examples/rust/sdk-reference/error-handling/linear-retry-strategy.rs"
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

=== "Go"

    The type is `LinearRetryConfig`.

    ```go
    --8<-- "examples/go/sdk-reference/error-handling/linear-retry-strategy-config-signature.go"
    ```

    **Fields:**

    - `MaxAttempts` Total attempts including the first. Default: `6`. Must not be
        negative. The zero value selects the default. Use `NoRetry()` or
        `MaxAttempts: 1` for a single attempt.
    - `InitialDelay` A `time.Duration`. Default: `1 * time.Second`. At least 1 second
        when set.
    - `Increment` A `time.Duration` added to the delay on each retry. Default:
        `1 * time.Second`. Must not be negative. A zero value selects the default, so
        this config cannot express a fixed interval. For a fixed interval, use
        `NewRetryStrategy` with a `BackoffRate` of 1.
    - `MaxDelay` A `time.Duration`. Default: `5 * time.Minute`. At least 1 second when
        set.
    - `Jitter` A `JitterStrategy` value. Default: `JitterNone`, so the default sequence
        is exact. When set, it must be one of the defined constants.
    - `RetryableErrors` A slice of `ErrorMatcher`. When empty, every error is retryable.
        Entries must not be nil.

=== "Rust"

    The type is `durable::builders::Backoff`. Pass it to
    `RetryStrategyConfigBuilder::backoff`. Set the other fields with the methods in
    [RetryStrategyConfig signature](#retrystrategyconfig-signature).

    ```rust
    --8<-- "examples/rust/sdk-reference/error-handling/linear-retry-strategy-config-signature.rs"
    ```

    **Constructors:**

    - `Backoff::exponential(rate)` Multiplies the delay by `rate` on each retry.
    - `Backoff::linear(increment)` Adds `increment`, a `Duration`, to the delay on each
        retry. A zero `increment` gives a fixed interval.

    Build a `Backoff` with these constructors. A `match` on a variant needs `..`, as in
    `Backoff::Linear { increment, .. }`.

    With a linear backoff, `build()` fills each unset field with the linear default.
    `max_attempts` defaults to `6`, `initial_delay` to 1 second, `max_delay` to 5
    minutes, and `jitter` to `JitterStrategy::Full`.

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

    `pause` pauses the execution after this attempt. See
    [Pause after a failed attempt](#pause-after-a-failed-attempt).

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/retry-strategy-signature.py"
    ```

    `pause` pauses the execution after this attempt. See
    [Pause after a failed attempt](#pause-after-a-failed-attempt).

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/error-handling/retry-strategy-signature.java"
    ```

=== "Go"

    A `RetryStrategy` is a function from a `RetryAttempt` to a `RetryDecision`. The
    strategy receives one struct, with the error and the 1-based attempt number as
    fields.

    ```go
    --8<-- "examples/go/sdk-reference/error-handling/retry-strategy-signature.go"
    ```

    `Pause` pauses the execution after this attempt. See
    [Pause after a failed attempt](#pause-after-a-failed-attempt).

=== "Rust"

    A retry strategy is a function or closure that takes the `&durable::StepError` and
    the 1-based attempt number, and returns a `durable::RetryDecision`. Pass it to the
    step with `.retry_strategy(..)`. `StepError::source()` returns the error that the
    step body returned.

    ```rust
    --8<-- "examples/rust/sdk-reference/error-handling/retry-strategy-signature.rs"
    ```

    `pause` pauses the execution after this attempt. Set it with `with_pause`. See
    [Pause after a failed attempt](#pause-after-a-failed-attempt).

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

=== "Go"

    Return `RetryDecision{Retry: true, Delay: d}` to retry, or `RetryDecision{}` to
    stop. A zero delay waits one second. The SDK sends the delay in whole seconds and
    rounds a fractional delay up. A negative delay fails the step.

    ```go
    --8<-- "examples/go/sdk-reference/error-handling/custom-retry-strategy.go"
    ```

=== "Rust"

    Return `RetryDecision::retry(delay)` to retry, or `RetryDecision::Stop` to stop.
    The SDK rounds a fractional delay up to whole seconds, and a delay under 1 second
    waits 1 second.

    ```rust
    --8<-- "examples/rust/sdk-reference/error-handling/custom-retry-strategy.rs"
    ```

=== "C#"

    Use `RetryStrategy.FromDelegate((error, attempt) => ...)`. Return
    `RetryDecision.DoNotRetry()` to stop, or `RetryDecision.RetryAfter(delay)` to retry.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/error-handling/custom-retry-strategy.cs"
    ```

## Pause after a failed attempt

!!! note "SDK support"

    The TypeScript, Python, Go, and Rust SDKs support pausing from a retry decision. The
    Java and C# SDKs do not support it yet.

A retry decision can pause the whole durable execution after a failed attempt. When a
step's function fails and the retry strategy returns a retry with `pause` set, the SDK
saves the retry checkpoint as usual. Then the SDK calls the Lambda
`PauseDurableExecution` API and ends the invocation. Lambda does not invoke the function
again until an operator calls `ResumeDurableExecution`. After the resume, the next attempt
runs as usual.

Use a pause when a failure needs a person to act, for example an outage of a downstream
service or a wrong configuration value. You fix the cause, and then you resume the
execution.

The SDK ignores `pause` when the decision does not retry. So `pause` never changes the
number of attempts. Only an attempt that failed in the current invocation can pause. A
step that was interrupted in an earlier invocation does not pause. Steps and the
submitter step of `waitForCallback` use `pause`. The `withRetry` helper ignores it.

If the pause call fails, for example because the execution role lacks a permission, the
SDK logs a warning and the step retries as usual. A client without pause support also
retries as usual.

### Pause every N attempts

=== "TypeScript"

    Set `pauseEvery` in `createRetryStrategy()`.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/pause-every.ts"
    ```

=== "Python"

    Set `pause_every` in `RetryStrategyConfig`.

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/pause-every.py"
    ```

=== "Go"

    Set `PauseEvery` in `RetryConfig`.

    ```go
    --8<-- "examples/go/sdk-reference/error-handling/pause-every.go"
    ```

=== "Rust"

    Call `pause_every` on `RetryStrategyConfig::builder()`.

    ```rust
    --8<-- "examples/rust/sdk-reference/error-handling/pause-every.rs"
    ```

This strategy pauses the execution after failed attempts 5 and 10. The strategy does not
retry attempt 15, so a failure there fails the step and does not pause.

### Pause from a custom strategy

=== "TypeScript"

    Set `pause: true` on a decision that has `shouldRetry: true`.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/error-handling/pause-custom-retry-strategy.ts"
    ```

=== "Python"

    Pass `pause=True` to `RetryDecision.retry()`.

    ```python
    --8<-- "examples/python/sdk-reference/error-handling/pause-custom-retry-strategy.py"
    ```

=== "Go"

    Set `Pause: true` on a decision that has `Retry: true`.

    ```go
    --8<-- "examples/go/sdk-reference/error-handling/pause-custom-retry-strategy.go"
    ```

=== "Rust"

    Call `with_pause(true)` on `RetryDecision::retry(delay)`. `with_pause` returns
    `RetryDecision::Stop` unchanged.

    ```rust
    --8<-- "examples/rust/sdk-reference/error-handling/pause-custom-retry-strategy.rs"
    ```

This strategy pauses the execution after attempt 5 when the error is a
`DownstreamUnavailableError`. Every other failure retries after 2 seconds, up to 10
attempts.

### Pause instead of failing

!!! tip "Redrive a failed step"

    A common question is how to redrive a failed execution, so that it starts again from
    where it failed and not from the beginning. You cannot redrive a failed durable
    execution. Instead, set `pause` on the retry decision before the step runs out of
    retries. The execution then pauses instead of failing. Fix the cause, then call
    `ResumeDurableExecution`. The execution continues from its last checkpoint and runs
    the failed step again. So the resume works like a redrive from the failure.

    === "TypeScript"

        ```typescript
        --8<-- "examples/typescript/sdk-reference/error-handling/pause-instead-of-fail.ts"
        ```

    === "Python"

        ```python
        --8<-- "examples/python/sdk-reference/error-handling/pause-instead-of-fail.py"
        ```

    === "Go"

        ```go
        --8<-- "examples/go/sdk-reference/error-handling/pause-instead-of-fail.go"
        ```

    === "Rust"

        ```rust
        --8<-- "examples/rust/sdk-reference/error-handling/pause-instead-of-fail.rs"
        ```

    This strategy retries attempts 1 and 2 as usual. From attempt 3 on, every decision
    retries with `pause`, so the next attempt runs only after a resume. The step never
    fails by itself. To give up, stop the execution.

### Pauses and other branches

=== "TypeScript"

    The pause ends the invocation at once. A step that still runs in another `parallel`
    or `map` branch stops. After the resume, an at-least-once step runs again, and an
    `AtMostOncePerRetry` step records an interrupted attempt.

=== "Python"

    Steps that still run in other `parallel` or `map` branches finish before the
    invocation ends. A step in a `map` or `parallel` branch can pause the execution after
    the `map` or `parallel` operation has finished, for example when its completion config
    needs only some branches.

=== "Go"

    Steps that still run in other `Parallel` or `Map` branches finish their current
    attempt. The SDK saves no checkpoint after the pause request. So an attempt that
    finishes after the request saves nothing and runs again after the resume. A step in a
    branch that its `Map` or `Parallel` no longer waits for does not pause the execution.

=== "Rust"

    Steps in other `map` or `parallel` branches keep running until the invocation saves
    its next checkpoint. Lambda saves that checkpoint and answers it without a checkpoint
    token. The SDK then saves no further checkpoint, and the invocation ends. Work whose
    result was not saved runs again after the resume. A step in a branch that a finished
    `map` or `parallel` no longer waits for does not pause the execution.

### Resume a paused execution

An operator resumes the execution with the AWS CLI:

```console
aws lambda resume-durable-execution \
  --durable-execution-arn <execution-arn> \
  --reason "Downstream service recovered"
```

The function's execution role needs `lambda:PauseDurableExecution` on its own durable
execution ARNs. If the function uses a customer managed AWS KMS key, the role also needs
`kms:Decrypt` on that key. The operator needs `lambda:ResumeDurableExecution` on the
execution. See [Pause and resume an execution](../../getting-started/manage-executions.md#pause-and-resume-an-execution)
for the execution statuses and the operator commands.

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

=== "Go"

    ```go
    --8<-- "examples/go/sdk-reference/error-handling/retry-presets.go"
    ```

    **`ExponentialBackoff()`** 6 attempts, 5s initial delay, 60s max, 2x backoff, full
    jitter. This is the default when you set no retry strategy.

    **`NoRetry()`** 1 attempt, fails immediately on error.

    **`MustLinearBackoff(LinearRetryConfig{})`** 6 attempts with linear delays of 1s, 2s,
    3s, 4s, 5s and no jitter.

    The SDK ships no transient, critical, fixed, or resource-availability preset.

=== "Rust"

    ```rust
    --8<-- "examples/rust/sdk-reference/error-handling/retry-presets.rs"
    ```

    **`RetryStrategyConfig::default_preset()`** 6 attempts, 5s initial delay, 60s max,
    2x backoff, full jitter. This is the default when you set no retry strategy.

    **`RetryStrategyConfig::linear()`** 6 attempts with linear delays of 1s, 2s, 3s, 4s,
    5s and no jitter.

    **`RetryStrategyConfig::no_retry()`** 1 attempt, fails immediately on error.

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

=== "Go"

    `Retry(ctx, name, fn, strategy, opts...)` runs `fn` and retries it on failure. `fn`
    receives a durable context and the 1-based attempt number, and may call any durable
    operation. By default each attempt runs in its own child context named
    `<name>-attempt-<n>`. `Retry` records no parent operation around the attempts.
    Between attempts, `Retry` waits with a `Wait` named `<name>-backoff-<n>`.

    ```go
    --8<-- "examples/go/sdk-reference/error-handling/with-retry-helper.go"
    ```

    When the strategy stops, `Retry` returns a `*RetryError`. Its `Attempts` field holds
    the number of attempts, and its `Err` field holds the final attempt's error. A zero
    delay waits one second. A negative delay makes `Retry` return an error. A suspension
    inside `fn`, such as an unfinished `Wait`, is not a failed attempt, and the strategy
    never sees it. `Retry` returns an error for a nil `fn`, a nil strategy, or a
    `Context` the SDK did not create.

    `WithAttemptChildContext(false)` runs `fn` directly in the caller's context. The
    strategy then receives the error `fn` returned. `WithAttemptChildOptions(opts...)`
    passes `ChildOption` values, such as `WithChildSerdes`, to each attempt's child
    context.

=== "Rust"

    `ctx.with_retry(f)` runs `f` and retries it on failure. `f` receives a
    `DurableContext` and the 1-based attempt number, and may call any durable
    operation. The SDK calls `f` once per attempt, so clone each captured value inside
    it. The strategy runs only after `f` returns an `Err`. A suspension inside `f`
    leaves the current attempt in progress.

    Set the strategy with `.retry_strategy_config(..)` or `.retry_strategy(..)`.
    Without one, the block uses `RetryStrategyConfig::default_preset()`. A
    `retry_if` predicate in this block receives the attempt's `durable::OperationError`
    as the source of its `StepError`. Downcast to that `OperationError` and match its
    `wire()` error type or message. The SDK rebuilds this error from the attempt's
    record in every invocation, including the one where the attempt failed, so its
    source chain never holds the error the body returned. An attempt error without a
    record, such as a failure to decode a successful attempt's result, reaches the
    predicate unchanged.

    ```rust
    --8<-- "examples/rust/sdk-reference/error-handling/with-retry-helper.rs"
    ```

    By default the block records one child context with the name you set with
    `.name(..)`. Each attempt runs in its own child context, `attempt-<n>`, inside it.
    Between attempts, a named block waits with a wait named `<name>-backoff-<n>`. A
    block without a name creates waits without names. The invocation suspends during
    this wait when all remaining SDK work is blocked.

    When the strategy stops, the block fails with a `durable::ChildContextError` whose
    source is the last attempt's error.

    `.virtual_context()` records no block, but keeps the attempts and waits in a
    separate operation namespace. The attempts of a block named `<name>` are then
    named `<name>-attempt-<n>`.

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

=== "Go"

    Set `RetryableErrors` on the config to a list of `ErrorMatcher` values. The SDK
    retries a failed attempt only when at least one matcher reports its error retryable.
    Any other error fails the step on that attempt.
    Build matchers with `ErrorAs`, `ErrorIs`, `ErrorTypeIs`, `ErrorContains`, or
    `ErrorMatches`.

    ```go
    --8<-- "examples/go/sdk-reference/error-handling/retry-specific-errors.go"
    ```

    `ErrorAs` and `ErrorIs` match a live error, such as the one a step returns. Inside a
    default `Retry` group, match the recorded error with `ErrorTypeIs`, `ErrorContains`,
    or `ErrorMatches`.

=== "Rust"

    Pass a predicate to the builder's `.retry_if(..)`. The SDK retries a failed attempt
    only when the predicate returns `true`. Any other error fails the step on that
    attempt. Read the step body's error with `error.source()`, which needs the
    `std::error::Error` trait in scope. Match its type with `is::<T>()` or
    `downcast_ref::<T>()`, or match its message with
    `error.source().map(ToString::to_string)`. The predicate must return the same answer
    for the same error, because replay can call it again.

    ```rust
    --8<-- "examples/rust/sdk-reference/error-handling/retry-specific-errors.rs"
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
