# Configuration

## Custom Lambda client

By default, the SDK initializes a Lambda client from your environment. You can provide a
custom client to control the region, retry settings, credentials, or other options.

=== "TypeScript"

    Pass a `LambdaClient` instance via the `config` parameter of `withDurableExecution`.

    ```typescript
    --8<-- "examples/typescript/configuration/custom-client.ts"
    ```

=== "Python"

    Pass a boto3 Lambda client via the `boto3_client` parameter of `@durable_execution`. The
    client must be a boto3 Lambda client.

    ```python
    --8<-- "examples/python/configuration/custom-client.py"
    ```

=== "Java"

    Override `createConfiguration()` in your `DurableHandler` subclass and use
    `DurableConfig.builder().withLambdaClientBuilder(...)`.

    ```java
    --8<-- "examples/java/configuration/custom-client.java"
    ```

=== "Go"

    The SDK takes no AWS Lambda client directly. `durable.WithExecutionClient`
    takes a `durable.ExecutionClient`, an SDK-owned interface. Implement it
    over a configured `*lambda.Client` to control the region, retry policy, or
    credentials. The example wraps a client built with a custom region and
    retry mode, and maps each call onto the `GetDurableExecutionState` and
    `CheckpointDurableExecution` Lambda APIs.

    The SDK's default client sets a 5 second connect timeout, a 50 second
    response timeout, and a 55 second total request timeout, and adds the
    SDK's user agent. A client you build gets none of these. Set the timeouts
    on its HTTP client if you need them.

    The SDK builds its default client from the default AWS config. To change
    only the region, credentials, or retry settings of the default client,
    set the standard AWS environment variables or shared config files, such as
    `AWS_REGION`, `AWS_MAX_ATTEMPTS`, and `AWS_RETRY_MODE`.

    ```go
    --8<-- "examples/go/configuration/custom-client.go"
    ```

=== "C#"

    Build an `AmazonLambdaConfig` (region, retry settings, and so on), construct an
    `AmazonLambdaClient` from it, and pass that `IAmazonLambda` as the fourth argument to
    `DurableFunction.WrapAsync`.

    ```csharp
    --8<-- "examples/csharp/configuration/custom-client.cs"
    ```
