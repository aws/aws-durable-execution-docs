var result = ctx.step("call-external-service", Map.class, stepCtx -> {
    try {
        // Call external service
        return makeApiCall();
    } catch (RuntimeException e) {
        // Trigger retry
        throw new RuntimeException("Service unavailable", e);
    }
});
