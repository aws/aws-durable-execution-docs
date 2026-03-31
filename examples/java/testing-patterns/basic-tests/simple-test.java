@Test
void testHelloWorld() {
    var handler = new SimpleHandler();
    var runner = LocalDurableTestRunner.create(String.class, handler);

    var result = runner.runUntilComplete("test");

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertEquals("Hello World!", result.getResult(String.class));
}
