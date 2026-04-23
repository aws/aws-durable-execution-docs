context.step("charge-payment", Receipt.class,
    ctx -> chargePayment(order));
context.step("send-confirmation", Void.class,
    ctx -> { sendConfirmationEmail(order); return null; });
context.step("update-inventory", Void.class,
    ctx -> { updateInventory(order); return null; });
