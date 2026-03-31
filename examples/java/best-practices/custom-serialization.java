// Use a POJO with Jackson-compatible annotations
public class Order {
    private String orderId;
    private double total;
    private List<String> items;
    // getters, setters, no-arg constructor
}

var order = ctx.step("process_order", Order.class, stepCtx -> {
    var o = new Order();
    o.setOrderId(orderData.get("order_id"));
    o.setTotal((double) orderData.get("total"));
    o.setItems((List<String>) orderData.get("items"));
    // Process order...
    return o;
});
