# Development Environment

This page covers how to set up your environment, wire up the local development loop,
configure a durable function, deploy it with infrastructure as code, and use the AI
agent (Kiro Power) that the team ships for building durable functions.

If you just want to deploy your first function with the AWS CLI, start with the
[Quickstart](quickstart.md). This page is about the day-to-day workflow after that.

## Development workflow

You develop durable functions in a tight inner loop: write the function, write tests,
and run them locally before you deploy. Once deployed, you run the same tests against
the deployed function to validate packaging and runtime configuration.

```mermaid
flowchart LR
    subgraph dev["Development (Local)"]
        direction LR
        A["Write Function"]
        B["Write Tests"]
        C["Run Tests"]
    end

    subgraph prod["Production (AWS)"]
        direction LR
        D["Deploy"]
        E["Test in Cloud"]
    end

    A --> B --> C --> D --> E

    style dev fill:#e3f2fd
    style prod fill:#fff3e0
```

The local runner replays your handler in-process, so you catch bugs in
milliseconds instead of waiting on a deploy. See [Testing](../testing/index.md) for the
full workflow.

## Prerequisites

- An AWS account and the [AWS CLI](https://docs.aws.amazon.com/cli/) (2.33.22 or later)
    configured with credentials. Verify with `aws sts get-caller-identity`.
- A language runtime (see the tabs below).
- One way to deploy: the [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/)
    (1.153.1 or later), the [AWS CDK](https://docs.aws.amazon.com/cdk/) (2.237.1 or
    later), or direct AWS CLI access.

## Write Function

Write your durable function handler and add the SDK to your project. Bundle the SDK with
your function code so you control the exact version, rather than relying on the Lambda
runtime-provided copy. The Lambda runtime-provided SDK version updates lag behind our aws-durable-execution-sdk-\* package updates, it can unclear what version of the SDK the runtime has and what features/bug-fixes are present. For the full handler code in each language, see the
[Quickstart](quickstart.md).

=== "TypeScript"

    Requires Node.js 22+.

    ```console
    npm install @aws/durable-execution-sdk-js
    ```

=== "Python"

    Requires Python 3.13+ (3.11 is the minimum the SDK supports; runtimes 3.13+ ship
    the SDK pre-installed).

    ```console
    pip install aws-durable-execution-sdk-python
    ```

=== "Java"

    Requires Java 17+ and Maven 3.8+. Add to your `pom.xml`:

    ```xml
    <dependency>
        <groupId>software.amazon.lambda.durable</groupId>
        <artifactId>aws-durable-execution-sdk-java</artifactId>
        <version>1.1.0</version>
    </dependency>
    ```

=== "C#"

    Requires the .NET 10 SDK.

    ```console
    dotnet add package Amazon.Lambda.DurableExecution
    ```

## Write Tests

Drive your handler with the testing SDK. The runner executes the handler through the
same replay-and-checkpoint loop the Lambda service uses, so local behavior matches the
cloud. TypeScript, Java, and C# ship a `LocalDurableTestRunner`; Python uses
`DurableFunctionTestRunner`.

A minimal test creates a runner with your handler, runs it, and asserts on the result:

=== "TypeScript"

    ```typescript
    --8<-- "examples/typescript/testing/authoring/minimal-test.ts"
    ```

=== "Python"

    ```python
    --8<-- "examples/python/testing/authoring/minimal-test.py"
    ```

=== "Java"

    ```java
    --8<-- "examples/java/testing/authoring/minimal-test.java"
    ```

=== "C#"

    ```csharp
    --8<-- "examples/csharp/testing/authoring/minimal-test.cs"
    ```

## Run Tests

Run the suite with your language's test runner:

=== "TypeScript"

    ```console
    npm test
    ```

=== "Python"

    ```console
    pytest
    ```

=== "Java"

    ```console
    mvn test
    ```

=== "C#"

    ```console
    dotnet test
    ```

Get operations by name (never by index) and invoke the runner more than once to assert
replay behavior. The [Testing](../testing/index.md) section covers installing the testing
SDK, [authoring tests](../testing/authoring.md), [assertions](../testing/assertions.md),
and [workflow patterns](../testing/workflow-patterns.md).

## Deploy

You can only set the DurableConfig at function creation time, not at a later update. Every durable function needs three things, whichever tool you
deploy with:

1. A `DurableConfig` on the function (`ExecutionTimeout` is required;
    `RetentionPeriodInDays` is optional). See the https://docs.aws.amazon.com/lambda/latest/dg/durable-configuration.html.
1. The
    [AWSLambdaBasicDurableExecutionRolePolicy](https://docs.aws.amazon.com/aws-managed-policy/latest/reference/AWSLambdaBasicDurableExecutionRolePolicy.html)
    managed policy on the execution role, which grants the checkpoint permissions.
1. A qualified ARN (a published version or an alias) to invoke. Durable execution is not
    supported on an unqualified function name.

For anything beyond a first experiment, we recommend you deploy with SAM or CDK so your function
configuration, IAM role, version, and alias live in source control. Tune `DurableConfig`
per environment: short timeouts and retention in development, longer values in
production.

=== "SAM"

    `template.yaml`:

    ```yaml
    AWSTemplateFormatVersion: '2010-09-09'
    Transform: AWS::Serverless-2016-10-31

    Resources:
      DurableFunction:
        Type: AWS::Serverless::Function
        Properties:
          Runtime: nodejs22.x
          Handler: index.handler
          CodeUri: ./src
          DurableConfig:
            ExecutionTimeout: 3600
            RetentionPeriodInDays: 7
          Policies:
            - arn:aws:iam::aws:policy/service-role/AWSLambdaBasicDurableExecutionRolePolicy
          AutoPublishAlias: prod

    Outputs:
      AliasArn:
        Value: !Ref DurableFunction.Alias
    ```

    Deploy:

    ```console
    sam build
    sam deploy --guided
    ```

    `AutoPublishAlias` gives you the qualified ARN (the `prod` alias) that durable
    invocation requires.

=== "CDK"

    ```typescript
    import * as cdk from 'aws-cdk-lib';
    import * as lambda from 'aws-cdk-lib/aws-lambda';

    export class DurableFunctionStack extends cdk.Stack {
      constructor(scope: cdk.App, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        const fn = new lambda.Function(this, 'DurableFunction', {
          runtime: lambda.Runtime.NODEJS_22_X,
          handler: 'index.handler',
          code: lambda.Code.fromAsset('lambda'),
          durableConfig: {
            executionTimeout: cdk.Duration.hours(1),
            retentionPeriod: cdk.Duration.days(7),
          },
        });

        // CDK adds the checkpoint permissions automatically when durableConfig is set.
        const alias = new lambda.Alias(this, 'ProdAlias', {
          aliasName: 'prod',
          version: fn.currentVersion,
        });

        new cdk.CfnOutput(this, 'AliasArn', { value: alias.functionArn });
      }
    }
    ```

    Deploy:

    ```console
    cdk deploy
    ```

You can also use CloudFormation (`AWS::Lambda::Function` with a `DurableConfig`
property). For durable invokes, callbacks, multi-environment stages, and log-group
management, see the deployment guidance in the
[Kiro Power](#agentic-development-with-kiro) below.

## Test in Cloud

After deploying, validate using Lambda in the cloud.

- Run your test suite against the deployed function with the
    [Cloud Runner](../testing/cloud-runner.md).

- Invoke locally in a container, or against the deployed function, with the
    [SAM CLI](../testing/sam-cli.md):

    ```console
    # Local container invoke — no deployment needed
    sam local invoke MyDurableFunction --durable-execution-name my-test

    # Remote invoke against a deployed function
    sam remote invoke MyDurableFunction --stack-name my-stack --event '{"name": "world"}'
    ```

## Agentic development with Kiro

The [Kiro](https://kiro.dev) Power for
**AWS Lambda durable functions** helps your AI coding assistant understand the replay model,
step and wait patterns, error handling, testing, and IaC for durable functions. It is
the fastest way to write correct durable functions with an agent, because it front-loads
the determinism rules that are easy to get wrong.

Install it from [kiro.dev/powers](https://kiro.dev/powers) or from your IDE, then
describe what you want to build:

```
Help me create a durable Lambda function that processes orders with retries
```

Kiro loads the relevant guidance and walks through the handler, steps with retry
strategies, error handling, tests with the local runner, and deployment. Mentioning
keywords such as `durable`, `workflow`, `saga`, `agentic`, `human-in-the-loop`, or
`callback` activates the Power automatically.

Durable functions are themselves a strong fit for **agentic** workloads: a GenAI loop
that calls tools, waits on human approval, and runs for hours or days survives
interruption and resumes from the last checkpoint. The Power includes patterns for
GenAI agent loops and human-in-the-loop approvals.

If you use another AI assistant, the same guidance is available as Markdown steering
files in the
[aws-lambda-durable-functions-power](https://github.com/aws/aws-durable-execution-docs/tree/main/aws-lambda-durable-functions-power)
directory of this repository — load them as context to get the same replay-model and
deployment rules.

## Next steps

- [Quickstart](quickstart.md) deploy your first durable function with the AWS CLI
- [Key Concepts](key-concepts.md) replay, checkpoints, and determinism
- [Testing](../testing/index.md) the runner, authoring tests, and assertions
- [Configuration](../sdk-reference/configuration/index.md) `DurableConfig` and the
    Lambda client
- [Manage Executions](manage-executions.md) list, inspect, stop, and clean up
