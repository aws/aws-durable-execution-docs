import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class DurableContextExample extends DurableHandler<Object, String> {

    @Override
    public String handleRequest(Object input, DurableContext context) {
        // Your function receives DurableContext instead of Lambda context
        // Use context.step(), context.wait(), etc.
        return context.step("my-step", String.class, ctx -> "step completed");
    }
}
