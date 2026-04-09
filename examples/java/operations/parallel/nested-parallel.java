import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.model.ParallelResult;

public class NestedParallel extends DurableHandler<Void, ParallelResult> {
    @Override
    public ParallelResult handleRequest(Void input, DurableContext context) {
        try (var outer = context.parallel("outer")) {
            outer.branch("group-a", ParallelResult.class, ctx -> {
                try (var inner = ctx.parallel("inner-a")) {
                    inner.branch("a1", String.class, c -> c.step("a1", String.class, s -> "a1"));
                    inner.branch("a2", String.class, c -> c.step("a2", String.class, s -> "a2"));
                    return inner.get();
                }
            });
            outer.branch("group-b", ParallelResult.class, ctx -> {
                try (var inner = ctx.parallel("inner-b")) {
                    inner.branch("b1", String.class, c -> c.step("b1", String.class, s -> "b1"));
                    inner.branch("b2", String.class, c -> c.step("b2", String.class, s -> "b2"));
                    return inner.get();
                }
            });
            return outer.get();
        }
    }
}
