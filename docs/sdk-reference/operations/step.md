# Step

## Checkpointed results

A step executes the code you provide and checkpoints the result. On replay, the SDK
returns the checkpointed result rather than re-running the code inside the step.

Use steps to encapsulate any code that should not re-run once it has completed.

Wrapping non-deterministic code in steps is the primary way you ensure that your durable
execution is [deterministic](../../getting-started/key-concepts.md#determinism).
Non-deterministic code includes fetching the current time, generating a random number or
UUID, causing side-effects such as writing to disk, or calling an API that might return
a different result on different calls.

When you encapsulate such code in a step it becomes deterministic in your durable
execution because the step doesn’t generate different results on replay.

If a step fails during execution, it retries according to its configured retry strategy.
The step will checkpoint the last error after exhausting all retry attempts.

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

## Method signature

### step

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/steps/step-signature.ts"
    ```

    **Parameters:**

    - `name` (optional) A name for the step. Pass `undefined` to omit.
    - `fn` A function that receives a `StepContext` and returns a `Promise<T>`.
    - `config` (optional) A `StepConfig<T>` object.

    **Returns:** `DurablePromise<T>`. Use `await` to get the result.

    **Throws:** `DurableOperationError` wrapping the original error after retries are
    exhausted. `StepInterruptedError` if an at-most-once step was interrupted.

=== "Python"

    ```python
    --8<-- "examples/python/operations/steps/step-signature.py"
    ```

    **Parameters:**

    - `func` A callable that receives a `StepContext` and returns `T`.
    - `name` (optional) A name for the step. Defaults to the function's name when using
        `@durable_step`.
    - `config` (optional) A `StepConfig` object.

    **Returns:** `T`, the return value of `func`.

    **Raises:** `CallableRuntimeError` wrapping the original exception after retries are
    exhausted. `StepInterruptedError` if an at-most-once step was interrupted.

=== "Java"

    ```java
    --8<-- "examples/java/operations/steps/step-signature.java"
    ```

    **Parameters:**

    - `name` (required) A name for the step.
    - `resultType` The `Class<T>` or `TypeToken<T>` for deserialization.
    - `func` A `Function<StepContext, T>` to execute.
    - `config` (optional) A `StepConfig` object.

    **Returns:** `T` (sync) or `DurableFuture<T>` (async via `stepAsync()`).

    **Throws:** The original exception re-thrown after deserialization if possible,
    otherwise `StepFailedException`. `StepInterruptedException` if an at-most-once step was
    interrupted.

### StepConfig

=== "TypeScript"

    ```typescript
    interface StepConfig<T> {
      retryStrategy?: (error: Error, attemptCount: number) => RetryDecision;
      semantics?: StepSemantics;
      serdes?: Serdes<T>;
    }
    ```

    **Parameters:**

    - `retryStrategy` (optional) A function returning a `RetryDecision`. Use
        `createRetryStrategy()` to build one. See
        [Retry strategies](../error-handling/retries.md).
    - `semantics` (optional) `StepSemantics.AtLeastOncePerRetry` (default) or
        `StepSemantics.AtMostOncePerRetry`.
    - `serdes` (optional) Custom `Serdes<T>` for the step result. See
        [Serialization](../state/serialization.md).

=== "Python"

    ```python
    @dataclass(frozen=True)
    class StepConfig:
        retry_strategy: Callable[[Exception, int], RetryDecision] | None = None
        step_semantics: StepSemantics = StepSemantics.AT_LEAST_ONCE_PER_RETRY
        serdes: SerDes | None = None
    ```

    **Parameters:**

    - `retry_strategy` (optional) A callable returning a `RetryDecision`. Use
        `create_retry_strategy()` to build one. See
        [Retry strategies](../error-handling/retries.md).
    - `step_semantics` (optional) `StepSemantics.AT_LEAST_ONCE_PER_RETRY` (default) or
        `StepSemantics.AT_MOST_ONCE_PER_RETRY`.
    - `serdes` (optional) Custom `SerDes` for the step result. See
        [Serialization](../state/serialization.md).

=== "Java"

    ```java
    StepConfig.builder()
        .retryStrategy(RetryStrategy)  // optional
        .semantics(StepSemantics)      // optional
        .serDes(SerDes)                // optional
        .build()
    ```

    **Parameters:**

    - `retryStrategy` (optional) A `RetryStrategy` instance. Use
        `RetryStrategies.exponentialBackoff()` to build one. See
        [Retry strategies](../error-handling/retries.md).
    - `semantics` (optional) `StepSemantics.AT_LEAST_ONCE_PER_RETRY` (default) or
        `StepSemantics.AT_MOST_ONCE_PER_RETRY`.
    - `serDes` (optional) Custom `SerDes` for the step result. See
        [Serialization](../state/serialization.md).

### StepContext

=== "TypeScript"

    ```typescript
    interface StepContext {
      logger: DurableContextLogger;
    }
    ```

    - `logger` A logger enriched with execution context metadata. See
        [Logging](../observability/logging.md).

=== "Python"

    ```python
    @dataclass(frozen=True)
    class StepContext:
        logger: LoggerInterface
    ```

    - `logger` A logger enriched with execution context metadata. See
        [Logging](../observability/logging.md).

=== "Java"

    ```java
    interface StepContext {
        DurableLogger getLogger();
        int getAttempt();      // current retry attempt, 0-based
        boolean isReplaying();
    }
    ```

    - `getLogger()` A logger enriched with execution context metadata. See
        [Logging](../observability/logging.md).
    - `getAttempt()` The current retry attempt number (0-based).
    - `isReplaying()` Whether the function is currently replaying from a checkpoint.

### StepSemantics

=== "TypeScript"

    ```typescript
    enum StepSemantics {
      AtLeastOncePerRetry = "AT_LEAST_ONCE_PER_RETRY",
      AtMostOncePerRetry  = "AT_MOST_ONCE_PER_RETRY",
    }
    ```

    - `AtLeastOncePerRetry` (default) Re-executes the step if the function replays before
        the result is checkpointed. Safe for idempotent operations.
    - `AtMostOncePerRetry` Executes the step at most once per retry attempt. If the function
        replays before the result is checkpointed, the SDK skips the step and raises
        `StepInterruptedError`. Use for operations with side effects.

=== "Python"

    ```python
    class StepSemantics(Enum):
        AT_LEAST_ONCE_PER_RETRY = "AT_LEAST_ONCE_PER_RETRY"
        AT_MOST_ONCE_PER_RETRY  = "AT_MOST_ONCE_PER_RETRY"
    ```

    - `AT_LEAST_ONCE_PER_RETRY` (default) Re-execute the step if the function replays before
        the result has checkpointed. Safe for idempotent operations.
    - `AT_MOST_ONCE_PER_RETRY` Execute the step at most once per retry attempt. If the
        function replays before the result has checkpointed, the SDK skips the step and
        raises `StepInterruptedError`. Use for operations with side effects.

=== "Java"

    ```java
    enum StepSemantics {
        AT_LEAST_ONCE_PER_RETRY,
        AT_MOST_ONCE_PER_RETRY
    }
    ```

    - `AT_LEAST_ONCE_PER_RETRY` (default) Re-executes the step if the function replays
        before the result is checkpointed. Safe for idempotent operations.
    - `AT_MOST_ONCE_PER_RETRY` Executes the step at most once per retry attempt. If the
        function replays before the result is checkpointed, the SDK skips the step and
        throws `StepInterruptedException`. Use for operations with side effects.

## The Step's function

A step function receives a `StepContext` as its first parameter.

=== "TypeScript"

    Pass any async function directly.

    ```typescript
    --8<-- "examples/typescript/operations/steps/validate-order.ts"
    ```

    Step functions are async. `await` the result of `context.step()`.

=== "Python"

    Use the `@durable_step` decorator. It automatically uses the function's name as the step
    name. Step functions must be synchronous.

    ```python
    --8<-- "examples/python/operations/steps/validate-order.py"
    ```

=== "Java"

    Pass a lambda or method reference directly. Step functions are synchronous. Use
    `stepAsync()` to get a `DurableFuture<T>` you can compose with other async operations.

    ```java
    --8<-- "examples/java/operations/steps/validate-order.java"
    ```

### Anonymous step functions

You can also use inline lambdas.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/steps/lambda-step-no-name.ts"
    ```

=== "Python"

    If you use an anonymous function it will not automatically get named like the
    `@durable_step` decorator does.

    ```python
    --8<-- "examples/python/operations/steps/lambda-step-no-name.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/steps/lambda-step-no-name.java"
    ```

### Pass arguments to the step function

=== "TypeScript"

    Capture arguments in a closure:

    ```typescript
    --8<-- "examples/typescript/operations/steps/multi-argument-step.ts"
    ```

=== "Python"

    Use `@durable_step` and pass arguments when calling the function:

    ```python
    --8<-- "examples/python/operations/steps/multi-argument-step.py"
    ```

=== "Java"

    Capture arguments in a lambda:

    ```java
    --8<-- "examples/java/operations/steps/multi-argument-step.java"
    ```

## Naming steps

Name your steps so they're easy to identify in logs and tests. Use descriptive names
that explain what the step does. Names don't need to be unique, but distinct names make
debugging easier.

=== "TypeScript"

    The name is the first argument. Pass `undefined` to omit it.

=== "Python"

    The `@durable_step` decorator uses the function's name automatically as the step name.
    Override it with the `name` keyword argument.

=== "Java"

    The name is always the first argument. Pass `null` for no name. 

## Configuration

Configure step behavior using `StepConfig`:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/steps/process-data.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/steps/process-data.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/steps/process-data.java"
    ```

## Pass data between steps

Pass data between steps through return values. Do not use shared variables or closure
mutations. Steps return cached results on replay, so mutations to outer variables are
lost.

### wrong way to pass data between steps

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/steps/passing-data-wrong.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/steps/passing-data-wrong.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/steps/passing-data-wrong.java"
    ```

### correct way to pass data between steps

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/operations/steps/passing-data-correct.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/operations/steps/passing-data-correct.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/operations/steps/passing-data-correct.java"
    ```

## Nesting steps

You cannot nest steps. Do not attempt to invoke another step from inside a step. If you
want to group or nest operations, use a [child context](child-context.md).

## Concurrency

Do not run steps concurrently. For concurrent operations, see [map](map.md) and
[parallel](parallel.md).

To code your own concurrency use a [child context](child-context.md) to encapsulate each
concurrent code path.

## See also

- [Retries](../error-handling/retries.md)
- [Testing](../../testing/index.md)
- [Wait operations](wait.md)
- [Child contexts](child-context.md)
