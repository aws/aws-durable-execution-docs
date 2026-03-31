@Test
void testConcurrencyLimit() {
    var runner = LocalDurableTestRunner.create(Map.class, new ControlledConcurrencyHandler());

    List<String> items = IntStream.range(0, 20)
        .mapToObj(String::valueOf)
        .collect(Collectors.toList());

    var result = runner.runUntilComplete(Map.of("items", items));

    // All items should be processed
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    var resultMap = result.getResult(Map.class);
    var results = (List<?>) resultMap.get("results");
    assertEquals(20, results.size());
}
