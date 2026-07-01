using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class CallbackConfigExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<PaymentEvent, string>(Workflow, input, context);

    private async Task<string> Workflow(PaymentEvent input, IDurableContext ctx)
    {
        var config = new CallbackConfig
        {
            Timeout = TimeSpan.FromHours(24),
            HeartbeatTimeout = TimeSpan.FromMinutes(30),
        };

        ICallback<string> callback = await ctx.CreateCallbackAsync<string>(
            "wait-for-payment", config);

        await SubmitPaymentRequestAsync(callback.CallbackId, input.Amount);
        return await callback.GetResultAsync();
    }

    private static Task SubmitPaymentRequestAsync(string callbackId, decimal amount)
        => Task.CompletedTask;
}

public record PaymentEvent(decimal Amount);
