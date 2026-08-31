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
