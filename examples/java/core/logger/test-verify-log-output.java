@Test
void testLoggingOutput() {
    // Use a test appender to capture log output
    var runner = LocalDurableTestRunner.create(Map.class, new BasicUsageHandler());

    var result = runner.runUntilComplete(Map.of("id", "test-123"));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Verify log messages via test appender or log capture framework
    // e.g., with Log4j2 ListAppender or Logback's ListAppender:
    // assertTrue(logEvents.stream().anyMatch(e -> e.getMessage().contains("Starting workflow")));
    // assertTrue(logEvents.stream().anyMatch(e -> e.getMessage().contains("Workflow completed")));
}
