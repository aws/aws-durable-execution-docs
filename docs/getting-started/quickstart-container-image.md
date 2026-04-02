# Quickstart for Container Image with Java

Deploy your first durable function as a container image using the AWS CLI.

You can deploy any of the supported SDK languages in a container image.

This quickstart shows how to do so with Java.

## Prerequisites

- AWS CLI installed and configured with credentials
- Docker installed and running
- An Amazon ECR repository to push your image to
- Java 17+ and Maven 3.8+

## Create the execution role

Follow the [execution role setup](quickstart.md#create-the-execution-role) in the main
quickstart, then return here.

## Write the function

=== "Java"

    ```java
    --8<-- "examples/java/getting-started/quickstart.java"
    ```

## Set up your Maven project

Add to your `pom.xml` dependencies:

```xml
<dependency>
    <groupId>software.amazon.lambda.durable</groupId>
    <artifactId>aws-durable-execution-sdk-java</artifactId>
</dependency>
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-lambda-java-core</artifactId>
</dependency>
```

Add the `maven-shade-plugin` to produce a fat jar with all dependencies bundled:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.6.2</version>
    <configuration>
        <createDependencyReducedPom>false</createDependencyReducedPom>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
        </execution>
    </executions>
</plugin>
```

## Build and push the container image

Create a `Dockerfile` using a multi-stage build:

```dockerfile
FROM --platform=linux/amd64 amazoncorretto:21-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM public.ecr.aws/lambda/java:21
COPY --from=builder /build/target/*.jar ${LAMBDA_TASK_ROOT}/lib/
CMD ["QuickstartFunction::handleRequest"]
```

Authenticate, build, and push to ECR:

```console
aws ecr get-login-password --region us-east-1 \
  | docker login --username AWS --password-stdin \
    123456789012.dkr.ecr.us-east-1.amazonaws.com

docker build -t my-durable-function .

docker tag my-durable-function:latest \
  123456789012.dkr.ecr.us-east-1.amazonaws.com/my-durable-function:latest

docker push \
  123456789012.dkr.ecr.us-east-1.amazonaws.com/my-durable-function:latest
```

Replace `123456789012` and `us-east-1` with your account ID and region.

## Deploy

Replace `123456789012` with your AWS account ID and the role arn with that of the
execution role you just created.

```console
aws lambda create-function \
  --function-name my-durable-function \
  --package-type Image \
  --code ImageUri=123456789012.dkr.ecr.us-east-1.amazonaws.com/my-durable-function:latest \
  --role arn:aws:iam::123456789012:role/durable-function-role \
  --architectures x86_64 \
  --durable-config '{"ExecutionTimeout": 900, "RetentionPeriodInDays": 1}'
```

## Publish a version

```console
aws lambda publish-version --function-name my-durable-function
```

## Invoke

```console
aws lambda invoke \
  --function-name my-durable-function:1 \
  --cli-binary-format raw-in-base64-out \
  --payload '{}' \
  response.json

cat response.json
```

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
