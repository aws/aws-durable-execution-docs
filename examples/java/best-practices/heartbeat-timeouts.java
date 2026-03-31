var callback = ctx.createCallback("approval", Map.class,
    CallbackConfig.builder()
        .timeout(Duration.ofHours(24))           // Maximum wait time
        .heartbeatTimeout(Duration.ofHours(2))   // Fail if no heartbeat for 2 hours
        .build());
