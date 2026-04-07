import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.exception.InvokeFailedException;
import software.amazon.lambda.durable.exception.InvokeTimedOutException;

public class HandleInvocationError extends DurableHandler<OrderEvent, OrderResult> {

    @Override
    public OrderResult handle(OrderEvent event, DurableContext context) {
        try {
            PaymentResult payment = context.invoke(
                "process-payment",
                "payment-processor-function:live",
                event,
                PaymentResult.class
            );
            return OrderResult.success(payment.getTransactionId());
        } catch (InvokeTimedOutException e) {
            return OrderResult.failed("payment timed out");
        } catch (InvokeFailedException e) {
            return OrderResult.failed(e.getMessage());
        }
    }
}
