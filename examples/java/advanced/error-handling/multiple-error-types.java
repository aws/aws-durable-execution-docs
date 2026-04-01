public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
    try {
        var result = ctx.step("complex_operation", Map.class, stepCtx -> {
            // Validate input
            if (event == null || event.isEmpty()) {
                throw new IllegalArgumentException("Data is required");
            }

            // Check business rules
            var amount = ((Number) event.getOrDefault("amount", 0)).doubleValue();
            if (amount < 0) {
                throw new StepFailedException("Amount must be positive");
            }

            // Call external service
            return callExternalService(event);
        });
        return Map.of("status", "success", "result", result);
    } catch (IllegalArgumentException e) {
        return Map.of("status", "invalid", "error", e.getMessage());
    } catch (StepFailedException e) {
        return Map.of("status", "failed", "error", e.getMessage());
    } catch (RuntimeException e) {
        // Let Lambda retry
        throw e;
    }
}
