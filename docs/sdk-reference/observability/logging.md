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

=== "Go"

    `ctx.Logger()` returns a `*slog.Logger`. Its records carry `requestId`,
    `executionArn`, and `tenantId` when the invocation has one. The default
    handler writes one JSON object per record to stderr.

    ```go
    --8<-- "examples/go/sdk-reference/observability/basic-usage.go"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/sdk-reference/observability/basic-usage.cs"
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

=== "Go"

    `ctx.Logger()` and `stepCtx.Logger()` return `*slog.Logger`, so the log
    methods are the standard library's. Pass structured fields as alternating
    key and value arguments, or as `slog.Attr` values. With the SDK's default
    handler, an attribute whose value is an `error` expands into `errorType`,
    `errorMessage`, and, when the error carries recorded frames, `stackTrace`.

    ```go
    logger := ctx.Logger()
    logger.Debug("message", "key", value)
    logger.Info("message", "key", value)
    logger.Warn("message", "key", value)
    logger.Error("message", "err", err)
    logger.Log(ctx, slog.LevelInfo, "message", "key", value)
    ```

    The four named `slog` levels are `LevelDebug`, `LevelInfo`, `LevelWarn`, and
    `LevelError`.

=== "C#"

    ```csharp
    // context.Logger and stepContext.Logger are Microsoft.Extensions.Logging.ILogger.
    // Use the standard ILogger extension methods:
    context.Logger.LogTrace(string message, params object?[] args);
    context.Logger.LogDebug(string message, params object?[] args);
    context.Logger.LogInformation(string message, params object?[] args);
    context.Logger.LogWarning(string message, params object?[] args);
    context.Logger.LogError(string message, params object?[] args);
    context.Logger.LogError(Exception exception, string message, params object?[] args);
    context.Logger.LogCritical(string message, params object?[] args);
    ```

    The `ILogger` methods use message templates. Pass `{Name}` placeholders and
    positional arguments.

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
      "executionArn": "arn:aws:lambda:...",
      "operationId": "abc123",
      "operationName": "process",
      "attempt": "1"
    }
    ```

=== "Go"

    The SDK's default handler always writes one JSON object per record to
    stderr. The field names are the SDK's own, and they do not depend on
    Lambda advanced logging controls. A record from a step looks like:

    ```json
    {
      "timestamp": "2025-11-21T18:39:24.743Z",
      "level": "INFO",
      "message": "Running step",
      "requestId": "72171fff-...",
      "executionArn": "arn:aws:lambda:...",
      "operationId": "abc123",
      "operationName": "process",
      "attempt": 1
    }
    ```

    `timestamp` is ISO 8601 UTC with millisecond precision and a `Z` suffix.
    `level` is `DEBUG`, `INFO`, `WARN`, or `ERROR`. An attribute whose value
    is an `error` expands into `errorType`, `errorMessage`, and `stackTrace`
    when the error carries recorded frames. The default handler reads its
    minimum level from `AWS_LAMBDA_LOG_LEVEL`. An unset or unrecognized value
    selects `INFO`. A handler you supply applies its own level.

    The SDK also writes records of its own through the installed handler.
    When a checkpoint response without a token suspends the invocation, the
    SDK writes one `WARN` record. A handler enabled at `DEBUG` also receives
    the SDK's trace of its own work, with the messages `operation claimed`,
    `replay complete; executing live`, `checkpoint enqueued`,
    `checkpoint flushed`, `invocation suspending`, and `operation completed`.
    The handler's level is the only switch for these `DEBUG` records.

