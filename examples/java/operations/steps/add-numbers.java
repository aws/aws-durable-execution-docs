import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;

public class AddNumbersExample extends DurableHandler<Object, Integer> {
    @Override
    public Integer handleRequest(Object input, DurableContext context) {
        int result = context.step("add_numbers", Integer.class,
            (StepContext ctx) -> 5 + 3);
        return result;
    }
}
