import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;

public class Walkthrough extends DurableHandler<Object, Map<String, String>> {
    @Override
    public Map<String, String> handleRequest(Object input, DurableContext context) {
        // No SerDes config — the SDK serializes and deserializes the result automatically.
        Map<String, String> order = context.step(
            "fetch-order",
            Map.class,
            (StepContext ctx) -> Map.of("id", "order-123", "total", "99.99")
        );
        return order;
    }
}
