import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationType;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class SequentialWorkflowTest {

    record Order(String orderId) {}

    @Test
    void executesAllStepsInOrder() {
        var runner = LocalDurableTestRunner.create(
            Order.class,
            (input, context) -> {
                var validated = context.step("validate", Map.class,
                    ctx -> Map.of("orderId", input.orderId(), "status", "validated"));
                var paid = context.step("payment", Map.class,
                    ctx -> Map.of("orderId", input.orderId(), "payment", "completed"));
                return context.step("fulfillment", Map.class,
                    ctx -> Map.of("orderId", input.orderId(), "fulfillment", "shipped"));
            }
        );

        var result = runner.runUntilComplete(new Order("order-123"));

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

        var stepOps = result.getOperations().stream()
            .filter(op -> op.getType() == OperationType.STEP)
            .toList();
        assertEquals(3, stepOps.size());
        assertEquals("validate", stepOps.get(0).getName());
        assertEquals("payment", stepOps.get(1).getName());
        assertEquals("fulfillment", stepOps.get(2).getName());
    }
}
