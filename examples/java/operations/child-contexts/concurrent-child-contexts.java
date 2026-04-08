import java.util.List;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableFuture;
import software.amazon.lambda.durable.DurableHandler;

public class ConcurrentChildContexts extends DurableHandler<String, String> {

    // WRONG - a and b share the parent counter, so their IDs depend on which future
    // completes first. On replay, if the order differs, a gets b's checkpointed result
    // and b gets a's.
    public String wrongHandleRequest(String input, DurableContext context) {
        DurableFuture<String> a = context.stepAsync("fetch-a", String.class, ctx -> fetchA());
        DurableFuture<String> b = context.stepAsync("fetch-b", String.class, ctx -> fetchB());
        List<String> results = DurableFuture.allOf(a, b);
        return results.get(0) + " " + results.get(1);
    }

    // CORRECT - each branch has its own isolated counter, so IDs are stable regardless
    // of completion order
    @Override
    public String handleRequest(String input, DurableContext context) {
        DurableFuture<String> a = context.runInChildContextAsync(
            "branch-a", String.class, child -> child.step("fetch-a", String.class, ctx -> fetchA())
        );
        DurableFuture<String> b = context.runInChildContextAsync(
            "branch-b", String.class, child -> child.step("fetch-b", String.class, ctx -> fetchB())
        );
        List<String> results = DurableFuture.allOf(a, b);
        return results.get(0) + " " + results.get(1);
    }

    private String fetchA() { return "result-a"; }
    private String fetchB() { return "result-b"; }
}
