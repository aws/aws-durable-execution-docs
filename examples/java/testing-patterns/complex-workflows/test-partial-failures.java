@Test
void testPartialFailure() {
    var runner = LocalDurableTestRunner.create(Map.class, new PartialFailureHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.FAILED, result.getStatus());

    // First two steps succeeded
    var step1 = result.getOperation("step1");
    assertEquals("Step 1 complete", step1.getStepResult(String.class));

    var step2 = result.getOperation("step2");
    assertEquals("Step 2 complete", step2.getStepResult(String.class));

    assertTrue(result.getError().getMessage().contains("Step 3 failed"));
}
