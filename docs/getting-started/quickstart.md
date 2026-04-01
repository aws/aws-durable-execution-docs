# Quickstart

Create and deploy your first durable function using the AWS CLI. This guide covers
TypeScript and Python.

!!! note "Adding all your dependencies to the deployment package"

    This guide shows you how to package all your dependencies, including the Durable
    Execution SDK, and deploy together with your custom code as a zip archive. This ensures
    that you control the exact version of the Durable Execution SDK that your code uses. You
    can create a durable function for quick testing purposes in the AWS Console, but then
    the version of the SDK might be older and it might not contain the latest features and
    optimizations.

## Prerequisites

- AWS CLI installed and configured with credentials

=== "TypeScript"

    - Node.js 22+

=== "Python"

    - Python 3.13+

## Create the execution role

Create an IAM role that grants your function permission to perform checkpoint
operations.

Save the following as `trust-policy.json`:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {
                "Service": "lambda.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
        }
    ]
}
```

Create the role and attach the
[AWSLambdaBasicDurableExecutionRolePolicy](https://docs.aws.amazon.com/aws-managed-policy/latest/reference/AWSLambdaBasicDurableExecutionRolePolicy.html)
managed policy:

```console
# Replace durable-function-role with your preferred role name
aws iam create-role \
  --role-name durable-function-role \
  --assume-role-policy-document file://trust-policy.json

aws iam attach-role-policy \
  --role-name durable-function-role \
  --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicDurableExecutionRolePolicy
```

Note the role ARN returned. You'll need it in the next step.

## Write the durable function

!!! note "Using Java?"

    Java durable functions currently only deploy in container images. See
    [Quickstart for Container Image](quickstart-container-image.md).

=== "TypeScript"

    Save as `index.mjs`

    ```typescript
    --8<-- "examples/typescript/getting-started/quickstart.ts"
    ```

=== "Python"

    Save as `lambda_function.py`

    ```python
    --8<-- "examples/python/getting-started/quickstart.py"
    ```

The wait here is for 10 seconds just for an easy quick example, but it could as easily
be 10 days without incurring extra compute.

## Package and deploy

Replace `123456789012` with your AWS account ID and the role arn with that of the
execution role you just created.

=== "TypeScript"

    ```console
    mkdir my-function && cd my-function
    npm init -y
    npm install @aws/durable-execution-sdk-js
    ```

    Save the function code above as `index.mjs`, then package and deploy:

    ```console
    zip -r function.zip index.mjs node_modules/

    aws lambda create-function \
      --function-name my-durable-function \
      --runtime nodejs22.x \
      --role arn:aws:iam::123456789012:role/durable-function-role \
      --handler index.handler \
      --zip-file fileb://function.zip \
      --durable-config '{"ExecutionTimeout": 900, "RetentionPeriodInDays": 1}'
    ```

=== "Python"

    ```console
    mkdir -p package
    pip install aws-durable-execution-sdk-python --target package/
    cp lambda_function.py package/
    cd package && zip -r ../function.zip . && cd ..

    aws lambda create-function \
      --function-name my-durable-function \
      --runtime python3.14 \
      --role arn:aws:iam::123456789012:role/durable-function-role \
      --handler lambda_function.lambda_handler \
      --zip-file fileb://function.zip \
      --durable-config '{"ExecutionTimeout": 900, "RetentionPeriodInDays": 1}'
    ```

### Publish a version

You must invoke a durable functions with a published version or alias to ensure
deterministic replay.

For quick testing here in the Quickstart we can just invoke the durable function with
`$LATEST`. Note that you should NOT do this for production workloads.

Be sure to publish a version if this is for production. Invoking `$LATEST` directly is
not supported for production workloads.

```console
aws lambda publish-version --function-name my-durable-function
```

Note the version number in the returned ARN (for example, `:1`).

## Invoke

For synchronous invocation:

```console
aws lambda invoke \
  --function-name 'my-durable-function:$LATEST' \
  --cli-binary-format raw-in-base64-out \
  --payload '{}' \
  response.json

cat response.json
```

The function runs `step-1`, then pauses for 10 seconds without consuming compute. After
the wait, it resumes and returns the result.

## Clean up

See [delete durable functions](manage-executions.md#delete-durable-functions) to clean
up your function and IAM role.

## Next steps

- [Manage Executions](manage-executions.md) list, inspect, stop, update, and clean up
- [Development Environment](development-environment.md) write and run tests locally
    before deploying
- [Key Concepts](key-concepts.md) understand replay, checkpoints, and determinism
- [Steps](../sdk-reference/operations/step.md) retry strategies and checkpointing
- [Wait](../sdk-reference/operations/wait.md) pause execution up to a year
