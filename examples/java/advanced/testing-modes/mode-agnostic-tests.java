@Test
void testMyFunction() {
    // Works with both LocalDurableTestRunner and CloudDurableTestRunner
    var runner = LocalDurableTestRunner.create(String.class, new MyHandler());

    var result = runner.runUntilComplete(Map.of("value", 42));

    // These assertions work in both modes
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertEquals("expected output", result.getResult(String.class));
}
