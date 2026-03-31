@Test
void testLoggerExample() {
    var runner = LocalDurableTestRunner.create(Map.class, new BasicUsageHandler());

    var result = runner.runUntilComplete(Map.of("id", "test-123"));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertEquals("processed", result.getResult(String.class));
}
