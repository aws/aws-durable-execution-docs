@Test
void testValidationFailure() {
    var runner = LocalDurableTestRunner.create(Map.class, new ValidationHandler());

    var result = runner.runUntilComplete(Map.of("invalid", "data"));

    assertEquals(ExecutionStatus.FAILED, result.getStatus());
    assertTrue(result.getError().getMessage().contains("ValidationError"));
}
