import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class ValidationErrorExample extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext context) {
        // The SDK throws IllegalArgumentException for invalid configuration values.
        // For example, passing a duration shorter than 1 second to context.wait():
        try {
            context.wait("short-wait", Duration.ofMillis(500)); // invalid: less than 1 second
        } catch (IllegalArgumentException e) {
            context.getLogger().error("Invalid SDK configuration: " + e.getMessage());
            return Map.of("error", "InvalidConfiguration", "message", e.getMessage());
        }
        return Map.of("status", "ok");
    }
}
