import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.dag.DagConfig;
import software.amazon.lambda.durable.dag.DagResult;

public class NestedDag extends DurableHandler<Void, Integer> {
    @Override
    public Integer handleRequest(Void input, DurableContext context) {
        DagResult result = context.dag(
                "outer",
                dag -> {
                    dag.step("prepare", String.class, (deps, ctx) -> "ready");
                    dag.dag(
                            "inner",
                            nested -> {
                                nested.step("a", Integer.class, (deps, ctx) -> 1);
                                nested.step("b", Integer.class, (deps, ctx) -> 2);
                            },
                            DagConfig.builder().maxConcurrency(5).build());
                },
                DagConfig.builder().maxConcurrency(10).build());

        return result.successCount();
    }
}
