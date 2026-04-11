import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;

public class StepContextLogger extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object event, DurableContext context) {
        return context.step("process", String.class, (StepContext stepCtx) -> {
            // stepCtx.getLogger() includes operationId, operationName, and attempt in MDC.
            stepCtx.getLogger().info("Running step");
            return "done";
        });
    }
}
