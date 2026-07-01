using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class BasicCallbackExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<RequestEvent, ApprovalResult>(Workflow, input, context);

    private async Task<ApprovalResult> Workflow(RequestEvent input, IDurableContext ctx)
    {
        ICallback<string> callback = await ctx.CreateCallbackAsync<string>("wait-for-approval");

        // Send callback.CallbackId to the external system that will resume this function.
        await SendApprovalRequestAsync(callback.CallbackId, input.RequestId);

        // Execution suspends here until the external system calls back.
        string result = await callback.GetResultAsync();
        return new ApprovalResult(Approved: true, Result: result);
    }

    private static Task SendApprovalRequestAsync(string callbackId, string requestId)
        => Task.CompletedTask;
}

public record RequestEvent(string RequestId);
public record ApprovalResult(bool Approved, string Result);
