@Test
void testLongRunning() {
    var runner = LocalDurableTestRunner.create(Map.class, new LongRunningHandler());

    var result = runner.runUntilComplete(Map.of("input", "test"));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
}
