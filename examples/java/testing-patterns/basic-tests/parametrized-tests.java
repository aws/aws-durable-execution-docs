@ParameterizedTest
@CsvSource({
    "5, 3, 8",
    "10, 20, 30",
    "0, 0, 0",
    "-5, 5, 0"
})
void testAddition(int a, int b, int expected) {
    var runner = LocalDurableTestRunner.create(Map.class, new AddHandler());

    var result = runner.runUntilComplete(Map.of("a", a, "b", b));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertEquals(expected, result.getResult(Integer.class));
}
