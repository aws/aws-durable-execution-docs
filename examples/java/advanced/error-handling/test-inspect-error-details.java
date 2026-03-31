@Test
void testErrorDetails() {
    var handler = new ErrorHandler();
    var runner = LocalDurableTestRunner.create(Map.class, handler);

    var result = runner.runUntilComplete(Map.of());

    // Check error details
    assertEquals(ExecutionStatus.FAILED, result.getStatus());
    assertNotNull(result.getError());
    assertTrue(result.getError().getMessage().contains("RuntimeException"));
}
