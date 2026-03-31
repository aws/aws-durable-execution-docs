@Test
void testMapOperations() {
    var runner = LocalDurableTestRunner.create(Map.class, new SquareHandler());

    var result = runner.runUntilComplete(Map.of());

    // Check overall status
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Check the MapResult
    var resultMap = result.getResult(Map.class);
    assertEquals(5, resultMap.get("total"));
    assertTrue((boolean) resultMap.get("allSucceeded"));

    // Check individual results
    var results = (List<Integer>) resultMap.get("results");
    assertEquals(1, results.get(0));
    assertEquals(4, results.get(1));
    assertEquals(9, results.get(2));
}
