@Test
void testInvokeErrorHandling() {
    var runner = LocalDurableTestRunner.create(Map.class, new RetryHandler());

    var result = runner.runUntilComplete(Map.of());

    // Function should handle the error gracefully
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    var output = result.getResult(Map.class);
    assertEquals("failed", output.get("status"));
    assertNotNull(output.get("error"));
}
