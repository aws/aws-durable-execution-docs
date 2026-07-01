using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ParallelConfigExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string?>(Workflow, input, context);

    private async Task<string?> Workflow(object input, IDurableContext ctx)
    {
        var config = new ParallelConfig
        {
            MaxConcurrency = 2,
            CompletionConfig = CompletionConfig.FirstSuccessful(),
        };

        IBatchResult<string> result = await ctx.ParallelAsync(
            new Func<IDurableContext, CancellationToken, Task<string>>[]
            {
                async (branch, ct) => await branch.StepAsync(
                    async (_, _) => "primary result", name: "primary"),
                async (branch, ct) => await branch.StepAsync(
                    async (_, _) => "secondary result", name: "secondary"),
                async (branch, ct) => await branch.StepAsync(
                    async (_, _) => "cache result", name: "cache"),
            },
            name: "fetch-data",
            config: config);

        return result.GetResults().FirstOrDefault();
    }
}
