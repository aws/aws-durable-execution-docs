import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;

public class ValidateOrderExample extends DurableHandler<Map<String, Object>, Map<String, Object>> {
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext context) {
        String orderId = (String) event.get("order_id");

        Map<String, Object> validation = context.step("validate_order", Map.class,
            (StepContext ctx) -> Map.of("order_id", orderId, "valid", true));

        return validation;
    }
}
