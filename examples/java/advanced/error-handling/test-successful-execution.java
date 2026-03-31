@Test
void testSuccess() {
    var handler = new MyHandler();
    var runner = LocalDurableTestRunner.create(Map.class, handler);

    var result = runner.runUntilComplete(Map.of("data", "test"));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
}
