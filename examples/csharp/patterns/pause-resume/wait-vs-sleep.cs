using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Microsoft.Extensions.Logging;

public class WaitVsSleepExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<CoolOffEvent, string>(Workflow, input, context);

    private async Task<string> Workflow(CoolOffEvent input, IDurableContext ctx)
    {
        // Do NOT use Thread.Sleep or Task.Delay to pause a durable function.
        // They keep the invocation running (billing compute) and reset to zero
        // on replay:
        //     await Task.Delay(TimeSpan.FromHours(24)); // wrong

        // Use WaitAsync instead: the SDK suspends the execution, checkpoints the
        // wait, and re-invokes the handler when it elapses. No compute charge
        // while suspended. Name every wait so it reads clearly in logs and tests.
        await ctx.WaitAsync(TimeSpan.FromHours(24), name: "cool-off");

        ctx.Logger.LogInformation("Cool-off complete for {OrderId}", input.OrderId);
        return "done";
    }
}

public record CoolOffEvent(string OrderId);
