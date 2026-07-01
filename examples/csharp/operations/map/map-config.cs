using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class MapConfigExample
{
    private static readonly HttpClient Http = new();

    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<UrlEvent, IReadOnlyList<string>>(
            Workflow, input, context);

    private async Task<IReadOnlyList<string>> Workflow(
        UrlEvent input, IDurableContext ctx)
    {
        var config = new MapConfig
        {
            MaxConcurrency = 5,
            CompletionConfig = new CompletionConfig { ToleratedFailureCount = 2 },
        };

        IBatchResult<string> result = await ctx.MapAsync(
            input.Urls,
            async (itemCtx, url, index, urls, ct) =>
                await itemCtx.StepAsync(
                    async (_, stepCt) => await Http.GetStringAsync(url, stepCt),
                    name: $"fetch-{index}"),
            name: "fetch-urls",
            config: config);

        return result.GetResults();
    }
}

public record UrlEvent(IReadOnlyList<string> Urls);
