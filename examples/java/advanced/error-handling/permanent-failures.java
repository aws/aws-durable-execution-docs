public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
    try {
        var result = ctx.step("process_payment", Map.class, stepCtx -> {
            var card = (String) event.get("card");
            // Validate card
            if (!isValidCard(card)) {
                // Don't retry invalid cards
                throw new StepFailedException("Invalid card number");
            }
            var amount = ((Number) event.get("amount")).doubleValue();
            return Map.of("transaction_id", "txn_123", "amount", amount);
        });
        return Map.of("status", "success", "transaction", result);
    } catch (StepFailedException e) {
        // Permanent failure, don't retry
        return Map.of("status", "failed", "error", e.getMessage());
    }
}
