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
