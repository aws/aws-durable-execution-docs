import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;

public class MultiArgumentStepExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        String arg1 = "value";
        int arg2 = 42;

        String result = context.step("my_step", String.class,
            (StepContext ctx) -> arg1 + ": " + arg2);

        return result;
    }
}
