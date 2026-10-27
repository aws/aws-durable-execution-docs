# Python SDK

## Execution SDK

The execution SDK (`aws-durable-execution-sdk-python`) runs in your Lambda functions. It
provides `DurableContext`, operations, and decorators. Install it in your Lambda
deployment package.

```console
pip install aws-durable-execution-sdk-python
```

## Testing SDK

The testing SDK (`aws-durable-execution-sdk-python-testing`) lets you test durable
functions locally without AWS. It provides `DurableFunctionTestRunner`, pytest
integration, and result inspection. Install it in your development environment only.

```console
pip install aws-durable-execution-sdk-python-testing
```

## Decorators

The SDK provides decorators to mark functions as durable:

- **`@durable_execution`** marks your Lambda handler as a durable function.
- **`@durable_step`** wraps a function so it can be passed to `context.step()`,
    binding any extra arguments and preserving the function name.
- **`@durable_with_child_context`** wraps a function that receives a child
    `DurableContext`, for use with `context.run_in_child_context()`.
- **`@durable_parallel_branch`** wraps a function into a named `ParallelBranch`
    for `context.parallel()`. Call it with an optional `name` to produce the
    decorator.
- **`@durable_wait_for_callback`** wraps a submitter function for
    `context.wait_for_callback()`, binding extra arguments alongside the
    `callback_id` and `WaitForCallbackContext`.

The Python SDK uses synchronous methods and does not support `await`.

## Upgrade from 2.x to 3.0

Core 3.0 creates a fresh instrumentation plugin for each Lambda invocation. This isolates
plugin state when invocations run concurrently. The release also changes operation
subtypes to strings and preserves `None` results in `BatchResult`. Review those changes
even if your application uses no plugins.

### Upgrade dependencies together

The [Core 3.0 and OTel 2.0 release](https://github.com/aws/aws-durable-execution-sdk-python/releases/tag/sdk-v3.0.0%2Cotel-v2.0.0%2Ctesting-v1.4.0%2Ctelemetry-exporter-v0.0.2)
is available on PyPI and requires Python 3.11 or later. Use these compatible released
versions:

| Package                                               | Version |
| ----------------------------------------------------- | ------- |
| `aws-durable-execution-sdk-python`                    | `3.0.0` |
| `aws-durable-execution-sdk-python-otel`               | `2.0.0` |
| `aws-durable-execution-sdk-python-testing`            | `1.4.0` |
| `aws-durable-execution-sdk-python-telemetry-exporter` | `1.0.0` |

Upgrade Core and OTel together in your requirements, lock file, deployment bundle, and
Lambda layers. OTel remains optional, but OTel 2 requires Core `>=3.0.0,<4`. Testing SDK
1.4.0 requires Core 3 and supports custom subtypes; earlier testing releases cannot load
them. If you use the optional Telemetry Exporter, upgrade it to the stable
[1.0.0 release](https://github.com/aws/aws-durable-execution-sdk-python/releases/tag/telemetry-exporter-v1.0.0).
It requires Core `>=3.0.0,<4`; version 0.0.1 uses the Core 2 plugin contract. Run
`python -m pip check` in the built application environment.

The base OTel package uses the OpenTelemetry distribution supplied by your ADOT or
community layer. If your application configures its own provider, install
`aws-durable-execution-sdk-python-otel[standalone]==2.0.0` and configure the provider and
exporter. Installing the extra alone does not export traces.

### Update plugin registrations

Replace plugin instances in `plugins` with factory objects. Each factory's synchronous
`create_plugin(info)` method must return a fresh `DurableInstrumentationPlugin` subclass
instance. Pass a factory instance, not a bare lambda or an uninstantiated factory class
with an ordinary instance method. Keep shared resources on the factory and invocation
state on the plugin.

Replace `InvocationOtelPlugin(...)` with `InvocationOtelPluginFactory(...)`, or
`ExecutionOtelPlugin(...)` with `ExecutionOtelPluginFactory(...)`. Choose one OTel view
across the decorator and `DURABLE_EXECUTION_PLUGINS`.

Replace the removed `DurableInstrumentationPluginProvider` wrapper with a factory
object. Update the distribution's `aws_durable_execution.plugins` entry point to name
that object, then rebuild and reinstall the provider package or layer.

The Telemetry Exporter's `durable_execution_telemetry(config)` helper now returns a
factory. Pass its result directly to `plugins`.

### Review subtype and batch-result changes

Read `sub_type` as `str | None`. Replace `.value` reads with the string itself and `is`
comparisons with `==`. Import `OperationSubType` from
`aws_durable_execution_sdk_python.lambda_service`; the `plugin` module no longer
re-exports it.

`StepConfig` and `CallbackConfig` accept a keyword-only `sub_type`, and
`ChildConfig.sub_type` accepts custom strings. A custom subtype must contain 1 to 32
ASCII letters, digits, hyphens, or underscores. Preserve its value for executions already in
flight because replay compares it with the checkpoint. Testing SDK 1.4.0 does not yet
validate invalid subtypes locally, so verify custom subtypes in cloud tests too.

`BatchResult.succeeded()` and `get_results()` now retain every successful branch,
including results equal to `None`. Update aggregations and assertions that assumed these
methods omitted `None`. `failed()` and `get_errors()` likewise retain every failed item.

### Update custom operation hooks

Hooks no longer run on the checkpoint thread. Each hook still runs synchronously on its
own calling thread, and a slow hook delays that thread. `on_operation_change` runs on a
dedicated thread and can combine several checkpoint responses into one notification.
Do not assume one callback per response or ordering across threads. Synchronize state
shared between hooks, and finish plugin-owned background work in `on_invocation_end`.

### Validate the upgrade

Check the package versions imported by the deployed function and its layers. Test a
workflow that suspends and resumes, then test concurrent invocations and parallel
branches, including branches that return `None` or fail. Confirm fresh plugin instances
and telemetry for successful, failed, and retried invocations. Plugin fields do not
survive suspension, and shared resources still need synchronization. Preserve operation
names, types, subtypes, order, nesting, and serialization while executions remain in
flight, and verify completed steps do not repeat their side effects on replay.

For all API changes and before/after examples, see the
[Python 2.x to 3.0 migration guide](https://github.com/aws/aws-durable-execution-sdk-python/blob/7d9528047d5d0a2c9237075033631e10b7ebfd4f/docs/migration-2.x-to-3.0.md)
from the released SDK 3.0.0 commit.

## 2.x Upgrade

When upgrading from `1.x` to `2.x`, review the Python SDK migration guide in the
[SDK repository](https://github.com/aws/aws-durable-execution-sdk-python/blob/main/docs/migration-1.x-to-2.0.md).
The main changes are:

- Catch the typed, per-operation errors `StepError`, `InvokeError`,
    `ChildContextError`, and `WaitForConditionError` (or the base
    `DurableOperationError`) instead of the removed `CallableRuntimeError`.
- Replace `except CallableRuntimeError` after `BatchResult.throw_if_error()` with
    `ChildContextError`, `SerDesError`, and `BatchCompletionError`.
- Catch serialization failures as `SerDesError` (a direct child of
    `DurableExecutionsError`) instead of `ExecutionError`.
- Expect the first-run serialize/deserialize round trip for `step`, child contexts,
    `map`/`parallel`, and `wait_for_condition`, and ensure `wait_for_condition`
    `initial_state` is serializable by the configured serdes.
- Read the new `attempt` field on `StepContext` and `WaitForConditionCheckContext`, and
    pass `attempt` when constructing those contexts directly in tests.
