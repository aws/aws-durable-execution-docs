// Good - structured and queryable via MDC
MDC.put("order_id", orderId);
MDC.put("amount", "100");
ctx.getLogger().info("Order processed");

// Avoid - harder to query
ctx.getLogger().info("Order " + orderId + " processed with amount 100");
