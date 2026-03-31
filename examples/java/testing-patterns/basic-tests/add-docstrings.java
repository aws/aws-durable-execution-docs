@Test
@DisplayName("Payment processing retries on transient failures, "
    + "eventually succeeds, and returns a transaction ID")
void testPaymentWithRetry() {
    var runner = LocalDurableTestRunner.create(Map.class, new PaymentHandler());

    var result = runner.runUntilComplete(Map.of("amount", 50.0));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
}
