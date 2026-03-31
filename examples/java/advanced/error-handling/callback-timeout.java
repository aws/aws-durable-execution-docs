// Increase timeout
var config = CallbackConfig.builder()
    .timeout(Duration.ofHours(2))
    .heartbeatTimeout(Duration.ofMinutes(5))
    .build();

var callback = ctx.createCallback("long_running_approval", String.class, config);
