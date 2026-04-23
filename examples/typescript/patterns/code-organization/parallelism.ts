// A small number of fixed branches.
const { fx, weather, quote } = await context.parallel("enrich", [
  async (ctx) => ctx.step("fx", () => fxRates.latest()),
  async (ctx) => ctx.step("weather", () => weatherApi.get()),
  async (ctx) => ctx.step("quote", () => quoteApi.get()),
]);

// A variable number of items.
const results = await context.map(items, async (ctx, item) =>
  ctx.step("process", () => process(item)),
  { maxConcurrency: 10 },
);
