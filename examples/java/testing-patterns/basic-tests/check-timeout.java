@Test
void testCompletesQuickly() {
    var runner = LocalDurableTestRunner.create(Map.class, new QuickHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
}
