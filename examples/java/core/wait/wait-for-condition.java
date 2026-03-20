import java.time.Duration;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.WaitForConditionConfig;
import software.amazon.lambda.durable.WaitStrategies;
import software.amazon.lambda.durable.retry.JitterStrategy;

public class WaitForConditionExample extends DurableHandler<Object, Map<String, Object>> {
    @Override
    public Map<String, Object> handleRequest(Object input, DurableContext context) {
        var strategy = WaitStrategies.<Map<String, Object>>builder(
                state -> !Boolean.TRUE.equals(state.get("done")))
            .initialDelay(Duration.ofSeconds(5))
            .backoffRate(2.0)
            .maxDelay(Duration.ofMinutes(5))
            .jitter(JitterStrategy.NONE)
            .build();

        var initialState = Map.<String, Object>of(
            "jobId", "job-123", "status", "pending", "done", false);

        var config = WaitForConditionConfig.<Map<String, Object>>builder(strategy, initialState)
            .build();

        return context.waitForCondition("wait_for_job", new TypeToken<>() {},
            (state, stepCtx) -> {
                var status = getJobStatus((String) state.get("jobId"));
                return Map.of(
                    "jobId", state.get("jobId"),
                    "status", status,
                    "done", "COMPLETED".equals(status));
            }, config);
    }
}
