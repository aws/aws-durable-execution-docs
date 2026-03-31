// Process 100 items, but only 10 at a time
ParallelConfig config = ParallelConfig.builder()
    .maxConcurrency(10)
    .build();

MapResult<String> result = ctx.map("parallel-work", items, String.class,
    (item, index, childCtx) ->
        childCtx.step("step-" + index, String.class, stepCtx -> process(item)),
    MapConfig.builder().maxConcurrency(10).build());
