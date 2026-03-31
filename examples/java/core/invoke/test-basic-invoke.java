@Test
void testInvoke() {
    var runner = LocalDurableTestRunner.create(Map.class, new ProcessOrderHandler());

    var result = runner.runUntilComplete(
        Map.of("order_id", "order-123", "amount", 100.0)
    );

    // Check overall status
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Check final result
    var output = result.getResult(Map.class);
    assertEquals("completed", output.get("status"));
}
