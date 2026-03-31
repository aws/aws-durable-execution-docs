// Timeout after 30 seconds
InvokeConfig config30s = InvokeConfig.builder()
    .timeout(Duration.ofSeconds(30))
    .build();

// Timeout after 5 minutes
InvokeConfig config5m = InvokeConfig.builder()
    .timeout(Duration.ofMinutes(5))
    .build();

// Timeout after 2 hours
InvokeConfig config2h = InvokeConfig.builder()
    .timeout(Duration.ofHours(2))
    .build();
