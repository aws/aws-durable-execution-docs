---
name: "aws-lambda-durable-functions"
displayName: "Build applications with Lambda durable functions"
description: "Build resilient multi-step applications and AI workflows with automatic state persistence, retry logic, and workflow orchestration for long-running executions."
keywords: [
  "lambda", "durable", "workflow", "orchestration", "state machine",
  "retry", "checkpoint", "long-running", "stateful", "saga",
  "agentic", "ai workflow", "human-in-the-loop", "callback",
  "aws", "serverless"
]
author: "AWS"
---

# AWS Lambda durable functions

Build resilient multi-step applications and AI workflows that can execute for up to 1 year while maintaining reliable progress despite interruptions.

## Onboarding

### Step 1: Validate Prerequisites

Before using AWS Lambda durable functions, verify:

1. **AWS CLI** is installed (2.33.22 or higher) and configured:

   ```bash
   aws --version
   aws sts get-caller-identity
   ```

2. **Runtime environment** is ready:
   - For TypeScript/JavaScript: Node.js 22+ (`node --version`)
   - For Python: Python 3.11+ (`python --version`. Note that currently only Lambda runtime environments 3.13+ come with the Durable Execution SDK pre-installed. 3.11 is the min supported Python version by the Durable SDK itself, however, you could use OCI to bring your own container image with your own Python runtime + Durable SDK.)

3. **Deployment capability** exists (one of):
   - AWS SAM CLI (`sam --version`) 1.153.1 or higher
   - AWS CDK (`cdk --version`) v2.237.1 or higher
   - Direct Lambda deployment access

## Step 2: Check user and project preferences

Ask which IaC framework to use for new projects.
Ask which programming language to use if unclear, clarify between JavaScript and TypeScript if necessary.
Ask to create a git repo for projects if one doesn't exist already.

### Error Scenarios

#### Unsupported Language

- List detected language
- State: "Durable Execution SDK is not yet available for [framework]"
- Suggest supported languages as alternatives

#### Unsupported IaC Framework

- List detected framework
- State: "[framework] might not support Lambda durable functions yet"
- Suggest supported frameworks as alternatives

### Step 3: Install SDK

**For TypeScript/JavaScript:**

```bash
npm install @aws/durable-execution-sdk-js
npm install --save-dev @aws/durable-execution-sdk-js-testing
```

**For Python:**

```bash
pip install aws-durable-execution-sdk-python
pip install aws-durable-execution-sdk-python-testing
```

## When to Load Reference Files

Load the appropriate reference file based on what the user is working on:

- **Getting started**, **basic setup**, **example**, **ESLint**, or **Jest setup** -> see [getting-started.md](steering/getting-started.md)
- **Understanding replay model**, **determinism**, or **non-deterministic errors** -> see [replay-model-rules.md](steering/replay-model-rules.md)
- **Creating steps**, **step operations**, or **retry logic** -> see [step-operations.md](steering/step-operations.md)
- **Waiting**, **delays**, **callbacks**, **external systems**, or **polling** -> see [wait-operations.md](steering/wait-operations.md)
- **Parallel execution**, **map operations**, **batch processing**, or **concurrency** -> see [concurrent-operations.md](steering/concurrent-operations.md)
- **Error handling**, **retry strategies**, **saga pattern**, or **compensating transactions** -> see [error-handling.md](steering/error-handling.md)
- **Advanced error handling**, **timeout handling**, **circuit breakers**, or **conditional retries** -> see [advanced-error-handling.md](steering/advanced-error-handling.md)
- **Testing**, **local testing**, **cloud testing**, **test runner**, or **flaky tests** -> see [testing-patterns.md](steering/testing-patterns.md)
- **Deployment**, **CloudFormation**, **CDK**, **SAM**, **log groups**, **deploy**, or **infrastructure** -> see [deployment-iac.md](steering/deployment-iac.md)
- **Advanced patterns**, **GenAI agents**, **completion policies**, **step semantics**, or **custom serialization** -> see [advanced-patterns.md](steering/advanced-patterns.md)
- **troubleshooting**, **stuck execution**, **failed execution**, **debug execution ID**, or **execution history** -> see [troubleshooting-executions.md](steering/troubleshooting-executions.md)

## Quick Reference

### Basic Handler Pattern

**TypeScript:**

