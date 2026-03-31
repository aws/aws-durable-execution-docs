@Test
void testCalculationResult() {
    var runner = LocalDurableTestRunner.create(Map.class, new CalculatorHandler());

    var result = runner.runUntilComplete(Map.of("a", 5, "b", 3));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertEquals(8, result.getResult(Integer.class));
}
