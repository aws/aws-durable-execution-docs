using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Microsoft.Extensions.Logging;

public class QuickstartFunction
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, Response>(Workflow, input, context);

    private async Task<Response> Workflow(object input, IDurableContext ctx)
    {
        string message = await ctx.StepAsync(
            async (stepCtx, _) =>
            {
                stepCtx.Logger.LogInformation("Hello from step-1");
                return "Hello from Durable Lambda!";
            },
            name: "step-1");

        // Pause for 10 seconds without consuming CPU or incurring usage charges
        await ctx.WaitAsync(TimeSpan.FromSeconds(10), name: "wait-10s");

        // Replay-aware: logs once even though the function replays after the wait
        ctx.Logger.LogInformation("Resumed after wait");

        return new Response(200, message);
    }
}

public record Response(int StatusCode, string Body);
