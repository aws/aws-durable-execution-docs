using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class GroupConfigExample
{
    // Define shared configuration once and reuse it. The name makes the intent clear.
    private static readonly StepConfig PaymentConfig = new()
    {
        Semantics = StepSemantics.AtMostOncePerRetry,
        RetryStrategy = RetryStrategy.None,
    };

    private static readonly StepConfig IdempotentConfig = new()
    {
        RetryStrategy = RetryStrategy.Default,
    };

    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<OrderEvent, User>(Workflow, input, context);

    private async Task<User> Workflow(OrderEvent input, IDurableContext ctx)
    {
        await ctx.StepAsync(
            async (_, _) => Charge(input.Order),
            name: "charge",
            config: PaymentConfig);

        await ctx.StepAsync(
            async (_, _) => Refund(input.Order),
            name: "refund",
            config: PaymentConfig);

        return await ctx.StepAsync(
            async (_, _) => GetUser(input.UserId),
            name: "fetch-user",
            config: IdempotentConfig);
    }

    private static Receipt Charge(Order order) => new(order.Id, order.Total);
    private static Receipt Refund(Order order) => new(order.Id, -order.Total);
    private static User GetUser(string id) => new(id, "Ada");
}

public record Order(string Id, decimal Total);
public record OrderEvent(Order Order, string UserId);
public record Receipt(string OrderId, decimal Amount);
public record User(string Id, string Name);
