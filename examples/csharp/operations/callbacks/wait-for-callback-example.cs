using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class WaitForCallbackExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<RequestEvent, ApprovalResult>(Workflow, input, context);

    private async Task<ApprovalResult> Workflow(RequestEvent input, IDurableContext ctx)
    {
        string result = await ctx.WaitForCallbackAsync<string>(
            async (callbackId, _, _) => await SendApprovalRequestAsync(callbackId, input.RequestId),
            name: "wait-for-approval");
        return new ApprovalResult(Approved: true, Result: result);
    }

    private static Task SendApprovalRequestAsync(string callbackId, string requestId)
        => Task.CompletedTask;
}

public record RequestEvent(string RequestId);
public record ApprovalResult(bool Approved, string Result);
