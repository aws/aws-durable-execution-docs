// Java equivalent of Pydantic models: POJOs or records with Jackson
public record Order(String orderId, double amount, String customer) {}

public class OrderHandler extends DurableHandler<Map, Map> {

    @Override
    protected Map handleRequest(Map event, DurableContext ctx) {
        var order = new Order("ORD-123", 99.99, "Jane Doe");

        // Jackson serializes records automatically (no model_dump() needed)
        var result = ctx.step("process-order", Map.class, stepCtx -> processOrder(order));
        return result;
    }
}
