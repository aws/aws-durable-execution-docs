# Durable Execution Telemetry MCP server

The Durable Execution Telemetry MCP server lets an AI agent query the
[telemetry records](index.md#the-execution-record) that the
[exporter](exporter.md) writes. It is a local server that speaks the
[Model Context Protocol](https://modelcontextprotocol.io) (MCP) over stdio, so any
MCP-capable agent, such as Kiro, Claude Code, or Cursor, can use it.

The tools read the record shape that the TypeScript, Python, and Java exporters write.
They do not support Go records yet. Go writes `startTimestamp` instead of `startTime`, so
the `since` and `until` filters skip Go records. Go's Lambda log line also has no
`level` and `message` envelope, so `list_executions` on `lambda-log-exporter` returns no
Go records.

Answering "why did last night's batch fail" requires knowing where the records live,
which query language that store uses, and how the record schema maps onto it. DynamoDB
PartiQL, Athena SQL, Aurora PostgreSQL, Redshift, OpenSearch, and CloudWatch Logs Insights
all differ. The server knows these details for the destination you configure, so the
agent can ask a question without writing a query for that store by hand. You ask the
agent a question, such as which steps in `order-processor` failed most often in the last
24 hours, and the agent picks the tools.

The server runs on your machine and validates query text before execution. Version
1.0 also requires the user to accept the risk of writes before data queries run. Your
credentials decide what queries can actually do, as described in
[Read-only access and data handling](#read-only-access-and-data-handling). It makes no
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

    `--scope project` writes `.mcp.json` at the repository root. The default scope,
    `local`, stores the server in `~/.claude.json` for this project only.

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
its configuration is incomplete, so this tool can report what is wrong. When the
connectivity checks pass, the result includes `writeAccess`. Its `status` is
`can-write`, with `findings` naming each way the credentials can change data, or
`unverified`, with a `reason`. Neither status certifies read-only access.

`describe_schema` returns the record schema, the query language, the table or log group,
and the row cap for the configured destination. It makes no AWS call. Field names,
column casing, and quoting differ between destinations, so an agent should call it before
it writes a query.

`list_executions` lists records filtered by `status`, `functionName`, `since`, and
`until`, with an optional `limit`. `since` and `until` accept an ISO 8601 date or date
and time. The server reads a value with no offset as UTC, and converts each bound to the
stored timestamp format before it compares. It needs no query language and answers most
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

Most results hold at most 1,000 rows. The server adds `| limit 1000` to a CloudWatch Logs
Insights `query` only when the query has no `limit` and no `stats` of its own, so such a
query can return more rows. A smaller `limit` returns fewer rows. A result with
`truncated: true` means the store may hold more matching data, so narrow the filters
instead of assuming the result is complete.

CloudWatch Logs Insights queries always cover a time window. `list_executions` derives
the window from `since`. `query` covers the last 24 hours and `get_execution` the last 7
days unless you pass `lookbackHours`. `list_executions` and `get_execution` report the
window they searched in `searchedLookbackHours`. On a log destination, `get_execution`
returns `found: false` when the record is not in that window, and also when the Logs
Insights call fails, for example with `AccessDenied`.

## Read-only access and data handling

Before `list_executions`, `get_execution`, or `query` runs, the server checks write
access and asks the user to accept that queries could change data. Acceptance
lasts for that server process. Declining, cancelling, or using a client that cannot
show the prompt blocks that query. The server checks and asks again on the next
data query. `test_destination` and `describe_schema` remain available.

Only the user can accept. For a client without MCP form prompts, the user can
set `DURABLE_TELEMETRY_ACCEPT_WRITE_ACCESS=true` in the server's environment
configuration and restart it. An agent must not set this on the user's behalf.
The setting records acceptance; it does not restrict credentials or make queries
read-only.

The server refuses a SQL query that does not start with `SELECT` or `WITH`, contains a
write keyword or a second statement, or uses SQL syntax its validator does not support.
The server rejects these statements before query execution. Write-access checks
can still use credentials to inspect the destination. The CloudWatch Logs Insights
language has no statements that write data.

The validator reads query text, and a database can perform writes it cannot see, for
example through a function called from a `SELECT`. So your credentials are what prevent
writes. Give the server credentials that can only read, and grant only the read actions
for your destination. For Aurora and Redshift, point
`DURABLE_TELEMETRY_AURORA_SECRET_ARN`, `DURABLE_TELEMETRY_REDSHIFT_SECRET_ARN`, or
`DURABLE_TELEMETRY_REDSHIFT_DB_USER` at a database user that has only `SELECT` on the
telemetry table, not the user the exporter writes with. For OpenSearch, map the identity
to a role with read-only permissions on the telemetry index.

The tools return record data as it is stored, including execution input and output. When
an agent queries production records, those payloads enter the agent's context and reach
the model behind it. Where the data goes from there depends on your agent's
configuration, not on the server. To keep sensitive values out of records in the first
place, use the exporter's [content transforms](exporter.md#content).

## See also

- [Durable Execution Telemetry](index.md)
- [Durable Execution Telemetry Exporter](exporter.md)
