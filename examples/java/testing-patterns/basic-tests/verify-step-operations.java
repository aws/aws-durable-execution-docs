@Test
void testStepExecution() {
    var runner = LocalDurableTestRunner.create(Map.class, new StepHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Get step by name
    var stepOp = result.getOperation("add_numbers");
    assertNotNull(stepOp);
    assertEquals(8, stepOp.getStepResult(Integer.class));
}
