# OpenTelemetry

The OpenTelemetry plugin instruments a durable execution and emits distributed
traces to any OTLP backend, such as Amazon CloudWatch or AWS X-Ray. It builds on
the [plugin interface](plugins.md): you register it in your handler
configuration, and it opens spans at the invocation, operation, and attempt
boundaries as the execution runs.

A durable execution runs across many Lambda invocations. The plugin derives
deterministic trace and span IDs from the execution ARN, and from the X-Ray
trace header when one is present, so the spans from every invocation join a
single trace. Without deterministic IDs, each invocation would produce its own
disconnected trace.

!!! note "Experimental feature"

    The OpenTelemetry plugin is experimental and may change in a future
    release. It is not recommended for production workloads yet. Feedback is
    welcome through our
    [GitHub Discussion](https://github.com/aws/aws-durable-execution-docs/discussions/206).

!!! note "C# support"

    The .NET plugin ships when the AWS Lambda Durable Execution SDK for C#
    becomes generally available. Until then, this page covers TypeScript,
    Python, and Java.

## Install the plugin

=== "TypeScript"

    ```bash
    npm install @aws/durable-execution-sdk-js-otel
    ```

    When a plugin creates its own tracer provider, install the OpenTelemetry
    packages it configures:

    ```bash
    npm install @opentelemetry/sdk-trace-node \
                @opentelemetry/exporter-trace-otlp-http \
                @opentelemetry/propagator-aws-xray \
                @opentelemetry/instrumentation-http \
                @opentelemetry/resources
    ```

    When a plugin uses the ADOT layer's global tracer provider, the layer
    supplies these packages and you only need `@opentelemetry/api`.

=== "Python"

    ```bash
    pip install aws-durable-execution-sdk-python-otel
    ```

    The package depends on `opentelemetry-api`, `opentelemetry-sdk`, and
    `opentelemetry-exporter-otlp`.

=== "Java"

    ```xml
    <dependency>
        <groupId>software.amazon.lambda.durable</groupId>
        <artifactId>aws-durable-execution-sdk-java-plugin-otel</artifactId>
        <version>${durable.sdk.version}</version>
    </dependency>
    ```

    Add the OpenTelemetry SDK and an exporter:

    ```xml
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk</artifactId>
        <version>1.63.0</version>
    </dependency>
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporter-otlp</artifactId>
        <version>1.63.0</version>
    </dependency>
    ```

## The two plugins

The package ships two plugins. Both correlate a whole durable execution into a
single trace with deterministic IDs. They differ in where they root the trace
and when they export the root span. You register exactly one of them.

### ExecutionOtelPlugin

`ExecutionOtelPlugin` opens a synthetic `Workflow` span as the trace root. Every
invocation span and operation span nests under it. The plugin exports the
`Workflow` span only when the execution reaches a terminal status, so an
execution that is still running does not leave a dangling root span. Choose this
plugin when you want one unified trace per execution, with the workflow as the
logical root.

=== "TypeScript"

    ```typescript
    import { withDurableExecution } from "@aws/durable-execution-sdk-js";
    import { ExecutionOtelPlugin } from "@aws/durable-execution-sdk-js-otel";

    export const handler = withDurableExecution(
      async (event, context) => {
        const data = await context.step("fetch-data", async () => fetch(event.id));
        await context.wait("cooldown", { seconds: 5 });
        return await context.step("process", async () => transform(data));
      },
      { plugins: [new ExecutionOtelPlugin()] },
    );
    ```

=== "Python"

    ```python
    from aws_durable_execution_sdk_python import DurableContext, durable_execution
    from aws_durable_execution_sdk_python_otel import ExecutionOtelPlugin


    @durable_execution(plugins=[ExecutionOtelPlugin()])
    def handler(event: dict, context: DurableContext) -> dict:
        data = context.step(lambda ctx: fetch(event["id"]), name="fetch-data")
        context.wait(duration=Duration.from_seconds(5))
        return context.step(lambda ctx: transform(data), name="process")
    ```

=== "Java"

    ```java
    import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
    import io.opentelemetry.sdk.trace.SdkTracerProvider;
    import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
    import software.amazon.lambda.durable.DurableConfig;
    import software.amazon.lambda.durable.DurableContext;
    import software.amazon.lambda.durable.DurableHandler;
    import software.amazon.lambda.durable.otel.ExecutionOtelPlugin;

    public class ExampleHandler extends DurableHandler<Event, String> {
        @Override
        protected DurableConfig createConfiguration() {
            var plugin = new ExecutionOtelPlugin(
                    SdkTracerProvider.builder()
                            .addSpanProcessor(
                                    SimpleSpanProcessor.create(OtlpGrpcSpanExporter.getDefault())));
            return DurableConfig.builder().withPlugins(plugin).build();
        }

        @Override
        protected String handleRequest(Event event, DurableContext context) {
            var data = context.step("fetch-data", String.class, ctx -> fetch(event.id()));
            context.wait("cooldown", Duration.ofSeconds(5));
            return context.step("process", String.class, ctx -> transform(data));
        }
    }
    ```

The trace you see groups every invocation of the execution under one `Workflow`
root:

```text
Workflow                          (root, exported on terminal status)
├── Invocation                    (one span per Lambda invocation)
├── Operation: fetch-data  (STEP)
│   └── Attempt: fetch-data attempt 1
├── Operation: cooldown    (WAIT)
└── Operation: process     (STEP)
    └── Attempt: process attempt 1
```

Operation spans link to the invocation span that produced them, so you can tell
which invocation ran each operation even though the operations root under the
`Workflow` span.

### InvocationOtelPlugin

`InvocationOtelPlugin` is lighter. It roots each trace at the invocation span and
attaches operation spans directly to it. With the community collector layer it
also opens a `Workflow` root span, exported only on terminal status, so you still
get one unified trace. Choose this plugin when you want per-invocation traces, or
when you delegate span creation to the ADOT layer.

=== "TypeScript"

    ```typescript
    import { withDurableExecution } from "@aws/durable-execution-sdk-js";
    import { InvocationOtelPlugin } from "@aws/durable-execution-sdk-js-otel";

    export const handler = withDurableExecution(
      async (event, context) => {
        return await context.step("process", async () => "done");
      },
      { plugins: [new InvocationOtelPlugin()] },
    );
    ```

=== "Python"

    ```python
    from aws_durable_execution_sdk_python import DurableContext, durable_execution
    from aws_durable_execution_sdk_python_otel import InvocationOtelPlugin


    @durable_execution(plugins=[InvocationOtelPlugin()])
    def handler(event: dict, context: DurableContext) -> str:
        return context.step(lambda ctx: "done", name="process")
    ```

=== "Java"

    ```java
    import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
    import io.opentelemetry.sdk.trace.SdkTracerProvider;
    import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
    import software.amazon.lambda.durable.DurableConfig;
    import software.amazon.lambda.durable.DurableContext;
    import software.amazon.lambda.durable.DurableHandler;
    import software.amazon.lambda.durable.otel.InvocationOtelPlugin;

    public class ExampleHandler extends DurableHandler<Object, String> {
        @Override
        protected DurableConfig createConfiguration() {
            var plugin = new InvocationOtelPlugin(
                    SdkTracerProvider.builder()
                            .addSpanProcessor(
                                    SimpleSpanProcessor.create(OtlpGrpcSpanExporter.getDefault())));
            return DurableConfig.builder().withPlugins(plugin).build();
        }

        @Override
        protected String handleRequest(Object event, DurableContext context) {
            return context.step("process", String.class, ctx -> "done");
        }
    }
    ```

The trace you see roots at the invocation span:

```text
Workflow                          (community collector only; terminal status)
└── Invocation                    (trace root; one span per Lambda invocation)
    ├── Operation: fetch-data  (STEP)
    │   └── Attempt: fetch-data attempt 1
    ├── Operation: cooldown    (WAIT)
    └── Operation: process     (STEP)
```

Operations that resume in a later invocation link back to the original
operation's deterministic span ID, so a retry or a resumed wait relates to its
first appearance.

## Span structure and attributes

Both plugins open three levels of spans. An invocation span covers one Lambda
invocation. Operation spans nest one per durable operation, such as a step, wait,
or child invoke. Attempt spans nest one per try under a step or
wait-for-condition operation, so retries appear as sibling spans.

| Span       | Attributes                                                                                                                                                   |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Invocation | `durable.execution.arn`, `durable.invocation.status`, `durable.invocation.first`                                                                             |
| Operation  | `durable.execution.arn`, `durable.operation.id`, `durable.operation.type`, `durable.operation.name`, `durable.operation.subtype`, `durable.operation.status` |
| Attempt    | `durable.execution.arn`, `durable.operation.id`, `durable.operation.type`, `durable.operation.name`, `durable.attempt.number`, `durable.attempt.outcome`     |

`durable.operation.type` is one of `STEP`, `WAIT`, `CONTEXT`, `CHAINED_INVOKE`,
or `CALLBACK`. The plugin also attaches standard FaaS resource attributes, such
as the function name and region, from the Lambda environment.

=== "Java"

    Attempt spans are not opened for `CONTEXT` operations, so a child context
    contributes an operation span but no attempt span.

## Deploy with the ADOT layer

The AWS Distro for OpenTelemetry (ADOT) Lambda layer bundles OpenTelemetry
auto-instrumentation and a collector extension. The collector listens on
`localhost:4318` and forwards spans to X-Ray and CloudWatch. Add the layer,
enable X-Ray active tracing so the runtime populates the `_X_AMZN_TRACE_ID`
header the plugin reads, and grant the function's role the
`AWSXRayDaemonWriteAccess` managed policy.

=== "TypeScript"

    Set `AWS_LAMBDA_EXEC_WRAPPER` to `/opt/otel-instrument` to activate the
    layer's instrumentation, and construct the plugin with
    `useDefaultTracerProvider: true` so it uses the layer's global tracer
    provider.

    ```yaml
    MyFunction:
      Type: AWS::Serverless::Function
      Properties:
        Runtime: nodejs22.x
        Handler: index.handler
        Layers:
          - !Sub arn:aws:lambda:${AWS::Region}:615299751070:layer:AWSOpenTelemetryDistroJs:7
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
    [ADOT Lambda layer documentation](https://aws-otel.github.io/docs/getting-started/lambda/lambda-python).

    ```yaml
    MyFunction:
      Type: AWS::Serverless::Function
      Properties:
        Runtime: python3.12
        Handler: index.handler
        Layers:
          - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:aws-otel-python-amd64-ver-<version>
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

    Add the layer for its collector, but do not set `AWS_LAMBDA_EXEC_WRAPPER`.
    The Java plugin always builds its own tracer provider from the builder you
    pass, and the wrapper would attach a second, competing provider that splits
    the trace into disconnected service nodes on the X-Ray map. Point the
    plugin's exporter at the layer's collector on `localhost:4318`.

    ```yaml
    MyFunction:
      Type: AWS::Serverless::Function
      Properties:
        Runtime: java17
        Handler: com.example.ExampleHandler
        Layers:
          - !Sub arn:aws:lambda:${AWS::Region}:901920570463:layer:aws-otel-java-agent-amd64-ver-1-32-0:6
        Tracing: Active
        Policies:
          - arn:aws:iam::aws:policy/service-role/AWSLambdaBasicDurableExecutionRolePolicy
          - arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess
    ```

**With ExecutionOtelPlugin,** the plugin still opens the `Workflow` root span and
exports it on terminal status. When it uses the ADOT layer's global provider, it
does not open its own invocation span. It links operations to the layer's
ambient invocation span instead.

**With InvocationOtelPlugin,** the plugin delegates span creation to the ADOT
layer's provider and does not open its own `Workflow` or invocation span.
Operations attach to the layer's ambient invocation span, so you get
per-invocation traces correlated by deterministic span IDs rather than one
`Workflow`-rooted trace.

## Deploy with the community collector layer

The OpenTelemetry community collector layer runs a collector extension only,
without auto-instrumentation, so it is smaller than the ADOT layer. The plugin
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
without first sending them to X-Ray.

=== "TypeScript"

    Construct either plugin with no provider option. It auto-creates a provider
    that exports to `localhost:4318`.

    ```yaml
    Layers:
      - !Sub arn:aws:lambda:${AWS::Region}:184161586896:layer:opentelemetry-nodejs-0_22_0:1
    Environment:
      Variables:
        OPENTELEMETRY_COLLECTOR_CONFIG_URI: /var/task/collector.yaml
    ```

    ```typescript
    new ExecutionOtelPlugin();   // auto-creates a provider
    new InvocationOtelPlugin();  // auto-creates a provider
    ```

=== "Python"

    `ExecutionOtelPlugin()` auto-creates a provider that exports to the collector.
    `InvocationOtelPlugin` does not auto-create a provider. It uses the globally
    configured provider, so with the community collector layer, pass an explicit
    provider through `trace_provider`, or use `ExecutionOtelPlugin`.

    ```yaml
    Layers:
      - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<collector-layer>
    Environment:
      Variables:
        OPENTELEMETRY_COLLECTOR_CONFIG_URI: /var/task/collector.yaml
    ```

=== "Java"

    Both plugins already build their own provider from the builder you pass, so
    add an OTLP exporter pointed at `localhost:4318`. This is the same wiring
    shown in [The two plugins](#the-two-plugins).

    ```yaml
    Layers:
      - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<collector-layer>
    Environment:
      Variables:
        OPENTELEMETRY_COLLECTOR_CONFIG_URI: /var/task/collector.yaml
    ```

**With ExecutionOtelPlugin,** you get one `Workflow`-rooted trace per execution.
The plugin exports the `Workflow` span only on terminal status, so intermediate
invocations do not emit incomplete workflow spans.

**With InvocationOtelPlugin,** the plugin opens a synthetic `Workflow` root span
with a deterministic ID and an invocation span as its child. It exports the
`Workflow` span only on terminal status, so you still get one clean trace across
invocations.

## Configuration

=== "TypeScript"

    Pass an `OtelPluginConfig` to either plugin.

    - **tracerProvider** A provider to use as-is. Takes precedence over the other
        provider options.
    - **useDefaultTracerProvider** Use the globally registered provider, such as
        the ADOT layer's. Defaults to `false`.
    - **contextExtractor** Extracts upstream trace context. Defaults to
        `xRayContextExtractor`. Use `w3cClientContextExtractor` for W3C
        `traceparent` propagation.
    - **exporterConfig** OTLP `endpoint` and `headers`, used only when the plugin
        creates its own provider.
    - **propagators** Replaces the default `[AWSXRay, W3CTraceContext]`
        propagators.
    - **enableHttpInstrumentation** Registers HTTP instrumentation. Defaults to
        `true`.
    - **instrumentationName** Instrumentation scope name. Defaults to
        `aws-durable-execution-sdk-js`.
    - **workflowSpanName** Name of the `Workflow` root span. Defaults to
        `Workflow`.

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
        `w3c_client_context_extractor` for W3C `traceparent` propagation.
    - **instrument_name** Instrumentation scope name. Defaults to
        `aws-durable-execution-sdk-python`.
    - **enrich_logger** Installs a root-logger filter that stamps trace context
        onto log records. Defaults to `True`.

    Control sampling through the ADOT layer with `OTEL_TRACES_SAMPLER` and
    `OTEL_TRACES_SAMPLER_ARG`.

=== "Java"

    Each plugin's constructor takes the tracer provider builder and three
    optional arguments.

    ```java
    new InvocationOtelPlugin(tracerProviderBuilder);
    new InvocationOtelPlugin(tracerProviderBuilder, contextExtractor);
    new InvocationOtelPlugin(tracerProviderBuilder, contextExtractor, enableMdc);
    new InvocationOtelPlugin(tracerProviderBuilder, contextExtractor, enableMdc, workflowSpanName);
    ```

    - **contextExtractor** Defaults to `XRayContextExtractor`.
    - **enableMdc** Injects `traceId`, `spanId`, and `traceSampled` into the SLF4J
        MDC. Defaults to `true`.
    - **workflowSpanName** Name of the `Workflow` root span. Defaults to
        `Workflow`.

## Correlate logs with traces

The plugin stamps the active trace and span IDs onto your log records, so a log
line in CloudWatch links to the span that emitted it. See [Logging](logging.md)
for the SDK logger.

=== "TypeScript"

    The plugin enriches log context with `traceId` and `spanId` through the
    plugin log hook described in [Logging from a plugin](plugins.md#logging-from-a-plugin).

=== "Python"

    With `enrich_logger=True` (the default), the plugin installs a filter on the
    root logger that adds `traceId`, `spanId`, and `otelTraceSampled` to every
    record when a span is active.

=== "Java"

    With `enableMdc=true` (the default), the plugin puts `traceId`, `spanId`, and
    `traceSampled` into the SLF4J MDC. Configure your logging framework to include
    MDC fields in its output.

## Verify

Invoke a durable function that includes a wait or a resume, so the execution runs
across more than one invocation. In the CloudWatch console, open Traces and
confirm the invocation and operation spans appear under one trace ID. Check that
your log entries carry `traceId` and `spanId` matching those spans.

When you use the community collector layer, enable CloudWatch Transaction Search
in your account for traces to appear. If no traces show up, the collector layer
is missing or its configuration variable is unset. If traces fragment across
several IDs, active tracing is off. If some operation spans are missing, the
sampling ratio is below `1.0`.

## See also

- [Plugins](plugins.md)
- [Logging](logging.md)
- [Steps](../operations/step.md)
