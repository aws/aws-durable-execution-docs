@Test
void testApprovalWorkflow() {
    var runner = LocalDurableTestRunner.create(Map.class, new HumanApprovalWorkflowHandler());

    var result = runner.run(Map.of("order_id", "order-123", "amount", 1000));

    // Verify workflow completed
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Check callback was created
    var callbackOp = runner.getOperation("order_approval");
    assertNotNull(callbackOp);
    assertEquals("order_approval", callbackOp.getName());
}