```typescript
import { withDurableExecution, DurableContext } from '@aws/durable-execution-sdk-js';

export const handler = withDurableExecution(async (event, context: DurableContext) => {
  const result = await context.step('process', async () => processData(event));
  return result;
});
```

**Python:**

```python
from aws_durable_execution_sdk_python import durable_execution, DurableContext

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    result = context.step(lambda _: process_data(event), name='process')
    return result
```

### Critical Rules

1. **All non-deterministic code outside durable operations MUST be moved into durable operations** (`context.step`, `waitForCallback`, `waitForCondition`, `parallel`/`map` branches)
2. **Durable operation bodies are not guaranteed to be atomic** - prefer stable identity and idempotent behavior for external side effects; for non-idempotent steps, consider at-most-once-per-retry semantics with zero retries
3. **Cannot nest durable operations** - use `runInChildContext` to group operations
4. **Closure mutations are lost on replay** - return values from steps
5. **Side effects outside durable operations repeat** - prefer `context.logger`; custom loggers may duplicate on replay

### Python API Differences

The Python SDK differs from TypeScript in several key areas:

- **Steps**: Use `@durable_step` decorator + `context.step(my_step(args))`, or inline `context.step(lambda _: ..., name='...')`. Prefer the decorator for automatic step naming.
- **Wait**: `context.wait(duration=Duration.from_seconds(n), name='...')`
- **Exceptions**: `ExecutionError` (permanent), `InvocationError` (transient), `CallbackError` (callback failures)
- **Testing**: Use `DurableFunctionTestRunner` class directly - instantiate with handler, use context manager, call `run(input=...)`

### Invocation Requirements

Durable functions **require qualified ARNs** (version, alias, or `$LATEST`):

```bash
# Valid
aws lambda invoke --function-name my-function:1 output.json
aws lambda invoke --function-name my-function:prod output.json

# Invalid - will fail
aws lambda invoke --function-name my-function output.json
```

## IAM Permissions

Your Lambda execution role MUST have the `AWSLambdaBasicDurableExecutionRolePolicy` managed policy attached. This includes:

- `lambda:CheckpointDurableExecution` - Persist execution state
- `lambda:GetDurableExecutionState` - Retrieve execution state
- CloudWatch Logs permissions

See here: https://docs.aws.amazon.com/lambda/latest/dg/durable-security.html

**Additional permissions needed for:**

- **Durable invokes**: `lambda:InvokeFunction` on target function ARNs
- **External callbacks**: Systems need `lambda:SendDurableExecutionCallbackSuccess` and `lambda:SendDurableExecutionCallbackFailure`

## Validation Guidelines

When writing or reviewing durable function code, ALWAYS check for these replay model violations:

1. **Non-deterministic code outside durable operations**: `Date.now()`, `Math.random()`, UUID generation, API calls, database queries must all be inside durable operations
2. **Non-atomic durable operation bodies**: Functions passed to `context.step()`, `waitForCallback()`, `waitForCondition()`, and `parallel()`/`map()` branches may be re-attempted before persistence is fully committed — prefer stable identity and idempotent external effects; for non-idempotent steps, use at-most-once-per-retry semantics with zero retries when duplicate execution is unacceptable
3. **Nested durable operations in step functions**: Cannot call `context.step()`, `context.wait()`, or `context.invoke()` inside a step function — use `context.runInChildContext()` instead
4. **Closure mutations that won't persist**: Variables mutated inside steps are NOT preserved across replays — return values from steps instead
5. **Side effects outside durable operations that repeat on replay**: Prefer `context.logger` because it is replay-aware and deduplicates automatically; custom loggers are allowed but may emit duplicates unless `context.logger` is configured to wrap them

When implementing or modifying tests for durable functions, ALWAYS verify:

1. All operations have descriptive names
2. Tests get operations by NAME, never by index
3. Replay behavior is tested with multiple invocations
4. TypeScript: Use `LocalDurableTestRunner` for local testing
5. Python: Use `DurableFunctionTestRunner` class directly

## Resources

- [AWS Lambda durable functions Documentation](https://docs.aws.amazon.com/lambda/latest/dg/durable-functions.html)
- [JavaScript SDK Repository](https://github.com/aws/aws-durable-execution-sdk-js)
- [Python SDK Repository](https://github.com/aws/aws-durable-execution-sdk-python)
- [IAM Policy Reference](https://docs.aws.amazon.com/aws-managed-policy/latest/reference/AWSLambdaBasicDurableExecutionRolePolicy.html)
