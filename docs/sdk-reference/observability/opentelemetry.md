# OpenTelemetry

The OpenTelemetry plugin instruments a durable execution and emits distributed
traces to any OTLP backend, such as Amazon CloudWatch. It builds on
the [plugin interface](plugins.md): you register it in your handler
configuration, and it opens spans at the invocation, operation, and attempt
boundaries as the execution runs.

A durable execution runs across many Lambda invocations. The plugin derives
deterministic trace and span IDs from the execution ARN, and from the X-Ray
trace header when one is present, so the spans from every invocation join a
single trace. Without deterministic IDs, each invocation would produce its own
disconnected trace.

Some names on this page keep the X-Ray label (for example the X-Ray trace
header and the `awsxray` collector exporter), but your spans land in CloudWatch.

!!! note "Experimental feature"

    The OpenTelemetry plugin is experimental and may change in a future
    release. It is not recommended for production workloads yet. Feedback is
    welcome through our
    [GitHub Discussion](https://github.com/aws/aws-durable-execution-docs/discussions/206).

!!! note "C# support"

    The OpenTelemetry plugin for C# is a work in progress. Until it ships, this
    page covers TypeScript, Python, and Java.

## Install the plugin

=== "TypeScript"

    ```bash
    npm install @aws/durable-execution-sdk-js-otel
    ```

=== "Python"

    ```bash
    pip install aws-durable-execution-sdk-python-otel
    ```

=== "Java"

    ```xml
    <dependency>
        <groupId>software.amazon.lambda.durable</groupId>
        <artifactId>aws-durable-execution-sdk-java-plugin-otel</artifactId>
        <version>${durable.sdk.version}</version>
    </dependency>
    ```

## The two plugins

The package ships two plugins. Both correlate a whole durable execution into a
single trace with deterministic IDs, and both emit a `Workflow` root span for the
execution. They differ in where they root the invocation view and when they
export the root span. You register exactly one of them.

### ExecutionOtelPlugin

`ExecutionOtelPlugin` opens a synthetic `Workflow` span as the trace root. Every
invocation span and operation span nests under it. The plugin exports the
`Workflow` span only when the execution reaches a terminal status, so an
execution that is still running does not leave a dangling root span, but you also
cannot watch the trace in progress, and a span held open for hours can exceed the
ingestion limits of some observability platforms. Choose this plugin for short
executions, or when you want one unified trace per execution with the workflow as
the logical root.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/observability/opentelemetry/execution-plugin.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/observability/opentelemetry/execution-plugin.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/observability/opentelemetry/execution-plugin.java"
    ```

The trace you see groups every invocation of the execution under one `Workflow`
root:

```text
Workflow                              (root, exported on terminal status)
├── Invocation #1
├── Invocation #2
├── Operation: fetch-data  (STEP)      -> link to Invocation #1
│   └── Attempt: fetch-data attempt 1  -> link to Invocation #1
├── Operation: cooldown    (WAIT)      -> link to Invocation #2
└── Operation: process     (STEP)      -> link to Invocation #2
    └── Attempt: process attempt 1     -> link to Invocation #2
```

Operation spans link to the invocation span that produced them, so you can tell
which invocation ran each operation even though the operations root under the
`Workflow` span.

### InvocationOtelPlugin

`InvocationOtelPlugin` keeps the trace invocation-rooted. It opens a parentless
`Workflow` root span in both provider modes, keyed to a deterministic ID from the
execution ARN and ended only at the terminal invocation. The invocation span is
not a child of that `Workflow` span. Each invocation span roots its own operation
and attempt spans, and those spans carry a link to the `Workflow` span for
execution-scoped correlation.

The plugin exports each invocation span as that invocation ends. A Lambda
invocation lasts at most 15 minutes, so spans stay within platform limits and
appear as the execution runs, which renders reliably across most platforms.
Choose this plugin for long-running executions, or when you want per-invocation
traces that still correlate to one workflow and to view an execution in progress.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/observability/opentelemetry/invocation-plugin.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/observability/opentelemetry/invocation-plugin.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/observability/opentelemetry/invocation-plugin.java"
    ```

The `Workflow` span is a parentless root that operation and attempt spans link
to. Each Lambda invocation roots its own view. The example above suspends at the
`cooldown` wait, so it runs across two invocations, and the wait produces a span
in each:

```text
Workflow                              (parentless root; deterministic ID; ended at the terminal invocation)

Invocation #1                         (per-invocation root; not nested under Workflow)
├── Operation: fetch-data  (STEP)      -> link to Workflow
│   └── Attempt: fetch-data attempt 1  -> link to Workflow
└── Operation: cooldown    (WAIT)      -> link to Workflow      (wait starts; execution suspends)

Invocation #2                         (per-invocation root; resumes after the wait)
├── Operation: cooldown    (WAIT)      -> link to the first cooldown span, and to Workflow
└── Operation: process     (STEP)      -> link to Workflow
    └── Attempt: process attempt 1     -> link to Workflow
```

An operation that completes in a later invocation, such as an invoke, wait, or
callback, can appear as more than one span across invocations. Each later span
carries a link back to the first span for that operation, so a retry or a resumed
wait relates to its first appearance. CloudWatch does not yet visualize these
links.

### Choosing a plugin

The `ExecutionOtelPlugin` and `InvocationOtelPlugin` sections above cover when to
choose each. This table summarizes the differences. For both plugins, your
observability platform's quotas and limits apply.

| Consideration          | ExecutionOtelPlugin                                               | InvocationOtelPlugin                                                             |
| ---------------------- | ----------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| Trace root             | Synthetic `Workflow` span                                         | Invocation span; a `Workflow` span is also emitted and linked from operations    |
| Root span export       | Only when the execution completes                                 | Each invocation span exports when that invocation ends                           |
| Operation span         | Exported once, in its entirety, when complete                     | Can appear under multiple invocation spans                                       |
| In-progress visibility | Fragmented until the execution finishes                           | Each invocation appears as it completes                                          |
| Better for             | Short executions                                                  | Longer-running executions, or watching one in progress                           |
| Platform compatibility | A long-open root span can exceed some platforms' ingestion limits | Invocation spans stay within the 15-minute Lambda limit, so they render reliably |

## Span structure and attributes

Both plugins emit a `Workflow` root span for the whole execution. Beneath it
(ExecutionOtelPlugin) or linked to it (InvocationOtelPlugin) sit three levels of
spans. An invocation span covers one Lambda invocation. Operation spans nest one
per durable operation, such as a step, wait, or child invoke. Attempt spans nest
one per try under a step or wait-for-condition operation, so retries appear as
sibling spans.

| Span       | Attributes                                                                                                                                                   |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Workflow   | `durable.execution.arn`, `durable.execution.status`                                                                                                          |
| Invocation | `durable.execution.arn`, `durable.invocation.status`, `durable.invocation.first`                                                                             |
| Operation  | `durable.execution.arn`, `durable.operation.id`, `durable.operation.type`, `durable.operation.name`, `durable.operation.subtype`, `durable.operation.status` |
| Attempt    | `durable.execution.arn`, `durable.operation.id`, `durable.operation.type`, `durable.operation.name`, `durable.attempt.number`, `durable.attempt.outcome`     |

`durable.operation.type` is one of `STEP`, `WAIT`, `CONTEXT`, `CHAINED_INVOKE`,
or `CALLBACK`. A `CONTEXT` operation (a child context) gets an operation span but
no attempt span, since attempt spans apply only to steps and wait-for-conditions.

## Deploy with the ADOT layer

The AWS Distro for OpenTelemetry (ADOT) Lambda layer bundles OpenTelemetry
auto-instrumentation and a collector extension. The collector listens on
`localhost:4318` and forwards spans to CloudWatch. Add the layer,
enable active tracing so the runtime populates the `_X_AMZN_TRACE_ID`
header the plugin reads, and grant the function's role the
`AWSXRayDaemonWriteAccess` managed policy.

=== "TypeScript"

    Set `AWS_LAMBDA_EXEC_WRAPPER` to `/opt/otel-instrument` to activate the
    layer's instrumentation, and construct the plugin with
    `useDefaultTracerProvider: true` so it uses the layer's global tracer
    provider. Find the current ADOT JavaScript layer ARN for your region and
    architecture in the
    [ADOT Lambda layer documentation](https://aws-otel.github.io/docs/getting-started/lambda#adot-lambda-layer-arns).

    ```yaml
    MyFunction:
      Type: AWS::Serverless::Function
      Properties:
        Runtime: nodejs24.x
        Handler: index.handler
        Layers:
          - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<adot-js-layer>:<version>
        Environment:
          Variables:
            AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-instrument
        Tracing: Active
        Policies:
          - arn:aws:iam::aws:policy/service-role/AWSLambdaBasicDurableExecutionRolePolicy
          - arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess
    ```

    ```typescript
    new ExecutionOtelPlugin({ useDefaultTracerProvider: true });
    // or
    new InvocationOtelPlugin({ useDefaultTracerProvider: true });
    ```

=== "Python"

    Set `AWS_LAMBDA_EXEC_WRAPPER` to `/opt/otel-instrument`. Find the current
    layer ARN for your region and architecture in the
    [ADOT Lambda layer documentation](https://aws-otel.github.io/docs/getting-started/lambda#adot-lambda-layer-arns).

    ```yaml
    MyFunction:
      Type: AWS::Serverless::Function
      Properties:
        Runtime: python3.14
        Handler: index.handler
        Layers:
          - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<adot-python-layer>:<version>
        Environment:
          Variables:
            AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-instrument
        Tracing: Active
        Policies:
          - arn:aws:iam::aws:policy/service-role/AWSLambdaBasicDurableExecutionRolePolicy
          - arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess
    ```

    `InvocationOtelPlugin()` uses the layer's global provider by default.
    `ExecutionOtelPlugin(OtelPluginConfig(use_default_tracer_provider=True))`
    does the same.

=== "Java"

    Set `AWS_LAMBDA_EXEC_WRAPPER` to `/opt/otel-instrument` to activate the ADOT
    Java agent, and register the plugin jar as an agent extension through
    `OTEL_JAVAAGENT_EXTENSIONS` (the path to the bundled plugin jar) so its SPI
    installs deterministic ID generation into the agent's provider. Then
    construct either plugin with the no-arg constructor, which reads the agent's
    global provider. Find the current ADOT Java layer ARN for your region and
    architecture in the
    [ADOT Lambda layer documentation](https://aws-otel.github.io/docs/getting-started/lambda#adot-lambda-layer-arns).

    ```yaml
    MyFunction:
      Type: AWS::Serverless::Function
      Properties:
        Runtime: java25
        Handler: com.example.ExampleHandler
        Layers:
          - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<adot-java-layer>:<version>
        Environment:
          Variables:
            AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-instrument
            OTEL_JAVAAGENT_EXTENSIONS: /opt/otel-plugin-extension.jar
        Tracing: Active
        Policies:
          - arn:aws:iam::aws:policy/service-role/AWSLambdaBasicDurableExecutionRolePolicy
          - arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess
    ```

    ```java
    new ExecutionOtelPlugin();   // uses the agent's global provider
    new InvocationOtelPlugin();  // uses the agent's global provider
    ```

## Deploy with the community collector layer

The OpenTelemetry community collector layer runs a collector extension only,
without auto-instrumentation. The plugin
creates its own tracer provider and exports spans to the collector on
`localhost:4318`. Do not set `AWS_LAMBDA_EXEC_WRAPPER` with this layer.

Include a `collector.yaml` in your function bundle and set
`OPENTELEMETRY_COLLECTOR_CONFIG_URI` to its path:

```yaml
receivers:
  otlp:
    protocols:
      http:
        endpoint: "localhost:4318"
exporters:
  awsxray:
    region: "${AWS_REGION}"
service:
  pipelines:
    traces:
      receivers: [otlp]
      exporters: [awsxray]
```

Routing spans through a collector also lets you export to a third-party platform
such as Datadog, Honeycomb, or Grafana by changing the collector's exporter,
without first sending them to CloudWatch. Check https://github.com/open-telemetry/opentelemetry-lambda/releases
for the most recent Collector layer releases.

=== "TypeScript"

    Construct either plugin with `useDefaultTracerProvider: false` (the default),
    so it auto-creates a provider that exports to `localhost:4318`.

    ```yaml
    Layers:
      - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<collector-layer>:<version>
    Environment:
      Variables:
        OPENTELEMETRY_COLLECTOR_CONFIG_URI: /var/task/collector.yaml
    ```

    ```typescript
    new ExecutionOtelPlugin({ useDefaultTracerProvider: false });
    new InvocationOtelPlugin({ useDefaultTracerProvider: false });
    ```

=== "Python"

    `ExecutionOtelPlugin()` auto-creates a provider that exports to the collector.
    `InvocationOtelPlugin` does not auto-create a provider. It uses the globally
    configured provider, so with the community collector layer, pass an explicit
    provider through `trace_provider`, or use `ExecutionOtelPlugin`.

    ```yaml
    Layers:
      - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<collector-layer>:<version>
    Environment:
      Variables:
        OPENTELEMETRY_COLLECTOR_CONFIG_URI: /var/task/collector.yaml
    ```

=== "Java"

    Do not set `AWS_LAMBDA_EXEC_WRAPPER`. Construct either plugin with a builder
    that adds an OTLP exporter pointed at `localhost:4318`, so the plugin owns
    its provider instead of reading the agent's.

    ```yaml
    Layers:
      - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<collector-layer>:<version>
    Environment:
      Variables:
        OPENTELEMETRY_COLLECTOR_CONFIG_URI: /var/task/collector.yaml
    ```

    ```java
    var exporter = OtlpHttpSpanExporter.getDefault();
    var plugin = new ExecutionOtelPlugin(
            SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)));
    ```

## Configuration

=== "TypeScript"

    Pass an `OtelPluginConfig` to either plugin.

    - **tracerProvider** A provider to use as-is. Takes precedence over the other
        provider options.
    - **useDefaultTracerProvider** Use the globally registered provider, such as
        the ADOT layer's. Defaults to `false`.
    - **contextExtractor** Extracts upstream trace context. Defaults to
        `xRayContextExtractor`. Use `w3cClientContextExtractor` for W3C
        `traceparent` propagation. `w3cClientContextExtractor` currently does no work in Lambda.
    - **exporterConfig** OTLP `endpoint` and `headers`, used only when the plugin
        creates its own provider.
    - **propagators** Replaces the default `[AWSXRay, W3CTraceContext]`
        propagators. W3CTraceContext currently does not work in Lambda.
    - **enableHttpInstrumentation** Registers HTTP instrumentation. Defaults to
        `true`.
    - **instrumentationName** Instrumentation scope name. Defaults to
        `aws-durable-execution-sdk-js`.
    - **workflowSpanName** Name of the `Workflow` root span. Defaults to
        `Workflow`.
    - **enrichLogger** Adds `traceId`, `spanId`, and `otelTraceSampled` to each
        durable log record through `enrichLogContext()`. Defaults to `true`.

    Control sampling with the `OTEL_DURABLE_SAMPLING_RATIO` environment variable,
    from `0.0` to `1.0`. All invocations of one execution are sampled or dropped
    together.

=== "Python"

    `ExecutionOtelPlugin` takes an `OtelPluginConfig` dataclass with
    `tracer_provider`, `use_default_tracer_provider`, `context_extractor`,
    `exporter_config`, `propagators`, `enable_http_instrumentation`,
    `instrument_name`, `workflow_span_name`, and `enrich_logger`.

    `InvocationOtelPlugin` takes keyword arguments.

    - **trace_provider** A tracer provider to use. Defaults to the globally
        configured provider.
    - **context_extractor** Defaults to `xray_context_extractor`. Use
        `w3c_client_context_extractor` for W3C `traceparent` propagation. W3C
        `traceparent` propagation is currently not working in Lambda.
    - **instrument_name** Instrumentation scope name. Defaults to
        `aws-durable-execution-sdk-python`.
    - **enrich_logger** Installs a root-logger filter that stamps trace context
        onto log records. Defaults to `True`.
    - **workflow_span_name** Name of the `Workflow` root span. Defaults to
        `Workflow`.

    Control sampling through the ADOT layer with `OTEL_TRACES_SAMPLER` and
    `OTEL_TRACES_SAMPLER_ARG`.

=== "Java"

    Each plugin has a no-arg constructor that uses the ADOT Java agent's global
    provider, plus builder constructors for supplying your own provider. The
    no-arg form requires the ADOT agent and the plugin jar registered through
    `OTEL_JAVAAGENT_EXTENSIONS`.

    ```java
    new InvocationOtelPlugin();
    new InvocationOtelPlugin(tracerProviderBuilder);
    new InvocationOtelPlugin(tracerProviderBuilder, contextExtractor);
    new InvocationOtelPlugin(tracerProviderBuilder, contextExtractor, enableMdc);
    new InvocationOtelPlugin(tracerProviderBuilder, contextExtractor, enableMdc, workflowSpanName);
    ```

    `ExecutionOtelPlugin` offers the same no-arg and builder constructors.

    - **contextExtractor** Defaults to `XRayContextExtractor`.
    - **enableMdc** Injects `traceId`, `spanId`, and `otelTraceSampled` into the SLF4J
        MDC. Defaults to `true`.
    - **workflowSpanName** Name of the `Workflow` root span. Defaults to
        `Workflow`.

## Correlate logs with traces

The plugin stamps the active trace and span IDs onto your log records, so a log
line in CloudWatch links to the span that emitted it. See [Logging](logging.md)
for the SDK logger.

=== "TypeScript"

    With `enrichLogger` enabled (the default), the plugin adds `traceId`,
    `spanId`, and `otelTraceSampled` to each durable log record through the
    `enrichLogContext()` hook described in
    [Logging from a plugin](plugins.md#logging-from-a-plugin).

=== "Python"

    With `enrich_logger=True` (the default), the plugin installs a filter on the
    root logger that adds `traceId`, `spanId`, and `otelTraceSampled` to every
    record when a span is active.

=== "Java"

    With `enableMdc=true` (the default), the plugin puts `traceId`, `spanId`, and
    `otelTraceSampled` into the SLF4J MDC. Configure your logging framework to include
    MDC fields in its output.

## Verify

Invoke a durable function that includes a wait or a resume, so the execution runs
across more than one invocation. In the CloudWatch console, open Traces and
confirm the invocation and operation spans appear under one trace ID. Check that
your log entries carry `traceId`, `spanId`, and `otelTraceSampled` matching those spans.

When you use the community collector layer, enable CloudWatch Transaction Search
in your account for traces to appear. If no traces show up, the collector layer
is missing or its configuration variable is unset. If traces fragment across
several IDs, active tracing is off. If some operation spans are missing, the
sampling ratio is below `1.0`.

## See also

- [Plugins](plugins.md)
- [Logging](logging.md)
- [Steps](../operations/step.md)
