# OpenTelemetry

The OpenTelemetry plugins trace durable operations across Lambda invocations.
Create a factory for the execution view (the default) or select the invocation
view explicitly. Configure an OpenTelemetry provider through a telemetry layer
or application code. The
provider owns exporters, resources, library instrumentation, and sampling policy.

The plugins are available for TypeScript, Python, and Java. Go and C# do not yet
provide this integration.

## Install the plugin

These examples use the unified factory APIs. The version ranges below identify
compatible SDK families; they do not guarantee that a release contains the new
entry point. Use a build containing the change linked in your language's tab.
Upgrade the core and plugin together, including copies supplied by Lambda layers.

=== "TypeScript"

    Requires Node.js 22 or later and a plugin build containing
    [`createOtelPluginFactory`](https://github.com/aws/aws-durable-execution-sdk-js/pull/1171).
    OTel 2.0.0 does not include this entry point.

    ```bash
    npm install @aws/durable-execution-sdk-js@^3 \
      @aws/durable-execution-sdk-js-otel@^2 \
      @opentelemetry/api @opentelemetry/core @opentelemetry/sdk-trace-node
    ```

=== "Python"

    Use a plugin build containing
    [`create_otel_plugin_factory`](https://github.com/aws/aws-durable-execution-sdk-python/pull/905).
    OTel 2.0.0 does not include this entry point.

    When a telemetry layer supplies the OpenTelemetry libraries:

    ```bash
    pip install "aws-durable-execution-sdk-python-otel>=2,<3"
    ```

    The package installs a compatible core SDK but leaves OpenTelemetry libraries
    to the layer. When the application supplies its own provider, install the
    `standalone` extra instead:

    ```bash
    pip install "aws-durable-execution-sdk-python-otel[standalone]>=2,<3"
    ```

=== "Java"

    Use the same version as the core SDK, containing
    [`OtelPlugin.factory`](https://github.com/aws/aws-durable-execution-sdk-java/pull/873).
    SDK/plugin 3.0.1 does not include this entry point.

    ```xml
    <dependency>
        <groupId>software.amazon.lambda.durable</groupId>
        <artifactId>aws-durable-execution-sdk-java-plugin-otel</artifactId>
        <version>${durable.sdk.version}</version>
    </dependency>
    ```

## Choose and register a view

Register one factory using the unified entry point for your language. Omitting
the mode selects the execution view. The SDK creates a separate plugin instance
for each invocation, while the provider can be shared across invocations.

### Choosing a plugin

For both views, your observability platform's quotas and limits apply.

| Consideration              | Execution view                                                   | Invocation view                                                                               |
| -------------------------- | ---------------------------------------------------------------- | --------------------------------------------------------------------------------------------- |
| Top-level operation parent | `Workflow`                                                       | Current `Invocation`                                                                          |
| Correlation link           | Operation or attempt to current `Invocation`                     | Operation or attempt to `Workflow`                                                            |
| Continuation link          | Completed operation links only to the invocation that exports it | Later segment links to the initial logical operation span                                     |
| Operation visibility       | When the operation completes                                     | At each invocation boundary                                                                   |
| In-progress visibility     | Limited until operations and the execution complete              | Each invocation appears as it completes                                                       |
| Better for                 | Shorter executions and a workflow-centered hierarchy             | Long executions and progress through individual invocations                                   |
| Platform compatibility     | Long-open spans can exceed ingestion time limits                 | Operation segments stay within an invocation; long Workflow spans still face ingestion limits |

Nested operations remain under their child-context operation span; attempts
remain under their operation. Both views export `Invocation` spans as invocations
end and `Workflow` only when the SDK observes execution success or failure.
Workflow spans can cover long durations, so backend ingestion limits still apply
to both views. Export and flush are best effort; a timeout, stop, or terminated
runtime can leave spans missing.

The diagrams below show an execution with a valid remote backend parent and
ambient Lambda spans in the same trace. See [trace identity and sampling](#trace-identity-and-sampling)
for other parent contexts. The tree shows parent-child relationships; arrows
show span links.

### Execution view

Top-level operation spans are children of `Workflow` and link to the current `Invocation`.
The factory selects this view by default. An operation that suspends is exported
when it completes, using its durable
start and end times. Its span links to the invocation that exports the completion;
it does not accumulate links to every earlier invocation.

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

For an execution that suspends at `cooldown` and resumes in a second invocation,
the trace resembles:

```text
Remote backend server span
|-- Workflow                              (exported on terminal status)
|   |-- Operation: fetch-data  (STEP)      -> link to Invocation #1
|   |   `-- Attempt: fetch-data attempt 1 -> link to Invocation #1
|   |-- Operation: cooldown    (WAIT)      -> link to Invocation #2
|   `-- Operation: process     (STEP)      -> link to Invocation #2
|       `-- Attempt: process attempt 1    -> link to Invocation #2
|-- Ambient Lambda span #1
|   `-- Invocation #1
`-- Ambient Lambda span #2
    `-- Invocation #2
```

The links to `Invocation` do not make invocation spans children of `Workflow`.

### Invocation view

Top-level operation spans are children of the `Invocation` that emitted them and link to
`Workflow`, so each invocation's spans export as it ends. Select invocation mode
explicitly. Completed work uses checkpointed results on replay. A resumed operation or
replayed context can produce another span segment without rerunning a completed
step's body.

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

The same execution, using invocation view, produces this hierarchy:

```text
Remote backend server span
|-- Workflow                               (exported on terminal status)
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
span. A continuation segment uses a new span ID and links to the
initial logical operation span, which uses the deterministic operation span ID.
It also retains the `Workflow` link. CloudWatch does not yet visualize span
links.

## Configure a telemetry layer

A language auto-instrumentation layer registers the global provider used by the
default factories. Select a layer compatible with your runtime, architecture,
and region:

| Layer                                  | `AWS_LAMBDA_EXEC_WRAPPER` | Setup                                                                                               |
| -------------------------------------- | ------------------------- | --------------------------------------------------------------------------------------------------- |
| AWS Distro for OpenTelemetry (ADOT)    | `/opt/otel-instrument`    | [ADOT Lambda layers](https://aws-otel.github.io/docs/getting-started/lambda)                        |
| OpenTelemetry community language layer | `/opt/otel-handler`       | [Lambda auto-instrumentation](https://opentelemetry.io/docs/platforms/faas/lambda-auto-instrument/) |

Add the following entries under `Properties` in your existing
`AWS::Serverless::Function`. Merge them with the function's existing layers,
environment variables, and policies. SAM applies `Policies` only when it creates
the execution role. If you specify `Role`, attach the required permissions
directly to that role.

### ADOT layer

For ADOT with X-Ray export, enable `Active` tracing and grant
`AWSXRayDaemonWriteAccess` alongside the durable execution permissions.

```yaml
Layers:
  - <adot-language-layer-arn>
Environment:
  Variables:
    AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-instrument
Tracing: Active
Policies:
  - arn:aws:iam::aws:policy/service-role/AWSLambdaBasicDurableExecutionRolePolicy
  - arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess
```

### Community language layer

For a community language layer exporting directly to an OTLP backend, use
[`PassThrough` tracing](https://docs.aws.amazon.com/lambda/latest/dg/services-xray.html#services-xray-default).
Lambda passes the upstream X-Ray trace header to the
function without emitting native Lambda X-Ray segments. The layer and durable
plugin still provide application instrumentation.

Propagate the caller's trace context and sampling decision. Without an upstream
header, Lambda generates a header with `Sampled=0`; the documented plugins honor
that decision even with a local `always_on` sampler. See
[supported sampling modes](#supported-sampling-modes).

```yaml
Layers:
  - <community-language-layer-arn>
Environment:
  Variables:
    AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-handler
    OTEL_TRACES_EXPORTER: otlp
    OTEL_EXPORTER_OTLP_PROTOCOL: http/protobuf
    OTEL_EXPORTER_OTLP_ENDPOINT: https://<otlp-endpoint>
    OTEL_METRICS_EXPORTER: none
    OTEL_LOGS_EXPORTER: none
Tracing: PassThrough
Policies:
  - arn:aws:iam::aws:policy/service-role/AWSLambdaBasicDurableExecutionRolePolicy
```

Configure `OTEL_EXPORTER_OTLP_HEADERS` if your backend requires authentication.
OTLP export does not require X-Ray write permissions. SAM still adds an X-Ray
policy when it creates a role for `Tracing: PassThrough`.

With `Active` tracing, a durable span can reference a native Lambda parent sent
only to X-Ray. The plugin preserves that parent without re-exporting it, so a
shared trace ID does not guarantee a complete parent tree in an OTLP backend.
If you need native Lambda platform spans, use an integration that receives both
OTLP spans and their X-Ray ancestors.

`PassThrough` does not supply missing caller spans. For a complete upstream
hierarchy, export the real caller span to the same backend and propagate its
trace ID, span ID, and sampling decision through `X-Amzn-Trace-Id` when using the
default plugin extractors. Preserve this propagation across downstream durable
invokes.

### Provider initialization

These requirements apply to both layer choices.

=== "TypeScript"

    The default factory resolves the global provider at invocation start.

=== "Python"

    Use the base plugin package with a telemetry layer so function dependencies
    do not shadow the layer's OpenTelemetry libraries.

=== "Java"

    The Java agent must also load the plugin JAR as an extension to install
    durable ID generation and sampling. Package the JAR in a layer under
    `java/lib`, add that layer to the function, and set:

    ```text
    OTEL_JAVAAGENT_EXTENSIONS=/opt/java/lib/aws-durable-execution-sdk-java-plugin-otel-<version>.jar
    ```

    Obtain the ADOT Java layer from the
    [Java instrumentation releases](https://github.com/aws-observability/aws-otel-java-instrumentation/releases/latest).

If the global SDK provider or required durable ID integration is unavailable,
the plugin logs a warning and disables its telemetry for that invocation.
The next invocation retries initialization.

### Load the factory without handler registration

`DURABLE_EXECUTION_PLUGINS` selects a default-configured OTel factory from an installed
package or layer. These factories use the global OpenTelemetry tracer provider
initialized by your telemetry layer or application. Choose one view through either
code or environment configuration.

The value follows each SDK's discovery mechanism: an importable module specifier
or a registered provider name.

| Language and lookup                  | Execution view (default)             | Invocation view                                      |
| ------------------------------------ | ------------------------------------ | ---------------------------------------------------- |
| TypeScript: Node.js module specifier | `@aws/durable-execution-sdk-js-otel` | `@aws/durable-execution-sdk-js-otel/otel-invocation` |
| Python: package entry-point name     | `otel`                               | `otel-invocation`                                    |
| Java: `ServiceLoader` provider name  | `otel`                               | `otel-invocation`                                    |

!!! warning "Configure exactly one OTel view"

    Registering two distinct OTel factories fails validation because the OTel
    views share an exclusive group in each SDK. This applies to naming two views
    in `DURABLE_EXECUTION_PLUGINS`, and to naming one there while also registering
    a separate factory in handler code.

=== "TypeScript"

    `@aws/durable-execution-sdk-js-otel/otel-execution` selects the same execution
    view as the bare `@aws/durable-execution-sdk-js-otel` package name. Configure
    only one of these entry points in `DURABLE_EXECUTION_PLUGINS`.

=== "Python"

    `otel-execution` is an alias for the default `otel` execution view. Both names
    resolve to the same factory object. If both appear in
    `DURABLE_EXECUTION_PLUGINS`, the SDK loads one factory and logs a warning that
    it skipped the duplicate. Configure one name.

=== "Java"

    `otel-execution` also selects the execution view. Each provider name creates
    a separate factory, so `DURABLE_EXECUTION_PLUGINS=otel-execution,otel` fails
    at startup:

    ```text
    Conflicting plugin factories otel-execution (ExecutionOtelPlugin) and otel (ExecutionOtelPlugin) in exclusive group 'durable-otel-view'. Configure only one plugin from this group.
    ```

    Configure one provider name.

## Configure an application-owned provider

Configure the exporter and processors once, then register the resulting factory
as in the handler examples above. The plugin flushes at invocation end but does
not take over provider shutdown. These examples send OTLP HTTP to a collector
listening on loopback port 4318.

=== "TypeScript"

    Install `@opentelemetry/exporter-trace-otlp-http`. The provider factory receives
    the durable ID-generator factory; pass its result to the provider constructor.

    ```typescript
    import { createOtelPluginFactory } from "@aws/durable-execution-sdk-js-otel";
    import { OTLPTraceExporter } from "@opentelemetry/exporter-trace-otlp-http";
    import { BatchSpanProcessor, NodeTracerProvider } from "@opentelemetry/sdk-trace-node";

    const pluginFactory = createOtelPluginFactory({
      tracerProviderFactory: (createIdGenerator) => {
        const provider = new NodeTracerProvider({
          idGenerator: createIdGenerator(),
          spanProcessors: [new BatchSpanProcessor(new OTLPTraceExporter({
            url: "http://localhost:4318/v1/traces",
          }))],
        });
        provider.register();
        return provider;
      },
    });
    ```

    Set `mode: "invocation"` in the same options object to select the invocation view.

=== "Python"

    Install the `standalone` extra and `opentelemetry-exporter-otlp-proto-http`.

    ```python
    from opentelemetry import trace
    from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
    from opentelemetry.sdk.trace import TracerProvider
    from opentelemetry.sdk.trace.export import BatchSpanProcessor
    from aws_durable_execution_sdk_python_otel import (
        create_otel_plugin_factory,
        OtelPluginConfig,
    )

    provider = TracerProvider()
    provider.add_span_processor(BatchSpanProcessor(
        OTLPSpanExporter(endpoint="http://localhost:4318/v1/traces")
    ))
    trace.set_tracer_provider(provider)
    plugin_factory = create_otel_plugin_factory(
        OtelPluginConfig(tracer_provider=provider)
    )
    ```

    Pass `mode="invocation"` after the configuration argument to select the invocation view.

    `tracer_provider` is what the plugin uses, so `set_tracer_provider` is only
    needed to point *other* instrumentation at the same provider. It takes effect
    once per process and is ignored with a warning if a telemetry layer already
    registered a global provider; omit it in that case.

=== "Java"

    Add `io.opentelemetry:opentelemetry-exporter-otlp` to your dependencies. The
    factory wraps the provider builder's ID generator and sampler, then builds
    the provider once.

    ```java
    import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
    import io.opentelemetry.sdk.trace.SdkTracerProvider;
    import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
    import software.amazon.lambda.durable.otel.OtelPlugin;

    var exporter = OtlpHttpSpanExporter.builder()
            .setEndpoint("http://localhost:4318/v1/traces")
            .build();
    var pluginFactory = OtelPlugin.factory(
            OtelPlugin.Mode.EXECUTION,
            SdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(exporter).build()));
    ```

    Use `OtelPlugin.Mode.INVOCATION` as the first argument to select the invocation view.

### Plugin options

=== "TypeScript"

    Pass `OtelPluginFactoryOptions` to `createOtelPluginFactory(options)`. These
    options extend `OtelPluginConfig` with the view selection.

    | Option                  | Default                        | Purpose                                                           |
    | ----------------------- | ------------------------------ | ----------------------------------------------------------------- |
    | `mode`                  | `"execution"`                  | Select `"execution"` or `"invocation"`                            |
    | `tracerProviderFactory` | Global provider                | Build an application-owned provider                               |
    | `contextExtractor`      | `xRayContextExtractor`         | Extract this execution's upstream context                         |
    | `instrumentationName`   | `aws-durable-execution-sdk-js` | Instrumentation scope                                             |
    | `workflowSpanName`      | `Workflow`                     | Workflow span name                                                |
    | `enrichLogger`          | `true`                         | Add trace fields to durable logs                                  |
    | `flushTimeoutMillis`    | `1000`                         | Total time budget for awaiting provider flushes at invocation end |

    `w3cClientContextExtractor` reads `traceparent` from Lambda client context.
    The default X-Ray extractor reads invocation-local `xRayTraceId` when
    present, otherwise `_X_AMZN_TRACE_ID`.
    A flush timeout stops waiting; it does not cancel exports or synchronous
    provider work.

=== "Python"

    Pass `OtelPluginConfig` to `create_otel_plugin_factory(config)`. Select the
    invocation view with `create_otel_plugin_factory(config, mode="invocation")`;
    `mode` is a keyword argument, separate from `OtelPluginConfig`.

    | Option               | Default                            | Purpose                                          |
    | -------------------- | ---------------------------------- | ------------------------------------------------ |
    | `tracer_provider`    | Global provider                    | Use an application-owned SDK provider            |
    | `context_extractor`  | `xray_context_extractor`           | Extract upstream context from `_X_AMZN_TRACE_ID` |
    | `instrument_name`    | `aws-durable-execution-sdk-python` | Instrumentation scope                            |
    | `workflow_span_name` | `Workflow`                         | Workflow span name                               |
    | `enrich_logger`      | `True`                             | Add trace fields through root logger handlers    |

    `w3c_client_context_extractor` is a placeholder that returns `None`.
    Use the default X-Ray extractor or implement a custom extractor.

=== "Java"

    `OtelPlugin` exposes `factory()`, `factory(mode)`, `factory(mode, config)`,
    `factory(mode, providerBuilder)`, and `factory(mode, providerBuilder, config)`.
    Use `OtelPlugin.Mode.EXECUTION` or `OtelPlugin.Mode.INVOCATION` for `mode`.
    Only `factory()` omits the mode and defaults to execution. Build
    `OtelPluginConfig` with:

    | Builder method             | Default                          | Purpose                        |
    | -------------------------- | -------------------------------- | ------------------------------ |
    | `contextExtractor(...)`    | `new XRayContextExtractor()`     | Extract upstream trace context |
    | `instrumentationName(...)` | `aws-durable-execution-sdk-java` | Instrumentation scope          |
    | `workflowSpanName(...)`    | `Workflow`                       | Workflow span name             |
    | `enableMdc(...)`           | `true`                           | Add trace fields to SLF4J MDC  |

A custom extractor must return the durable execution's own context consistently
across resumes. An unrelated or per-invocation trace ID splits the execution trace.

## Trace identity and sampling

Both views keep one canonical trace ID per execution. A valid propagated X-Ray
`Root` supplies that ID, and a valid `Parent` becomes the remote execution
ancestor. Missing `Sampled` does not invalidate a complete remote parent.
`Workflow` has a stable execution-scoped span ID and parents to that ancestor.
`Invocation` uses an active ambient span, such as the Lambda span, only when it
belongs to the same trace; otherwise it parents to the execution ancestor.

```text
Execution ancestor
|-- Workflow
|-- Ambient Lambda span #1
|   `-- Invocation #1
`-- Ambient Lambda span #2
    `-- Invocation #2
```

When no complete remote parent is available, the SDK uses a synthetic
`DurableExecutionRoot`. It retains a valid propagated trace ID, or derives one
from the execution ARN and stable execution start time. With a supported provider
and that start time, sampled invocations export a zero-duration root with stable
identity to preserve ancestry before the execution completes. Resumes can export
another copy. `Workflow` carries duration and outcome; the fallback root carries
neither. Remote backend spans are owned by the backend and are never exported
by the plugin.

Configure a [supported sampling mode](#supported-sampling-modes) through the provider,
or through `OTEL_TRACES_SAMPLER` and
`OTEL_TRACES_SAMPLER_ARG` when using auto-configuration. Keep the plugin's sampler
integration installed so durable spans share the resolved decision while other
instrumentation retains its own policy.

=== "TypeScript"

    An explicit `Sampled=1` or `Sampled=0` on a valid extracted trace takes
    precedence. Otherwise, the configured sampler evaluates its root policy
    using the canonical trace ID. Missing `Sampled` therefore does not make a
    `ParentBased` sampler automatically drop the trace.

    A tracer that hides its sampler cannot enforce a shared durable sampling
    decision and retains terminal-only fallback-root export.

=== "Python"

    Sampling follows the explicit backend decision, then the recording/sampled
    state of a same-trace ambient span, then the configured sampler's root policy.
    The plugin resolves the result once per invocation for its durable spans.

=== "Java"

    Sampling follows the explicit backend decision, then a same-trace ambient
    span, then the configured sampler. Retain `DurableSampler`, installed by the
    provider-builder and agent-extension paths, as the final sampler. Replacing
    it with a plain sampler removes the shared-decision guarantee.

### Supported sampling modes

These rules apply to TypeScript core 3.0.0 / OTel 2.1.0, Python core 3.0.2 / OTel 2.1.0,
and Java SDK/plugin 3.1.0, in both views. These versions include the unified
factory APIs used by this guide.

- **Explicit upstream decision:** a valid `Sampled=0` or `Sampled=1` overrides the
    local sampler for durable spans. Thus `Sampled=1` still exports durable spans
    with local `always_off`, and `Sampled=0` drops them with local `always_on`.
    Active tracing alone does not imply `Sampled=1`.
- **Otherwise:** use a deterministic policy based on the canonical trace ID, such
    as `always_on`, `always_off`, or `traceidratio` with a fixed ratio. For
    `traceidratio`, `OTEL_TRACES_SAMPLER_ARG=0.5` selects whole execution traces when
    the local policy is consulted.

Keep the policy, ratio, and any inherited upstream or ambient decision stable across
retries, resumes, and processes. Random, time-, load-, or mutable-state-dependent
policies are unsupported and can produce partial traces. The plugins do not
checkpoint sampling decisions; caching or application persistence does not make
another policy supported.

The provider or auto-configuration layer resolves the environment settings, defaults,
and invalid values. They do not override an explicitly configured application
provider. Keep the plugin's sampler integration installed; other application and
ADOT spans retain their own policy.

Sampling behavior may change in future SDK/plugin releases.
[Issue #295](https://github.com/aws/aws-durable-execution-docs/issues/295) proposes
letting the configured sampler decide whether to inherit upstream sampling, using
standard direct and `parentbased_*` semantics. That proposal is not implemented in
the versions listed above. Check the release notes and this section when upgrading.

Sampling does not guarantee export or a complete parent tree: a durable span may be
sampled while its application parent is dropped, or its native X-Ray parent may be
absent from OTLP. See the [community layer setup](#community-language-layer) for
trace propagation and native X-Ray parents.

## Span attributes and failures

Optional attributes such as `durable.operation.name` and
`durable.operation.subtype` are emitted only when supplied.

| Span       | Main attributes                                                                                                                                              |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Workflow   | `durable.execution.arn`, `durable.execution.status`                                                                                                          |
| Invocation | `durable.execution.arn`, `durable.invocation.status`, `durable.invocation.first`                                                                             |
| Operation  | `durable.execution.arn`, `durable.operation.id`, `durable.operation.type`, `durable.operation.name`, `durable.operation.subtype`, `durable.operation.status` |
| Attempt    | `durable.execution.arn`, `durable.operation.id`, `durable.operation.type`, `durable.operation.name`, `durable.attempt.number`, `durable.attempt.outcome`     |

Operation types include `STEP`, `WAIT`, `CONTEXT`, `CHAINED_INVOKE`, and `CALLBACK`.
A context has an operation span without a separate attempt span. An invocation
that will retry reports `durable.invocation.status=RETRYING` with OTel `UNSET`.
A caught operation failure can have an `ERROR` span while the enclosing invocation
and workflow succeed. Inspect operation status as well as exception events;
a backend can report failure without diagnostic details.

=== "TypeScript"

    `FAILED`, `TIMED_OUT`, and `STOPPED` operations set `ERROR` even without
    diagnostics. The plugins export newly observed external completion updates
    for waits, callbacks, and invokes when their metadata is visible, even if replay
    skips the operation's body. When completed child contexts would otherwise
    prune that metadata, configure
    `pluginsConfig.childOperationsDepth` as described in
    [operation detail](telemetry/exporter.md#operation-detail).

=== "Python"

    `FAILED`, `TIMED_OUT`, and `STOPPED` operations set `ERROR` even without an
    error object. Exception events preserve the checkpointed type, message, and
    stack trace; missing fields are omitted. No exception event is emitted when
    both type and message are absent. Terminal operation and attempt exception
    events use the span end timestamp.

    Completion spans can be missing when replay returns a cached parent result
    without visiting its children, even if their operation metadata is still
    visible. Pruned operation metadata cannot be reconstructed.

=== "Java"

    `FAILED`, `TIMED_OUT`, and `STOPPED` operations set `ERROR` even without
    diagnostics. Missing details do not produce an exception event. Execution-view
    operation exceptions use the known backend end timestamp when available;
    invocation-view exceptions retain normal OpenTelemetry event timing.
    Since SDK/plugin 3.0.1, operation exception events report the stored original
    error type when available, including errors from another language, while
    retaining the throwable's message and stack diagnostics.

## Correlate logs and verify traces

Log correlation is enabled by default. `traceId` identifies the execution trace,
`spanId` identifies the active span, and `otelTraceSampled` carries its sampled
flag. Application-created spans on that trace can become the active context too.

=== "TypeScript"

    `enrichLogger` adds fields through the durable logger's `enrichLogContext`
    hook. In execution view, handler code between operations uses `Workflow`;
    invocation view uses `Invocation`. Step bodies use their attempt span.
    Custom logger adapters must forward the enriched context.

=== "Python"

    `enrich_logger` installs a filter on root logger handlers. Handler code uses
    `Invocation`, step bodies use their attempt, and child-context bodies use
    their context span. Fields are optional: initialization logs and threads
    without invocation context are left uncorrelated.

=== "Java"

    `enableMdc` adds the fields to SLF4J MDC. Configure your logging framework
    to include MDC fields in its output. In CloudWatch, use **Group by nodes**
    to inspect the hierarchy; the ungrouped X-Ray Segments Timeline cannot nest
    OTLP spans under the Lambda platform segment.

Test with an execution that waits and resumes. Confirm that its spans share a
trace ID, invocations appear as they finish, completed steps do not run again,
and logs identify the active span. `Workflow` appears after terminal completion.
Span links correlate the two views but do not change parent-child relationships;
CloudWatch does not currently visualize these links.

If spans are missing, check provider warnings, exporter configuration, the layer's
execution wrapper, and [sampling](#supported-sampling-modes). For Java agents, also check
`OTEL_JAVAAGENT_EXTENSIONS`.
`Workflow` requires a terminal invocation to export it. An unresolved parent can
belong to the remote backend or an undelivered fallback root.

## See also

- [Plugins](plugins.md)
- [Logging](logging.md)
- [Durable Execution Telemetry](telemetry/index.md)
- [Steps](../operations/step.md)
- [Feedback and discussion](https://github.com/aws/aws-durable-execution-docs/discussions/206)
