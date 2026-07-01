using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Microsoft.Extensions.Logging;

public class BasicUsageExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        ctx.Logger.LogInformation("Starting workflow");

        string result = await ctx.StepAsync(
            async (_, _) => "done",
            name: "process");

        ctx.Logger.LogInformation("Workflow complete: {Result}", result);
        return result;
    }
}
