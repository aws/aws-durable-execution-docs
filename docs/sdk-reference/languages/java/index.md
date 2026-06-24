# Java SDK

The Java SDK (`aws-durable-execution-sdk-java`) runs in your Lambda functions and
provides `DurableHandler`, `DurableContext`, durable operations, configurable
serialization, and local/cloud testing utilities.

The SDK supports Java 17 and newer. Virtual threads require Java 21.

## Installation

Add the execution SDK to the Lambda function package. Replace the version with the
latest release from
[Maven Central](https://central.sonatype.com/artifact/software.amazon.lambda.durable/aws-durable-execution-sdk-java).

```xml
<dependency>
    <groupId>software.amazon.lambda.durable</groupId>
    <artifactId>aws-durable-execution-sdk-java</artifactId>
    <version>1.2.1</version>
</dependency>
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-lambda-java-core</artifactId>
    <version>1.4.0</version>
</dependency>
```

For local and cloud tests, add the testing SDK in test scope:

```xml
<dependency>
    <groupId>software.amazon.lambda.durable</groupId>
    <artifactId>aws-durable-execution-sdk-java-testing</artifactId>
    <version>1.2.1</version>
    <scope>test</scope>
</dependency>
```

Package the Lambda as a shaded jar so the SDK and its dependencies are available at
runtime:

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

## Usage

Extend `DurableHandler<I, O>` and implement `handleRequest(I input, DurableContext ctx)`.
Use the context to create steps, waits, callbacks, child contexts, invokes, maps, and
parallel branches.

```java
import java.time.Duration;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class OrderProcessor extends DurableHandler<Order, OrderResult> {

    @Override
    public OrderResult handleRequest(Order order, DurableContext ctx) {
        var reservation = ctx.step("reserve-inventory", Reservation.class,
                stepCtx -> inventoryService.reserve(order.items()));

        ctx.wait("warehouse-delay", Duration.ofHours(2));

        var shipment = ctx.step("ship-order", Shipment.class,
                stepCtx -> shippingService.ship(reservation, order.address()));

        return new OrderResult(order.id(), shipment.trackingNumber());
    }
}
```

Operation names are required in Java. Use stable, descriptive names so replay can match
new execution to checkpointed state.

## Configuration

Override `createConfiguration()` to customize serialization, the Lambda client, logging,
polling, checkpoint batching, plugins, or the executor used for user-defined async work.

```java
import java.time.Duration;
import java.util.concurrent.Executors;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class ConfiguredHandler extends DurableHandler<Input, Output> {

    @Override
    protected DurableConfig createConfiguration() {
        var lambdaClientBuilder = LambdaClient.builder()
                .region(Region.US_WEST_2);

        return DurableConfig.builder()
                .withLambdaClientBuilder(lambdaClientBuilder)
                .withExecutorService(Executors.newFixedThreadPool(16))
                .withCheckpointDelay(Duration.ofMillis(10))
                .build();
    }

    @Override
    public Output handleRequest(Input input, DurableContext ctx) {
        // Durable function logic
    }
}
```

The configured executor is used for user operations such as async steps and concurrent
branches. Internal SDK polling and checkpoint coordination use SDK-managed threads.

## 2.x Upgrade

When upgrading from `1.x` to `2.x`, review the Java SDK migration guide in the
[SDK repository](https://github.com/aws/aws-durable-execution-sdk-java/blob/main/docs/migration-1.x-to-2.x.md).
The main changes are:

- Replace `StepConfig.builder().semantics(...)` with `semanticsPerRetry(...)`.
- Preserve old `AT_MOST_ONCE_PER_RETRY` behavior by also setting
  `retryStrategy(RetryStrategies.Presets.NO_RETRY)`.
- Update log queries and dashboards from `durableExecutionArn`, `contextId`, and
  `contextName` to `executionArn`, `operationId`, and `operationName`.
- Move replay checks to `DurableContext.isReplaying()`; `StepContext` no longer exposes
  replay state.
- Update tests and error handling that expect invalid context usage to throw
  `IllegalDurableOperationException`; `2.x` throws `IllegalStateException`.
- Verify custom `SerDes` implementations can deserialize values immediately after
  serialization. `2.x` validates this round trip before checkpointing by default.

Useful searches before upgrading:

```console
rg -n "\.semantics\(" .
rg -n "durableExecutionArn|contextId|contextName" .
rg -n "isReplaying|StepContext" .
```

If you need a temporary log compatibility window, configure old MDC key names while
dashboards are migrated:

```java
import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.logging.LoggerConfig;

@Override
protected DurableConfig createConfiguration() {
    return DurableConfig.builder()
            .withLoggerConfig(new LoggerConfig(true, true))
            .build();
}
```

## FAQ

### Can I use a virtual thread pool?

Yes. The SDK baseline is Java 17, so it does not use virtual threads by default. On
Java 21 or newer, provide a virtual-thread executor through `DurableConfig`.

```java
import java.util.concurrent.Executors;
import software.amazon.lambda.durable.DurableConfig;

@Override
protected DurableConfig createConfiguration() {
    return DurableConfig.builder()
            .withExecutorService(Executors.newVirtualThreadPerTaskExecutor())
            .build();
}
```

This executor only runs user-defined operation code. Virtual threads can help when you
create many concurrent async steps or branches, but they do not remove Lambda memory,
timeout, downstream service, or durable execution service limits.

### Can a Java durable function be triggered by SQS, SNS, EventBridge, or other event sources?

Yes, but Java event model deserialization needs care. The default user-data serializer is
the SDK's Jackson-based `JacksonSerDes`, not the same serializer the Lambda Java runtime
uses for event classes from `aws-lambda-java-events`. For broad event-source support, add
the Lambda Java serialization library, which provides the
`com.amazonaws.services.lambda.runtime.serialization` package, and route Lambda event
classes through it.

```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-lambda-java-serialization</artifactId>
    <version>VERSION</version>
</dependency>
```

Then provide a `SerDes` adapter:

```java
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import java.lang.reflect.Type;
import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.TypeToken;
import software.amazon.lambda.durable.serde.JacksonSerDes;
import software.amazon.lambda.durable.serde.SerDes;

@Override
protected DurableConfig createConfiguration() {
    return DurableConfig.builder()
            .withSerDes(new LambdaEventSerDes())
            .build();
}

final class LambdaEventSerDes implements SerDes {
    private final SerDes fallback = new JacksonSerDes();
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    @Override
    public String serialize(Object value) {
        return fallback.serialize(value);
    }

    @Override
    public <T> T deserialize(String data, TypeToken<T> typeToken) {
        Type type = typeToken.getType();
        if (type instanceof Class<?> clazz && isLambdaEvent(clazz)) {
            @SuppressWarnings("unchecked")
            var serializer = LambdaEventSerializers.serializerFor((Class<T>) clazz, classLoader);
            return serializer.fromJson(data);
        }

        return fallback.deserialize(data, typeToken);
    }

    private static boolean isLambdaEvent(Class<?> clazz) {
        return clazz.getName().startsWith("com.amazonaws.services.lambda.runtime.events.");
    }
}
```

This pattern addresses the event-trigger deserialization problem discussed in
[aws/aws-durable-execution-sdk-java#366](https://github.com/aws/aws-durable-execution-sdk-java/issues/366).
The configured `SerDes` is also used as the default for steps, child contexts, callbacks,
and other operations unless an operation-specific config provides its own serializer.

### Can I use Lambda SnapStart?

Yes, but avoid the Java SDK default Lambda client if SnapStart restores a snapshot after
the environment-variable credentials captured at initialization have expired. The
default client uses `EnvironmentVariableCredentialsProvider`, which can cause
authentication failures after restore.

For SnapStart, provide your own `LambdaClient` with a credentials provider that refreshes
after restore, then pass it to `withLambdaClientBuilder(...)`:

```java
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.lambda.durable.DurableConfig;

@Override
protected DurableConfig createConfiguration() {
    var lambdaClientBuilder = LambdaClient.builder()
            .region(Region.of(System.getenv("AWS_REGION")))
            .credentialsProvider(ContainerCredentialsProvider.builder().build());

    return DurableConfig.builder()
            .withLambdaClientBuilder(lambdaClientBuilder)
            .build();
}
```

If you customize the Lambda client for SnapStart, keep the region explicit and avoid
caching resolved credentials yourself during initialization.

### Where is the Java SDK source and API reference?

The source is in
[aws/aws-durable-execution-sdk-java](https://github.com/aws/aws-durable-execution-sdk-java).
The generated Javadoc is published at
[aws.github.io/aws-durable-execution-sdk-java/javadoc](https://aws.github.io/aws-durable-execution-sdk-java/javadoc/).
