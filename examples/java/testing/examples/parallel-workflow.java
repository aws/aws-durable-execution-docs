import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class ParallelWorkflowTest {

    @Test
    void executesBranchesInParallel() {
        var runner = LocalDurableTestRunner.create(
            Void.class,
            (input, context) -> {
                try (var parallel = context.parallel("fetch-all")) {
                    parallel.branch("fetch-a", String.class, ctx -> "data-a");
                    parallel.branch("fetch-b", String.class, ctx -> "data-b");
                    parallel.branch("fetch-c", String.class, ctx -> "data-c");
                }
                return null;
            }
        );

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    }
}
