@Test
void testMultipleChildContexts() {
    var runner = LocalDurableTestRunner.create(Map.class,
        new MultipleChildContextsHandler());

    var result = runner.runUntilComplete(Map.of("data", Map.of("value", 42)));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    var finalResult = result.getResult(Map.class);
    assertEquals(true, finalResult.get("validated"));
    assertEquals(true, finalResult.get("transformed"));

    // Verify both contexts ran
    var validationOp = result.getOperation("validation");
    var transformationOp = result.getOperation("transformation");
    assertNotNull(validationOp);
    assertNotNull(transformationOp);
}
