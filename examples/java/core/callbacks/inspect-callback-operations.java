@Test
void testCallbackOperation() {
    var runner = LocalDurableTestRunner.create(String.class, new CallbackHandler());

    var result = runner.run("test");

    // Find callback operation
    var callbackOp = runner.getOperation("example_callback");

    // Verify callback properties
    assertNotNull(callbackOp);
    assertEquals("example_callback", callbackOp.getName());
    assertNotNull(callbackOp.getCallbackId());
}
