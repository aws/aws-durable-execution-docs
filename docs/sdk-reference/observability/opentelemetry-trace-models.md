# OpenTelemetry trace model tradeoffs

A durable execution can run across many Lambda invocations. A durable operation
can start in one invocation, suspend, and complete in another. This lifecycle
does not fit a conventional OpenTelemetry span tree without a tradeoff.

!!! note "Design direction"

    The OpenTelemetry plugins are experimental. This page describes the intended
    trace models. Current SDK implementations can differ while the plugins
    converge on these models.

## The unattainable triangle

A durable trace model cannot provide all three of these properties:

1. One root span per trace
1. A continuous child hierarchy across Lambda invocations
1. A short-lived root span

A continuous hierarchy needs a parent that covers the complete durable
execution. That parent must remain open until the execution finishes.

A short-lived root can cover only one Lambda invocation. When a durable
operation crosses an invocation boundary, the plugin must split that operation
into multiple spans.

Ending a root before its later children finish creates a misleading timeline.
Assigning one trace ID to several parentless invocation spans creates multiple
roots instead. The invocation and execution views make different choices within
this constraint.

## Consequences of each sacrifice

### Sacrifice one root span per trace

Several parentless spans share the same trace ID. An observability backend can
render them as separate top-level branches or select one branch as the display
root. The trace no longer has one authoritative span for its duration, outcome,
or service-map entry. Users must inspect peer roots and use attributes or links
to understand how they relate.

This choice can keep invocation spans short while retaining one execution-wide
trace ID. It does not create a single causal tree. Parent-child queries cannot
walk from one root into another.

### Sacrifice a continuous child hierarchy

The plugin splits work at invocation boundaries. One durable operation can
produce several operation segments with different span IDs and, when each
invocation starts a new trace, different trace IDs.

No individual span reports the operation's complete duration or final history.
Trace timelines show the time spent in each invocation instead. Users must
group segments by execution ARN and operation ID to calculate end-to-end
duration or follow the operation across invocations. Span links can assist this
navigation, but some observability backends do not display links.

### Sacrifice a short-lived root span

The root remains open for the complete durable execution. The backend might
receive child spans before it receives the root, so the trace can appear
incomplete while the execution runs. The root might never be exported if the
execution never reaches a terminal status.

Long-running roots can also exceed backend duration, retention, or ingestion
limits. Trace-level duration measures the complete durable execution, including
time spent suspended, rather than compute time in one Lambda invocation.

## Invocation view

The invocation view prioritizes bounded span trees that describe one Lambda
invocation.

```text
Invocation 1
`-- Operation segment A
    `-- Attempt
        `-- HTTP request

Invocation 2
`-- Operation segment B
```

This view sacrifices a continuous child hierarchy across invocations. Each
invocation contains only the work observed during that invocation. If an
operation starts in invocation 1 and completes in invocation 2, each invocation
records a separate operation segment.

The later segment can link to the earlier segment when the plugin has the
earlier `SpanContext`. A span link records correlation, but it does not make one
segment the parent of the other. The segments also carry the durable execution
ARN and operation ID so an observability backend can group them without a link.
As a result, no operation span reports the operation's complete
cross-invocation duration. Queries and dashboards must aggregate its segments.

The invocation span inherits propagated trace context when available. Without
propagated context, the provider generates a new trace ID. A deployment that
requires one standalone trace per invocation can start a new trace and link the
invocation span to the upstream context.

For an operation named `op-1`, the invocation view produces:

```text
Trace A
`-- Invocation A
    `-- op-1 segment A

Trace B
`-- Invocation B
    `-- op-1 segment B -- link to op-1 segment A
```

The invocation view does not create a `Workflow` span. It answers what happened
during one Lambda invocation. Consumers aggregate operation segments by
execution ARN and operation ID to build an execution-wide picture.

## Execution view

The execution view prioritizes one logical span hierarchy for the complete
durable execution.

```text
Workflow
`-- Operation
    `-- Attempt
        `-- HTTP request
```

This view sacrifices a short-lived root. The `Workflow` root starts at the
durable execution start time and ends when the execution reaches a terminal
status. It can remain open for seconds, hours, or days.

Top-level durable operations are children of `Workflow`. Nested operations are
children of their logical parent operations. The plugin uses deterministic
trace and span IDs to reconstruct a logical operation when it starts and
completes in different Lambda processes.

For the same `op-1` operation, the execution view produces:

```text
Workflow
`-- op-1
    |-- work observed in invocation A
    `-- work observed in invocation B
```

The execution view does not add an `Invocation` span to the workflow hierarchy.
It can use links and attributes to correlate logical spans with the Lambda
invocation that observed them.

The plugin can export operation spans before it exports the `Workflow` root. It
exports the root only after a terminal status. An execution that never reaches a
terminal status might never export its root. Observability backends must also
accept and retain long-running spans. As a result, the trace can lack its root
while the execution is in progress, and backend limits can prevent the complete
workflow span from appearing.

## Compare the views

| Consideration                | Invocation view                    | Execution view                |
| ---------------------------- | ---------------------------------- | ----------------------------- |
| Plugin anchor lifetime       | One Lambda invocation              | Complete durable execution    |
| Standalone trace roots       | One per invocation                 | One per durable execution     |
| Operation across invocations | One segment per invocation         | One logical span              |
| Invocation correlation       | Parent hierarchy                   | Links and attributes          |
| Workflow span                | No                                 | Yes                           |
| Root export                  | End of each invocation             | Terminal execution status     |
| In-progress visibility       | Each invocation appears as it ends | Root appears after completion |

Use the invocation view when bounded spans and in-progress visibility matter
more than a continuous operation span. Use the execution view when one logical
execution hierarchy matters more than root-span duration.

## Why hybrid models do not remove the tradeoff

Giving every invocation root the same deterministic trace ID groups spans, but
it creates a trace with multiple roots and no unambiguous causal hierarchy.

Ending `Workflow` after the first invocation while retaining it as the parent
of later spans places children outside the parent's lifetime.

Creating both `Workflow` and `Invocation` as parentless spans in one trace
creates competing roots. A link between them records correlation, but it does
not create a single span tree.

Both views keep deterministic ID generation scoped to plugin-owned span
creation. Unrelated instrumentation keeps the provider's normal ID generator.
An auto-instrumented span, such as an HTTP request inside a step attempt,
remains a child of the active attempt span in either view.

## See also

- [OpenTelemetry](opentelemetry.md)
- [Plugins](plugins.md)
- [Logging](logging.md)
