import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class ReplaySuppression extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object event, DurableContext context) {
        // context.getLogger() suppresses duplicate logs during replay by default.
        // Logs from completed operations do not repeat when the SDK replays.
        context.getLogger().info("Step 1 starting");

        String result = context.step("step-1", String.class, stepCtx -> "result");

        context.getLogger().info("Step 1 complete: {}", result);
        return result;
    }
}
