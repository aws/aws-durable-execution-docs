import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class PassArguments extends DurableHandler<String, String> {

    @Override
    public String handleRequest(String orderId, DurableContext context) {
        String userId = "user-123";

        // Capture arguments in a lambda
        return context.runInChildContext("process-order", String.class, child -> {
            String validated = child.step("validate", String.class, ctx -> validate(orderId, userId));
            return child.step("charge", String.class, ctx -> charge(validated));
        });
    }

    private String validate(String orderId, String userId) { return orderId + ":" + userId; }
    private String charge(String order) { return order + ":charged"; }
}
