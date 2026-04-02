import java.time.Duration;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class NamedWaitExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        // Wait with explicit name
        context.wait("custom_wait", Duration.ofSeconds(2));
        return "Wait with name completed";
    }
}
