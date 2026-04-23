// Business logic lives on service classes, not the handler.
public class OrderWorkflow implements DurableHandler<Order, OrderResult> {
    private final OrderValidator validator;
    private final PaymentService payments;
    private final ShipmentService shipments;

    @Override
    public OrderResult handle(Order order, DurableContext context) {
        context.step("validate", ValidationResult.class,
            ctx -> validator.validate(order));

        Receipt receipt = context.step("charge", Receipt.class,
            ctx -> payments.charge(order.total(), order.cardToken()));

        String shipmentId = context.step("schedule", String.class,
            ctx -> shipments.schedule(order.id(), order.address()));

        return new OrderResult(receipt, shipmentId);
    }
}
