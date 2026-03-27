import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.TypeToken;
import software.amazon.lambda.durable.config.WaitForConditionConfig;
import software.amazon.lambda.durable.model.WaitForConditionResult;
import software.amazon.lambda.durable.retry.JitterStrategy;
import software.amazon.lambda.durable.retry.WaitStrategies;

public class WaitForConditionExample extends DurableHandler<Object, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Object input, DurableContext context) {
        var strategy = WaitStrategies.<Map<String, Object>>exponentialBackoff(
                60, Duration.ofSeconds(5), Duration.ofMinutes(5), 2.0, JitterStrategy.NONE);

        var initialState = Map.<String, Object>of(
                "jobId", "job-123", "status", "pending", "done", false);

        var config = WaitForConditionConfig.<Map<String, Object>>builder()
                .initialState(initialState)
                .waitStrategy(strategy)
                .build();

        return context.waitForCondition(
                "wait_for_job",
                new TypeToken<>() {},
                (state, stepCtx) -> {
                    var status = getJobStatus((String) state.get("jobId"));
                    var updatedState = Map.<String, Object>of(
                            "jobId", state.get("jobId"),
                            "status", status,
                            "done", "COMPLETED".equals(status));
                    if (Boolean.TRUE.equals(updatedState.get("done"))) {
                        return WaitForConditionResult.stopPolling(updatedState);
                    }
                    return WaitForConditionResult.continuePolling(updatedState);
                },
                config);
    }
}
