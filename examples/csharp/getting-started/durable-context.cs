using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Microsoft.Extensions.Logging;

public class DurableContextExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        // Your workflow receives IDurableContext instead of the Lambda context.
        // Use it to access durable operations (ctx.StepAsync, ctx.WaitAsync, ...),
        // the replay-safe logger, and metadata about the current execution.
        ctx.Logger.LogInformation(
            "Running execution {Arn}", ctx.ExecutionContext.DurableExecutionArn);

        return await ctx.StepAsync(
            async (_, _) => "step completed",
            name: "my-step");
    }
}
