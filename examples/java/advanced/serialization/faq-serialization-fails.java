try {
    var result = ctx.step("process-data", Map.class, stepCtx -> processData(data));
} catch (SerDesException e) {
    System.err.println("Serialization failed: " + e.getMessage());
    // Handle error or convert data
}
