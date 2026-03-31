@Test
void testParallelBranches() {
    var runner = LocalDurableTestRunner.create(List.class, new BasicParallelHandler());

    var result = runner.runUntilComplete(Map.of());

    // Verify all step operations exist
    var stepOps = result.getOperations().stream()
        .filter(op -> op.getOperationType().equals("STEP"))
        .toList();
    assertEquals(3, stepOps.size());

    // Check step names
    var stepNames = stepOps.stream()
        .map(op -> op.getName())
        .collect(Collectors.toSet());
    assertEquals(Set.of("task1", "task2", "task3"), stepNames);
}
