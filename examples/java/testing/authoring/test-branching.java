import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class TestBranchingTest {

    record Input(boolean premium) {}

    private LocalDurableTestRunner<Input, String> createRunner() {
        return LocalDurableTestRunner.create(
            Input.class,
            (input, context) -> {
                if (input.premium()) {
                    return context.step("premium-path", String.class, ctx -> "premium");
                }
                return context.step("standard-path", String.class, ctx -> "standard");
            }
        );
    }

    @Test
    void takesPremiumPath() {
        var result = createRunner().runUntilComplete(new Input(true));

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertEquals("premium", result.getResult(String.class));
    }

    @Test
    void takesStandardPath() {
        var result = createRunner().runUntilComplete(new Input(false));

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertEquals("standard", result.getResult(String.class));
    }
}
