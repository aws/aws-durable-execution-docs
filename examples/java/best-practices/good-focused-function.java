// Process a single order through validation, payment, and fulfillment
var orderId = (String) input.get("order_id");

var validation = ctx.step("validate_order", Map.class, stepCtx -> validateOrder(orderId));
var payment = ctx.step("process_payment", Map.class, stepCtx -> processPayment(orderId, input.get("amount")));
var fulfillment = ctx.step("fulfill_order", Map.class, stepCtx -> fulfillOrder(orderId));

return Map.of("order_id", orderId, "status", "completed");
