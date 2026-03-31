try {
    var result = ctx.step("operation", Object.class, stepCtx -> doWork());
} catch (CallbackFailedException e) {
    System.out.println("Callback failed: " + e.getMessage());
} catch (NonDeterministicExecutionException e) {
    System.out.println("Non-deterministic error: " + e.getMessage());
}
