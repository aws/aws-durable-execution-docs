# Durable Execution SDK for Python 2.x

_September 2026_

Version 2.0 of the AWS Lambda Durable Execution SDK for Python is a breaking major
release. Every change is a bug fix or brings Python in line with the JavaScript and Java SDKs. Review the [Python migration guide](https://github.com/aws/aws-durable-execution-sdk-python/blob/main/docs/migration-1.x-to-2.0.md) before upgrading from 1.x.

## What changed

- **Typed error hierarchy** The single `CallableRuntimeError` is replaced by typed,
    per-operation errors under a new `DurableOperationError` base: `StepError`,
    `InvokeError`, `ChildContextError`, and `WaitForConditionError`. `UserlandError` is
    removed.
- **First-run round trip** `step`, child contexts, `map`, `parallel`, and
    `wait_for_condition` now serialize then deserialize the result on the first run, so
    first-run output matches replay. `wait_for_condition` also round-trips `initial_state`.
- **Serialization errors** `SerDesError` signals a permanent failure; the new
    `RetryableSerDesError` signals a transient one that fails the invocation so the
    backend retries.
- **Removed configuration** `InvokeConfig.timeout` / `timeout_seconds`, `ItemBatcher`,
    `ChildConfig.item_serdes`, and `WaitDecision` are removed. See the migration guide for
    replacements.

## Get started

- [Python language guide](../sdk-reference/languages/python/index.md)

## Reference

- [Python migration guide (1.x to 2.0)](https://github.com/aws/aws-durable-execution-sdk-python/blob/main/docs/migration-1.x-to-2.0.md)
- [Python SDK releases](https://github.com/aws/aws-durable-execution-sdk-python/releases)
