using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class LambdaStepNoNameExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        // Omit name — the SDK infers one from the call site
        string result = await ctx.StepAsync(async (_, _) => "some value");
        return result;
    }
}
