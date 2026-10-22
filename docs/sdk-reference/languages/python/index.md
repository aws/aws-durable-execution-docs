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
plugin state when invocations run concurrently. Applications that register no plugins,
select none through `DURABLE_EXECUTION_PLUGINS`, and ship no plugin providers can update
their dependencies without changing handler or durable operation calls.

### Upgrade dependencies together

Use these coordinated versions after publication:

| Package                                    | Version |
| ------------------------------------------ | ------- |
| `aws-durable-execution-sdk-python`         | `3.0.0` |
| `aws-durable-execution-sdk-python-otel`    | `2.0.0` |
| `aws-durable-execution-sdk-python-testing` | `1.3.0` |

Upgrade Core and OTel together in your requirements, lock file, deployment bundle, and
Lambda layers. OTel remains optional, but OTel 2 requires Core 3. Testing SDK 1.3.0 already
declares a compatible Core range and does not need a major version bump. Run
`python -m pip check` in the built application environment.

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

### Validate the upgrade

Check the package versions imported by the deployed function and its layers. Test a
workflow that suspends and resumes, then test concurrent invocations and parallel
branches. Confirm fresh plugin instances and telemetry for each invocation. Plugin
fields do not survive suspension, and shared resources still need synchronization.
Preserve operation names, order, nesting, and serialization while executions remain
in flight.

For all API changes and before/after examples, see the
[Python 2.x to 3.0 migration guide](https://github.com/aws/aws-durable-execution-sdk-python/blob/df8951032df6e4fbbb0468bafd2ac09e9a2fdbd4/docs/migration-2.x-to-3.0.md).

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
