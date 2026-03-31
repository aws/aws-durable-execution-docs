MapResult<String> result = ctx.map("work", items, String.class,
    (item, index, childCtx) ->
        childCtx.step("step-" + index, String.class, stepCtx -> process(item)));

List<String> successful = result.succeeded();
List<MapError> failed = result.failed();
