var result = ctx.step("call_external_api", Map.class, stepCtx -> {
    // No error handling - all errors cause retry, even permanent ones
    var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return parseJson(response.body());
});
