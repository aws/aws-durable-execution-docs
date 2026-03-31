@Test
void testInvokeOperations() {
    var runner = LocalDurableTestRunner.create(Map.class, new ProcessOrderHandler());

    var result = runner.runUntilComplete(
        Map.of("order_id", "order-123", "amount", 100.0)
    );

    // Get all operations
    var operations = result.getOperations();

    // Find invoke operations
    var invokeOps = operations.stream()
        .filter(op -> "CHAINED_INVOKE".equals(op.getOperationType()))
        .toList();

    // Verify invoke operations were created
    assertEquals(2, invokeOps.size());

    // Check specific invoke operation
    var validateOp = result.getOperation("validate_order");
    assertEquals(ExecutionStatus.SUCCEEDED, validateOp.getStatus());
}
