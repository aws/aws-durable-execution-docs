import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;

public class LambdaStepNoNameExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        // Java requires a name — use a descriptive string
        String result = context.step("my_step", String.class,
            (StepContext ctx) -> "some value");
        return result;
    }
}
