# Durable Execution Telemetry Exporter

The Durable Execution Telemetry Exporter is a plugin that builds a
[telemetry record](index.md#the-execution-record) for each durable execution and sends
it to one or more destinations. It builds on the [plugin interface](../plugins.md). It
runs inside your function, in the same process as your handler, and subscribes to the
SDK's lifecycle hooks. It makes no calls of its own until a record is due, and an
exporter failure never fails the execution.

## Install the exporter

=== "TypeScript"

    ```bash
    npm install @aws/durable-execution-sdk-js-telemetry-exporter@^1
    ```

    Exporter 1.x requires Core SDK 3.x and Node.js 22 or later. The AWS SDK clients that the AWS
    exporters use are optional peer dependencies. The Lambda Node.js runtime already
    provides them. The package imports all 12 of them when it loads, so when you bundle
    your own dependencies or run outside Lambda, install all 12, even for exporters you
    do not use:

    ```bash
    npm install @aws-sdk/client-s3 @aws-sdk/client-dynamodb @aws-sdk/util-dynamodb \
      @aws-sdk/client-rds-data @aws-sdk/client-cloudwatch-logs @aws-sdk/client-firehose \
      @aws-sdk/client-eventbridge @aws-sdk/client-redshift-data @aws-sdk/client-sqs \
      @aws-sdk/credential-provider-node "@smithy/signature-v4@^4" @aws-crypto/sha256-js
    ```

=== "Python"

    ```bash
    pip install "aws-durable-execution-sdk-python-telemetry-exporter>=1,<2"
    ```

    Exporter 1.x requires Core SDK 3.x and Python 3.11 or later. Core SDK 3.x
    already installs `boto3`, which the AWS exporters use.

=== "Java"

    ```xml
    <dependency>
        <groupId>software.amazon.lambda.durable</groupId>
        <artifactId>aws-durable-execution-sdk-java-plugin-telemetry-exporter</artifactId>
        <version>${durable.sdk.version}</version>
    </dependency>
    ```

    Set `durable.sdk.version` to `3.0.0` for the SDK and exporter.
    The exporter requires Java 17 or later. The AWS SDK for Java 2.x clients are
    optional dependencies. Add the client for each AWS exporter you use, for example
    `software.amazon.awssdk:s3` for `S3Exporter` or `software.amazon.awssdk:dynamodb`
    for `DynamoDBExporter`.

=== "Go"

    ```bash
    go get github.com/aws/aws-durable-execution-sdk-go/telemetry
    ```

    The exporter is a separate module from the SDK. Each AWS exporter takes a client
    from the AWS SDK for Go v2, so add the matching service module, such as
    `github.com/aws/aws-sdk-go-v2/service/s3`.

## Register the exporter

Register the exporter in your handler configuration. With no configuration,
the exporter uses `LambdaLogExporter`, which writes each record as one `INFO` log line to
your function's own CloudWatch log group. Your function already has permission to write
its own logs, so this needs no new infrastructure and no new IAM permission. In
TypeScript, Python, and Java, this exporter writes the `operationsByName` map, not the
`operations` array. With an application log level of `WARN` or higher, Lambda drops
these `INFO` lines, so no records arrive.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/observability/telemetry/basic-usage.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/observability/telemetry/basic-usage.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/observability/telemetry/basic-usage.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/sdk-reference/observability/telemetry/basic-usage.go"
    ```

You can query the records with CloudWatch Logs Insights right away. Durable functions
always use the JSON log format. In TypeScript, Python, and Java, each record arrives as
a JSON string in the `message` field of an `INFO` log line. Match the line, then parse
the fields you need out of the string:

```
filter level = "INFO" and message like /DurableExecutionTelemetry/
| parse message '"executionName":"*"' as executionName
| parse message '"status":"*"' as status
| parse message '"durationMs":*,' as duration
| filter status = "FAILED"
| fields tonumber(duration) as durationMs
| display executionName, status, durationMs
| sort durationMs desc
```

Each `parse` takes the first match in the record. The record writes the execution's
`executionName`, `status`, and `durationMs` before its operations, so the query reads the
execution's values and not those of an operation.

Go is the exception. Lambda leaves Go's application log lines as they are, so each Go
record arrives as a bare JSON object, and Logs Insights discovers its fields directly:

```
fields executionName, status, durationMs
| filter recordType = "DurableExecutionTelemetry" and status = "FAILED"
| sort durationMs desc
```

## Method signature

=== "TypeScript"

    ```typescript
    function durableExecutionTelemetry(
      config: DurableExecutionTelemetryConfig,
    ): DurableInstrumentationPluginFactory
    ```

    **Parameters:**

    - `config` (required): A `DurableExecutionTelemetryConfig`. Pass `{}` to use every
        default.

    **Returns:** A plugin factory for the `plugins` array of `withDurableExecution`.

=== "Python"

    ```python
    def durable_execution_telemetry(
        config: DurableExecutionTelemetryConfig,
    ) -> DurableExecutionTelemetryPluginFactory
    ```

    **Parameters:**

    - `config` (required): A `DurableExecutionTelemetryConfig`. Pass
        `DurableExecutionTelemetryConfig()` to use every default.

    **Returns:** A plugin factory for the `plugins` list of `@durable_execution`.

=== "Java"

    ```java
    public static DurableExecutionPluginFactory durableExecutionTelemetry(
            DurableExecutionTelemetryConfig config)
    ```

    **Parameters:**

    - `config` (required): A `DurableExecutionTelemetryConfig`. Pass
        `DurableExecutionTelemetryConfig.builder().build()` to use every default.

    **Returns:** A plugin factory to pass to `DurableConfig.Builder.withPlugins`.

=== "Go"

    ```go
    func New(cfg Config) *TelemetryPlugin
    func (p *TelemetryPlugin) Plugin() durable.Plugin
    ```

    **Parameters:**

    - `cfg`: A `telemetry.Config`. Pass `telemetry.Config{}` to use every default.

    **Returns:** A `*TelemetryPlugin`. Pass `Plugin()` to `durable.WithPlugins`.

## Choose exporters

Log groups do not suit questions across thousands of executions, such as the slowest
step in the last week. The exporter ships with 13 destinations, so you can send records
where those questions are cheap to answer:

| Destination               | Exporter                 | Use it for                                   |
| ------------------------- | ------------------------ | -------------------------------------------- |
| Function's own log group  | `LambdaLogExporter`      | Getting started with no setup                |
| Any CloudWatch log group  | `CloudWatchLogsExporter` | Collecting records from many functions       |
| Amazon S3                 | `S3Exporter`             | Amazon Athena SQL and long-term retention    |
| Amazon DynamoDB           | `DynamoDBExporter`       | Fast lookup by execution and live dashboards |
| Amazon Aurora             | `AuroraExporter`         | Joining workflow data with business data     |
| Amazon Redshift           | `RedshiftExporter`       | Analytics across functions at scale          |
| Amazon OpenSearch Service | `OpenSearchExporter`     | Full-text search and dashboards              |
| Amazon EventBridge        | `EventBridgeExporter`    | Alerting and remediation on failure          |
| Amazon Data Firehose      | `FirehoseExporter`       | Delivery to S3, Splunk, or third parties     |
| Amazon SQS                | `SQSExporter`            | Downstream processing                        |
| Any OTLP endpoint         | `OTelExporter`           | Third-party observability platforms          |
| Any HTTPS endpoint        | `HttpExporter`           | Custom backends and webhooks                 |
| File system or Amazon EFS | `FileExporter`           | Local development and EFS persistence        |

The plugin sends each record to every configured exporter. If one exporter fails, the
others still receive the record. TypeScript, Java, and Go send to the exporters
concurrently. Python calls them one at a time on a single worker thread. A common setup
uses one store for analytics, one for lookup, and one for alerting:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/observability/telemetry/multiple-exporters.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/observability/telemetry/multiple-exporters.py"
    ```

    Each AWS exporter creates its `boto3` client when you construct it, so it needs a
    region at that point. Lambda sets `AWS_REGION`. Pass `region` or `client` to
    override it. `OpenSearchExporter` is the exception: its `region` is required, and it
    takes no `client`.

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/observability/telemetry/multiple-exporters.java"
    ```

=== "Go"

    ```go
    --8<-- "examples/go/sdk-reference/observability/telemetry/multiple-exporters.go"
    ```

    Each AWS exporter takes its client in the `API` field, except `OpenSearchExporter`,
    which takes an HTTP client in `Client`. The Go module has no
    `OTelExporter`. Its Aurora exporter is named `RDSExporter`. Use `NewFileExporter`
    and `NewHTTPExporter` to create the file and HTTP exporters.

Each exporter has defaults that match the [MCP server](mcp.md) defaults. `S3Exporter`
writes under the prefix `durable-execution-telemetry/` in Hive-style date partitions
(`year=YYYY/month=MM/day=DD/`). The TypeScript exporter's
[CDK stack](https://github.com/aws/aws-durable-execution-sdk-js/tree/main/packages/aws-durable-execution-sdk-js-telemetry-exporter/cdk)
creates a Glue table with partition projection over that prefix, so Athena can query it
without a crawler. `S3Exporter` writes one object per execution, and later records
overwrite it. `DynamoDBExporter` writes the execution ARN to the `pk` attribute and the
emit time to the `sk` attribute. So a table keyed on `pk` and `sk` keeps every record,
and a table keyed on `pk` alone, like the CDK stack's, keeps only the latest one. On a
`pk`-only table, you can stop writing `sk` with `sortKey: ""` in TypeScript,
`sort_key=None` in Python, `.sortKey("")` in Java, or
`SortKeyField: telemetry.NoSortKey` in Go. On a table keyed on `pk` and `sk`, keep
writing `sk`, because every write fails without it. `AuroraExporter` and
`RedshiftExporter` write to the `durable_execution_telemetry` table, and
`OpenSearchExporter` writes to the `durable-execution-telemetry` index.
`EventBridgeExporter` sends events to the default bus with the source
`software.amazon.lambda.durable.telemetry` and the record status as the detail type.

For example, this Athena query finds the steps that fail most often in June:

```sql
SELECT op.name, COUNT(*) AS failures, AVG(op.durationms) AS avg_ms
FROM durable_execution_telemetry
CROSS JOIN UNNEST(operations) AS t(op)
WHERE op.type = 'STEP' AND op.status = 'FAILED' AND year = '2026' AND month = '06'
GROUP BY op.name
ORDER BY failures DESC;
```

## Emit mode

The emit mode decides when the plugin sends a record. The default, `on-complete`, sends
one record when the execution reaches `SUCCEEDED` or `FAILED`. `on-change` also sends a
`RUNNING` record as operations start and finish, so you can watch an execution progress.
`on-failure` sends one record only when the execution fails, which has the lowest
overhead.

Each record is a complete snapshot, so the plugin coalesces updates. If records arrive
faster than an exporter can send them, the plugin sends only the latest one.

=== "TypeScript"

    Set `emitMode` to `"on-complete"`, `"on-change"`, or `"on-failure"`.

=== "Python"

    Set `emit_mode` to `"on-complete"`, `"on-change"`, or `"on-failure"`, or to an
    `EmitMode` value. An unknown string raises `ValueError`.

=== "Java"

    Set `emitMode` to `EmitMode.ON_COMPLETE`, `EmitMode.ON_CHANGE`, or
    `EmitMode.ON_FAILURE`.

=== "Go"

    Set `EmitMode` to `telemetry.EmitOnComplete`, `telemetry.EmitOnChange`, or
    `telemetry.EmitAlways`. `EmitAlways` sends a record after every invocation,
    including invocations that suspend or retry. Go has no failure-only mode. Use
    `EmitOnComplete` and filter on `status` downstream.

## Sampling

The sampling rate reduces how many executions produce records. It is a number from 0 to
1, and the default is 1, which records every execution. The plugin derives the decision
from a hash of the execution ARN. The ARN stays the same across replays, so the decision
never changes during an execution. A sampled-in execution emits all of its records, and
a sampled-out execution emits none. You never receive a partly recorded execution.

=== "TypeScript"

    Set `samplingRate`. The plugin clamps a value outside 0 to 1, and uses 1 for a
    non-numeric value or `NaN`. Both cases log a warning.

=== "Python"

    Set `sampling_rate`. The plugin clamps a value outside 0 to 1, and uses 1 for a
    non-numeric value.

=== "Java"

    Set `samplingRate`. The plugin clamps a value outside 0 to 1, and uses 1 for `NaN`.

=== "Go"

    Set `SamplingRate`. A value of 0 or less, or greater than 1, becomes 1. So you
    cannot set 0 to turn recording off. Leave the plugin out of `durable.WithPlugins`
    instead.

## Operation detail

The operation detail decides which operations a record includes. The default,
`top-level`, includes only operations without a parent. It produces the same set of
operations no matter when the record is emitted. `full-tree` also includes the children
of child contexts, such as map items and parallel branches.

By default, the durable execution backend removes a finished child context's children
from the state it passes to later invocations. So after a suspend and resume, a
`full-tree` record contains only the children of contexts that were still running in the
invocation that emitted it.

=== "TypeScript"

    Set `operationDetail` to `"top-level"` or `"full-tree"`.

    To keep the full tree across a resume, also set `pluginsConfig.childOperationsDepth`
    on `withDurableExecution`. A value of `1` keeps the direct children of top-level
    contexts, `2` keeps their children too, and `Infinity` keeps the whole tree. Each
    preserved context costs an extra replay pass on resume, so keep the depth as low as
    you can.

    ```typescript
    export const handler = withDurableExecution(workflow, {
      plugins: [durableExecutionTelemetry({ operationDetail: "full-tree" })],
      pluginsConfig: { childOperationsDepth: 1 },
    });
    ```

=== "Python"

    Set `operation_detail` to `"top-level"` or `"full-tree"`. The Python SDK has no
    option to keep finished children across a resume.

=== "Java"

    Set `operationDetail` to `OperationDetail.TOP_LEVEL` or `OperationDetail.FULL_TREE`.

=== "Go"

    Set `OperationDetail` to `telemetry.OperationDetailTopLevel` or
    `telemetry.OperationDetailFullTree`.

## Content

By default, a record includes the execution input and output as they are, plus the
errors of each operation. That is convenient during development, but production input
and output often contain data that should not leave your function. Use the content
configuration to drop fields or replace them with a transformed copy. If a transform
throws, the plugin omits that field rather than send the raw value.

This example records 10% of executions and keeps only the order ID from the input and
the status from the output:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/sdk-reference/observability/telemetry/redact-content.ts"
    ```

    `input` and `output` accept `true`, `false`, or a transform function.
    `operations.overrides` takes entries with an `operationName`, and either
    `exclude: true` or a `result` transform. The plugin omits operation results unless
    an override opts one in. The transform receives the checkpointed result, parsed as
    JSON. It does not run your custom `Serdes`. `operations.includeErrors` controls
    operation errors.

=== "Python"

    ```python
    --8<-- "examples/python/sdk-reference/observability/telemetry/redact-content.py"
    ```

    `ContentConfig.input` and `ContentConfig.output` accept `True`, `False`, or a
    callable. `ContentConfig.operations` takes a `ContentOperations` with a list of
    `OperationOverride` entries and an `include_errors` flag. An `OperationOverride`
    matches an `operation_name` and sets `exclude=True` or a `result` callable.
    The plugin omits operation results unless an override opts one in.

=== "Java"

    ```java
    --8<-- "examples/java/sdk-reference/observability/telemetry/redact-content.java"
    ```

    Build a `ContentConfig` with `input(boolean)`, `inputTransform`,
    `output(boolean)`, `outputTransform`, and `includeErrors`. A transform receives a
    JSON-compatible copy, so an object arrives as a `Map`. `addOverride` takes an
    `OperationOverride.exclude(name)` or an `OperationOverride.withResult(name, fn)`.
    The plugin omits operation results unless an override opts one in.

=== "Go"

    ```go
    --8<-- "examples/go/sdk-reference/observability/telemetry/redact-content.go"
    ```

    `ContentConfig` uses flags and one `Redactor` function. The `Redactor` receives the
    raw JSON string of the input and of the output, and returns the string to record.
    `IncludeOperationResults`, `OperationFilter`, and `MaxContentLength` control
    operation results, which operations appear, and the maximum content length. If
    you set none of the `Include` flags, the plugin uses `DefaultContentConfig()`,
    which includes operation results and ignores your `Redactor`. So set the flags
    explicitly when you set a `Redactor`.

## Record size and truncation

Every destination limits the size of a payload. For example, a CloudWatch Logs event can
be 1 MB and a DynamoDB item 400 KB. Each exporter has a maximum record size. When a
record is larger, the plugin trims a copy for that exporter only, so the same record can
reach one exporter whole and another trimmed.

The plugin trims in this order until the record fits:

1. Every error `cause`
1. Operation results, oldest operation first
1. Whole operations, oldest first
1. The execution input, then the output

The plugin never removes the identity and timeline fields. A trimmed record has
`truncated: true`. Each operation that lost its result has `truncated: true`, and the
record uses `droppedCauses`, `droppedInput`, and `droppedOutput` as boolean
markers and `droppedOperations` as the number of operations removed. So you can always tell a trimmed record from a complete one.
Content transforms run before truncation, so prefer them to bound input and output on
purpose.

The default limits sit below each destination's hard limit, for example 256 KB for
CloudWatch Logs and 400 KB for DynamoDB. The HTTP and file exporters have no default
limit. Override a limit with the exporter's maximum record size setting.

## Custom exporters

To send records somewhere else, implement the exporter interface. The plugin calls
`export` with each record. It calls `flush` at the end of each sampled-in invocation,
and never at the same time as `export`. In TypeScript's `on-change` mode, an operation
change that arrives during or after that flush triggers another `flush`, so write
`flush` to allow repeated calls. An exporter that sets a maximum record size receives a
trimmed copy when a record is larger.

=== "TypeScript"

    ```typescript
    interface TelemetryExporter {
      export(record: DurableExecutionTelemetryRecord): Promise<void>;
      flush?(): Promise<void>;
      maxRecordSizeBytes?: number;
      render?(record: DurableExecutionTelemetryRecord): unknown;
    }
    ```

    The size limit measures the output of `render` when you provide it.

=== "Python"

    ```python
    class TelemetryExporter(Protocol):
        max_record_size_bytes: int | None

        def render(self, record: dict) -> Any: ...
        def export(self, record: dict) -> None: ...
        def flush(self) -> None: ...
    ```

    All three methods are required. `export` is synchronous and runs on a background
    thread. Return the record from `render` to size it as is.

=== "Java"

    ```java
    public interface TelemetryExporter {
        void export(DurableExecutionTelemetryRecord record);
        default void flush() {}
        default Integer maxRecordSizeBytes() { return null; }
        default Object render(DurableExecutionTelemetryRecord record) { return record.toWireMap(); }
    }
    ```

=== "Go"

    ```go
    type Exporter interface {
        Export(ctx context.Context, record Record) error
    }
    ```

    Implement `Flusher` to receive `Flush(ctx context.Context) error`, and
    `SizeLimiter` to set `MaxRecordSizeBytes() int`.

## Executions that end outside your function

The exporter runs inside your function, so it can emit a record only when your function
runs. Two terminal states start in the Lambda service without an invocation, so the
exporter never sees them. A manual stop ends the execution as `STOPPED`, and an exceeded
execution timeout ends it as `TIMED_OUT`. To record those states too, add an EventBridge
rule on the default bus for Lambda durable execution status changes. Point it at a small
function that updates your destination:

```json
{
  "source": ["aws.lambda"],
  "detail-type": ["Durable Execution Status Change"],
  "detail": { "status": ["STOPPED", "TIMED_OUT"] }
}
```

## Naming operations

The plugin uses operation names as they are, as keys and identifiers, and never escapes
them. A `.` in a name separates field paths in CloudWatch Logs Insights and OpenSearch,
so a name with a dot is hard to query there. Names made of letters, digits, `-`, and `_`
work in every destination.

## See also

- [Durable Execution Telemetry](index.md)
- [Durable Execution Telemetry MCP server](mcp.md)
- [Plugins](../plugins.md)
- [Child Context](../../operations/child-context.md)
