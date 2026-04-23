CallbackConfig cb = CallbackConfig.builder()
    .timeout(Duration.ofHours(24))
    .heartbeatTimeout(Duration.ofMinutes(10))
    .build();
