@Test
void testOrderProcessing() {
    var runner = LocalDurableTestRunner.create(Map.class, new OrderProcessorHandler());

    var result = runner.runUntilComplete(Map.of("order_id", "order-123", "amount", 100.0));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    var orderResult = result.getResult(Map.class);
    assertEquals("order-123", orderResult.get("order_id"));
    assertEquals("completed", orderResult.get("status"));
    assertEquals(100.0, orderResult.get("amount"));
}
