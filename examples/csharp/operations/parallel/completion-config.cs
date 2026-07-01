using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class CompletionConfigExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string?>(Workflow, input, context);

    private async Task<string?> Workflow(object input, IDurableContext ctx)
    {
        var config = new ParallelConfig
        {
            // Complete as soon as one branch succeeds
            CompletionConfig = CompletionConfig.FirstSuccessful(),
        };

        IBatchResult<string> result = await ctx.ParallelAsync(
            new Func<IDurableContext, CancellationToken, Task<string>>[]
            {
                async (branch, ct) => await branch.StepAsync(
                    async (_, _) => "result from a", name: "source-a"),
                async (branch, ct) => await branch.StepAsync(
                    async (_, _) => "result from b", name: "source-b"),
                async (branch, ct) => await branch.StepAsync(
                    async (_, _) => "result from c", name: "source-c"),
            },
            name: "race",
            config: config);

        return result.GetResults().FirstOrDefault();
    }
}
