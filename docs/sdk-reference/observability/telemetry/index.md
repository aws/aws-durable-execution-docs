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

`recordType` is always `"DurableExecutionTelemetry"`. Use it to separate telemetry
records from other data in the same table or log group. In your function's own log group,
a TypeScript, Python, or Java record is a JSON string inside the log line, so filter on
the text instead, as shown in
[Register the exporter](exporter.md#register-the-exporter). `status` is `RUNNING`,
`SUCCEEDED`, or `FAILED`. A suspended or retrying execution reports `RUNNING`. `endTime`
and `durationMs` appear once the execution finishes. In `on-change` mode, TypeScript and
Java also set them on the `RUNNING` record they send at the end of each invocation.

Every timestamp in a record has the format `YYYY-MM-DDTHH:MM:SS.sssZ`. This covers
`emittedAt`, `startTime`, `endTime`, and each operation's `startTime` and `endTime`. The
value is in UTC, has exactly 3 fraction digits, and ends with `Z`. DynamoDB and
CloudWatch Logs Insights compare these values as text. Text order equals time order only
when every value has the same width, so the format never drops a zero fraction.
Destinations with a time type store the value in that type. Aurora PostgreSQL and
Redshift use `TIMESTAMPTZ` columns, OpenSearch maps the fields as `date`, and the Athena
table in the TypeScript exporter's
[CDK stack](https://github.com/aws/aws-durable-execution-sdk-js/tree/main/packages/aws-durable-execution-sdk-js-telemetry-exporter/cdk)
declares them as `timestamp`. With `engine: "mysql"`, the Aurora exporter writes plain
strings. `emittedAt` strictly increases within one invocation, so two records for the
same execution never share an `emittedAt`.

An error in a record has the same shape in the execution `error`, in `operations[].error`,
and in `operationsByName.<name>.error`:

```json
"error": {
  "name": "StepError",
  "message": "card declined",
  "cause": { "name": "PaymentError", "message": "card declined" }
}
```

`name` and `message` are the error's own type name and message. `cause` holds the error
inside it, with the same shape, when the SDK provides one. The chain stops after 16
levels.

Error names are not the same across SDKs. For a step that throws `PaymentError`,
TypeScript reports `StepError` for both the execution and the operation, with
`PaymentError` as the `cause`. Python reports
`aws_durable_execution_sdk_python.exceptions.StepError` for the execution and the thrown
type's qualified name, such as `orders.PaymentError`, for the operation. Java reports
the thrown class's simple name, `PaymentError`, for the execution and its fully
qualified name, such as `com.example.PaymentError`, for the operation. So query the
execution `error.name` and `operations[].error.name` separately for each SDK, or query
on `message`.

Each entry in `operations` describes one durable operation, with its `name`, `type`,
`status`, timing, `attempt` count, and `error`. The record omits the SDK's internal
`EXECUTION` operation and operations without a name. An operation's `result` appears only
when you opt in, as described in [Content](exporter.md#content).

Destinations that cannot filter an array element by name store an `operationsByName`
map instead of the `operations` array. In TypeScript, Python, and Java, the exporters for
CloudWatch Logs, Lambda logs, and DynamoDB use this shape. Each key is an operation name.
Each value aggregates every occurrence of that name with `count`, `failedCount`,
`minDurationMs`, `maxDurationMs`, `totalDurationMs`, `maxAttempt`, and the latest
`type`, `subType`, and `status`. `result` and `error` appear only when the name ran once.
A query can then reach an operation by a dot path, such as
`operationsByName.charge-payment.failedCount`. The exporters for EventBridge, Data
Firehose, SQS, OTLP, HTTP, and files accept an operations format option with the values
`array`, `by-name`, or `both`.

=== "TypeScript"

    The record type is `DurableExecutionTelemetryRecord`. Set the operations format with
    `operationsFormat: "by-name"`.

    A failed step keeps one level of cause. The SDK checkpoints the thrown error's type,
    message, and data, and rebuilds the `StepError` from that checkpoint. So the
    rebuilt error's `cause` is the thrown error without its own `cause`. The handler's
    `catch` receives the same rebuilt error. If the root cause matters, put it in the
    message of the error that the step throws.

=== "Python"

    The record is a plain `dict` with the fields shown above. Set the operations format
    with `operations_format="by-name"`. Errors never include `cause`, because the plugin
    receives only the checkpointed error, which has no cause.

=== "Java"

    The record type is `DurableExecutionTelemetryRecord`. A custom exporter reads the
    full record through `toWireMap()`. Set the operations format with
    `.operationsFormat(OperationsFormat.BY_NAME)`.

    The execution `error` keeps the cause's message, with the name `Throwable`.
    Operation errors have no `cause`.

=== "Go"

    The record type is `telemetry.Record`. Its field set differs from the other SDKs.
    It uses `startTimestamp` and `endTimestamp` instead of `startTime` and `endTime`. It
    has no `functionQualifier`, `region`, or `accountId`. It adds `invocationCount` and
    `custom`. `input` and `output` are objects with a string `value` and a `truncated`
    flag, and `error` holds `type`, `message`, and `stackTrace`. Timestamps use Go's
    default `time.Time` format, which drops trailing zero fraction digits. So their
    width varies, and text order does not always equal time order.

    The Go exporters for CloudWatch Logs, Lambda logs, and DynamoDB write the
    `operations` array, not `operationsByName`. The DynamoDB item stores the whole record
    as a JSON string in its `recordJson` attribute. Set the operations format with the
    `OperationsFormat` field. Go has no OTLP exporter.

## Explore records in an editor or desktop app

Two experimental apps let you browse and query telemetry records without an AI agent. The
[Durable Execution Telemetry VS Code extension](https://github.com/aws/aws-durable-execution-sdk-js/tree/main/packages/aws-durable-execution-sdk-js-telemetry-vscode)
and the
[Durable Execution Telemetry Desktop app](https://github.com/aws/aws-durable-execution-sdk-js/tree/main/packages/aws-durable-execution-sdk-js-telemetry-desktop)
connect to the same destinations as the MCP server and turn a question in plain
English into a read-only query. Both are previews, and both run from a checkout of the
`aws-durable-execution-sdk-js` repository.

## See also

- [Durable Execution Telemetry Exporter](exporter.md)
- [Durable Execution Telemetry MCP server](mcp.md)
- [Plugins](../plugins.md)
- [OpenTelemetry](../opentelemetry.md)
- [Logging](../logging.md)
