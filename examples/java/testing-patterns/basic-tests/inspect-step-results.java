import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class InspectStepResultsErrorTest {
    @Test
    void testStepFailsWithError() {
        var runner = LocalDurableTestRunner.create(Object.class, new FailingStepExample());

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertTrue(result.getError().isPresent());
    }
}
