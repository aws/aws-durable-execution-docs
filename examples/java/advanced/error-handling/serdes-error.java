try {
    var result = ctx.step("process-data", Map.class, stepCtx -> {
        // Process data that might not be serializable
        return Map.of("result", data);
    });
} catch (SerDesException e) {
    // Handle serialization failure
    System.out.println("Cannot serialize result: " + e.getMessage());
}
