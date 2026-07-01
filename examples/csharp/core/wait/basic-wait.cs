using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class BasicWaitExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        // Wait for 5 seconds
        await ctx.WaitAsync(TimeSpan.FromSeconds(5));
        return "Wait completed";
    }
}
