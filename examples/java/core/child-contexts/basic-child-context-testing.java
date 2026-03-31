@Test
void testRunInChildContext() {
    var runner = LocalDurableTestRunner.create(String.class, new ChildContextHandler());

    var result = runner.runUntilComplete("test");

    // Check overall status
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertEquals("Child context result: 10", result.getResult(String.class));
}
