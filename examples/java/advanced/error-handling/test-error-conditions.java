@Test
void testInputValidation() {
    var handler = new ValidationHandler();
    var runner = LocalDurableTestRunner.create(Map.class, handler);

    var result = runner.runUntilComplete(Map.of());

    // Function should return error response for invalid input
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    var output = result.getResult(Map.class);
    assertEquals("InvalidInput", output.get("error"));
}
