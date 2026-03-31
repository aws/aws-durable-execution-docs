var isValid = ctx.step("validate_input", Boolean.class, stepCtx -> {
    return input.containsKey("name") && input.containsKey("email");
});
return Map.of("valid", isValid);
