@Test
void testRetryExhausted() {
    var handler = new AlwaysFailsHandler();
    var runner = LocalDurableTestRunner.create(Map.class, handler);

    var result = runner.runUntilComplete(Map.of());

    // Should fail after all retries
    assertEquals(ExecutionStatus.FAILED, result.getStatus());
    assertTrue(result.getError().getMessage().contains("RuntimeException"));
}
