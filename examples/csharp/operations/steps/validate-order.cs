using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ValidateOrderExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderEvent, Validation>(Workflow, input, context);

    private async Task<Validation> Workflow(OrderEvent input, IDurableContext ctx)
    {
        Validation validation = await ctx.StepAsync(
            async (_, _) => ValidateOrder(input.OrderId),
            name: "validate_order");
        return validation;
    }

    private static Validation ValidateOrder(string orderId) => new(orderId, Valid: true);
}

public record OrderEvent(string OrderId);
public record Validation(string OrderId, bool Valid);
