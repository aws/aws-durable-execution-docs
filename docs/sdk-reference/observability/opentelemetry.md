# OpenTelemetry

The OpenTelemetry plugin instruments a durable execution and emits distributed
traces to any OpenTelemetry backend, such as Amazon CloudWatch. It builds on the
[plugin interface](plugins.md): you register it in your handler configuration,
and it opens spans at the workflow, invocation, operation, and attempt
boundaries.

A durable execution can run across many Lambda invocations. The plugin exposes
two correlated views:

- **Workflow view**: One execution-scoped `Workflow` trace uses stable trace and
    span IDs across all Lambda invocations. With `ExecutionOtelPlugin`, it
    contains the operation and attempt hierarchy. With `InvocationOtelPlugin`,
    it acts as a correlation root that operation and attempt spans link to. The
    `Workflow` root span is exported when the execution reaches a terminal
    status.
- **Invocation view**: Each Lambda invocation produces an `Invocation` span in
    the current Lambda or application trace. With `InvocationOtelPlugin`, it
    parents the operations and attempts from that invocation. With
    `ExecutionOtelPlugin`, those spans remain in the Workflow view and link to
    the `Invocation` span instead. The invocation view is exported when each
    Lambda invocation ends.

Span links connect the two views. The deterministic ID overrides are scoped to
spans created by the plugin, so unrelated OpenTelemetry instrumentation
continues to use the provider's normal ID generator.

Provider and export-pipeline configuration are external to the plugins. By
default, each plugin uses the global provider. For an application-owned
pipeline, supply the provider, provider factory, or provider builder and
configure its processors, exporter, sampling, resources, propagators, and
library instrumentation.

Some names on this page keep the X-Ray label, such as the X-Ray trace header and
the `awsxray` collector exporter, but the resulting traces are available in
CloudWatch.

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

    The TypeScript plugin requires Node.js 22 or later.

=== "Python"

    When a Lambda telemetry layer supplies the OpenTelemetry libraries:

    ```bash
    pip install aws-durable-execution-sdk-python-otel
    ```

    The base package intentionally does not install OpenTelemetry libraries,
    which prevents function dependencies from shadowing the version-aligned
    libraries in the layer.

    When the application configures its own provider, install the `standalone`
    extra:

    ```bash
    pip install "aws-durable-execution-sdk-python-otel[standalone]"
    ```

=== "Java"

    ```xml
    <dependency>
        <groupId>software.amazon.lambda.durable</groupId>
        <artifactId>aws-durable-execution-sdk-java-plugin-otel</artifactId>
        <version>${durable.sdk.version}</version>
    </dependency>
    ```

    Add an exporter dependency when the application supplies its own tracer
    provider. The ADOT Java agent supplies the export pipeline for the no-arg
    plugin constructors.

## The two plugins

Both plugins emit the same span types and a deterministic `Workflow` root span.
They differ in the parent of operation spans and when those spans become
visible. Register exactly one of them.

### ExecutionOtelPlugin

`ExecutionOtelPlugin` provides a workflow-centered view. Operation spans are
children of the `Workflow` span and link to the `Invocation` span that observed
them. The `Workflow` span is exported only when the execution reaches a terminal
status. An operation that suspends is also left unexported until it completes.

Choose this plugin for shorter executions or when the durable workflow hierarchy
is more important than seeing each invocation as it completes. Long-open
workflow spans can exceed the ingestion limits of some observability platforms.

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

With an ADOT or global provider, the workflow and invocation views are separate
traces:

```text
Durable workflow trace:
Workflow                              (root; exported on terminal status)
|-- Operation: fetch-data  (STEP)      -> link to Invocation #1
|   `-- Attempt: fetch-data attempt 1  -> link to Invocation #1
|-- Operation: cooldown    (WAIT)      -> link to Invocation #2
`-- Operation: process     (STEP)      -> link to Invocation #2
    `-- Attempt: process attempt 1     -> link to Invocation #2

