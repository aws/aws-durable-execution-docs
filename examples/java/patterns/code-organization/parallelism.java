// A small number of fixed branches.
var enrich = context.parallel("enrich")
    .step("fx", FxRates.class, ctx -> fxRates.latest())
    .step("weather", Weather.class, ctx -> weatherApi.get())
    .step("quote", Quote.class, ctx -> quoteApi.get())
    .run();

// A variable number of items.
BatchResult<Result> processed = context.map(
    "process-items",
    items,
    Result.class,
    (ctx, item) -> process(item));
