import java.util.LinkedHashMap;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.TypeToken;
import software.amazon.lambda.durable.dag.DagResult;

public class RunIf extends DurableHandler<Void, Map<String, String>> {
    @Override
    public Map<String, String> handleRequest(Void input, DurableContext context) {
        DagResult result = context.dag("conditional-refund", dag -> {
            var order = dag.step(
                    "load-order", new TypeToken<Map<String, Integer>>() {}, (deps, ctx) -> Map.of("total", 0));
            var refund = dag.step(
                            "issue-refund",
                            String.class,
                            (deps, ctx) -> "refunded " + deps.get(order).get("total"))
                    .reads(order)
                    .runIf(deps -> deps.get(order).get("total") > 0);
            dag.step("notify", String.class, (deps, ctx) -> "sent: " + deps.get(refund))
                    .reads(refund);
        });

        Map<String, String> statuses = new LinkedHashMap<>();
        statuses.put("refund", result.getStatus("issue-refund").map(Enum::name).orElse(null));
        statuses.put("notify", result.getStatus("notify").map(Enum::name).orElse(null));
        return statuses;
    }
}
