import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;

public class ProcessPaymentExample extends DurableHandler<Map<String, Object>, Map<String, Object>> {
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext context) {
        double amount = (double) event.get("amount");

        Map<String, Object> result = context.step("process_payment", Map.class,
            (StepContext ctx) -> Map.of("status", "completed", "amount", amount));

        return result;
    }
}
