using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ReusableStepExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<Order, ValidationResult>(Workflow, input, context);

    private async Task<ValidationResult> Workflow(Order order, IDurableContext ctx)
    {
        // Reference the reusable method as the step body.
        return await ctx.StepAsync(
            async (_, ct) => await ValidateOrder(order, ct),
            name: "validate-order");
    }

    // Define a reusable step method once and reference it repeatedly.
    private static Task<ValidationResult> ValidateOrder(Order order, CancellationToken ct)
        => Task.FromResult(new ValidationResult(order.OrderId, Valid: true));
}

public record Order(string OrderId);
public record ValidationResult(string OrderId, bool Valid);
