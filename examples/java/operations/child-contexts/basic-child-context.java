import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class BasicChildContext extends DurableHandler<String, String> {

    @Override
    public String handleRequest(String orderId, DurableContext context) {
        return context.runInChildContext("process-order", String.class, child -> {
            String validated = child.step("validate", String.class, ctx -> validate(orderId));
            return child.step("charge", String.class, ctx -> charge(validated));
        });
    }

    private String validate(String orderId) { return orderId + ":validated"; }
    private String charge(String order) { return order + ":charged"; }
}
