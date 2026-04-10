import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.retry.RetryStrategies;

public class RetryPresetsExample extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext context) {
        // Default: 6 attempts, 5s initial delay, 60s max, 2x backoff, full jitter
        String result = context.step("call-api", String.class,
                stepCtx -> callApi(),
                StepConfig.builder().retryStrategy(RetryStrategies.Presets.DEFAULT).build());

        // No retry: fail immediately on first error
        String critical = context.step("charge-payment", String.class,
                stepCtx -> chargePayment(),
                StepConfig.builder().retryStrategy(RetryStrategies.Presets.NO_RETRY).build());

        return Map.of("result", result, "critical", critical);
    }

    private String callApi() { return "ok"; }
    private String chargePayment() { return "charged"; }
}
