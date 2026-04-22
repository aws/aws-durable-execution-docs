# Cloud Runner

The Durable Execution SDK Test Cloud Runner invokes a deployed Lambda function and polls
for completion. Use it to validate IAM permissions, real service integrations, and
deployment correctness. The same test assertions you write for local testing work with
the cloud runner.

## Set up the cloud runner

Create a `CloudDurableTestRunner` with the qualified function name and region. The
runner invokes the function, extracts the execution ARN from the response, and polls
until the execution completes.

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing/cloud-runner/cloud-runner.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing/cloud-runner/cloud-runner.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing/cloud-runner/cloud-runner.java"
    ```

### Deploy before running

Cloud mode requires a deployed function. Deploy with your preferred tool before running
cloud tests:

```bash
sam build
sam deploy --stack-name my-durable-function
```

After deployment, get the qualified function name or ARN:

```bash
aws lambda get-function --function-name MyFunction \
  --query 'Configuration.FunctionArn' --output text
```

### Configure polling

The runner polls for completion, then retrieves the full operation history for
assertions. Configure the poll interval to control how frequently the runner checks for
completion.

=== "TypeScript"

    Pass `config: { pollInterval }` (in milliseconds) to the constructor:

    ```typescript
    --8<-- "examples/typescript/testing/cloud-runner/cloud-runner-timeout.ts"
    ```

=== "Python"

    Pass `timeout` in seconds to `run()`:

    ```python
    --8<-- "examples/python/testing/cloud-runner/cloud-runner-timeout.py"
    ```

=== "Java"

    Use `withTimeout(Duration)` to set the maximum wait time:

    ```java
    --8<-- "examples/java/testing/cloud-runner/cloud-runner-timeout.java"
    ```

### Required IAM permissions

Cloud mode requires AWS credentials in the environment. The runner uses the default
credential chain, so any standard AWS credential configuration works.

The IAM principal running the tests needs these permissions on the target function:

```json
{
  "Effect": "Allow",
  "Action": [
    "lambda:InvokeFunction",
    "lambda:GetDurableExecution",
    "lambda:GetDurableExecutionHistory"
  ],
  "Resource": "arn:aws:lambda:region:account-id:function:function-name"
}
```

## Troubleshooting

### Credentials not configured

The runner throws when it cannot find AWS credentials. Run `aws sts get-caller-identity`
to verify your credentials are configured.

### Function not found

Verify the function name or ARN is correct and the function exists in the target region:
`aws lambda get-function --function-name MyFunction`.

### Execution timeout

The function took longer than the configured timeout. Increase the timeout or check the
function logs: `aws logs tail /aws/lambda/MyFunction --follow`.

!!! note

    SAM CLI also supports invoking deployed functions with `sam remote invoke` and resolving
    callbacks with `sam remote callback succeed`. See [SAM CLI](sam-cli.md).

## See also

- [Authoring](authoring.md) Set up the local test runner.
- [Assertions](assertions.md) Inspect operation history after a test run.
