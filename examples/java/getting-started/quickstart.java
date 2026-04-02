import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class QuickstartFunction extends DurableHandler<Map<String, String>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, String> event, DurableContext context) {
        String message = context.step("step-1", String.class, stepCtx -> {
            stepCtx.getLogger().info("Hello from step-1");
            return "Hello from Durable Lambda!";
        });

        // Pause for 10 seconds without consuming CPU or incurring usage charges
        context.wait("wait-10s", Duration.ofSeconds(10));

        // Replay-aware: logs once even though the function replays after the wait
        context.getLogger().info("Resumed after wait");

        return Map.of("statusCode", 200, "body", message);
    }
}
