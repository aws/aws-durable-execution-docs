# Manage Executions

Use the AWS CLI to inspect, stop, update, and clean up durable functions and their
executions.

## List executions

```console
aws lambda list-durable-executions-by-function \
  --function-name my-durable-function
```

## Get execution details

```console
aws lambda get-durable-execution \
  --durable-execution-arn <execution-arn>
```

## Get execution history

View the checkpoint history for an execution:

```console
aws lambda get-durable-execution-history \
  --durable-execution-arn <execution-arn>
```

## Stop an execution

```console
aws lambda stop-durable-execution \
  --durable-execution-arn <execution-arn>
```

Stopping an execution affects the durable execution and the running Lambda invocation
differently. Understanding the distinction helps you predict what happens to code that is
already running.

### What stopping does

`StopDurableExecution` stops the **durable execution**. Once stopped, the execution no
longer accepts checkpoints. Any operation that was in progress stays in the `STARTED`
state, and no further terminal checkpoints (`SUCCEEDED` or `FAILED`) are recorded for it.

Stopping the execution does **not** stop the **Lambda invocation** that is currently
running. Lambda has no mechanism to interrupt a function that is already executing, so any
code that is mid-flight (for example, inside a [step](../sdk-reference/operations/step.md)
or a [child context](../sdk-reference/operations/child-context.md)) keeps running.

### What happens to running code

If your function is actively running when the execution is stopped, it continues until it
reaches the next checkpoint. At that point the SDK detects that the execution has been
stopped, raises an invocation error, and ends the current invocation. In practice this is
usually at the end of the current block of code, when the SDK tries to checkpoint that
block's result.

!!! note

    Because the invocation runs until its next checkpoint, work that is already underway
    is not rolled back or cancelled. Side effects performed before the next checkpoint (for
    example, an external API call inside a step) still take effect. Design long-running or
    side-effecting steps with this in mind if you expect executions to be stopped.

## Update function code

After updating your code, publish a new version and point your alias to it.

=== "Zip (TypeScript/Python/C#)"

    ```console
    aws lambda update-function-code \
      --function-name my-durable-function \
      --zip-file fileb://function.zip

    aws lambda wait function-updated \
      --function-name my-durable-function

    aws lambda publish-version \
      --function-name my-durable-function

    aws lambda update-alias \
      --function-name my-durable-function \
      --name prod \
      --function-version <new-version>
    ```

=== "Container image (Java)"

    ```console
    aws lambda update-function-code \
      --function-name my-durable-function \
      --image-uri 123456789012.dkr.ecr.us-east-1.amazonaws.com/my-durable-function:latest

    aws lambda wait function-updated \
      --function-name my-durable-function

    aws lambda publish-version \
      --function-name my-durable-function

    aws lambda update-alias \
      --function-name my-durable-function \
      --name prod \
      --function-version <new-version>
    ```

Running executions will continue to use the version they started with. New invocations
use the updated alias.

If you're still actively developing and you don't want to publish a new version each
time you update, you could use `LATEST$` just during development, but please be very
aware that executions might not replay correctly (or even fail) if the underlying code
changes during running executions. Always use numbered versions or aliases in
production.

## View logs

```console
aws logs tail /aws/lambda/my-durable-function --follow
```

## Delete durable functions

```console
aws lambda delete-function --function-name my-durable-function

aws iam detach-role-policy \
  --role-name durable-function-role \
  --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicDurableExecutionRolePolicy

aws iam delete-role --role-name durable-function-role
```

If you deployed as a container image, also
[delete the image](https://docs.aws.amazon.com/AmazonECR/latest/userguide/delete_image.html)
from ECR:

```console
aws ecr batch-delete-image \
  --repository-name my-durable-function \
  --image-ids imageTag=latest
```

Replace `latest` with the tag you pushed if you used a different tag. To delete multiple
tags at once, specify each with a separate `imageTag=` argument:

```console
aws ecr batch-delete-image \
  --repository-name my-durable-function \
  --image-ids imageTag=latest imageTag=v1.0.0
```
