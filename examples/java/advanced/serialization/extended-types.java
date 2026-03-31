// Jackson handles common Java types with appropriate modules
var orderData = Map.of(
    "orderId", UUID.randomUUID().toString(),       // UUID as String
    "amount", new BigDecimal("99.99"),              // BigDecimal
    "createdAt", Instant.now().toString(),          // Instant
    "deliveryDate", LocalDate.now().toString(),     // LocalDate
    "signature", Base64.getEncoder().encodeToString(
        "binary_signature_data".getBytes()),        // byte[] as Base64
    "coordinates", List.of(40.7128, -74.0060)      // List<Double>
);

var result = ctx.step("process-order", Map.class, stepCtx -> processOrder(orderData));