=== "C#"

    The SDK writes through `Microsoft.Extensions.Logging.ILogger` and attaches
    execution context fields via `ILogger.BeginScope`. When your Lambda function's
    log format is set to JSON and your logger provider includes scope fields in its
    output, those fields appear as top-level keys in the JSON log record. See
    [Using Lambda advanced logging controls with C#](https://docs.aws.amazon.com/lambda/latest/dg/csharp-logging.html#csharp-logging-advanced).

    Field names and structure depend on your logger provider configuration. A JSON
    console output might look like:

    ```json
    {
      "Timestamp": "2025-11-21T18:39:24.743Z",
      "LogLevel": "Information",
      "Message": "Running step",
      "requestId": "72171fff-...",
      "executionArn": "arn:aws:lambda:...",
      "operationId": "abc123",
      "operationName": "process",
      "attempt": 1
    }
    ```

## Execution metadata

The SDK automatically enriches log entries with execution metadata. The metadata varies
depending on the logging context.

### DurableContext at handler level

This is the `DurableContext` passed to the durable handler. It enriches the output with
the following fields:

- `executionArn` the ARN of the current durable execution
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

    - `operationId` the operation ID of the child context operation
    - `operationName` the name given to the child context, when you provide one

=== "Go"

    - `operationId` the ID of the child context operation
    - `operationName` the name given to the child context, when you provide one

    The SDK has one `Context` type. A child context comes from
    `RunInChildContext`, `Go`, `Map`, `Parallel`, or `WaitForCallback`.

=== "C#"

    - `operationId` the deterministic operation ID of the child context operation
    - `operationName` the name given to the child context, when you provide one

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

=== "Go"

    `stepCtx.Logger()` returns a `*slog.Logger` whose records add this
    operation's `operationId`, its `operationName` when it has one, and the
    `attempt` number. The step body, the condition check, and the callback
    submitter all receive the same `StepContext` type, and `attempt` is set
    for all three. Inside a child context, a step's records report the step's
    `operationId` and `operationName` in place of the child's.

    ```go
    --8<-- "examples/go/sdk-reference/observability/step-context-logger.go"
    ```

=== "C#"

    The C# SDK attaches fields via `ILogger.BeginScope`. Configure your logger
    provider to include scope fields in its output.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/observability/step-context-logger.cs"
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

=== "Go"

    ```go
    --8<-- "examples/go/sdk-reference/observability/replay-suppression.go"
    ```

    Suppression is a `ReplayLogMode`. Its three values are `ReplayLogModeSuppress` (the
    default), `ReplayLogModeEmit`, and `ReplayLogModeUnchanged`.
    `ReplayLogModeUnchanged` is the zero value. `WithReplayLogMode` treats it as
    `ReplayLogModeSuppress`. To emit replayed records, each tagged `replay=true`, set
    the mode at `durable.Start`:

    ```go
    durable.Start(handler, durable.WithReplayLogMode(durable.ReplayLogModeEmit))
    ```

    To change the mode from inside the handler, call `ConfigureLogging`. See
    [Configure logger](#configure-logger).

=== "C#"

    ```csharp
    --8<-- "examples/csharp/sdk-reference/observability/replay-suppression.cs"
    ```

    Call `context.ConfigureLogger(new LoggerConfig { ModeAware = false })` to emit logs
    on every replay. See [Configure logger](#configure-logger).

## Custom logger

You can change where the SDK's logger sends its entries.

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

=== "Go"

    Install any `slog.Handler` with `durable.WithLogHandler` at `durable.Start`, or call
    `ConfigureLogging` from inside the handler. The SDK attaches its structured
    attributes to the handler you supply and keeps its replay suppression.

    ```go
    --8<-- "examples/go/sdk-reference/observability/custom-logger.go"
    ```

=== "C#"

    Any `Microsoft.Extensions.Logging.ILogger` works. Pass it as the `CustomLogger`
    on `LoggerConfig` to `context.ConfigureLogger`.

    ```csharp
    context.ConfigureLogger(new LoggerConfig { CustomLogger = myLogger });
    ```

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
    public record LoggerConfig(boolean suppressReplayLogs, boolean oldKeyNames) {
        public static LoggerConfig defaults()          // suppress replay logs (default)
        public static LoggerConfig withReplayLogging() // allow logs during replay
    }
    ```

    **`LoggerConfig` parameters:**

    - `suppressReplayLogs` (`boolean`) When `true` (default), the SDK suppresses logs during
        replay. Use `LoggerConfig.withReplayLogging()` to set this to `false`.
    - `oldKeyNames` (`boolean`) When `false` (default), the SDK uses the Java 2.x MDC keys
        `executionArn`, `operationId`, and `operationName`. Set it to `true` temporarily
        to emit the 1.x keys `durableExecutionArn`, `contextId`, and `contextName` while
        migrating log queries and dashboards.

    ```java
    DurableConfig.builder()
        .withLoggerConfig(LoggerConfig.withReplayLogging())
        .build();
    ```

=== "Go"

    Set the handler and the replay mode at construction with `WithLogHandler`
    and `WithReplayLogMode`. To choose them inside the handler, for example
    from the event payload, call `ConfigureLogging`. At `durable.Start`, a
    `nil` handler selects the default handler, and `ReplayLogModeUnchanged`
    selects `ReplayLogModeSuppress`.

    ```go
    func WithLogHandler(h slog.Handler) HandlerOption
    func WithReplayLogMode(mode ReplayLogMode) HandlerOption

    type LogConfig struct {
        Handler       slog.Handler
        ReplayLogMode ReplayLogMode
        // Has unexported fields.
    }

    func ConfigureLogging(ctx Context, cfg LogConfig) error
    ```

    **`LogConfig` fields:**

    - `Handler` (`slog.Handler`) The handler behind `Context.Logger` and
        `StepContext.Logger`. The SDK attaches its execution and operation
        attributes to it and wraps it with replay suppression. `nil` keeps the
        current handler.
    - `ReplayLogMode` (`ReplayLogMode`) The treatment of records emitted during
        replay. `ReplayLogModeUnchanged`, the zero value, keeps the current mode.

    `ConfigureLogging` applies to `ctx` and to every child context and branch
    derived after the call, for the current invocation only. A logger you
    obtained before the call keeps the previous handler. A new replay mode
    reaches every logger of `ctx`, including loggers obtained before the call.
    `ConfigureLogging` claims no operation ID and writes no checkpoint, so a
    conditional call does not make replay non-deterministic. Called off the goroutine that owns `ctx`, it
    returns an error that wraps `ErrWrongGoroutine`. It also returns an error
    for a `Context` the SDK did not create. It returns no other error.

=== "C#"

    Configure the logger on the handler's `IDurableContext`.

    ```csharp
    void IDurableContext.ConfigureLogger(LoggerConfig config);
    ```

    **`LoggerConfig`**

    ```csharp
    public sealed class LoggerConfig
    {
        public ILogger? CustomLogger { get; init; }  // null = keep current logger
        public bool ModeAware { get; init; } = true;
    }
    ```

    **`LoggerConfig` parameters:**

    - `CustomLogger` (optional) An `ILogger` to use instead of the SDK default. When
        null, the durable context keeps its existing inner logger.
    - `ModeAware` (optional) When `true` (default), the SDK suppresses logs during
        replay. Set to `false` to emit logs on every replay.

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

=== "Go"

    The logger interface is the standard library `slog.Handler`. Implement it, or wrap
    an existing handler, and install it with `durable.WithLogHandler`. The SDK attaches
    its structured attributes through the handler's `WithAttrs` method.

=== "C#"

    The C# SDK uses `Microsoft.Extensions.Logging.ILogger` directly. There is no
    SDK-specific interface to implement; pass any `ILogger` as the `CustomLogger` on
    `LoggerConfig`.

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

=== "Go"

    Powertools for AWS Lambda has no Go version. For structured JSON output, install any
    `slog.Handler`, such as `slog.NewJSONHandler` or a third-party handler, with
    `durable.WithLogHandler`. See [Custom logger](#custom-logger).

=== "C#"

    The
    [Powertools for AWS Lambda (.NET) logger](https://docs.aws.amazon.com/powertools/dotnet/core/logging/)
    is a `Microsoft.Extensions.Logging.ILogger`. Pass it as the `CustomLogger` via
    `context.ConfigureLogger`.

    ```csharp
    --8<-- "examples/csharp/sdk-reference/observability/powertools-logger.cs"
    ```

## See also

- [Steps](../operations/step.md)
- [Child contexts](../operations/child-context.md)
- [Error handling](../error-handling/errors.md)
