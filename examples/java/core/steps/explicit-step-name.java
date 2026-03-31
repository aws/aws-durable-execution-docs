public String handleRequest(Map<String, Object> event, DurableContext ctx) {
    // Explicit name
    var result = ctx.step("custom-step", String.class, stepCtx -> {
        return "Step with explicit name";
    });
    return "Result: " + result;
}
