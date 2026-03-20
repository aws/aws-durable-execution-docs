import java.time.Duration;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.WaitForConditionConfig;
import software.amazon.lambda.durable.WaitStrategies;
import software.amazon.lambda.durable.retry.JitterStrategy;

public class PollingWithConditionExample extends DurableHandler<Object, Map<String, String>> {
    @Override
    public Map<String, String> handleRequest(Object input, DurableContext context) {
        var strategy = WaitStrategies.<Map<String, String>>builder(
                state -> !"completed".equals(state.get("status")))
            .initialDelay(Duration.ofMinutes(1))
            .jitter(JitterStrategy.NONE)
            .build();

        var config = WaitForConditionConfig.<Map<String, String>>builder(
                strategy, Map.of("jobId", "job-123", "status", "pending"))
            .build();

        return context.waitForCondition("poll_job", new TypeToken<>() {},
            (state, stepCtx) -> {
                var status = getJobStatus(state.get("jobId"));
                return Map.of("jobId", state.get("jobId"), "status", status);
            }, config);
    }
}
