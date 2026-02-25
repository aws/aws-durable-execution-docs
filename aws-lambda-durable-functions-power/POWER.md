---
name: "aws-lambda-durable-functions"
displayName: "AWS Lambda durable functions"
description: "Build resilient, long-running AWS Lambda functions with automatic state persistence, retry logic, and workflow orchestration for up to 1 year execution"
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
   - For Python: Python 3.11+ (`python --version`)

3. **Deployment capability** exists (one of):
   - AWS SAM CLI (`sam --version`) 1.153.1 or higher
   - AWS CDK (`cdk --version`) v2.237.1 or higher
   - Direct Lambda deployment access

## Step 2: Check user and project preferences

Ask which IaC framework to use for new projects.
Ask which programming language to use if unclear, clarify between JavaScript and TypeScript if necessary.
Ask to create a git repo for projects if one doesn't exist already.

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

### Step 4: Verify IAM Permissions

Your Lambda execution role MUST have the `AWSLambdaBasicDurableExecutionRolePolicy` managed policy attached. This includes:
- `lambda:CheckpointDurableExecutions` - Persist execution state
- `lambda:GetDurableExecutionState` - Retrieve execution state
- CloudWatch Logs permissions

**Additional permissions needed for:**
- **Durable invokes**: `lambda:InvokeFunction` on target function ARNs
- **External callbacks**: Systems need `lambda:SendDurableExecutionCallbackSuccess` and `lambda:SendDurableExecutionCallbackFailure`

### Step 5: Create Workspace Hooks

Add validation hooks to `.kiro/hooks/`:

**Replay Model Validator** (`.kiro/hooks/validate-replay-model.kiro.hook`):
```json
{
  "enabled": true,
  "name": "Validate Replay Model Rules",
  "description": "Check for replay model violations before deployment",
  "version": "1",
  "when": {
    "type": "beforeCommit"
  },
  "then": {
    "type": "askAgent",
    "prompt": "Review Lambda handler code for replay model violations: 1) Non-deterministic code outside steps (Date.now, Math.random, UUID, API calls), 2) Nested durable operations in step functions, 3) Closure mutations that won't persist, 4) Side effects outside steps that will repeat on replay"
  }
}
```

**Testing Reminder** (`.kiro/hooks/test-durable-function.kiro.hook`):
```json
{
  "enabled": true,
  "name": "Test durable function",
  "description": "Ensure proper testing with test runners",
  "version": "1",
  "when": {
    "type": "userTriggered"
  },
  "then": {
    "type": "askAgent",
    "prompt": "Create or update tests using LocalDurableTestRunner. Verify: 1) All operations have names, 2) Tests get operations by name not index, 3) Replay behavior is tested with multiple invocations"
  }
}
```

## When to Load Steering Files

- **Getting started** or **basic setup** or **example** → `getting-started.md`
- **Understanding replay model**, **determinism**, or **non-deterministic errors** → `replay-model-rules.md`
- **Creating steps**, **atomic operations**, or **retry logic** → `step-operations.md`
- **Waiting**, **delays**, **callbacks**, **external systems**, or **polling** → `wait-operations.md`
- **Parallel execution**, **map operations**, **batch processing**, or **concurrency** → `concurrent-operations.md`
- **Error handling**, **retry strategies**, **saga pattern**, or **compensating transactions** → `error-handling.md`
- **Testing**, **local testing**, **cloud testing**, or **test runner** → `testing-patterns.md`
- **Deployment**, **CloudFormation**, **CDK**, **SAM**, or **infrastructure** → `deployment-iac.md`
- **Advanced patterns**, **GenAI agents**, **completion policies**, **step semantics**, or **custom serialization** -> `advanced-patterns.md`

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

1. **All non-deterministic code MUST be in steps** (Date.now, Math.random, API calls)
2. **Cannot nest durable operations** - use `runInChildContext` to group operations
3. **Closure mutations are lost on replay** - return values from steps
4. **Side effects outside steps repeat** - use `context.logger` (replay-aware)

When implementing or modifying tests for durable functions, ALWAYS verify:

1. All operations have descriptive names
2. Tests get operations by NAME, never by index
3. Replay behavior is tested with multiple invocations
4. Use `LocalDurableTestRunner` for local testing

### Invocation Requirements

Durable functions **require qualified ARNs** (version, alias, or `$LATEST`):
```bash
# ✅ Valid
aws lambda invoke --function-name my-function:1 output.json
aws lambda invoke --function-name my-function:prod output.json

# ❌ Invalid
aws lambda invoke --function-name my-function output.json
```

## Resources

- [AWS Lambda durable functions Documentation](https://docs.aws.amazon.com/lambda/latest/dg/durable-functions.html)
- [JavaScript SDK Repository](https://github.com/aws/aws-durable-execution-sdk-js)
- [Python SDK Repository](https://github.com/aws/aws-durable-execution-sdk-python)
- [IAM Policy Reference](https://docs.aws.amazon.com/aws-managed-policy/latest/reference/AWSLambdaBasicDurableExecutionRolePolicy.html)
