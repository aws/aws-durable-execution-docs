import java.time.Duration;
import java.util.List;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.dag.DagResult;

public class TaskKinds extends DurableHandler<Void, Integer> {
    @Override
    public Integer handleRequest(Void input, DurableContext context) {
        DagResult result = context.dag("mixed-kinds", dag -> {
            dag.step("compute", Integer.class, (deps, ctx) -> 1);
            dag.map("square-each", List.of(1, 2, 3), Integer.class, (item, index, ctx) -> item * item);
            dag.wait("cooldown", Duration.ofSeconds(1));
            dag.runInChildContext(
                    "group", String.class, (deps, childCtx) -> childCtx.step("inner", String.class, s -> "done"));
        });

        return result.successCount();
    }
}
