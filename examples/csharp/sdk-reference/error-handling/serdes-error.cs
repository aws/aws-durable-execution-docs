using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Microsoft.Extensions.Logging;

public class SerdesErrorExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, ResultData>(Workflow, input, context);

    private async Task<ResultData> Workflow(object input, IDurableContext ctx)
    {
        // Result serialization and round-trip failures are terminal StepExceptions.
        // The SDK checkpoints the failure without retrying the successful body.
        try
        {
            ResultData result = await ctx.StepAsync(
                async (_, _) => new ResultData("hello"),
                name: "build-result");
            return result;
        }
        catch (StepException e)
        {
            ctx.Logger.LogError("Serialization failed: {Message}", e.Message);
            throw;
        }
    }
}

public record ResultData(string Message);
