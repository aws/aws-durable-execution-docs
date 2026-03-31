// Before - fails if Order lacks Jackson annotations or getters
var result = ctx.step("process-order", Order.class, stepCtx -> processOrder(orderObject));

// After - works with a POJO that has getters or Jackson annotations
var result = ctx.step("process-order", Map.class, stepCtx -> processOrder(orderObject.toMap()));
