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
        // The step result is serialized to checkpoint storage with the
        // ILambdaSerializer registered on ILambdaContext.Serializer. When
        // serialization or deserialization fails, the SDK surfaces the failure as
        // an unhandled exception, returns a FAILED status, and does not retry.
        try
        {
            ResultData result = await ctx.StepAsync(
                async (_, _) => new ResultData("hello"),
                name: "build-result");
            return result;
        }
        catch (Exception e)
        {
            ctx.Logger.LogError("Serialization failed: {Message}", e.Message);
            throw;
        }
    }
}

public record ResultData(string Message);
