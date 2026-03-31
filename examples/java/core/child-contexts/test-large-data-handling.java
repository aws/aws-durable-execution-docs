@Test
void testLargeDataProcessing() {
    var runner = LocalDurableTestRunner.create(Map.class, new LargeDataHandler());

    var result = runner.runUntilComplete(null);

    // Verify execution succeeded
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    var resultData = result.getResult(Map.class);
    assertTrue((boolean) resultData.get("success"));

    // Verify large data was processed
    var summary = (Map) resultData.get("summary");
    assertTrue((int) summary.get("totalDataSize") > 240); // ~250KB
    assertEquals(5, summary.get("stepsExecuted"));

    // Verify data integrity across wait
    assertTrue((boolean) resultData.get("dataIntegrityCheck"));
}