Ambient Lambda trace #1:
Lambda invocation
`-- Invocation #1

Ambient Lambda trace #2:
Lambda invocation
`-- Invocation #2
```

The operation links identify which invocation ran each part of the workflow.
The invocation spans are not children of `Workflow`.

!!! note "TypeScript application-owned provider"

    When TypeScript `ExecutionOtelPlugin` uses `tracerProviderFactory` and no
    ambient trace exists, its `Invocation` span is created under `Workflow`.
    The global-provider and ADOT path keeps the invocation in the ambient Lambda
    trace as shown above.

### InvocationOtelPlugin

`InvocationOtelPlugin` provides an invocation-centered view. Each `Invocation`
span parents the operation and attempt spans emitted during that Lambda
invocation. Those operation and attempt spans link to the deterministic
`Workflow` span for execution-scoped correlation.

The plugin exports an invocation and its child spans when that invocation ends.
Choose it for long-running executions or when you want traces to appear as the
execution progresses.

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

For an execution that suspends at a wait, the traces resemble:

```text
Durable workflow trace:
Workflow                              (root; exported on terminal status)

Ambient Lambda trace #1:
Lambda invocation
`-- Invocation #1
    |-- Operation: fetch-data  (STEP)      -> link to Workflow
    |   `-- Attempt: fetch-data attempt 1  -> link to Workflow
    `-- Operation: cooldown    (WAIT)      -> link to Workflow

Ambient Lambda trace #2:
Lambda invocation
`-- Invocation #2
    |-- Operation: cooldown    (WAIT)      -> link to Workflow
    `-- Operation: process     (STEP)      -> link to Workflow
        `-- Attempt: process attempt 1     -> link to Workflow
```

An operation that crosses an invocation boundary can produce more than one
span. All SDKs correlate those spans through the `Workflow` span. TypeScript may
also emit a deterministic operation link; Python and Java do not.

### Choosing a plugin

For both plugins, your observability platform's quotas and limits apply.

| Consideration          | ExecutionOtelPlugin                                       | InvocationOtelPlugin                                 |
| ---------------------- | --------------------------------------------------------- | ---------------------------------------------------- |
| Operation parent       | Deterministic `Workflow` trace                            | Current `Invocation` trace                           |
| Cross-view link        | Operation or attempt to `Invocation`                      | Operation or attempt to `Workflow`                   |
| Workflow export        | On terminal execution status                              | On terminal execution status                         |
| Operation visibility   | When the operation completes                              | At each invocation boundary                          |
| In-progress visibility | Limited until operations and the execution complete       | Each invocation appears as it completes              |
| Better for             | Short executions and a workflow-centered hierarchy        | Long executions and an invocation-centered hierarchy |
| Platform compatibility | Long-open spans can exceed platform ingestion time limits | Spans stay within the Lambda invocation time limit   |

## Span structure and attributes

Both plugins emit four span types. An invocation span covers one Lambda
invocation. Operation spans cover durable operations such as steps, waits, child
contexts, callbacks, and chained invokes. Attempt spans cover user function
attempts, so retries appear as sibling spans.

| Span       | Attributes                                                                                                                                                   |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Workflow   | `durable.execution.arn`, `durable.execution.status`                                                                                                          |
| Invocation | `durable.execution.arn`, `durable.invocation.status`, `durable.invocation.first`                                                                             |
| Operation  | `durable.execution.arn`, `durable.operation.id`, `durable.operation.type`, `durable.operation.name`, `durable.operation.subtype`, `durable.operation.status` |
| Attempt    | `durable.execution.arn`, `durable.operation.id`, `durable.operation.type`, `durable.operation.name`, `durable.attempt.number`, `durable.attempt.outcome`     |

`durable.operation.type` includes `STEP`, `WAIT`, `CONTEXT`, `CHAINED_INVOKE`,
and `CALLBACK`. A `CONTEXT` operation gets an operation span but not a separate
attempt span.

## Deploy with the ADOT layer

The AWS Distro for OpenTelemetry (ADOT) Lambda layer supplies a global provider,
auto-instrumentation, and a collector extension. Add the language-specific
layer, set `AWS_LAMBDA_EXEC_WRAPPER=/opt/otel-instrument`, enable active tracing
so invocation spans can inherit the Lambda trace, and grant the function role
the `AWSXRayDaemonWriteAccess` managed policy.

The no-arg plugin constructors use the global provider. You do not need a
provider option.

=== "TypeScript"

    Find the current ADOT JavaScript layer ARN for your region and architecture
    in the
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
    new ExecutionOtelPlugin();
    // or
    new InvocationOtelPlugin();
    ```

