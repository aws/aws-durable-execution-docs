var callback = ctx.createCallback("approval", Map.class);
var result = callback.result();
return Map.of("approved", result.get("approved"));  // Crashes if timeout!
