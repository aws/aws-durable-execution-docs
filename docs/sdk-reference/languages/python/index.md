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

## 2.x Upgrade

When upgrading from `1.x` to `2.x`, review the Python SDK migration guide in the
[SDK repository](https://github.com/aws/aws-durable-execution-sdk-python/blob/main/docs/migration-1.x-to-2.x.md).
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
