@Test
void testParallelOperations() {
    var runner = LocalDurableTestRunner.create(Map.class, new ParallelHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Verify all step operations
    var succeeded = result.getSucceededOperations();
    assertEquals(3, succeeded.size());

    // Verify step names
    var stepNames = succeeded.stream()
        .map(TestOperation::getName)
        .collect(Collectors.toSet());
    assertEquals(Set.of("task1", "task2", "task3"), stepNames);
}
