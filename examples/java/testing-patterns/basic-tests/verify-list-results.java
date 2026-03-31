@Test
void testParallelResults() {
    var runner = LocalDurableTestRunner.create(Map.class, new ParallelHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    var results = result.getResult(List.class);
    assertEquals(3, results.size());
    assertEquals(List.of("Task 1 complete", "Task 2 complete", "Task 3 complete"), results);
}
