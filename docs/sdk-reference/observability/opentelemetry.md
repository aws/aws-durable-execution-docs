# OpenTelemetry

The OpenTelemetry plugin instruments a durable execution and emits distributed
traces to any OpenTelemetry backend, such as Amazon CloudWatch. It builds on the
[plugin interface](plugins.md): you register it in your handler configuration,
and it opens spans at the workflow, invocation, operation, and attempt
boundaries.

A durable execution can run across many Lambda invocations. The durable backend
propagates a stable server span through `_X_AMZN_TRACE_ID`. When the header
contains valid `Root` and `Parent` fields, every plugin span for one execution
uses that canonical trace ID. The execution-scoped `Workflow` span is a direct
child of the remote backend span and keeps a deterministic span ID across
reinvocations.

Each Lambda invocation also produces an `Invocation` span. It uses the active
ambient Lambda span as its parent only when that span belongs to the canonical
trace. Otherwise, it is a direct child of the remote backend span. This places
the workflow, invocations, operations, and attempts in one trace.

The two plugins provide different operation views within that trace:

- **Workflow view**: `ExecutionOtelPlugin` parents operations to `Workflow`.
    Operations and attempts link to the `Invocation` span that observed them.
- **Invocation view**: `InvocationOtelPlugin` parents each operation segment to
    the current `Invocation`. Operations and attempts link to `Workflow`.
    Continuation and replay segments also link to the initial logical operation
    span.

Links provide correlation and do not replace parent-child relationships. The
deterministic ID overrides are scoped to spans created by the plugin, so
unrelated OpenTelemetry instrumentation continues to use the provider's normal
ID generator.

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
    npm install @aws/durable-execution-sdk-js-otel \
      @opentelemetry/api \
      @opentelemetry/core \
      @opentelemetry/sdk-trace-node
    ```

    The TypeScript plugin requires Node.js 22 or later. The OpenTelemetry
    packages are peer dependencies used by the configured provider or telemetry
    layer.

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

Both plugins emit the same span types in one canonical trace. They differ in the
parent of operation spans, the links on those spans, and when the spans become
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

For an execution that suspends at a wait, the trace resembles:

```text
Remote backend server span
|-- Workflow                              (exported on terminal status)
|   |-- Operation: fetch-data  (STEP)     -> link to Invocation #1
|   |   `-- Attempt: fetch-data attempt 1 -> link to Invocation #1
|   |-- Operation: cooldown    (WAIT)     -> links to Invocation #1 and #2
|   `-- Operation: process     (STEP)     -> link to Invocation #2
|       `-- Attempt: process attempt 1    -> link to Invocation #2
|-- Ambient Lambda span #1
|   `-- Invocation #1
`-- Ambient Lambda span #2
    `-- Invocation #2
```

The operation links identify which invocation ran each part of the workflow.
They do not make the invocation spans children of `Workflow`.

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

For an execution that suspends at a wait, the trace resembles:

```text
Remote backend server span
|-- Workflow
|-- Ambient Lambda span #1
|   `-- Invocation #1
|       |-- Operation: fetch-data  (STEP)  -> link to Workflow
|       |   `-- Attempt: fetch-data attempt 1
|       |       `-> link to Workflow
|       `-- Operation: cooldown    (WAIT)  -> link to Workflow
`-- Ambient Lambda span #2
    `-- Invocation #2
        |-- Operation: cooldown    (WAIT)  -> links to initial cooldown
        |                                      span and Workflow
        `-- Operation: process     (STEP)  -> link to Workflow
            `-- Attempt: process attempt 1 -> link to Workflow
