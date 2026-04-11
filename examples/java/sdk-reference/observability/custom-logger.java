import software.amazon.lambda.durable.DurableConfig;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.logging.LoggerConfig;

public class CustomLogger extends DurableHandler<Object, Void> {

    @Override
    protected DurableConfig createConfiguration() {
        // Allow logs during replay by overriding the default LoggerConfig.
        return DurableConfig.builder()
                .withLoggerConfig(LoggerConfig.withReplayLogging())
                .build();
    }

    @Override
    public Void handleRequest(Object event, DurableContext context) {
        context.getLogger().info("Logs appear on every replay");
        return null;
    }
}
