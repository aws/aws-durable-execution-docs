@Test
void testParallelOperations() {
    var runner = LocalDurableTestRunner.create(Map.class, new ParallelHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    var results = result.getResult(List.class);
    assertEquals(3, results.size());

    // Verify all steps ran
    var succeeded = result.getSucceededOperations();
    assertEquals(3, succeeded.size());

    var stepNames = succeeded.stream()
        .map(TestOperation::getName)
        .collect(Collectors.toSet());
    assertEquals(Set.of("task1", "task2", "task3"), stepNames);
}
