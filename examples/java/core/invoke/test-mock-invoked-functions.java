@Test
void testInvokeWithMock() {
    // The testing framework handles invocations internally.
    // You can test the orchestration logic without deploying all functions.
    var runner = LocalDurableTestRunner.create(Map.class, new ProcessOrderHandler());

    var result = runner.runUntilComplete(
        Map.of("order_id", "order-123", "amount", 100.0)
    );

    // Verify the orchestration logic
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
}
