context.runInChildContext("process-order", String.class, child -> {
    child.step("validate", ValidationResult.class,
        ctx -> validator.validate(order));
    Receipt receipt = child.step("charge", Receipt.class,
        ctx -> payments.charge(order.total(), order.cardToken()));
    child.step("schedule", String.class,
        ctx -> shipments.schedule(order.id(), order.address()));
    return "ok";
});