```

An operation that crosses an invocation boundary can produce more than one
span. A continuation or replay segment uses a new span ID and links to the
initial logical operation span, which uses the deterministic operation span ID.
It also retains the `Workflow` link. CloudWatch does not yet visualize span
links.

### Choosing a plugin

For both plugins, your observability platform's quotas and limits apply.

| Consideration          | ExecutionOtelPlugin                                       | InvocationOtelPlugin                                  |
| ---------------------- | --------------------------------------------------------- | ----------------------------------------------------- |
| Operation parent       | `Workflow`                                                | Current `Invocation`                                  |
| Correlation link       | Operation or attempt to current `Invocation`              | Operation or attempt to `Workflow`                    |
| Continuation link      | One operation span can link to several invocations        | Later segment links to initial logical operation span |
| Workflow export        | On terminal execution status                              | On terminal execution status                          |
| Operation visibility   | When the operation completes                              | At each invocation boundary                           |
| In-progress visibility | Limited until operations and the execution complete       | Each invocation appears as it completes               |
| Better for             | Short executions and a workflow-centered hierarchy        | Long executions and an invocation-centered hierarchy  |
| Platform compatibility | Long-open spans can exceed platform ingestion time limits | Spans stay within the Lambda invocation time limit    |

## Trace parent and sampling

The plugins resolve the execution parent and the sampling decision
independently. A missing or unusable `Sampled` field does not replace a valid
remote parent with a synthetic root.

| `_X_AMZN_TRACE_ID` state                                              | Canonical trace ID                                            | Common execution ancestor | Sampling                                                                |
| --------------------------------------------------------------------- | ------------------------------------------------------------- | ------------------------- | ----------------------------------------------------------------------- |
| Valid `Root`, `Parent`, `Sampled=1`                                   | Reuse `Root`                                                  | Remote `Parent`           | Preserve sampled                                                        |
| Valid `Root`, `Parent`, `Sampled=0`                                   | Reuse `Root`                                                  | Remote `Parent`           | Preserve not sampled                                                    |
| Valid `Root`, `Parent`, no valid `Sampled`                            | Reuse `Root`                                                  | Remote `Parent`           | Leave the sampled trace flag unset; configured sampler behavior applies |
| Valid `Root`, missing or invalid `Parent`, `Sampled=1` or `Sampled=0` | Reuse `Root`                                                  | Synthetic execution root  | Preserve the explicit decision                                          |
| Valid `Root`, missing or invalid `Parent`, no valid `Sampled`         | Reuse `Root`                                                  | Synthetic execution root  | Configured root sampler decides                                         |
| Missing or invalid `Root`                                             | Derive from the execution ARN and stable execution start time | Synthetic execution root  | Configured root sampler decides                                         |

Only `Sampled=0` and `Sampled=1` carry authoritative upstream decisions. An
absent or unusable value does not mean that the upstream system explicitly
chose not to sample. Its OpenTelemetry span context still represents the value
with an unset sampled bit.

A `ParentBased` sampler treats a remote parent with an unset sampled bit as not
sampled. A directly configured non-parent-based `TraceIdRatioBased` sampler can
decide from the canonical trace ID instead. The reused or derived trace ID
remains stable across reinvocations, so trace-ID-ratio decisions also remain
stable. Other sampler decisions can change between reinvocations unless the
application persists them.

When `Root` and `Parent` are valid, the normal hierarchy is:

```text
Remote backend server span (`Root` / `Parent`)
|-- Workflow
|-- Ambient Lambda span #1
|   `-- Invocation #1
|-- Ambient Lambda span #2
|   `-- Invocation #2
`-- Invocation #N  (when no valid same-trace ambient span exists)
```

`Workflow` inherits the canonical trace ID and keeps a deterministic,
execution-scoped span ID. An ambient span can parent `Invocation` only when it
uses the same trace ID. A valid remote parent remains the real ancestor when
`Sampled` is missing. The plugins do not create a synthetic-root link to it.

When the plugins cannot construct a valid remote parent, they use:

