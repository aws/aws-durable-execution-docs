@Test
void testParallel() {
    var runner = LocalDurableTestRunner.create(List.class, new BasicParallelHandler());

    var result = runner.runUntilComplete(Map.of("data", "test"));

    // Check overall status
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Check the result contains expected values
    var list = result.getResult(List.class);
    assertEquals(3, list.size());
    assertTrue(list.contains("Task 1 complete"));
}
