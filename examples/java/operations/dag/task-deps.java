import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.dag.DagResult;

public class TaskDeps extends DurableHandler<Void, Integer> {
    @Override
    public Integer handleRequest(Void input, DurableContext context) {
        DagResult result = context.dag("diamond", dag -> {
            var seed = dag.step("seed", Integer.class, (deps, ctx) -> 10);
            var left = dag.step("left", Integer.class, (deps, ctx) -> deps.get(seed) + 1).reads(seed);
            var right = dag.step("right", Integer.class, (deps, ctx) -> deps.get(seed) * 2).reads(seed);
            dag.step("merge", Integer.class, (deps, ctx) -> deps.get(left) + deps.get(right)).reads(left, right);
        });

        return (Integer) result.getResult("merge").orElseThrow();
    }
}