```text
Synthetic execution root
|-- Workflow
|-- Invocation #1
|-- Invocation #2
`-- Invocation #N
```

The synthetic execution root has a deterministic span ID that remains stable
across reinvocations and differs from Workflow and operation span IDs. A valid
`Root` still supplies the canonical trace ID. If `Root` is also invalid, the
plugins derive the trace ID from the execution ARN and stable execution start
time. Fallback mode ignores unrelated ambient context. TypeScript, Python, and
Java use this same topology with global and application-owned providers. The
selected sampling decision applies consistently to every descendant.

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
so the runtime supplies `_X_AMZN_TRACE_ID`, and grant the function role the
`AWSXRayDaemonWriteAccess` managed policy. Auto-instrumentation can then create
an ambient Lambda span in the same trace as the durable backend parent.

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

When the plugins use the global provider, they resolve it at invocation start.
If a usable SDK provider or the integration required for scoped deterministic
IDs is unavailable, all three SDKs log a warning and disable telemetry for that
invocation. They retry provider resolution on the next invocation instead of
emitting spans without deterministic durable IDs.

## Deploy with the community auto-instrumentation layer

The OpenTelemetry community language layer initializes auto-instrumentation,
registers a global OpenTelemetry provider, and exports spans using OTLP. Set
`AWS_LAMBDA_EXEC_WRAPPER` to `/opt/otel-handler` and configure the OTLP endpoint
for your observability backend. Enable active tracing so the plugin can reuse
the durable backend parent and the layer can create a same-trace ambient Lambda
span.

The no-arg plugin constructors use the global provider. You do not need to
configure or pass a provider to the plugin.

The examples below use OTLP HTTP and disable metrics and log export. If the
backend requires authentication, also configure `OTEL_EXPORTER_OTLP_HEADERS`.

Follow the
[OpenTelemetry Lambda auto-instrumentation documentation](https://opentelemetry.io/docs/platforms/faas/lambda-auto-instrument/)
to obtain the language layer.

=== "TypeScript"

    ```yaml
    MyFunction:
      Type: AWS::Serverless::Function
      Properties:
        Runtime: nodejs24.x
        Handler: index.handler
        Layers:
          - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<community-nodejs-layer>:<version>
        Environment:
          Variables:
            AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-handler
            OTEL_TRACES_EXPORTER: otlp
            OTEL_EXPORTER_OTLP_PROTOCOL: http/protobuf
            OTEL_EXPORTER_OTLP_ENDPOINT: https://<otlp-endpoint>
            OTEL_METRICS_EXPORTER: none
            OTEL_LOGS_EXPORTER: none
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

    ```yaml
    MyFunction:
      Type: AWS::Serverless::Function
      Properties:
        Runtime: python3.14
        Handler: index.handler
        Layers:
          - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<community-python-layer>:<version>
        Environment:
          Variables:
            AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-handler
            OTEL_TRACES_EXPORTER: otlp
            OTEL_EXPORTER_OTLP_PROTOCOL: http/protobuf
            OTEL_EXPORTER_OTLP_ENDPOINT: https://<otlp-endpoint>
            OTEL_METRICS_EXPORTER: none
            OTEL_LOGS_EXPORTER: none
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

    Use the community Java agent layer for auto-instrumentation. The agent must
    also load the plugin JAR as an agent extension so its auto-configuration SPI
    can scope deterministic ID generation to durable spans. Package the JAR in
    a layer under `java/lib`, then point `OTEL_JAVAAGENT_EXTENSIONS` to the
    deployed path.

    ```yaml
    MyFunction:
      Type: AWS::Serverless::Function
      Properties:
        Runtime: java25
        Handler: com.example.ExampleHandler
        Layers:
          - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<community-java-agent-layer>:<version>
          - <otel-plugin-layer-arn>
        Environment:
          Variables:
            AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-handler
            OTEL_TRACES_EXPORTER: otlp
            OTEL_EXPORTER_OTLP_PROTOCOL: http/protobuf
            OTEL_EXPORTER_OTLP_ENDPOINT: https://<otlp-endpoint>
            OTEL_METRICS_EXPORTER: none
            OTEL_LOGS_EXPORTER: none
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

### Add a collector extension (optional)

Add the community collector extension layer when you need local processing,
routing, or an exporter that is not available in the language layer. Keep
`AWS_LAMBDA_EXEC_WRAPPER=/opt/otel-handler` so the language layer continues to
initialize the provider and the ambient Lambda span.

Append the collector layer, point the OTLP exporter at its local receiver, and
set the collector configuration path:

```yaml
Layers:
  - <community-language-layer-arn>
  - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<community-collector-layer>:<version>
Environment:
  Variables:
    AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-handler
    OTEL_EXPORTER_OTLP_ENDPOINT: http://localhost:4318
    OPENTELEMETRY_COLLECTOR_CONFIG_URI: /var/task/collector.yaml
```

