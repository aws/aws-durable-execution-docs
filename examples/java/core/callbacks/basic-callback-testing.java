@Test
void testCallback() {
    var runner = LocalDurableTestRunner.create(String.class, new CallbackHandler());

    var result = runner.run("test");

    // Check overall status
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Verify callback was created
    assertTrue(result.getResult(String.class).contains("Callback created with ID:"));
}
