import software.amazon.lambda.durable.DurableContext;

record Order(String id, double amount) {}
record Receipt(String orderId, double charged) {}

// MapFunction<Order, Receipt> implementation
Receipt processOrder(Order order, int index, DurableContext ctx) {
    var validated = ctx.step("validate", Order.class, s -> {
        if (order.amount() <= 0) throw new IllegalArgumentException("Invalid amount");
        return order;
    });
    var charged = ctx.step("charge", Double.class, s -> validated.amount());
    return new Receipt(validated.id(), charged);
}
