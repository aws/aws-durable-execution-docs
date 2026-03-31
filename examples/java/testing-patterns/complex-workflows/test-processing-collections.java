@Test
void testCollectionProcessing() {
    var runner = LocalDurableTestRunner.create(Map.class, new CollectionHandler());

    var result = runner.runUntilComplete(
        Map.of("numbers", List.of(1, 2, 3, 4, 5)));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertEquals(List.of(2, 4, 6, 8, 10), result.getResult(List.class));

    // Verify all steps ran
    var succeeded = result.getSucceededOperations();
    assertEquals(5, succeeded.size());
}
