var result = ctx.step("call_api", Map.class, stepCtx -> {
    var response = httpClient.send(
        HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(10)).build(),
        HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() >= 500) {
        throw new RuntimeException("Server error");
    }
    return parseJson(response.body());
}, StepConfig.builder()
    .retryStrategy(RetryStrategies.exponentialBackoff(
        3,                        // max attempts
        Duration.ofSeconds(1),    // initial delay
        Duration.ofSeconds(10),   // max delay
        2.0,                      // backoff multiplier
        JitterStrategy.FULL))
    .build());
return result;
