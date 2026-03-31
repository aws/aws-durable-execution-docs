// Java equivalent: test that MDC context enrichment works with structured logging

@Test
void testStructuredLogging() {
    // Configure Log4j2/Logback with JSON layout and a test appender
    var runner = LocalDurableTestRunner.create(Map.class, new BasicUsageHandler());

    var result = runner.runUntilComplete(Map.of("id", "test-123"));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Parse captured JSON log entries and verify SDK enrichment fields:
    // - "durableExecutionArn" is present
    // - "requestId" is present
    // - "operationId" / "operationName" appear in step logs
}
