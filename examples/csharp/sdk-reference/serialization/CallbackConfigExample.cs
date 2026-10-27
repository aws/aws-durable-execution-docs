using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.Serialization.SystemTextJson;

public class CallbackConfigExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, ApprovalResult>(Workflow, input, context);

    private async Task<ApprovalResult> Workflow(object input, IDurableContext ctx)
    {
        // Set CallbackConfig.Serializer to deserialize the callback payload with a specific
        // ILambdaSerializer. When null (default), the payload the external system delivers is
        // deserialized with the ILambdaSerializer registered on ILambdaContext.Serializer.
        // Only the deserialize path is used for callbacks.
        var config = new CallbackConfig
        {
            Timeout = TimeSpan.FromHours(1),
            Serializer = new DefaultLambdaJsonSerializer(),
        };

        ICallback<ApprovalResult> callback =
            await ctx.CreateCallbackAsync<ApprovalResult>("await-approval", config);

        // Send callback.CallbackId to the external system here.
        return await callback.GetResultAsync();
    }
}

public record ApprovalResult(bool Approved, string Reason);
