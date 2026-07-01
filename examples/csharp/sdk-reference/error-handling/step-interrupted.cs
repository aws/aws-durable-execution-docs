using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Microsoft.Extensions.Logging;

public class StepInterruptedExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<PaymentEvent, PaymentResult>(Workflow, input, context);

    private async Task<PaymentResult> Workflow(PaymentEvent input, IDurableContext ctx)
    {
        var config = new StepConfig
        {
            Semantics = StepSemantics.AtMostOncePerRetry,
        };
        try
        {
            Charge result = await ctx.StepAsync(
                async (_, _) => ChargePayment(input.Amount),
                name: "charge-payment",
                config: config);
            return new PaymentResult("charged", result);
        }
        catch (StepInterruptedException)
        {
            // The step started but Lambda was interrupted before the result was
            // checkpointed. The SDK will not re-run the step on the next invocation.
            // Inspect your payment system to determine whether the charge succeeded.
            ctx.Logger.LogWarning("Payment step interrupted — check payment system");
            return new PaymentResult("unknown", null);
        }
    }

    private static Charge ChargePayment(double amount) => new(Charged: amount);
}

public record PaymentEvent(double Amount);
public record Charge(double Charged);
public record PaymentResult(string Status, Charge? Result);
