// POJO with Jackson annotations (serializes automatically)
public class Order {
    private String orderId;
    private double amount;
    private String customer;

    public Order() {}

    public Order(String orderId, double amount, String customer) {
        this.orderId = orderId;
        this.amount = amount;
        this.customer = customer;
    }

    // Getters and setters
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public String getCustomer() { return customer; }
}

// Usage in handler
var order = new Order("ORD-123", 99.99, "Jane Doe");
var result = ctx.step("process-order", Map.class, stepCtx -> processOrder(order));
