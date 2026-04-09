import java.util.List;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.model.ParallelResult;

public class SimpleParallel extends DurableHandler<Void, ParallelResult> {
    @Override
    public ParallelResult handleRequest(Void input, DurableContext context) {
        try (var parallel = context.parallel("check-services")) {
            parallel.branch("check-inventory", String.class,
                    ctx -> ctx.step("inventory", String.class, s -> "inventory ok"));
            parallel.branch("check-payment", String.class,
                    ctx -> ctx.step("payment", String.class, s -> "payment ok"));
            parallel.branch("check-shipping", String.class,
                    ctx -> ctx.step("shipping", String.class, s -> "shipping ok"));
            return parallel.get();
        }
    }
}
