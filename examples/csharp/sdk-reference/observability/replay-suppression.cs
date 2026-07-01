using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Microsoft.Extensions.Logging;

public class ReplaySuppressionExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        // ctx.Logger suppresses duplicate logs during replay by default.
        // Logs from completed operations do not repeat when the SDK replays.
        ctx.Logger.LogInformation("Step 1 starting");

        string result = await ctx.StepAsync(
            async (_, _) => "result",
            name: "step-1");

        ctx.Logger.LogInformation("Step 1 complete: {Result}", result);
        return result;
    }
}
