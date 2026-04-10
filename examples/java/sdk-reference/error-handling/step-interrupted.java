import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.config.StepSemantics;
import software.amazon.lambda.durable.exception.StepInterruptedException;

public class StepInterrupted extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext context) {
        StepConfig config = StepConfig.builder()
                .semantics(StepSemantics.AT_MOST_ONCE_PER_RETRY)
                .build();
        try {
            Map<String, Object> result = context.step(
                    "charge-payment", Map.class,
                    stepCtx -> chargePayment((Double) event.get("amount")),
                    config);
            return Map.of("status", "charged", "result", result);
        } catch (StepInterruptedException e) {
            // The step started but Lambda was interrupted before the result was
            // checkpointed. The SDK will not re-run the step on the next invocation.
            // Inspect your payment system to determine whether the charge succeeded.
            context.getLogger().warn("Payment step interrupted — check payment system");
            return Map.of("status", "unknown");
        }
    }

    private Map<String, Object> chargePayment(double amount) {
        return Map.of("charged", amount);
    }
}
