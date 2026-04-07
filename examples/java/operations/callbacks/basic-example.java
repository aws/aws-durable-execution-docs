import software.amazon.lambda.durable.DurableCallbackFuture;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class BasicCallbackExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        DurableCallbackFuture<String> callback =
                context.createCallback("wait-for-approval", String.class);

        // Send callback.callbackId() to the external system that will resume this function.
        sendApprovalRequest(callback.callbackId());

        // Execution suspends here until the external system calls back.
        return callback.get();
    }
}
