@Test
void testStep() {
    var runner = LocalDurableTestRunner.create(Map.class, new MyHandler());

    var result = runner.run(Map.of("data", "test"));

    // Check overall status
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Check final result
    assertEquals(8, result.getResult(Integer.class));
}
