import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class WaitForExternalResponseExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        // Wait for external approval
        var result = context.waitForCallback(
            "approval_wait", String.class,
            (callbackId, stepCtx) -> {
                // Send callback_id to external approval system
                sendToApprovalSystem(callbackId);
            });
        return result;
    }
}
