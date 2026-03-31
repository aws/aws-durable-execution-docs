@Test
void testStepRetry() {
    var runner = LocalDurableTestRunner.create(Map.class, new RetryHandler());

    var result = runner.run(Map.of());

    // Function should eventually succeed after retries
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Inspect the step that retried
    var stepOp = runner.getOperation("unreliable-operation");
    assertEquals(ExecutionStatus.SUCCEEDED, stepOp.getStatus());
}
