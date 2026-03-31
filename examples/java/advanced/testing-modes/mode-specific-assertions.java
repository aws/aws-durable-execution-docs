@Test
void testWithLocalRunner() {
    var runner = LocalDurableTestRunner.create(String.class, new MyHandler());

    var result = runner.runUntilComplete("test");

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Local-specific: inspect individual operations
    var stepOp = result.getOperation("process-data");
    assertNotNull(stepOp);
}

@Test
void testWithCloudRunner() {
    var runner = CloudDurableTestRunner.create(functionArn, String.class, String.class);

    var result = runner.runUntilComplete("test");

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    // Cloud-specific: validates against real AWS infrastructure
}
