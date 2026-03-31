var enriched = ctx.runInChildContext("validation_phase", Map.class, childCtx -> {
    var validated = childCtx.step("validate_data", Map.class,
        stepCtx -> validateData(input.get("data")));
    return childCtx.step("enrich_data", Map.class,
        stepCtx -> enrichData(validated));
});
return enriched;
