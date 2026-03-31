@Test
void testSdkValidationError() {
    var handler = new InvalidConfigHandler();
    var runner = LocalDurableTestRunner.create(Map.class, handler);

    var result = runner.runUntilComplete(Map.of());

    // SDK should catch invalid configuration
    assertEquals(ExecutionStatus.FAILED, result.getStatus());
    assertTrue(result.getError().getMessage().contains("IllegalArgumentException"));
}
