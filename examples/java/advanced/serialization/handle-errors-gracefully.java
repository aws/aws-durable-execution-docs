try {
    var result = ctx.step("process-data", Map.class, stepCtx -> processData(complexObject));
} catch (SerDesException e) {
    // Convert to serializable format and retry
    var simpleData = convertToMap(complexObject);
    var result = ctx.step("process-data-fallback", Map.class, stepCtx -> processData(simpleData));
}
