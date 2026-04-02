import java.time.Duration;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class ExecutionModelExample extends DurableHandler<java.util.Map<String, String>, String> {

    @Override
    public String handleRequest(java.util.Map<String, String> event, DurableContext context) {
        // Step 1: Fetch data — result is checkpointed
        String data = context.step("fetch-data", String.class,
                stepCtx -> fetchData(event.get("id")));

        // Step 2: Wait 30 seconds without consuming compute resources
        context.wait("wait-30s", Duration.ofSeconds(30));

        // Step 3: Process the data — only runs after the wait completes
        String result = context.step("process-data", String.class,
                stepCtx -> processData(data));

        return result;
    }

    private String fetchData(String id) {
        return "data-for-" + id;
    }

    private String processData(String data) {
        return "processed-" + data;
    }
}
