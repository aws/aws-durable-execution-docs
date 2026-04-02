import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class BasicStepTestingTest {
    @Test
    void testStep() {
        var runner = LocalDurableTestRunner.create(Object.class, new AddNumbersExample());

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertEquals(8, result.getResult(Integer.class));
    }
}
