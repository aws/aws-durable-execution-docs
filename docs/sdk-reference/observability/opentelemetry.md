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

    When the plugin creates its own tracer provider (the default), install the
    OpenTelemetry packages it configures:

    ```bash
    npm install @opentelemetry/sdk-trace-node \
                @opentelemetry/exporter-trace-otlp-http \
                @opentelemetry/propagator-aws-xray \
                @opentelemetry/instrumentation-http \
                @opentelemetry/resources
    ```

    When you use the ADOT layer's global tracer provider, the layer supplies
    these packages and you only need `@opentelemetry/api`.

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

## Register the plugin

Add the plugin to your handler configuration. The SDK calls it as the execution
runs, and you write no tracing code in your handler.

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

    The package exports two plugins, `InvocationOtelPlugin` and
    `ExecutionOtelPlugin`. They register the same way. See
    [Trace structure](#trace-structure) for how they differ.

=== "Python"

    ```python
    from aws_durable_execution_sdk_python import DurableContext, durable_execution
    from aws_durable_execution_sdk_python_otel import InvocationOtelPlugin


    @durable_execution(plugins=[InvocationOtelPlugin()])
    def handler(event: dict, context: DurableContext) -> str:
        return context.step(lambda ctx: "done", name="process")
    ```

    The package exports both `InvocationOtelPlugin` and `ExecutionOtelPlugin`.
    See [Trace structure](#trace-structure) for how they differ.

=== "Java"

    The plugin constructor takes the tracer provider builder. Add a span
    exporter to it, and the plugin sets the deterministic ID generator.

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
            return context.step("process", String.class, stepCtx -> "done");
        }
    }
    ```

## Deploy with a Lambda layer

The plugin produces spans, and a collector transports them from your function to
your backend. Attach a Lambda layer that runs the collector, enable X-Ray active
tracing so the runtime populates the `_X_AMZN_TRACE_ID` header the plugin reads,
and grant the function's role X-Ray write permissions with the
`AWSXRayDaemonWriteAccess` managed policy.

Enabling active tracing matters for trace continuity. Without it,
`_X_AMZN_TRACE_ID` is unset and the plugin falls back to the execution ARN for
the trace ID. The traces still form, but resumed invocations can fragment across
separate trace IDs.

=== "TypeScript"

    Use either the AWS Distro for OpenTelemetry (ADOT) layer or the smaller
    OpenTelemetry community collector layer.

    With the ADOT layer, set `AWS_LAMBDA_EXEC_WRAPPER` to `/opt/otel-instrument`
    and construct the plugin with `useDefaultTracerProvider: true` so it uses the
    layer's global provider:

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

    With the community collector layer, do not set `AWS_LAMBDA_EXEC_WRAPPER`. The
    plugin creates its own provider and exports to the collector on
    `localhost:4318`. Include a `collector.yaml` in your bundle and set
    `OPENTELEMETRY_COLLECTOR_CONFIG_URI` to its path.

=== "Python"

    Add the ADOT layer and set `AWS_LAMBDA_EXEC_WRAPPER` to `/opt/otel-instrument`.
    Find the current layer ARN for your region and architecture in the
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

=== "Java"

    Add the ADOT layer, then enable active tracing and X-Ray permissions. Do not
    set `AWS_LAMBDA_EXEC_WRAPPER`. The wrapper attaches the auto-instrumentation
    agent, which creates a second tracer provider and splits the trace into
    disconnected service nodes on the X-Ray map.

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

## Trace structure

The plugin opens three levels of spans. An invocation span covers one Lambda
invocation of the execution. Operation spans nest under it, one per durable
operation such as a step, wait, or child invoke. Attempt spans nest under a step
or wait-for-condition operation, one per try, so retries appear as sibling
spans.

Because trace and span IDs derive from the execution ARN and each operation's ID,
an operation that starts in one invocation and completes in a later one links
back to its original span. All invocations of one execution share a single trace
ID.

Both `InvocationOtelPlugin` and `ExecutionOtelPlugin` correlate a whole execution
into one trace. They differ in the trace root.

=== "TypeScript"

    `ExecutionOtelPlugin` opens a synthetic `Workflow` span as the trace root and
    exports it only when the execution reaches a terminal status, so incomplete
    executions do not leave dangling roots. `InvocationOtelPlugin` roots the trace
    at each invocation span, and with the community collector it also opens the
    `Workflow` root. Configure both with the shared `OtelPluginConfig`.

    ```typescript
    import { ExecutionOtelPlugin } from "@aws/durable-execution-sdk-js-otel";

    const plugin = new ExecutionOtelPlugin({ useDefaultTracerProvider: true });
    ```

=== "Python"

    `ExecutionOtelPlugin` opens a `Workflow` root span and takes an
    `OtelPluginConfig`. `InvocationOtelPlugin` roots the trace at the invocation
    span and takes keyword arguments.

    ```python
    from aws_durable_execution_sdk_python_otel import (
        ExecutionOtelPlugin,
        OtelPluginConfig,
    )

    plugin = ExecutionOtelPlugin(OtelPluginConfig(use_default_tracer_provider=True))
    ```

=== "Java"

    `InvocationOtelPlugin` opens a `Workflow` root span whose name you can set
    through the constructor. The service name on the exported spans' resource is
    `invocation`.

The spans carry these attributes.

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

    `InvocationOtelPlugin` takes keyword arguments.

    - **trace_provider** A tracer provider to use. Defaults to the globally
        configured provider.
    - **context_extractor** Defaults to `xray_context_extractor`. Use
        `w3c_client_context_extractor` for W3C `traceparent` propagation.
    - **instrument_name** Instrumentation scope name. Defaults to
        `aws-durable-execution-sdk-python`.
    - **enrich_logger** Installs a root-logger filter that stamps trace context
        onto log records. Defaults to `True`.

    `ExecutionOtelPlugin` takes an `OtelPluginConfig` dataclass with the same
    provider, extractor, exporter, and `workflow_span_name` options as the
    TypeScript config.

    Control sampling through the ADOT layer with `OTEL_TRACES_SAMPLER` and
    `OTEL_TRACES_SAMPLER_ARG`.

=== "Java"

    The constructor takes the tracer provider builder and three optional
    arguments.

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
