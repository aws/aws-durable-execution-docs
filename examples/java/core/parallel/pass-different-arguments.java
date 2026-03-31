// Use map to pass different arguments to each branch
List<String> values = List.of("arg1", "arg2", "arg3");

MapResult<String> result = ctx.map("process-all", values, String.class,
    (value, index, childCtx) ->
        childCtx.step("process-" + index, String.class,
            stepCtx -> process(value)));
