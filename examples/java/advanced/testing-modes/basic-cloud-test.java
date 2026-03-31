@Test
void testHelloWorld() {
    var runner = CloudDurableTestRunner.create(
        "arn:aws:lambda:us-east-1:123456789:function:hello-world",
        String.class,
        String.class
    );

    var result = runner.runUntilComplete("test");

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertEquals("Hello World!", result.getResult(String.class));
}
