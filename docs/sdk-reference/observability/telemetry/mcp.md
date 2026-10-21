# Durable Execution Telemetry MCP server

The Durable Execution Telemetry MCP server lets an AI agent query the
[telemetry records](index.md#the-execution-record) that the
[exporter](exporter.md) writes. It is a local server that speaks the
[Model Context Protocol](https://modelcontextprotocol.io) (MCP) over stdio, so any
MCP-capable agent, such as Kiro, Claude Code, or Cursor, can use it.

Answering "why did last night's batch fail" requires knowing where the records live,
which query language that store uses, and how the record schema maps onto it. DynamoDB
PartiQL, Athena SQL, Aurora PostgreSQL, Redshift, OpenSearch, and CloudWatch Logs Insights
all differ. The server knows these details for the destination you configure, so the
agent can ask a question without writing a query for that store by hand. You ask the
agent a question, such as which steps in `order-processor` failed most often in the last
24 hours, and the agent picks the tools.

The server runs on your machine and makes only read-only AWS API calls. It makes no
model calls of its own. It is a Node.js package and requires Node.js 22.12 or later.

## Add the server to your agent

You do not install the package yourself. Every client starts it with
`npx -y @aws/durable-execution-sdk-js-telemetry-mcp` and passes the destination settings
as environment variables. This example points the server at a DynamoDB table.

=== "Kiro"

    ```bash
    kiro-cli mcp add \
      --name durable-telemetry \
      --command npx \
      --args "-y,@aws/durable-execution-sdk-js-telemetry-mcp" \
      --env DURABLE_TELEMETRY_DESTINATION_TYPE=dynamodb \
      --env DURABLE_TELEMETRY_DYNAMODB_TABLE_NAME=durable-execution-telemetry-live \
      --env DURABLE_TELEMETRY_REGION=us-east-1 \
      --scope workspace
    ```

    `--scope workspace` writes `.kiro/settings/mcp.json`. Omit it to write
    `~/.kiro/settings/mcp.json` instead.

=== "Claude Code"

    ```bash
    claude mcp add durable-telemetry \
      --scope project \
      --env DURABLE_TELEMETRY_DESTINATION_TYPE=dynamodb \
      --env DURABLE_TELEMETRY_DYNAMODB_TABLE_NAME=durable-execution-telemetry-live \
      --env DURABLE_TELEMETRY_REGION=us-east-1 \
      -- npx -y @aws/durable-execution-sdk-js-telemetry-mcp
    ```

    `--scope project` writes `.mcp.json` at the repository root. The default user scope
    writes `~/.claude.json`.

=== "JSON"

    Clients without a helper command, such as Cursor, read the same `mcpServers` block
    from `.cursor/mcp.json` or `~/.cursor/mcp.json`.

    ```json
    {
      "mcpServers": {
        "durable-telemetry": {
          "command": "npx",
          "args": ["-y", "@aws/durable-execution-sdk-js-telemetry-mcp"],
          "env": {
            "DURABLE_TELEMETRY_DESTINATION_TYPE": "dynamodb",
            "DURABLE_TELEMETRY_DYNAMODB_TABLE_NAME": "durable-execution-telemetry-live",
            "DURABLE_TELEMETRY_REGION": "us-east-1"
          }
        }
      }
    }
    ```

A project-scope configuration file is usually committed, so it shares the destination
with everyone who checks out the repository. Use user scope when the destination is
yours alone.

After you add the server, reload your client. Ask the agent to run `test_destination`.
A result with `ok: true` confirms the connection.

## Configuration

The server reads its configuration only from environment variables, so it keeps no
settings file and no state. `DURABLE_TELEMETRY_DESTINATION_TYPE` selects the
destination, and each destination needs the variables in this table. Settings not
listed have defaults that match the exporter defaults.

| `DURABLE_TELEMETRY_DESTINATION_TYPE` | Required variables                                                                                                                 |
| ------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------- |
| `dynamodb`                           | `DURABLE_TELEMETRY_DYNAMODB_TABLE_NAME`                                                                                            |
| `s3` (queried with Athena)           | `DURABLE_TELEMETRY_ATHENA_DATABASE`, and one of `DURABLE_TELEMETRY_ATHENA_WORKGROUP` or `DURABLE_TELEMETRY_ATHENA_OUTPUT_LOCATION` |
| `aurora`                             | `DURABLE_TELEMETRY_AURORA_RESOURCE_ARN` and `DURABLE_TELEMETRY_AURORA_SECRET_ARN`                                                  |
| `redshift`                           | One of `DURABLE_TELEMETRY_REDSHIFT_WORKGROUP_NAME` or `DURABLE_TELEMETRY_REDSHIFT_CLUSTER_IDENTIFIER`                              |
| `opensearch`                         | `DURABLE_TELEMETRY_OPENSEARCH_ENDPOINT`                                                                                            |
| `cloudwatch-logs-exporter`           | `DURABLE_TELEMETRY_LOG_GROUP_NAME`, comma-separated for more than one log group                                                    |
| `lambda-log-exporter`                | `DURABLE_TELEMETRY_LOG_GROUP_NAME`, comma-separated for more than one log group                                                    |

If `DURABLE_TELEMETRY_DESTINATION_TYPE` is unset, the server uses
`cloudwatch-logs-exporter`. An unrecognized value falls back to the same default, and
the server logs a warning that lists the valid values. The table names default to
`durable_execution_telemetry` and the OpenSearch index to `durable-execution-telemetry`.
Override them with variables such as `DURABLE_TELEMETRY_ATHENA_TABLE` or
`DURABLE_TELEMETRY_OPENSEARCH_INDEX`. Each variable name is `DURABLE_TELEMETRY_` followed
by the setting name in upper snake case.

The server takes credentials from the standard AWS SDK credential chain. Set
`DURABLE_TELEMETRY_AWS_PROFILE` to choose a profile. `AWS_PROFILE` and `AWS_REGION` fill
in `DURABLE_TELEMETRY_AWS_PROFILE` and `DURABLE_TELEMETRY_REGION` when those are unset.
The identity needs read access to the destination.

SQS is not a queryable destination. A queue has no query engine behind it, so the query
tools refuse an `sqs` destination with an error.

## Tools

The server registers five tools. An agent usually calls them in this order.

`test_destination` runs read-only connectivity checks against the configured
destination. If a required variable is unset, it returns `ok: false` and lists the
missing variables in `missingEnvVars` without calling AWS. The server starts even when
its configuration is incomplete, so this tool can report what is wrong.

`describe_schema` returns the record schema, the query language, the table or log group,
and the row cap for the configured destination. It makes no AWS call. Field names,
column casing, and quoting differ between destinations, so an agent should call it before
it writes a query.

`list_executions` lists records filtered by `status`, `functionName`, `since`, and
`until`, with an optional `limit`. It needs no query language and answers most
questions, so an agent should prefer it to `query`.

`get_execution` fetches one record by its execution ARN. A missing record returns
`found: false` rather than an error. For the `s3` destination, you can also pass `year`,
`month`, and `day` to read a single partition.

`query` runs one read-only query for questions the filters cannot express. On the SQL
destinations it takes a `SELECT` or `WITH` statement. On the CloudWatch Logs
destinations it takes a CloudWatch Logs Insights query and a `lookbackHours` time window.

The server also provides two MCP prompts. `investigate_workflow_failure` guides an agent
through a failure investigation, and `explore_recent_executions` gives an overview of
recent executions. Each prompt runs the tools in the right order and reads the schema
from `describe_schema`.

## Results

Every result holds at most 1,000 rows. A smaller `limit` returns fewer rows. A result
with `truncated: true` means the store may hold more matching data, so narrow the
filters instead of assuming the result is complete.

CloudWatch Logs Insights queries always cover a time window. `list_executions` derives
the window from `since`. `query` covers the last 24 hours and `get_execution` the last 7
days unless you pass `lookbackHours`. Each tool reports the window it searched in
`searchedLookbackHours`. So on a log destination, `found: false` from `get_execution`
means the record is not in that window.

## Read-only access and data handling

Every query is read-only. On the SQL destinations, the server rejects any statement that
is not `SELECT` or `WITH` before it resolves credentials or calls AWS. The CloudWatch Logs
Insights language has no statements that write data. So the server has no way to change
the data in your destination.

The tools return record data as it is stored, including execution input and output. When
an agent queries production records, those payloads enter the agent's context and reach
the model behind it. Where the data goes from there depends on your agent's
configuration, not on the server. To keep sensitive values out of records in the first
place, use the exporter's [content transforms](exporter.md#content).

## See also

- [Durable Execution Telemetry](index.md)
- [Durable Execution Telemetry Exporter](exporter.md)
