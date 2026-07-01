using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ContextFunctionExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderEvent, ChargedOrder>(Workflow, input, context);

    private async Task<ChargedOrder> Workflow(OrderEvent input, IDurableContext ctx)
    {
        // Pass an async (child, ct) => ... lambda as the context function. The
        // function receives its own IDurableContext and returns a Task<T>.
        return await ctx.RunInChildContextAsync(
            async (child, ct) =>
            {
                Order validated = await child.StepAsync(
                    async (_, _) => Validate(input.OrderId),
                    name: "validate");
                return await child.StepAsync(
                    async (_, _) => Charge(validated),
                    name: "charge");
            },
            name: "process-order");
    }

    private static Order Validate(string orderId) => new(orderId, Valid: true);
    private static ChargedOrder Charge(Order order) => new(order.OrderId, order.Valid, Charged: true);
}

public record OrderEvent(string OrderId);
public record Order(string OrderId, bool Valid);
public record ChargedOrder(string OrderId, bool Valid, bool Charged);