=== "Python"

    Find the current ADOT Python layer ARN for your region and architecture in
    the
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

    ```python
    ExecutionOtelPlugin()
    # or
    InvocationOtelPlugin()
    ```

=== "Java"

    The Java agent must also load the plugin JAR as an agent extension so its
    auto-configuration SPI can scope deterministic ID generation to durable
    spans. Package the JAR in a layer under `java/lib`, then point
    `OTEL_JAVAAGENT_EXTENSIONS` to the deployed path.

    Find the current ADOT Java layer ARN in the
    [ADOT Java instrumentation releases](https://github.com/aws-observability/aws-otel-java-instrumentation/releases/latest).

    ```yaml
    MyFunction:
      Type: AWS::Serverless::Function
      Properties:
        Runtime: java25
        Handler: com.example.ExampleHandler
        Layers:
          - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<adot-java-layer>:<version>
          - <otel-plugin-layer-arn>
        Environment:
          Variables:
            AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-instrument
            OTEL_JAVAAGENT_EXTENSIONS: /opt/java/lib/aws-durable-execution-sdk-java-plugin-otel-<version>.jar
        Tracing: Active
        Policies:
          - arn:aws:iam::aws:policy/service-role/AWSLambdaBasicDurableExecutionRolePolicy
          - arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess
    ```

    ```java
    new ExecutionOtelPlugin();
    // or
    new InvocationOtelPlugin();
    ```

    To load the plugin from the layer without registering it in handler code,
    also set `DURABLE_EXECUTION_PLUGINS` to `otel-execution` or
    `otel-invocation`.

If the global SDK provider is not ready at plugin construction, the plugins
retry provider resolution at invocation start. Python and Java disable
telemetry for that invocation when a compatible SDK provider is still
unavailable, then retry on the next invocation. TypeScript emits no exported
spans when no SDK provider is registered. If TypeScript finds a registered
tracer whose runtime ID generator is incompatible, it logs the problem once
and continues without deterministic durable IDs.

## Deploy with the community collector layer

The OpenTelemetry community collector layer runs a collector extension without
auto-instrumentation. Do not set `AWS_LAMBDA_EXEC_WRAPPER`. Configure an
application-owned provider that exports OTLP HTTP spans to
`http://localhost:4318/v1/traces`, and pass that provider to the plugin using the
language-specific API below.

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

Add the collector layer and configuration variable:

```yaml
Layers:
  - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<collector-layer>:<version>
Environment:
  Variables:
    OPENTELEMETRY_COLLECTOR_CONFIG_URI: /var/task/collector.yaml
```

See the
[OpenTelemetry Lambda releases](https://github.com/open-telemetry/opentelemetry-lambda/releases)
for current collector layer releases.

=== "TypeScript"

    `tracerProviderFactory` receives a function that creates the plugin's scoped
    deterministic ID generator. Use it when constructing the provider. The
    application owns provider registration and shutdown.

    ```bash
    npm install @opentelemetry/exporter-trace-otlp-http
    ```

    ```typescript
    import { ExecutionOtelPlugin } from "@aws/durable-execution-sdk-js-otel";
    import { OTLPTraceExporter } from "@opentelemetry/exporter-trace-otlp-http";
    import {
      BatchSpanProcessor,
      NodeTracerProvider,
    } from "@opentelemetry/sdk-trace-node";

    const plugin = new ExecutionOtelPlugin({
      tracerProviderFactory: (createIdGenerator) => {
        const provider = new NodeTracerProvider({
          idGenerator: createIdGenerator(),
          spanProcessors: [
            new BatchSpanProcessor(
              new OTLPTraceExporter({
                url: "http://localhost:4318/v1/traces",
              }),
            ),
          ],
        });
        provider.register();
        return provider;
      },
    });
    ```

    Substitute `InvocationOtelPlugin` to use the invocation-centered view. To
    preserve a custom application ID generator for unrelated spans, pass it to
    `createIdGenerator(applicationIdGenerator)`.

=== "Python"

    Install the standalone dependencies and an OTLP HTTP exporter:

    ```bash
    pip install "aws-durable-execution-sdk-python-otel[standalone]" \
      opentelemetry-exporter-otlp-proto-http
    ```

    Pass the application-owned provider through the shared configuration
    object:

    ```python
    from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
    from opentelemetry.sdk.trace import TracerProvider
    from opentelemetry.sdk.trace.export import BatchSpanProcessor

    from aws_durable_execution_sdk_python_otel import (
        ExecutionOtelPlugin,
        OtelPluginConfig,
    )

    provider = TracerProvider()
    provider.add_span_processor(
        BatchSpanProcessor(
            OTLPSpanExporter(endpoint="http://localhost:4318/v1/traces")
        )
    )

    plugin = ExecutionOtelPlugin(
        OtelPluginConfig(tracer_provider=provider)
    )
    ```

    Substitute `InvocationOtelPlugin` to use the invocation-centered view.

=== "Java"

    Add the OpenTelemetry OTLP exporter dependency, then pass a provider builder
    to either plugin. The plugin wraps the builder's ID generator and builds the
    application-owned provider.

    ```xml
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporter-otlp</artifactId>
        <version>${opentelemetry.version}</version>
    </dependency>
    ```

    ```java
    import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
    import io.opentelemetry.sdk.trace.SdkTracerProvider;
    import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
    import software.amazon.lambda.durable.otel.ExecutionOtelPlugin;

    var exporter = OtlpHttpSpanExporter.builder()
            .setEndpoint("http://localhost:4318/v1/traces")
            .build();

    var providerBuilder = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(exporter).build());

    var plugin = new ExecutionOtelPlugin(providerBuilder);
    ```

    Substitute `InvocationOtelPlugin` to use the invocation-centered view.

Routing spans through a collector also lets you export to a third-party platform
by changing the collector exporter.

## Configuration

=== "TypeScript"

    Both plugins accept an `OtelPluginConfig` object:

    - **tracerProviderFactory** Creates an application-owned provider. Omit it
        to use the global provider.
    - **contextExtractor** Extracts upstream trace context. Defaults to
        `xRayContextExtractor`. Use `w3cClientContextExtractor` for W3C
        `traceparent` data in Lambda client context.
    - **instrumentationName** Instrumentation scope name. Defaults to
        `aws-durable-execution-sdk-js`.
    - **workflowSpanName** Name of the `Workflow` root span. Defaults to
        `Workflow`.
    - **enrichLogger** Adds `traceId`, `spanId`, and `otelTraceSampled` to each
        durable log record. Defaults to `true`.

    The application or ADOT layer owns exporters, propagators, resources,
    sampling, and HTTP or AWS SDK instrumentation.

=== "Python"

    Both plugins accept the same `OtelPluginConfig` dataclass:

    - **tracer_provider** Application-owned provider. Omit it to use the global
        provider.
    - **context_extractor** Defaults to `xray_context_extractor`. Use
        `w3c_client_context_extractor` for W3C `traceparent` data in Lambda
        client context.
    - **instrument_name** Instrumentation scope name. Defaults to
        `aws-durable-execution-sdk-python`.
    - **workflow_span_name** Name of the `Workflow` root span. Defaults to
        `Workflow`.
    - **enrich_logger** Installs a root-logger filter that stamps trace context
        onto log records. Defaults to `True`.

    ```python
    plugin = InvocationOtelPlugin(
        OtelPluginConfig(
            instrument_name="my-service",
            enrich_logger=False,
        )
    )
    ```

=== "Java"

    Each plugin supports the following constructor forms:

    ```java
    new InvocationOtelPlugin();
    new InvocationOtelPlugin(otelPluginConfig);
    new InvocationOtelPlugin(tracerProviderBuilder);
    new InvocationOtelPlugin(tracerProviderBuilder, otelPluginConfig);
    ```

    `ExecutionOtelPlugin` provides the same forms. The no-arg and config-only
    constructors use the global provider. The builder constructors create an
    application-owned provider.

    Build `OtelPluginConfig` with:

    - **contextExtractor(...)** Defaults to `new XRayContextExtractor()`.
    - **enableMdc(...)** Adds `traceId`, `spanId`, and `otelTraceSampled` to the
        SLF4J MDC. Defaults to `true`.
    - **workflowSpanName(...)** Defaults to `Workflow`.
    - **instrumentationName(...)** Defaults to
        `aws-durable-execution-sdk-java`.

    ```java
    var config = OtelPluginConfig.builder()
            .instrumentationName("my-service")
            .enableMdc(false)
            .build();

    var plugin = new InvocationOtelPlugin(config);
    ```

Configure sampling through the provider. With ADOT, use the standard
`OTEL_TRACES_SAMPLER` and `OTEL_TRACES_SAMPLER_ARG` environment variables.

## Load plugins dynamically

The SDK can discover a plugin from a Lambda layer or installed package without
handler registration code. Dynamic loading uses default configuration and
therefore requires a global OpenTelemetry provider.

=== "TypeScript"

    ```text
    DURABLE_EXECUTION_PLUGINS=@aws/durable-execution-sdk-js-otel/otel-invocation
    ```

    Use `@aws/durable-execution-sdk-js-otel/otel-execution` for the
    workflow-centered plugin.

=== "Python"

    ```text
    DURABLE_EXECUTION_PLUGINS=otel-invocation
    ```

    Use `otel-execution` for the workflow-centered plugin.

=== "Java"

    ```text
    DURABLE_EXECUTION_PLUGINS=otel-invocation
    ```

    Use `otel-execution` for the workflow-centered plugin. Put the plugin JAR
    under `java/lib` in the layer so it is on the Lambda class path.

## Correlate logs with traces

The plugin stamps active trace and span IDs onto log records. See
[Logging](logging.md) for the SDK logger.

=== "TypeScript"

    With `enrichLogger` enabled, the plugin adds `traceId`, `spanId`, and
    `otelTraceSampled` to each durable log record through the
    `enrichLogContext()` hook described in
    [Logging from a plugin](plugins.md#logging-from-a-plugin).

=== "Python"

    With `enrich_logger=True`, the plugin installs a filter on root logger
    handlers. It adds `traceId`, `spanId`, and `otelTraceSampled` when a valid
    span is active. Treat these fields as optional in your formatter.

=== "Java"

    With `enableMdc(true)`, the plugin puts `traceId`, `spanId`, and
    `otelTraceSampled` into the SLF4J MDC. Configure your logging framework to
    include MDC fields in its output.

## Verify

Invoke a durable function that includes a wait or resume so it runs across more
than one Lambda invocation. In CloudWatch Traces, expect:

- A deterministic `Workflow` trace exported when the execution completes.
- One ambient Lambda trace per invocation, containing the plugin's `Invocation`
    span.
- Operation links between the workflow and invocation views.
- Log records whose trace fields match the active invocation, operation, or
    attempt span.

For Java, use the CloudWatch **Group by nodes** view to inspect the hierarchy.
The ungrouped X-Ray Segments Timeline cannot attach OTLP-exported spans beneath
the Lambda platform segment.

When you use the community collector layer, enable CloudWatch Transaction
Search in your account for traces to appear.

If no plugin spans appear:

- Confirm a provider with a span processor and exporter is registered.
- With ADOT, confirm `AWS_LAMBDA_EXEC_WRAPPER` and active tracing are enabled.
- With the community collector, confirm the layer and
    `OPENTELEMETRY_COLLECTOR_CONFIG_URI` are configured.
- For Java ADOT, confirm the plugin JAR is listed in
    `OTEL_JAVAAGENT_EXTENSIONS`.
- Check provider sampling configuration when only some executions appear.

## See also

- [Plugins](plugins.md)
- [Logging](logging.md)
- [Steps](../operations/step.md)
