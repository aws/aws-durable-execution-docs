class OrderProcessingTest {

    private static LocalDurableTestRunner<Map> runner;

    @BeforeAll
    static void setUp() {
        runner = LocalDurableTestRunner.create(Map.class, new OrderHandler());
    }

    @AfterAll
    static void tearDown() {
        runner = null;
    }

    @Test
    void testOrderProcessing() {
        var result = runner.runUntilComplete(Map.of("order_id", "order-123"));
        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    }
}
