import java.util.Map;
import org.slf4j.LoggerFactory;
import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.otel.OtelPlugin;

public class LogCorrelationExample extends DurableHandler<Map<String, String>, String> {
    static {
        LoggerFactory.getLogger(LogCorrelationExample.class).info("Function initialized");
    }

    @Override
    protected DurableConfig createConfiguration() {
        return DurableConfig.builder().withPlugins(OtelPlugin.factory()).build();
    }

    @Override
    public String handleRequest(Map<String, String> event, DurableContext context) {
        context.getLogger().info("Processing request");
        return context.step("process", String.class, stepContext -> {
            stepContext.getLogger().info("Processing step");
            return "done";
        });
    }
}
