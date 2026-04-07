import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class WaitForCallbackExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        return context.waitForCallback(
                "wait-for-approval",
                String.class,
                (callbackId, stepCtx) -> sendApprovalRequest(callbackId));
    }
}
