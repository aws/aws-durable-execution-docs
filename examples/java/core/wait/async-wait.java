import java.time.Duration;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableFuture;
import software.amazon.lambda.durable.DurableHandler;

public class AsyncWaitExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        // Start wait and step concurrently — returns immediately
        DurableFuture<Void> waitFuture = context.waitAsync("min-delay", Duration.ofSeconds(5));
        DurableFuture<String> stepFuture = context.stepAsync("process", String.class, stepCtx -> processData(input));

        // Block until both complete — guarantees at least 5 seconds elapsed
        waitFuture.get();
        var result = stepFuture.get();

        return result;
    }
}
