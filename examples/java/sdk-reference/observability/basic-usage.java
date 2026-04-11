import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class BasicUsage extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object event, DurableContext context) {
        context.getLogger().info("Starting workflow");

        String result = context.step("process", String.class, stepCtx -> "done");

        context.getLogger().info("Workflow complete: {}", result);
        return result;
    }
}
