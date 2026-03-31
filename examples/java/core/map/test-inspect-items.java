@Test
void testMapIndividualItems() {
    var runner = LocalDurableTestRunner.create(Map.class, new SquareHandler());

    var result = runner.runUntilComplete(Map.of());

    // Get the map operation
    var mapOp = result.getOperation("square");
    assertNotNull(mapOp);

    // Verify all items were processed
    var resultMap = result.getResult(Map.class);
    assertEquals(5, resultMap.get("total"));

    // Check specific results
    var results = (List<Integer>) resultMap.get("results");
    assertEquals(1, results.get(0));
    assertEquals(9, results.get(2));
}
