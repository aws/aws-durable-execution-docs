# Durable Execution Telemetry

Durable Execution Telemetry records each durable execution as one JSON document. A
durable execution can run across many Lambda invocations, with waits and retries in
between. Lambda writes a separate log stream for each invocation, so the history of one
execution is spread across streams and interleaved with other executions. A telemetry
record collects that history in one place. It holds the execution's status, timing,
input, output, error, and every step, wait, callback, and invoke the execution ran.

Two components produce and read these records. The
[Durable Execution Telemetry Exporter](exporter.md) is a plugin for the Durable
Execution SDK. It builds a record inside your function and sends it to a destination you
choose, such as Amazon S3, Amazon DynamoDB, or your function's own CloudWatch log group.
The [Durable Execution Telemetry MCP server](mcp.md) lets an AI agent query the records
in that destination, read-only, through the Model Context Protocol (MCP).

Telemetry complements [logging](../logging.md) and
[OpenTelemetry](../opentelemetry.md). Logs show what your code printed during each
invocation. Traces show how long each span took and how spans relate to each other. A
telemetry record answers questions about the whole execution, such as which step failed,
how many attempts it took, and how long the execution ran. Because one record describes
one execution, those questions become ordinary queries against a table or an index.

!!! note "Experimental feature"

    Durable Execution Telemetry is experimental and may change in a future release.
    Feedback is welcome through our
    [GitHub Discussion](https://github.com/aws/aws-durable-execution-docs/discussions/206).

!!! note "C# support"

    The Durable Execution Telemetry Exporter is available for TypeScript, Python, Java,
    and Go. It is not yet available for C#.

## The execution record

The exporter builds a cumulative snapshot of the execution on each invocation. The
Durable Execution SDK replays your handler from the beginning on each resume and passes
the checkpointed state to the plugin. So the plugin can describe the whole execution so
far from any invocation, not just the work of the current one. Each record is complete
on its own, and a later record for the same execution supersedes earlier ones.

```json
{
  "recordType": "DurableExecutionTelemetry",
  "schemaVersion": "1.0",
  "emittedAt": "2026-06-16T17:00:27.514Z",
  "executionArn": "arn:aws:lambda:us-east-1:123456789012:function:order-processor:$LATEST/durable-execution/abc123/inv456",
  "executionName": "abc123",
  "functionName": "order-processor",
  "functionQualifier": "$LATEST",
  "region": "us-east-1",
  "accountId": "123456789012",
  "status": "SUCCEEDED",
  "startTime": "2026-06-16T17:00:22.100Z",
  "endTime": "2026-06-16T17:00:27.514Z",
  "durationMs": 5414,
  "input": { "orderId": "order-12345" },
  "output": { "orderId": "order-12345", "status": "completed" },
  "operations": [
    { "id": "c4ca4238a0b92382", "name": "validate-order", "type": "STEP", "status": "SUCCEEDED", "durationMs": 250, "attempt": 1 },
    { "id": "eccbc87e4b5ce2fe", "name": "cool-down", "type": "WAIT", "status": "SUCCEEDED", "durationMs": 5000 },
    { "id": "a87ff679a2f3e71d", "name": "charge-payment", "type": "STEP", "status": "SUCCEEDED", "durationMs": 80, "attempt": 1 }
  ]
}
```

`recordType` is always `"DurableExecutionTelemetry"`. Filter on it to separate
telemetry records from other data in the same log group or table. `status` is `RUNNING`,
`SUCCEEDED`, or `FAILED`. A suspended or retrying execution reports `RUNNING`. `endTime`
and `durationMs` appear only once the execution finishes. `error` holds the execution's
error `name` and `message` when it fails.

Each entry in `operations` describes one durable operation, with its `name`, `type`,
`status`, timing, `attempt` count, and `error`. The SDK's internal `EXECUTION` operation
and operations without a name are left out. An operation's `result` appears only when
you opt in, as described in [Content](exporter.md#content).

Destinations that cannot filter an array element by name store an `operationsByName`
map instead of the `operations` array. The exporters for CloudWatch Logs and DynamoDB use
this shape. Each key is an operation name. Each value aggregates every occurrence of
that name with `count`, `failedCount`, `minDurationMs`, `maxDurationMs`,
`totalDurationMs`, `maxAttempt`, and the latest `status`. A query can then reach an
operation by a dot path, such as `operationsByName.charge-payment.failedCount`. The
exporters for EventBridge, Data Firehose, SQS, OTLP, HTTP, and files accept an
`operationsFormat` option of `array`, `by-name`, or `both`.

=== "TypeScript"

    The record type is `DurableExecutionTelemetryRecord`.

=== "Python"

    The record is a plain `dict` with the fields shown above.

=== "Java"

    The record type is `DurableExecutionTelemetryRecord`. A custom exporter reads the
    full record through `toWireMap()`.

=== "Go"

    The record type is `telemetry.Record`. Its field set differs from the other SDKs.
    It uses `startTimestamp` and `endTimestamp` instead of `startTime` and `endTime`. It
    has no `functionQualifier`, `region`, or `accountId`. It adds `invocationCount` and
    `custom`. `input` and `output` are objects with a string `value` and a `truncated`
    flag, and `error` holds `type`, `message`, and `stackTrace`.

## Explore records in an editor or desktop app

Two experimental apps let you browse and query telemetry records without an AI agent. The
Durable Execution Telemetry VSCode extension and the Durable Execution Telemetry Desktop
app connect to the same destinations as the MCP server and turn a question in plain
English into a read-only query. Both are previews. The VS Code extension is available as
a `.vsix` file on the
[aws-durable-execution-sdk-js releases page](https://github.com/aws/aws-durable-execution-sdk-js/releases),
and the desktop app runs from a checkout of that repository.

## See also

- [Durable Execution Telemetry Exporter](exporter.md)
- [Durable Execution Telemetry MCP server](mcp.md)
- [Plugins](../plugins.md)
- [OpenTelemetry](../opentelemetry.md)
- [Logging](../logging.md)
