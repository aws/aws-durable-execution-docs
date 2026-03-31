@Test
void testParallelWithFailures() {
    var runner = LocalDurableTestRunner.create(Map.class, new BranchFailureHandler());

    var result = runner.runUntilComplete(Map.of());

    // Check that some branches succeeded and some failed
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    var resultMap = result.getResult(Map.class);
    var succeeded = (List<?>) resultMap.get("successful");
    assertTrue(succeeded.size() > 0);
    assertTrue((int) resultMap.get("failed_count") > 0);
}
