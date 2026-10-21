import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.telemetry.ContentConfig;
import software.amazon.lambda.durable.telemetry.DurableExecutionTelemetry;
import software.amazon.lambda.durable.telemetry.DurableExecutionTelemetryConfig;

public class RedactContentExample extends DurableHandler<Map<String, String>, Map<String, String>> {
    @Override
    protected DurableConfig createConfiguration() {
        return DurableConfig.builder()
                .withPlugins(DurableExecutionTelemetry.durableExecutionTelemetry(
                        DurableExecutionTelemetryConfig.builder()
                                .samplingRate(0.1)
                                .content(ContentConfig.builder()
                                        .inputTransform(input -> Map.of("orderId", ((Map<?, ?>) input).get("orderId")))
                                        .outputTransform(output -> Map.of("status", ((Map<?, ?>) output).get("status")))
                                        .build())
                                .build()))
                .build();
    }

    @Override
    public Map<String, String> handleRequest(Map<String, String> event, DurableContext context) {
        String order = context.step("fetch-order", String.class, ctx -> event.get("orderId"));
        context.wait("cooldown", Duration.ofSeconds(5));
        return Map.of("orderId", order, "status", "processed");
    }
}
