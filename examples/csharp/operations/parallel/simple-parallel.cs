using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class SimpleParallelExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, IReadOnlyList<string>>(Workflow, input, context);

    private async Task<IReadOnlyList<string>> Workflow(object input, IDurableContext ctx)
    {
        IBatchResult<string> result = await ctx.ParallelAsync(
            new Func<IDurableContext, CancellationToken, Task<string>>[]
            {
                async (branch, ct) => await branch.StepAsync(
                    async (_, _) => "inventory ok", name: "check-inventory"),
                async (branch, ct) => await branch.StepAsync(
                    async (_, _) => "payment ok", name: "check-payment"),
                async (branch, ct) => await branch.StepAsync(
                    async (_, _) => "shipping ok", name: "check-shipping"),
            },
            name: "check-services");

        return result.GetResults();
    }
}
