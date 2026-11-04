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

    Use the same 3.x version as the core SDK, containing
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
| Workflow export            | When the SDK observes execution success or failure               | When the SDK observes execution success or failure                                            |
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

### Execution view

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

### Invocation view

Select invocation mode explicitly. Completed work uses checkpointed results on replay. A resumed operation or
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

## Configure a telemetry layer

A language auto-instrumentation layer registers the global provider used by the
default factories. Select a layer compatible with your runtime, architecture,
and region:

| Layer                                  | `AWS_LAMBDA_EXEC_WRAPPER` | Setup                                                                                               |
| -------------------------------------- | ------------------------- | --------------------------------------------------------------------------------------------------- |
| AWS Distro for OpenTelemetry (ADOT)    | `/opt/otel-instrument`    | [ADOT Lambda layers](https://aws-otel.github.io/docs/getting-started/lambda)                        |
| OpenTelemetry community language layer | `/opt/otel-handler`       | [Lambda auto-instrumentation](https://opentelemetry.io/docs/platforms/faas/lambda-auto-instrument/) |

Enable active tracing to receive the durable backend's X-Ray trace context.
OTLP-only backends may lack the referenced native Lambda spans; see
[missing parents in an OTLP backend](#missing-parents-in-an-otlp-backend).
For ADOT, add these properties to your existing SAM function and grant its role
`AWSXRayDaemonWriteAccess` to export traces to CloudWatch through X-Ray:

```yaml
Layers:
  - <adot-language-layer-arn>
Environment:
  Variables:
    AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-instrument
Tracing: Active
```

For a community language layer exporting directly to an OTLP backend, use:

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
Tracing: Active
```

Configure `OTEL_EXPORTER_OTLP_HEADERS` if your backend requires authentication.
A collector extension is optional for local processing or additional exporters;
follow the [collector setup guide](https://github.com/open-telemetry/opentelemetry-lambda/tree/main/collector)
for its layer, receiver, and `OPENTELEMETRY_COLLECTOR_CONFIG_URI` configuration.
A collector alone does not initialize a language provider. For that setup, leave
`AWS_LAMBDA_EXEC_WRAPPER` unset and configure the provider in application code as
shown below. For collector export to CloudWatch, enable Transaction Search in
your account.

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

These rules apply to TypeScript core 3.0.0 / OTel 2.0.0, Python core 3.0.1 / OTel 2.0.0,
and Java SDK/plugin 3.0.1, in both views:

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

[Issue #295](https://github.com/aws/aws-durable-execution-docs/issues/295) proposes
letting the configured sampler decide whether to inherit upstream sampling, using
standard direct and `parentbased_*` semantics. That change is not available in the
versions above; follow the issue for runtime support and migration guidance.

Sampling does not guarantee export or a complete parent tree: a durable span may be
sampled while its application parent is dropped, or its native X-Ray parent may be
absent from OTLP. See [missing parents](#missing-parents-in-an-otlp-backend) to distinguish
sampling, raw export, and backend/UI results.

## Span attributes and failures

| Span       | Main attributes                                                                                                                                              |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Workflow   | `durable.execution.arn`, `durable.execution.status`                                                                                                          |
| Invocation | `durable.execution.arn`, `durable.invocation.status`, `durable.invocation.first`                                                                             |
| Operation  | `durable.execution.arn`, `durable.operation.id`, `durable.operation.type`, `durable.operation.name`, `durable.operation.subtype`, `durable.operation.status` |
| Attempt    | `durable.execution.arn`, `durable.operation.id`, `durable.attempt.number`, `durable.attempt.outcome`                                                         |

Operation types include `STEP`, `WAIT`, `CONTEXT`, `CHAINED_INVOKE`, and `CALLBACK`.
A context has an operation span without a separate attempt span. An invocation
that will retry reports `durable.invocation.status=RETRYING` with OTel `UNSET`.
A caught operation failure can have an `ERROR` span while the enclosing invocation
and workflow succeed. Inspect operation status as well as exception events;
a backend can report failure without diagnostic details.

=== "TypeScript"

    `FAILED`, `TIMED_OUT`, and `STOPPED` operations set `ERROR` even without
    diagnostics. The plugins export visible completion updates even when replay skips the
    operation's body. They need the operation metadata to do so. When completed
    child contexts would otherwise prune that metadata, configure
    `pluginsConfig.childOperationsDepth` as described in
    [operation detail](telemetry/exporter.md#operation-detail).

=== "Python"

    In OTel 2.0.0, a failed operation without an error object can remain `UNSET`.
    [PR #865](https://github.com/aws/aws-durable-execution-sdk-python/pull/865)
    fixes that status and aligns terminal exception timestamps with span ends.
    [PR #901](https://github.com/aws/aws-durable-execution-sdk-python/pull/901)
    preserves only checkpointed exception fields and omits events lacking both a
    type and a message. Both fixes are merged on `main` but absent from OTel 2.0.0.

    [PR #869](https://github.com/aws/aws-durable-execution-sdk-python/pull/869)
    remains open. It exports visible completion updates when cached-parent replay
    skips a completed child, with deduplication within an invocation. It does not
    guarantee exactly-once export. Until these fixes ship, use builds containing
    the changes. Pruned operation metadata cannot be reconstructed.

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

### Missing parents in an OTLP backend

With Lambda active tracing, a remote parent can be a native Lambda segment exported
only to X-Ray. The durable plugin preserves that external parent without re-exporting
it, so a shared trace ID does not guarantee a complete parent tree in an OTLP backend.

To locate a missing parent:

1. Query all spans for the trace ID. Filtering by `durable.execution.arn` can hide
    vendor or application ancestors already stored in the backend.
1. Compare unresolved parent IDs with raw OTLP exports and native X-Ray segments.
    A parent found only in X-Ray identifies the export boundary.
1. If the parent is in the OTLP export, check backend ingestion, query filters,
    and UI rendering.

`DASH0_XRAY_TRACES_ENABLED=true` does not import native X-Ray segments into OTLP.
[Issue #297](https://github.com/aws/aws-durable-execution-docs/issues/297) records
this limitation with JavaScript source `721217ae` and Dash0 Node layer v26 across
both views and multiple resumes, with X-Ray mode off and on. Those captures do not
qualify Dash0 backend/UI behavior or Python and Java combinations.

For native Lambda platform spans, use an integration that receives both OTLP spans
and their X-Ray ancestors. The alternative tracked in #297 uses a real caller span
exported to the same backend, propagates its context and sampling decision, and
selects `PassThrough` while retaining application instrumentation. It gives up native
Lambda X-Ray platform tracing and remains unvalidated end to end. Do not treat it as
a supported workaround.

## See also

- [Plugins](plugins.md)
- [Logging](logging.md)
- [Durable Execution Telemetry](telemetry/index.md)
- [Steps](../operations/step.md)
- [Feedback and discussion](https://github.com/aws/aws-durable-execution-docs/discussions/206)
