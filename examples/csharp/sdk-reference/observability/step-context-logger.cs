using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Microsoft.Extensions.Logging;

public class StepContextLoggerExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, string>(Workflow, input, context);

    private async Task<string> Workflow(object input, IDurableContext ctx)
    {
        return await ctx.StepAsync(
            async (stepCtx, _) =>
            {
                // stepCtx.Logger includes operationId, operationName, and attempt
                // in every log entry from this step.
                stepCtx.Logger.LogInformation("Running step");
                return "done";
            },
            name: "process");
    }
}
