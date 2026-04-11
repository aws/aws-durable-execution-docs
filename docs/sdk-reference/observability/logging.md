# Logging

## Replay-aware logger

Use the Durable Execution SDK logger to add structured log entries to your function. The
SDK automatically tags every entry with execution metadata such as the ARN, operation
name, and retry attempt. The logger will not emit duplicate log entries on replay. The
SDK provides a default logger, or you can [provide a custom logger](#custom-logger).

The [Powertools for AWS Lambda logger](#powertools-for-aws-lambda) works as a
replacement for the SDK's default logger. It adds structured JSON output, correlation
IDs, log sampling, and X-Ray tracing integration.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/observability/basic-usage.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/observability/basic-usage.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/observability/basic-usage.java"
    ```

## Log methods

=== "TypeScript"

    ```typescript
    // On DurableContext and StepContext:
    context.logger.info(...params: unknown[]): void
    context.logger.warn(...params: unknown[]): void
    context.logger.error(...params: unknown[]): void
    context.logger.debug(...params: unknown[]): void
    context.logger.log(level: "INFO" | "WARN" | "ERROR" | "DEBUG", ...params: unknown[]): void
    ```

=== "Python"

    ```python
    # On DurableContext and StepContext:
    context.logger.debug(msg, *args, extra=None)
    context.logger.info(msg, *args, extra=None)
    context.logger.warning(msg, *args, extra=None)
    context.logger.error(msg, *args, extra=None)
    context.logger.exception(msg, *args, extra=None)
    ```

    **Parameters (all log methods):**

    - `msg` (`object`) The log message.
    - `*args` (`object`) Arguments for message formatting, passed to the underlying logger.
    - `extra` (`Mapping[str, object] | None`) Additional fields to include in the log entry.
        These merge with the automatic context fields.

=== "Java"

    ```java
    // Obtain from any context:
    DurableLogger logger = context.getLogger();
    DurableLogger logger = stepContext.getLogger();

    // Available methods:
    logger.trace(String format, Object... args)
    logger.debug(String format, Object... args)
    logger.info(String format, Object... args)
    logger.warn(String format, Object... args)
    logger.error(String format, Object... args)
    logger.error(String message, Throwable t)
    ```

    The Java logger uses SLF4J format strings. Pass `{}` placeholders and positional
    arguments.

## Default log format

=== "TypeScript"

    The default logger always emits structured JSON. A log entry from a step looks like:

    ```json
    {
      "timestamp": "2025-11-21T18:39:24.743Z",
      "level": "INFO",
      "requestId": "72171fff-...",
      "executionArn": "arn:aws:lambda:...",
      "operationId": "abc123",
      "operationName": "process",
      "attempt": 1,
      "message": "Running step"
    }
    ```

=== "Python"

    When your Lambda function's log format is set to JSON, the log output includes the extra
    metadata as top-level keys in the JSON output. See
    [Using Lambda advanced logging controls with Python](https://docs.aws.amazon.com/lambda/latest/dg/python-logging.html#python-logging-advanced).

    ```json
    {
      "timestamp": "2025-11-21T18:39:24Z",
      "level": "INFO",
      "message": "Running step",
      "requestId": "72171fff-...",
      "executionArn": "arn:aws:lambda:...",
      "operationId": "abc123",
      "operationName": "process",
      "attempt": 1
    }
    ```

=== "Java"

    Calling `getLogger()` populates SLF4J MDC with execution context fields. When your
    Lambda function's log format is set to JSON and your logging framework includes MDC
    fields in its output, those fields appear as top-level keys in the JSON log record. See
    [Using Lambda advanced logging controls with Java](https://docs.aws.amazon.com/lambda/latest/dg/java-logging.html#java-logging-advanced).

    Field names and structure depend on your logging framework configuration. A Log4j2 JSON
    output might look like:

    ```json
    {
      "timestamp": "2025-11-21T18:39:24.743Z",
      "level": "INFO",
      "message": "Running step",
      "AWSRequestId": "72171fff-...",
      "durableExecutionArn": "arn:aws:lambda:...",
      "operationId": "abc123",
      "operationName": "process",
      "attempt": "1"
    }
    ```

## Execution metadata

The SDK automatically enriches log entries with execution metadata. The metadata varies
depending on the logging context.

### DurableContext at handler level

This is the `DurableContext` passed to the durable handler. It enriches the output with
the following fields:

- `executionArn` (TypeScript, Python) / `durableExecutionArn` (Java MDC) the ARN of the
    current durable execution
- `requestId` the Lambda request ID

### DurableContext (child)

All DurableContext fields, plus:

=== "TypeScript"

    - `operationId` hashed ID of the child context operation

=== "Python"

    - `parentId` the operation ID of the current child context. Operations inside this child
        context log both `parentId` (the containing child context) and `operationId` (the
        operation itself).

=== "Java"

    - `contextId` the operation ID of the child context operation
    - `contextName` the name given to the child context, when you provide one

### Operation context

The following operation contexts support the logger:

- [StepContext](../operations/step.md#stepcontext)
- [WaitForConditionContext](../operations/wait-for-condition.md#method-signature)
- [WaitForCallbackContext](../operations/callback.md#waitforcallback)

All DurableContext fields, plus:

- `operationId` the unique ID of the operation
- `operationName` the operation name, when you provide one
- `attempt` the current retry attempt number (1-indexed, steps only)

The following examples show logging from inside a step. Using the StepContext logger
instead of DurableContext's logger adds step-specific fields (`operationId`,
`operationName`, `attempt`) to every log entry from that step.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/observability/step-context-logger.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/observability/step-context-logger.py"
    ```

=== "Java"

    The Java SDK uses SLF4J MDC to attach fields. Configure your logging framework (e.g.
    Logback, Log4j2) to include MDC fields in your log pattern.

    ```java
    --8<-- "examples/java/sdk-reference/observability/step-context-logger.java"
    ```

## Replay log suppression

When the SDK replays, it runs your handler from the start until it reaches the next
incomplete operation. It does not re-emit log entries encountered before that point,
since these already emitted on the first invocation that traversed them.

Logs inside a retrying step body always emit, because the step has not completed yet.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/observability/replay-suppression.ts"
    ```

    Pass `modeAware: false` to `configureLogger` to emit logs on every replay.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/observability/disable-replay-suppression.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/observability/replay-suppression.py"
    ```

    The Python SDK checks `execution_state.is_replaying()` before each log call. You cannot
    disable this per-context, but you can log directly to the underlying logger to bypass
    it.

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/observability/replay-suppression.java"
    ```

    Pass `LoggerConfig.withReplayLogging()` to `DurableConfig` to emit logs on every replay.
    See [Configure logger](#configure-logger).

## Custom logger

You can replace the default logger with any logger that implements the SDK's logger
interface.

=== "TypeScript"

    Any object that implements `DurableLogger` works. Pass it to `configureLogger`.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/observability/custom-logger.ts"
    ```

=== "Python"

    Any object that satisfies the `LoggerInterface` protocol works. Pass it to
    `context.set_logger()`.

    ```python
    --8<-- "examples/python/sdk-reference/observability/custom-logger.py"
    ```

=== "Java"

    The Java SDK does not support swapping the underlying logger. `getLogger()` always wraps
    an SLF4J logger obtained from `LoggerFactory`. To change logging behavior, configure
    your SLF4J implementation (Logback, Log4j2) or adjust `LoggerConfig` via
    `DurableConfig`. See [Configure logger](#configure-logger).

## Configure logger

=== "TypeScript"

    Configure the logger on the handler's `DurableContext`.

    ```typescript
    context.configureLogger(config: LoggerConfig): void
    ```

    **`LoggerConfig`**

    ```typescript
    interface LoggerConfig<Logger extends DurableLogger> {
      customLogger?: Logger;
      modeAware?: boolean;
    }
    ```

    **`LoggerConfig` parameters:**

    - `customLogger` (optional) A [`DurableLogger`](#logger-interface) implementation to use
        instead of the default console logger.
    - `modeAware` (optional) When `true` (default), the SDK suppresses logs during replay.
        Set to `false` to emit logs on every replay.

=== "Python"

    Set the logger on the handler's `DurableContext`.

    ```python
    context.set_logger(new_logger: LoggerInterface) -> None
    ```

    Pass any object that satisfies the [`LoggerInterface`](#logger-interface) protocol.

=== "Java"

    Configure replay suppression via `LoggerConfig` on `DurableConfig`.

    **`LoggerConfig`**

    ```java
    public record LoggerConfig(boolean suppressReplayLogs) {
        public static LoggerConfig defaults()          // suppress replay logs (default)
        public static LoggerConfig withReplayLogging() // allow logs during replay
    }
    ```

    **`LoggerConfig` parameters:**

    - `suppressReplayLogs` (`boolean`) When `true` (default), the SDK suppresses logs during
        replay. Use `LoggerConfig.withReplayLogging()` to set this to `false`.

    ```java
    DurableConfig.builder()
        .withLoggerConfig(LoggerConfig.withReplayLogging())
        .build();
    ```

## Logger interface

=== "TypeScript"

    `DurableLogger` is the interface a custom logger must implement.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/observability/logger-interface.ts"
    ```

=== "Python"

    `LoggerInterface` is the protocol a custom logger must satisfy.

    ```python
    --8<-- "examples/python/sdk-reference/observability/logger-interface.py"
    ```

=== "Java"

    The Java SDK wraps any SLF4J `Logger` in `DurableLogger`. There is no interface to
    implement.

## Powertools for AWS Lambda

[Powertools for AWS Lambda](https://docs.aws.amazon.com/powertools/) provides a
structured logger that works as a drop-in replacement for the SDK's default logger.

=== "TypeScript"

    The
    [Powertools for AWS Lambda (TypeScript)](https://docs.aws.amazon.com/powertools/typescript/latest/features/logger/)
    `Logger` satisfies the `DurableLogger` interface. Pass it via `configureLogger`.

    ```typescript
    --8<-- "examples/typescript/sdk-reference/observability/powertools-logger.ts"
    ```

=== "Python"

    The
    [Powertools for AWS Lambda (Python)](https://docs.aws.amazon.com/powertools/python/latest/core/logger/)
    `Logger` satisfies the `LoggerInterface` protocol. Pass it via `context.set_logger()`.

    ```python
    --8<-- "examples/python/sdk-reference/observability/powertools-logger.py"
    ```

=== "Java"

    The
    [Powertools for AWS Lambda (Java) logger](https://docs.aws.amazon.com/powertools/java/latest/core/logging/)
    uses SLF4J, which is the same logging facade the SDK wraps. Add Powertools as your SLF4J
    implementation and the SDK's `getLogger()` will automatically use it. No additional
    wiring is required.

    ```java
    --8<-- "examples/java/sdk-reference/observability/powertools-logger.java"
    ```

## See also

- [Steps](../operations/step.md)
- [Child contexts](../operations/child-contexts.md)
- [Error handling](error-handling.md)
