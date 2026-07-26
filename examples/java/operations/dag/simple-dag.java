import java.util.List;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.TypeToken;
import software.amazon.lambda.durable.dag.DagResult;

public class SimpleDag extends DurableHandler<Void, Map<String, Integer>> {
    @Override
    public Map<String, Integer> handleRequest(Void input, DurableContext context) {
        DagResult result = context.dag("load-and-join", dag -> {
            var users = dag.step(
                    "fetch-users",
                    new TypeToken<List<Map<String, String>>>() {},
                    (deps, ctx) -> List.of(Map.of("id", "u1"), Map.of("id", "u2")));
            var orders = dag.step(
                    "fetch-orders",
                    new TypeToken<List<Map<String, String>>>() {},
                    (deps, ctx) -> List.of(Map.of("id", "o1")));
            dag.step(
                            "join",
                            new TypeToken<Map<String, Integer>>() {},
                            (deps, ctx) -> Map.of(
                                    "users", deps.get(users).size(),
                                    "orders", deps.get(orders).size()))
                    .reads(users, orders);
        });

        @SuppressWarnings("unchecked")
        Map<String, Integer> join =
                (Map<String, Integer>) result.getResult("join").orElseThrow();
        return join;
    }
}
