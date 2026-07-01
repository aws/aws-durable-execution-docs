using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class PassArgumentsExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderEvent, ChargedOrder>(Workflow, input, context);

    private async Task<ChargedOrder> Workflow(OrderEvent input, IDurableContext ctx)
    {
        string userId = "user-123";

        // Capture arguments in the closure
        return await ctx.RunInChildContextAsync(
            async (child, ct) =>
            {
                ValidatedOrder validated = await child.StepAsync(
                    async (_, _) => Validate(input.OrderId, userId),
                    name: "validate");
                return await child.StepAsync(
                    async (_, _) => Charge(validated),
                    name: "charge");
            },
            name: "process-order");
    }

    private static ValidatedOrder Validate(string orderId, string userId) => new(orderId, userId);
    private static ChargedOrder Charge(ValidatedOrder order) => new(order.OrderId, order.UserId, Charged: true);
}

public record OrderEvent(string OrderId);
public record ValidatedOrder(string OrderId, string UserId);
public record ChargedOrder(string OrderId, string UserId, bool Charged);
