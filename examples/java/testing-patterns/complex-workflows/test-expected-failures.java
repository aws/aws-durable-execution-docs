@Test
void testValidationFailure() {
    var runner = LocalDurableTestRunner.create(Map.class, new ValidationHandler());

    var result = runner.runUntilComplete(Map.of("value", -5));

    assertEquals(ExecutionStatus.FAILED, result.getStatus());
    assertTrue(result.getError().getMessage().contains("Value must be non-negative"));
}
