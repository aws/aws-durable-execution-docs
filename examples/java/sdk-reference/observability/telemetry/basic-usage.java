import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.telemetry.DurableExecutionTelemetry;
import software.amazon.lambda.durable.telemetry.DurableExecutionTelemetryConfig;

public class TelemetryExample extends DurableHandler<Map<String, String>, String> {
    @Override
    protected DurableConfig createConfiguration() {
        return DurableConfig.builder()
                .withPlugins(DurableExecutionTelemetry.durableExecutionTelemetry(
                        DurableExecutionTelemetryConfig.builder().build()))
                .build();
    }

    @Override
    public String handleRequest(Map<String, String> event, DurableContext context) {
        String data = context.step("fetch-data", String.class, ctx -> event.get("id"));
        context.wait("cooldown", Duration.ofSeconds(5));
        return context.step("process", String.class, ctx -> "processed " + data);
    }
}
