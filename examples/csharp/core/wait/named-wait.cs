using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class NamedWaitExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        // Wait with explicit name
        await ctx.WaitAsync(TimeSpan.FromSeconds(2), name: "custom_wait");
        return "Wait with name completed";
    }
}
