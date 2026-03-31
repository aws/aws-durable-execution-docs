var result = ctx.step("call_external_api", Map.class, stepCtx -> {
    try {
        var response = httpClient.send(
            HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(10)).build(),
            HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 500) {
            throw new RuntimeException("Server error: " + response.statusCode());  // Retry server errors
        }
        if (response.statusCode() >= 400) {
            // Don't retry client errors (400-499)
            return Map.of("error", "client_error", "status", response.statusCode());
        }
        return parseJson(response.body());
    } catch (java.net.http.HttpTimeoutException e) {
        throw new RuntimeException(e);  // Let retry handle timeouts
    }
});
