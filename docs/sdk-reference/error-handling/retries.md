# Retries

## Table of Contents

- [Overview](#overview)
- [Creating retry strategies](#creating-retry-strategies)
- [RetryStrategyConfig parameters](#retrystrategyconfig-parameters)
- [Retry presets](#retry-presets)
- [Retrying specific exceptions](#retrying-specific-exceptions)
- [Exponential backoff](#exponential-backoff)

[← Back to main index](../index.md)

## Overview

Retry strategies configure how the SDK responds to transient failures in steps. You can control the number of attempts, delay between retries, backoff rate, and which exceptions trigger a retry.

[↑ Back to top](#table-of-contents)

## Creating retry strategies

Use `RetryStrategyConfig` to define retry behavior:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/core/steps/unreliable-operation.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/core/steps/unreliable-operation.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/core/steps/unreliable-operation.java"
    ```

[↑ Back to top](#table-of-contents)

## RetryStrategyConfig parameters

**max_attempts** - Maximum number of attempts (including the initial attempt). Default: 3.

**initial_delay_seconds** - Initial delay before first retry in seconds. Default: 5.

**max_delay_seconds** - Maximum delay between retries in seconds. Default: 300 (5 minutes).

**backoff_rate** - Multiplier for exponential backoff. Default: 2.0.

**jitter_strategy** - Jitter strategy to add randomness to delays. Default: `JitterStrategy.FULL`.

**retryable_errors** - List of error message patterns to retry (strings or regex patterns). Default: matches all errors.

**retryable_error_types** - List of exception types to retry. Default: empty (retry all).

[↑ Back to top](#table-of-contents)

## Retry presets

The SDK provides preset retry strategies for common scenarios:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/retry-presets.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/retry-presets.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/retry-presets.java"
    ```

[↑ Back to top](#table-of-contents)

## Retrying specific exceptions

Only retry certain exception types:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/retry-specific-exceptions.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/retry-specific-exceptions.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/retry-specific-exceptions.java"
    ```

[↑ Back to top](#table-of-contents)

## Exponential backoff

Configure exponential backoff to avoid overwhelming failing services:

=== "TypeScript"

    ``` typescript
    --8<-- "examples/typescript/advanced/error-handling/exponential-backoff.ts"
    ```

=== "Python"

    ``` python
    --8<-- "examples/python/advanced/error-handling/exponential-backoff.py"
    ```

=== "Java"

    ``` java
    --8<-- "examples/java/advanced/error-handling/exponential-backoff.java"
    ```

With this configuration:
- Attempt 1: Immediate
- Attempt 2: After 1 second
- Attempt 3: After 2 seconds
- Attempt 4: After 4 seconds
- Attempt 5: After 8 seconds

[↑ Back to top](#table-of-contents)

## See also

- [Errors](errors.md) - Exception types and error responses
- [Step](../operations/step.md) - Configure retry for steps
