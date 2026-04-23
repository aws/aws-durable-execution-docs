import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.TypeToken;
import software.amazon.lambda.durable.config.WaitForConditionConfig;
import software.amazon.lambda.durable.model.WaitForConditionResult;
import software.amazon.lambda.durable.retry.JitterStrategy;
import software.amazon.lambda.durable.retry.WaitStrategies;

var strategy = WaitStrategies.<Map<String, Object>>exponentialBackoff(
    60, Duration.ofSeconds(5), Duration.ofMinutes(1), 2.0, JitterStrategy.FULL);

var config = WaitForConditionConfig.<Map<String, Object>>builder()
    .initialState(Map.of("jobId", input.jobId(), "status", "pending"))
    .waitStrategy(strategy)
    .build();

Map<String, Object> finalState = context.waitForCondition(
    "wait-for-job",
    new TypeToken<>() {},
    (state, stepCtx) -> {
        String status = jobService.getStatus((String) state.get("jobId"));
        var updated = Map.<String, Object>of("jobId", state.get("jobId"), "status", status);
        return "completed".equals(status)
            ? WaitForConditionResult.stopPolling(updated)
            : WaitForConditionResult.continuePolling(updated);
    },
    config);
