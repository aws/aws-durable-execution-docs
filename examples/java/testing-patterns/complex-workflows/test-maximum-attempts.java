@Test
void testMaxAttempts() {
    var runner = LocalDurableTestRunner.create(Map.class, new MaxAttemptsHandler());

    var result = runner.runUntilComplete(Map.of("target", 10));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    var finalResult = result.getResult(Map.class);
    assertEquals(5, finalResult.get("attempts"));
    assertEquals(5, finalResult.get("state"));
    assertEquals(false, finalResult.get("reached_target"));
}
