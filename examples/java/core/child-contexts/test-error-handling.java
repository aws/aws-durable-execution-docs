@Test
void testChildContextErrorHandling() {
    var runner = LocalDurableTestRunner.create(Map.class, new RiskyOperationHandler());

    var result = runner.runUntilComplete(Map.of("data", "invalid"));

    // Function should handle error gracefully
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    var resultMap = result.getResult(Map.class);
    assertEquals("fallback", resultMap.get("status"));
    assertNotNull(resultMap.get("error"));
}
