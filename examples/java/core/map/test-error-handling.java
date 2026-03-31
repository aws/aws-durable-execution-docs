@Test
void testMapErrorHandling() {
    var runner = LocalDurableTestRunner.create(Map.class, new MapWithErrorsHandler());

    var result = runner.runUntilComplete(Map.of());

    // Function should handle errors based on completion config
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    var resultMap = result.getResult(Map.class);

    // Check that some items succeeded and some failed
    assertTrue((int) resultMap.get("succeeded") > 0);
    assertTrue((int) resultMap.get("failed") > 0);
}
