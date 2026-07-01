using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ParallelismExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<ItemsEvent, IReadOnlyList<string>>(
            Workflow, input, context);

    private async Task<IReadOnlyList<string>> Workflow(ItemsEvent input, IDurableContext ctx)
    {
        // A small number of fixed, named branches.
        IBatchResult<string> enrich = await ctx.ParallelAsync(
            new[]
            {
                new DurableBranch<string>("fx", async (branch, ct) =>
                    await branch.StepAsync(async (_, _) => FxRatesLatest(), name: "fx")),
                new DurableBranch<string>("weather", async (branch, ct) =>
                    await branch.StepAsync(async (_, _) => WeatherApiGet(), name: "weather")),
                new DurableBranch<string>("quote", async (branch, ct) =>
                    await branch.StepAsync(async (_, _) => QuoteApiGet(), name: "quote")),
            },
            name: "enrich");

        // A variable number of items.
        IBatchResult<string> processed = await ctx.MapAsync(
            input.Items,
            async (itemCtx, item, index, items, ct) =>
                await itemCtx.StepAsync(async (_, _) => Process(item), name: $"process-{index}"),
            name: "process-items",
            config: new MapConfig { MaxConcurrency = 10 });

        return enrich.GetResults().Concat(processed.GetResults()).ToList();
    }

    private static string FxRatesLatest() => "fx";
    private static string WeatherApiGet() => "weather";
    private static string QuoteApiGet() => "quote";
    private static string Process(string item) => $"processed-{item}";
}

public record ItemsEvent(IReadOnlyList<string> Items);
