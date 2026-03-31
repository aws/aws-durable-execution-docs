// Java record (Java 16+) — serializes automatically with Jackson
public record Order(String orderId, double amount, String customer) {}

// Usage in handler
var order = new Order("ORD-123", 99.99, "Jane Doe");
var result = ctx.step("process-order", Map.class, stepCtx -> processOrder(order));
