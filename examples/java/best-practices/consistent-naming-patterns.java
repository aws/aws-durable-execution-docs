// Pattern: verb_noun for operations
ctx.step("validate_order", Map.class, stepCtx -> validateOrder(orderId));
ctx.step("process_payment", Map.class, stepCtx -> processPayment(amount));

// Pattern: noun_action for callbacks
ctx.createCallback("payment_callback", Map.class);
ctx.createCallback("approval_callback", Map.class);

// Pattern: descriptive_wait for waits
ctx.wait("payment_confirmation_wait", Duration.ofSeconds(30));
