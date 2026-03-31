@Test
void testMapWithConfig() {
    var runner = LocalDurableTestRunner.create(Map.class, new ProcessItemHandler());

    var result = runner.runUntilComplete(Map.of());

    // Verify the map operation completed
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Get the map operation
    var mapOp = result.getOperation("process_numbers");
    assertNotNull(mapOp);

    // Verify configuration was applied
    var resultMap = result.getResult(Map.class);
    assertTrue((int) resultMap.get("total") > 0);
}
