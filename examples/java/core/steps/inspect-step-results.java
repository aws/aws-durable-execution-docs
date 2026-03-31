@Test
void testStepResult() {
    var runner = LocalDurableTestRunner.create(Map.class, new MyHandler());

    runner.run(Map.of("data", "test"));

    // Get step by name
    var stepOp = runner.getOperation("add-numbers");
    assertEquals(8, stepOp.getStepResult(Integer.class));

    // Check step status
    assertEquals(ExecutionStatus.SUCCEEDED, stepOp.getStatus());
}
