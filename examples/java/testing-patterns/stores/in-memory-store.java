@Test
void testWithMemoryStore() {
    // Local runner uses in-memory store by default
    var runner = LocalDurableTestRunner.create(Map.class, new MyHandler());

    var result = runner.runUntilComplete(Map.of("data", "test"));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
}
