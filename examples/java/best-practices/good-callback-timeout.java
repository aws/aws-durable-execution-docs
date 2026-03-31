var callback = ctx.createCallback("approval", Map.class);
var result = callback.result();

if (result == null) {
    return Map.of("status", "timeout", "approved", false);
}

return Map.of("status", "completed", "approved", result.getOrDefault("approved", false));
