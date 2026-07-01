using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class CallbackConfigExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, ApprovalResult>(Workflow, input, context);

    private async Task<ApprovalResult> Workflow(object input, IDurableContext ctx)
    {
        // CallbackConfig has no serializer slot. The callback payload delivered by
        // the external system is deserialized with the ILambdaSerializer registered
        // on ILambdaContext.Serializer. To customize deserialization, register a
        // custom ILambdaSerializer at the host boundary.
        var config = new CallbackConfig
        {
            Timeout = TimeSpan.FromHours(1),
        };

        ICallback<ApprovalResult> callback =
            await ctx.CreateCallbackAsync<ApprovalResult>("await-approval", config);

        // Send callback.CallbackId to the external system here.
        return await callback.GetResultAsync();
    }
}

public record ApprovalResult(bool Approved, string Reason);
