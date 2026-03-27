import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.TypeToken;
import software.amazon.lambda.durable.config.WaitForConditionConfig;
import software.amazon.lambda.durable.model.WaitForConditionResult;
import software.amazon.lambda.durable.retry.WaitForConditionWaitStrategy;

public class PollingWithConditionExample extends DurableHandler<Object, Map<String, String>> {

    @Override
    public Map<String, String> handleRequest(Object input, DurableContext context) {
        WaitForConditionWaitStrategy<Map<String, String>> strategy =
                (state, attempt) -> Duration.ofMinutes(1);

        var config = WaitForConditionConfig.<Map<String, String>>builder()
                .initialState(Map.of("jobId", "job-123", "status", "pending"))
                .waitStrategy(strategy)
                .build();

        return context.waitForCondition(
                "poll_job",
                new TypeToken<>() {},
                (state, stepCtx) -> {
                    var status = getJobStatus(state.get("jobId"));
                    var updatedState = Map.of("jobId", state.get("jobId"), "status", status);
                    if ("completed".equals(status)) {
                        return WaitForConditionResult.stopPolling(updatedState);
                    }
                    return WaitForConditionResult.continuePolling(updatedState);
                },
                config);
    }
}
