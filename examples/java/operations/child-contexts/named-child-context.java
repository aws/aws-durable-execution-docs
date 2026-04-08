import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class NamedChildContext extends DurableHandler<String, String> {

    @Override
    public String handleRequest(String orderId, DurableContext context) {
        // The name is always required. Pass null to omit it.
        String unnamed = context.runInChildContext(null, String.class, child ->
            child.step("process", String.class, ctx -> orderId + ":processed")
        );

        String named = context.runInChildContext("process-order", String.class, child ->
            child.step("process", String.class, ctx -> orderId + ":processed")
        );

        return unnamed + " | " + named;
    }
}
