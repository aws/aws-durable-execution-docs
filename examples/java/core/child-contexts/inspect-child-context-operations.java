@Test
void testChildContextOperations() {
    var runner = LocalDurableTestRunner.create(String.class, new ChildContextHandler());

    var result = runner.runUntilComplete("test");

    // Verify child context operation exists
    var contextOps = result.getOperations().stream()
        .filter(op -> op.getOperationType().equals("CONTEXT"))
        .toList();
    assertTrue(contextOps.size() >= 1);

    // Get child context by name
    var childResult = result.getContext("child_operation");
    assertNotNull(childResult);
}
