// Step 1: Create the callback with timeout configuration
var callbackConfig = CallbackConfig.builder()
    .timeout(Duration.ofMinutes(2))
    .heartbeatTimeout(Duration.ofSeconds(60))
    .build();

var callback = ctx.createCallback("example-callback", String.class, callbackConfig);

// Step 2: Send callback ID to external system
ctx.step("notify-external", Void.class, stepCtx -> {
    sendToExternalSystem(callback.callbackId(), input);
    return null;
});

// Step 3: Wait for the result — execution suspends here
var result = callback.get();

// Step 4: Execution resumes when result is received
return Map.of("status", "completed", "result", result);
