import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationStatus;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class InspectStepResultsTest {
    @Test
    void testInspectStepResult() {
        var runner = LocalDurableTestRunner.create(Object.class, new AddNumbersExample());

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

        var stepOp = runner.getOperation("add_numbers");
        assertNotNull(stepOp);
        assertEquals(OperationStatus.SUCCEEDED, stepOp.getStatus());
        assertEquals(8, stepOp.getStepResult(Integer.class));
    }
}
