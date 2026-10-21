import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.telemetry.DurableExecutionTelemetry;
import software.amazon.lambda.durable.telemetry.DurableExecutionTelemetryConfig;
import software.amazon.lambda.durable.telemetry.DurableExecutionTelemetryConfig.EmitMode;
import software.amazon.lambda.durable.telemetry.exporters.DynamoDBExporter;
import software.amazon.lambda.durable.telemetry.exporters.EventBridgeExporter;
import software.amazon.lambda.durable.telemetry.exporters.S3Exporter;

public class MultipleExportersExample extends DurableHandler<Map<String, String>, String> {
    @Override
    protected DurableConfig createConfiguration() {
        return DurableConfig.builder()
                .withPlugins(
                        DurableExecutionTelemetry.durableExecutionTelemetry(DurableExecutionTelemetryConfig.builder()
                                .emitMode(EmitMode.ON_COMPLETE)
                                .samplingRate(1.0)
                                .addExporter(S3Exporter.builder()
                                        .bucket("durable-execution-telemetry-archive")
                                        .build())
                                .addExporter(DynamoDBExporter.builder()
                                        .tableName("durable-execution-telemetry-live")
                                        .build())
                                .addExporter(EventBridgeExporter.builder().build())
                                .build()))
                .build();
    }

    @Override
    public String handleRequest(Map<String, String> event, DurableContext context) {
        String data = context.step("fetch-data", String.class, ctx -> event.get("id"));
        context.wait("cooldown", Duration.ofSeconds(5));
        return context.step("process", String.class, ctx -> "processed " + data);
    }
}