For example, include the following `collector.yaml` in the function bundle to
export traces to X-Ray:

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

Change the collector exporter to route spans to another platform. See the
[collector extension documentation](https://github.com/open-telemetry/opentelemetry-lambda/tree/main/collector)
for available components and layer deployment instructions.

### Collector-only setup (advanced)

Use the collector extension without a language auto-instrumentation layer when
you need to configure the OpenTelemetry provider in application code. Add only
the collector layer, leave `AWS_LAMBDA_EXEC_WRAPPER` unset, keep active tracing
enabled, and pass the application-owned provider to the plugin. Without an
ambient Lambda span, `Invocation` uses the remote backend span directly.

```yaml
Layers:
  - !Sub arn:aws:lambda:${AWS::Region}:<account>:layer:<collector-layer>:<version>
Environment:
  Variables:
    OPENTELEMETRY_COLLECTOR_CONFIG_URI: /var/task/collector.yaml
Tracing: Active
```

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
    - **workflowSpanName** Name of the execution-scoped `Workflow` span.
        Defaults to `Workflow`.
    - **enrichLogger** Adds `traceId`, `spanId`, and `otelTraceSampled` to each
        durable log record. Defaults to `true`.

    The application or telemetry layer owns exporters, propagators, resources,
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
    - **workflow_span_name** Name of the execution-scoped `Workflow` span.
        Defaults to `Workflow`.
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

The plugin stamps active trace and span IDs onto log records. Logs from every
reinvocation of one execution use the same canonical `traceId`. The `spanId`
identifies the active invocation, operation, or attempt, and
`otelTraceSampled` reflects the effective sampled flag. See
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
than one Lambda invocation. In CloudWatch Traces, expect one canonical trace ID
for every span carrying the execution ARN:

- The durable backend server span is the common ancestor when CloudWatch
    returns it. `Workflow` is its direct child.
- Each `Invocation` is a child of a same-trace ambient Lambda span, or a direct
    child of the backend server span when no valid ambient span exists.
- `ExecutionOtelPlugin` operations are children of `Workflow` and link to the
    invocations that observed them.
- `InvocationOtelPlugin` operation segments are children of `Invocation` and
    link to `Workflow`. A resumed wait or replayed operation also links to its
    initial logical operation span.
- Log records retain the canonical `traceId`. Their `spanId` values match the
    active invocation, operation, or attempt.

Some backend queries omit the remote backend span. In that case, `Workflow` and
directly parented `Invocation` spans still contain its non-null parent span ID.
Do not interpret the unresolved parent as a parentless Workflow span.

For Java, use the CloudWatch **Group by nodes** view to inspect the hierarchy.
The ungrouped X-Ray Segments Timeline cannot attach OTLP-exported spans beneath
the Lambda platform segment.

When you use the community collector layer, enable CloudWatch Transaction
Search in your account for traces to appear.

If no plugin spans appear:

- Confirm a provider with a span processor and exporter is registered.
- Check for a warning that the global SDK provider or its deterministic ID
    integration was unavailable at invocation start.
- With ADOT, confirm `AWS_LAMBDA_EXEC_WRAPPER` and active tracing are enabled.
- With the community collector, confirm the layer and
    `OPENTELEMETRY_COLLECTOR_CONFIG_URI` are configured.
- For Java ADOT, confirm the plugin JAR is listed in
    `OTEL_JAVAAGENT_EXTENSIONS`.
- Check `_X_AMZN_TRACE_ID` and provider sampling configuration. An explicit
    `Sampled=0` suppresses export, and `ParentBased` treats a missing sampled bit
    as not sampled.

If spans carrying one execution ARN use different trace IDs, confirm active
tracing is enabled and that every invocation uses the same current plugin and
context extractor. Missing or invalid remote parent data should still produce
one canonical fallback trace, not one trace per invocation.

## See also

- [Plugins](plugins.md)
- [Logging](logging.md)
- [Steps](../operations/step.md)
