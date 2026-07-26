import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.dag.DagResult;
import software.amazon.lambda.durable.dag.DagTaskError;
import software.amazon.lambda.durable.dag.TaskExecution;

public class ErrorHandling extends DurableHandler<Void, Map<String, Object>> {
    @Override
    public Map<String, Object> handleRequest(Void input, DurableContext context) {
        DagResult result = context.dag("charge-and-ship", dag -> {
            var charge = dag.step("charge-card", String.class, (deps, ctx) -> {
                throw new IllegalStateException("card declined");
            });
            dag.step("ship-order", String.class, (deps, ctx) -> "shipped").reads(charge);
            dag.step("record-metrics", String.class, (deps, ctx) -> "recorded");
        });

        List<Map<String, String>> failures = new ArrayList<>();
        for (TaskExecution<?> task : result.failed()) {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("name", task.name());
            entry.put("error", task.error().map(DagTaskError::errorMessage).orElse(null));
            failures.add(entry);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("failures", failures);
        summary.put("shipStatus", result.getStatus("ship-order").map(Enum::name).orElse(null));
        summary.put("metricsStatus", result.getStatus("record-metrics").map(Enum::name).orElse(null));
        return summary;
    }
}
