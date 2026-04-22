import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.config.WaitForConditionConfig;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.model.WaitForConditionResult;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class PollingTest {

    record PollState(int attempts, boolean done) {}

    @Test
    void pollsUntilConditionIsMet() {
        var runner = LocalDurableTestRunner.create(
            Void.class,
            (input, context) -> context.waitForCondition(
                "poll-job",
                PollState.class,
                (state, ctx) -> {
                    var next = new PollState(state.attempts() + 1, state.attempts() >= 2);
                    return next.done()
                        ? WaitForConditionResult.stopPolling(next)
                        : WaitForConditionResult.continuePolling(next);
                },
                WaitForConditionConfig.<PollState>builder()
                    .initialState(new PollState(0, false))
                    .build()
            )
        );

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertTrue(result.getResult(PollState.class).done());
    }
}
