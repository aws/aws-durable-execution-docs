import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.exception.StepFailedException;

public class BasicErrorHandling extends DurableHandler<Map<String, String>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, String> event, DurableContext context) {
        try {
            Map<String, String> result = context.step("process-order", Map.class, stepCtx -> {
                String orderId = event.get("orderId");
                if (orderId == null || orderId.isEmpty()) {
                    throw new IllegalArgumentException("orderId is required");
                }
                return Map.of("orderId", orderId, "status", "processed");
            });
            return Map.of("result", result);
        } catch (StepFailedException e) {
            context.getLogger().error("Step failed: " + e.getMessage());
            return Map.of("error", e.getErrorObject().errorMessage());
        }
    }
}
