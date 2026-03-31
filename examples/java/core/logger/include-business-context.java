MDC.put("order_id", orderId);
MDC.put("customer_id", customerId);
MDC.put("payment_method", "credit_card");

ctx.getLogger().info("Processing payment");
