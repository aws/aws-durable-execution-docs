@Test
void testOrderWorkflow() {
    var runner = LocalDurableTestRunner.create(Map.class, new SequentialHandler());

    var result = runner.runUntilComplete(Map.of("order_id", "order-123"));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Check final result
    var finalResult = result.getResult(Map.class);
    assertEquals("order-123", finalResult.get("order_id"));
    assertEquals("completed", finalResult.get("payment_status"));
    assertEquals("shipped", finalResult.get("fulfillment_status"));

    // Verify all three steps ran
    var succeeded = result.getSucceededOperations();
    assertEquals(3, succeeded.size());

    // Check step order
    var stepNames = succeeded.stream()
        .map(TestOperation::getName)
        .toList();
    assertEquals(List.of("validate", "payment", "fulfillment"), stepNames);
}
