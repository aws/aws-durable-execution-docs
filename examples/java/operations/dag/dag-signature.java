import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.dag.DagResult;

public class DagSignature extends DurableHandler<Void, Integer> {
    @Override
    public Integer handleRequest(Void input, DurableContext context) {
        DagResult result = context.dag("compute", dag -> {
            dag.step("double", Integer.class, (deps, ctx) -> 42);
        });

        return (Integer) result.getResult("double").orElseThrow();
    }
}
