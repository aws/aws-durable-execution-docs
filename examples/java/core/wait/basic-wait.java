import java.time.Duration;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class BasicWaitExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        // Wait for 5 seconds
        context.wait(null, Duration.ofSeconds(5));
        return "Wait completed";
    }
}
