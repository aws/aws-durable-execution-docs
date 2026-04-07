import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;

public class ProcessOrder extends DurableHandler<OrderEvent, OrderResult> {

    @Override
    public OrderResult handle(OrderEvent event, DurableContext context) {
        ValidationResult validation = context.invoke(
            "validate-order",
            "validate-order-function:live",
            event,
            ValidationResult.class
        );

        if (!validation.isValid()) {
            return OrderResult.rejected(validation.getReason());
        }

        PaymentResult payment = context.invoke(
            "process-payment",
            "payment-processor-function:live",
            event,
            PaymentResult.class
        );

        return OrderResult.completed(payment.getTransactionId());
    }
}
