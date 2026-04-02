import java.util.Map;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.retries.AdaptiveRetryStrategy;
import software.amazon.awssdk.retries.api.RetryStrategy;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class CustomLambdaClient extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    protected DurableConfig createConfiguration() {
        RetryStrategy retryStrategy = AdaptiveRetryStrategy.builder()
                .maxAttempts(5)
                .build();

        LambdaClient.Builder lambdaClientBuilder = LambdaClient.builder()
                .region(Region.US_WEST_2)
                .overrideConfiguration(o -> o.retryStrategy(retryStrategy));

        return DurableConfig.builder()
                .withLambdaClientBuilder(lambdaClientBuilder)
                .build();
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext context) {
        // Your durable function logic
        return Map.of("status", "success");
    }
}
