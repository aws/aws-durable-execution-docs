@Test
void testCallbackTimeout() {
    var runner = LocalDurableTestRunner.create(Map.class, new CallbackConfigHandler());

    var result = runner.run(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertTrue(result.getResult(String.class).contains("60s timeout"));
}
