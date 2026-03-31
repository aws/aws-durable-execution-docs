@Test
void testFirstSuccessful() {
    var runner = LocalDurableTestRunner.create(String.class, new ParallelConfigHandler());

    var result = runner.runUntilComplete(Map.of());

    // Should succeed with at least one result
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertTrue(result.getResult(String.class).contains("First successful result:"));
}
