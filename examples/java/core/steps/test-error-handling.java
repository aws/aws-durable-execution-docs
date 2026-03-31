@Test
void testStepError() {
    var runner = LocalDurableTestRunner.create(Map.class, new ErrorHandler());

    var result = runner.run(Map.of());

    // Function should fail
    assertEquals(ExecutionStatus.FAILED, result.getStatus());

    // Check the error
    assertTrue(result.getError().getMessage().contains("RuntimeException"));
}
