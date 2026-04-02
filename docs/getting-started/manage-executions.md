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

## Update function code

After updating your code, publish a new version and point your alias to it.

=== "Zip (TypeScript/Python)"

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
