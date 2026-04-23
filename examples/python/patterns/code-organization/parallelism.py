# A small number of fixed branches.
results = context.parallel(
    [
        lambda ctx: ctx.step(fx_rates_latest()),
        lambda ctx: ctx.step(weather_api_get()),
        lambda ctx: ctx.step(quote_api_get()),
    ],
    name="enrich",
)

# A variable number of items.
processed = context.map(items, process, name="process-items")
