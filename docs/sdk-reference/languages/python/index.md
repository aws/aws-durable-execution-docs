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

- `@durable_execution` - Marks your Lambda handler as a durable function
- `@durable_step` - Marks a function that can be used with `context.step()`
- `@durable_with_child_context` - Marks a function that receives a child context

The Python SDK uses synchronous methods and does not support `await`.
