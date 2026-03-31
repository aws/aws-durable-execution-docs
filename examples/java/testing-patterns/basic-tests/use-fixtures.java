class OrderProcessingTest {

    private Map<String, Object> validOrder;

    @BeforeEach
    void setUp() {
        validOrder = Map.of(
            "order_id", "order-123",
            "customer_id", "customer-456",
            "amount", 100.0,
            "items", List.of(
                Map.of("product_id", "prod-1", "quantity", 2),
                Map.of("product_id", "prod-2", "quantity", 1)
            )
        );
    }

    @Test
    void testOrderProcessing() {
        var runner = LocalDurableTestRunner.create(Map.class, new OrderHandler());

        var result = runner.runUntilComplete(validOrder);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    }
}
