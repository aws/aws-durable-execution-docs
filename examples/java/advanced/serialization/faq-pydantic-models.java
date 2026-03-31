// Java equivalent of Pydantic model_dump(): Jackson serializes POJOs/records directly
var order = new Order("ORD-123", 99.99, "Jane Doe");
var result = ctx.step("process-order", Map.class, stepCtx -> processOrder(order));
