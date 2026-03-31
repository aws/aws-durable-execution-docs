// Inline lambdas are harder to test and reuse
var isValid = ctx.step("validate_input", Boolean.class, stepCtx ->
    input.containsKey("name") && input.containsKey("email"));
return Map.of("valid", isValid);
