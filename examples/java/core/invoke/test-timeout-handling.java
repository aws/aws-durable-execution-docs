@Test
void testInvokeTimeout() {
    var runner = LocalDurableTestRunner.create(Map.class, new TimeoutHandler());

    var result = runner.runUntilComplete(Map.of());

    // Check that timeout was handled
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    var output = result.getResult(Map.class);
    assertEquals("timeout", output.get("status"));
}
