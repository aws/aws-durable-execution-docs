import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.CloudDurableTestRunner;

class CloudRunnerTimeoutTest {

    @Test
    void runsWithCustomTimeout() {
        var runner = CloudDurableTestRunner
            .create(
                "arn:aws:lambda:us-east-1:123456789012:function:MyFunction:$LATEST",
                String.class,
                String.class
            )
            .withTimeout(Duration.ofSeconds(60));

        var result = runner.runUntilComplete("{\"name\": \"world\"}");

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    }
}
