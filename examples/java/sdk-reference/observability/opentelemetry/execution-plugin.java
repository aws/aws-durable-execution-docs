import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.otel.ExecutionOtelPlugin;

public class ExecutionOtelExample extends DurableHandler<Map<String, String>, String> {
    @Override
    protected DurableConfig createConfiguration() {
        return DurableConfig.builder().withPlugins(new ExecutionOtelPlugin()).build();
    }

    @Override
    protected String handleRequest(Map<String, String> event, DurableContext context) {
        String data = context.step("fetch-data", String.class, ctx -> event.get("id"));
        context.wait("cooldown", Duration.ofSeconds(5));
        return context.step("process", String.class, ctx -> "processed " + data);
    }
}
