import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;

public class Saga extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    // Compensation holder
    static class Compensation {
        final String name;
        final Runnable fn;

        Compensation(String name, Runnable fn) {
            this.name = name;
            this.fn = fn;
        }
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext context) {
        List<Compensation> compensations = new ArrayList<>();

        try {
            String orderId = (String) event.get("order_id");
            double amount  = ((Number) event.get("amount")).doubleValue();

            // Step 1: Reserve inventory
            Map<String, Object> reservation = context.step(
                "reserve-inventory", Map.class,
                (StepContext ctx) -> {
                    ctx.getLogger().info("Reserving inventory for order: " + orderId);
                    return reserveInventory(orderId);
                }
            );
            compensations.add(new Compensation(
                "cancel-reservation",
                () -> cancelReservation((String) reservation.get("id"))
            ));

            // Step 2: Charge payment
            Map<String, Object> payment = context.step(
                "charge-payment", Map.class,
                (StepContext ctx) -> {
                    ctx.getLogger().info("Charging payment: " + amount);
                    return chargePayment(amount);
                }
            );
            compensations.add(new Compensation(
                "refund-payment",
                () -> refundPayment((String) payment.get("id"))
            ));

            // Step 3: Create shipment (no compensation because it's the last step)
            context.step(
                "create-shipment", Map.class,
                (StepContext ctx) -> {
                    ctx.getLogger().info("Creating shipment for order: " + orderId);
                    return createShipment(orderId);
                }
            );

            context.getLogger().info("Order completed successfully: " + orderId);
            return Map.of("success", true);

        } catch (Exception error) {
            context.getLogger().error("Order failed, running compensations: " + error.getMessage());

            // Run compensations in reverse to undo completed steps
            for (int i = compensations.size() - 1; i >= 0; i--) {
                Compensation comp = compensations.get(i);
                try {
                    context.step(comp.name, Void.class, (StepContext ctx) -> {
                        comp.fn.run();
                        return null;
                    });
                } catch (Exception compError) {
                    // Log but continue to attempt all compensations even if one fails
                    context.getLogger().error("Compensation failed: " + comp.name + " - " + compError.getMessage());
                }
            }

            throw error;
        }
    }

    // Mock APIs for demonstration purposes

    private Map<String, String> reserveInventory(String orderId) {
        return Map.of("id", "RES-" + orderId);
    }

    private void cancelReservation(String reservationId) {
        System.out.println("Reservation " + reservationId + " cancelled");
    }

    private Map<String, String> chargePayment(double amount) {
        return Map.of("id", "PAY-" + amount);
    }

    private void refundPayment(String paymentId) {
        System.out.println("Payment " + paymentId + " refunded");
    }

    private Map<String, String> createShipment(String orderId) {
        return Map.of("tracking_id", "TRACK-" + orderId);
    }
}
