# OpenTelemetry

The OpenTelemetry plugins trace durable operations across Lambda invocations.
Choose a workflow or invocation view, register its factory, and configure an
OpenTelemetry provider through a telemetry layer or application code. The
provider owns exporters, resources, library instrumentation, and sampling policy.

!!! note "Experimental feature"

    The plugins are experimental and are not yet recommended for production workloads.
    They are available for TypeScript,
    Python, and Java; Go and C# do not yet provide this integration. Feedback is welcome
    in the [GitHub Discussion](https://github.com/aws/aws-durable-execution-docs/discussions/206).

## Install the plugin

These examples use the current factory API: core SDK 3.x with OTel plugin 2.x
for TypeScript and Python, and SDK/plugin 3.x for Java. Upgrade the core and
plugin together, including copies supplied by Lambda layers. Older plugin
instances cannot be registered with the 3.x factory API.

=== "TypeScript"

    Requires Node.js 22 or later:

    ```bash
    npm install @aws/durable-execution-sdk-js@^3 \
      @aws/durable-execution-sdk-js-otel@^2 \
      @opentelemetry/api @opentelemetry/core @opentelemetry/sdk-trace-node
    ```

=== "Python"

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

    Use the same 3.x version as the core SDK:

    ```xml
    <dependency>
        <groupId>software.amazon.lambda.durable</groupId>
        <artifactId>aws-durable-execution-sdk-java-plugin-otel</artifactId>
        <version>${durable.sdk.version}</version>
    </dependency>
    ```

## Choose and register a view

Register one OTel view factory. The SDK creates a separate plugin instance for
each invocation, while the provider can be shared across invocations.

| Aspect                          | Execution view                           | Invocation view                                                                         |
| ------------------------------- | ---------------------------------------- | --------------------------------------------------------------------------------------- |
| Top-level operation parent      | `Workflow`                               | Current `Invocation`                                                                    |
| Operation and attempt links     | Current `Invocation`                     | `Workflow`                                                                              |
| Operation spanning a suspension | One logical span, exported on completion | Separate segments across invocations; later segments link to the initial operation span |
| Best suited to                  | Inspecting the workflow hierarchy        | Inspecting progress during long executions                                              |

Nested operations remain under their child-context operation span; attempts
remain under their operation. Both views export `Invocation` spans as invocations
end and `Workflow` only when the SDK observes execution success or failure.
Workflow spans can cover long durations, so backend ingestion limits still apply
to both views. Export and flush are best effort; a timeout, stop, or terminated
runtime can leave spans missing.

### Invocation view

Use this view to see each invocation's work as the execution progresses.
Completed work uses checkpointed results on replay. A resumed operation or
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

### Execution view

Use this view to keep the durable operation hierarchy beneath `Workflow`.
An operation that suspends is exported when it completes, using its durable
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

## Configure a telemetry layer

A language auto-instrumentation layer registers the global provider used by the
default factories. Select a layer compatible with your runtime, architecture,
and region:

| Layer                                  | `AWS_LAMBDA_EXEC_WRAPPER` | Setup                                                                                               |
| -------------------------------------- | ------------------------- | --------------------------------------------------------------------------------------------------- |
| AWS Distro for OpenTelemetry (ADOT)    | `/opt/otel-instrument`    | [ADOT Lambda layers](https://aws-otel.github.io/docs/getting-started/lambda)                        |
| OpenTelemetry community language layer | `/opt/otel-handler`       | [Lambda auto-instrumentation](https://opentelemetry.io/docs/platforms/faas/lambda-auto-instrument/) |

Enable active tracing to receive the durable backend's X-Ray trace context.
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

`DURABLE_EXECUTION_PLUGINS` selects a default-configured factory from an installed
package or layer. It still requires a global provider. Choose one view through
either code or environment configuration.

| Language   | Invocation view                                      | Execution view                                      |
| ---------- | ---------------------------------------------------- | --------------------------------------------------- |
| TypeScript | `@aws/durable-execution-sdk-js-otel/otel-invocation` | `@aws/durable-execution-sdk-js-otel/otel-execution` |
| Python     | `otel-invocation`                                    | `otel-execution`                                    |
| Java       | `otel-invocation`                                    | `otel-execution`                                    |

## Configure an application-owned provider

Configure the exporter and processors once, then register the resulting factory
as in the handler examples above. The plugin flushes at invocation end but does
not take over provider shutdown. These examples send OTLP HTTP to a collector
listening on loopback port 4318.

=== "TypeScript"

    Install `@opentelemetry/exporter-trace-otlp-http`. The provider factory receives
    the durable ID-generator factory; pass its result to the provider constructor.

    ```typescript
    import { createInvocationOtelPluginFactory } from "@aws/durable-execution-sdk-js-otel";
    import { OTLPTraceExporter } from "@opentelemetry/exporter-trace-otlp-http";
    import { BatchSpanProcessor, NodeTracerProvider } from "@opentelemetry/sdk-trace-node";

    const pluginFactory = createInvocationOtelPluginFactory({
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

    Use `createExecutionOtelPluginFactory` for the execution view.

=== "Python"

    Install the `standalone` extra and `opentelemetry-exporter-otlp-proto-http`.

    ```python
    from opentelemetry import trace
    from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
    from opentelemetry.sdk.trace import TracerProvider
    from opentelemetry.sdk.trace.export import BatchSpanProcessor
    from aws_durable_execution_sdk_python_otel import (
        InvocationOtelPluginFactory,
        OtelPluginConfig,
    )

    provider = TracerProvider()
    provider.add_span_processor(BatchSpanProcessor(
        OTLPSpanExporter(endpoint="http://localhost:4318/v1/traces")
    ))
    trace.set_tracer_provider(provider)
    plugin_factory = InvocationOtelPluginFactory(
        OtelPluginConfig(tracer_provider=provider)
    )
    ```

    Use `ExecutionOtelPluginFactory` for the execution view.

=== "Java"

    Add `io.opentelemetry:opentelemetry-exporter-otlp` to your dependencies. The
    factory wraps the provider builder's ID generator and sampler, then builds
    the provider once.

    ```java
    import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
    import io.opentelemetry.sdk.trace.SdkTracerProvider;
    import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
    import software.amazon.lambda.durable.otel.InvocationOtelPlugin;

    var exporter = OtlpHttpSpanExporter.builder()
            .setEndpoint("http://localhost:4318/v1/traces")
            .build();
    var pluginFactory = InvocationOtelPlugin.factory(
            SdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(exporter).build()));
    ```

    Use `ExecutionOtelPlugin.factory` for the execution view.

### Plugin options

=== "TypeScript"

    Pass `OtelPluginConfig` to either `createInvocationOtelPluginFactory(config)`
    or `createExecutionOtelPluginFactory(config)`.

    | Option                  | Default                        | Purpose                                                           |
    | ----------------------- | ------------------------------ | ----------------------------------------------------------------- |
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

    Pass `OtelPluginConfig` to `InvocationOtelPluginFactory(config)` or
    `ExecutionOtelPluginFactory(config)`.

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

    Both plugins expose `factory()`, `factory(config)`, `factory(providerBuilder)`,
    and `factory(providerBuilder, config)`. Build `OtelPluginConfig` with:

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

Configure sampling through the provider, or through `OTEL_TRACES_SAMPLER` and
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

Sampling decisions are not checkpointed across invocations. An explicit upstream
decision or deterministic trace-ID-based policy keeps them consistent across
resumes; a stateful custom sampler can choose differently on a later invocation.

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

    Two open fixes refine this behavior:

    - [PR #865](https://github.com/aws/aws-durable-execution-sdk-python/pull/865)
        marks `FAILED`, `TIMED_OUT`, and `STOPPED` operations as `ERROR` even when
        diagnostics are absent, and places terminal exception events at the span's
        normalized end time. Without this fix, these outcomes can remain `UNSET`
        when no error object is supplied.
    - [PR #869](https://github.com/aws/aws-durable-execution-sdk-python/pull/869)
        exports visible terminal updates from invocation input and checkpoint
        notifications when cached-parent replay skips the completed child. It
        deduplicates successful observations within an invocation, without promising
        exactly-once delivery across retries or exporter failures.

    These changes require builds containing the PRs until they are merged and released.
    The plugins cannot reconstruct operation metadata that the service has pruned.

=== "Java"

    `FAILED`, `TIMED_OUT`, and `STOPPED` operations set `ERROR` even without
    diagnostics. Missing details do not produce an exception event. Execution-view
    operation exceptions use the known backend end timestamp when available;
    invocation-view exceptions retain normal OpenTelemetry event timing.

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

If plugin spans are missing, check provider initialization warnings, exporter
configuration, the layer's execution wrapper, and sampling. For the Java agent,
also check `OTEL_JAVAAGENT_EXTENSIONS`. A missing `Workflow` can mean the execution
has not finished or no terminal invocation exported it. A parent ID without a
visible span can belong to the remote backend or an undelivered fallback root.

## See also

- [Plugins](plugins.md)
- [Logging](logging.md)
- [Durable Execution Telemetry](telemetry/index.md)
- [Steps](../operations/step.md)
