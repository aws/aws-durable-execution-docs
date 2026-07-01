using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

// Business logic: no IDurableContext, pure work.
public static class OrderLogic
{
    public static ValidationResult ValidateOrder(Order order) => new(order.Id, Valid: true);

    public static Receipt ChargePayment(Order order) => new(order.Id, order.Total);

    public static string ScheduleShipment(Order order) => $"ship-{order.Id}";
}

// Orchestration: the workflow reads as a sequence of intent.
public class SeparateLogicExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<Order, OrderResult>(Workflow, input, context);

    private async Task<OrderResult> Workflow(Order order, IDurableContext ctx)
    {
        await ctx.StepAsync(
            async (_, _) => OrderLogic.ValidateOrder(order),
            name: "validate");

        Receipt receipt = await ctx.StepAsync(
            async (_, _) => OrderLogic.ChargePayment(order),
            name: "charge");

        string shipmentId = await ctx.StepAsync(
            async (_, _) => OrderLogic.ScheduleShipment(order),
            name: "schedule");

        return new OrderResult(receipt, shipmentId);
    }
}

public record Order(string Id, decimal Total, string Address, string CardToken);
public record ValidationResult(string OrderId, bool Valid);
public record Receipt(string OrderId, decimal Amount);
public record OrderResult(Receipt Receipt, string ShipmentId);
