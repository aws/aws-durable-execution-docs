# Configuration

- [Custom Lambda Client](custom-lambda-client.md) Configure the Lambda client used by
    the SDK

## Durable execution configuration

A durable function's behavior is controlled by its `DurableConfig`, which you set on
the function resource at deploy time (not in your handler code). The `DurableConfig`
holds two parameters:

| Parameter                | Required | Valid range                            | Default    | Description                                                                              |
| ------------------------ | -------- | -------------------------------------- | ---------- | ---------------------------------------------------------------------------------------- |
| `ExecutionTimeout`       | Yes      | `1` to `31622400` seconds (up to ~1 year) | None       | Maximum wall-clock time a single durable execution may run across all of its invocations. |
| `RetentionPeriodInDays`  | No       | `1` to `90` days                       | `14`       | How long the durable execution's history is retained after the execution completes.      |

These bounds are enforced by the Lambda service; a value outside the valid range is
rejected when you create or update the function.

You set `DurableConfig` wherever you define the function. For example, with the AWS CLI:

```bash
aws lambda create-function \
  --function-name my-durable-function \
  # ... other options ... \
  --durable-config '{"ExecutionTimeout": 900, "RetentionPeriodInDays": 7}'
```

The same parameters are available through CloudFormation/SAM (`DurableConfig` on the
function), the AWS CDK (`retentionPeriod`), and the C# `[DurableExecution]` annotation
(`RetentionPeriodInDays`).
