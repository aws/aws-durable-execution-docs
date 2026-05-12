import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.DurableFuture;
import software.amazon.lambda.durable.TypeToken;

public class ParallelSaga extends DurableHandler<Map<String, Object>, Map<String, Object>> {

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
            Map<?, ?> address = (Map<?, ?>) event.get("address");

            // Reserve inventory AND verify address in parallel
            DurableFuture<Map<String, Object>> reservationFuture;
            DurableFuture<Map<String, Object>> addressFuture;

            try (var parallel = context.parallel("pre-checks")) {
                reservationFuture = parallel.branch("reserve-inventory",
                    new TypeToken<Map<String, Object>>() {},
                    ctx -> ctx.step("reserve", Map.class, s -> reserveInventory(orderId))
                );
                addressFuture = parallel.branch("verify-address",
                    new TypeToken<Map<String, Object>>() {},
                    ctx -> ctx.step("address", Map.class, s -> verifyAddress(address))
                );
            }

            List<Map<String, Object>> results = DurableFuture.allOf(reservationFuture, addressFuture);
            Map<String, Object> reservation         = results.get(0);
            Map<String, Object> addressVerification = results.get(1);

            // Only reserve-inventory needs a compensation, verify-address is stateless
            compensations.add(new Compensation(
                "cancel-reservation",
                () -> cancelReservation((String) reservation.get("id"))
            ));

            // Stop execution if address is invalid, catch block will cancel reservation
            if (!"true".equals(addressVerification.get("valid"))) {
                throw new IllegalArgumentException("Invalid shipping address");
            }

            // Charge payment
            Map<String, Object> payment = context.step("charge-payment", Map.class,
                ctx -> chargePayment(amount)
            );
            compensations.add(new Compensation(
                "refund-payment",
                () -> refundPayment((String) payment.get("id"))
            ));

            // Create shipment
            Map<String, Object> shipment = context.step("create-shipment", Map.class,
                ctx -> createShipment(orderId)
            );

            context.getLogger().info("Order completed: " + orderId);
            return Map.of("success", true, "tracking_id", shipment.get("tracking_id"));

        } catch (Exception error) {
            context.getLogger().error("Order failed, running compensations: " + error.getMessage());

            for (int i = compensations.size() - 1; i >= 0; i--) {
                Compensation comp = compensations.get(i);
                try {
                    context.step(comp.name, Void.class, ctx -> {
                        comp.fn.run();
                        return null;
                    });
                } catch (Exception compError) {
                    context.getLogger().error("Compensation failed: " + comp.name + " - " + compError.getMessage());
                }
            }

            throw error;
        }
    }

    // Mock APIs for demonstration purposes

    private Map<String, Object> reserveInventory(String orderId) {
        return Map.of("id", "RES-" + orderId);
    }

    private void cancelReservation(String reservationId) {
        System.out.println("Reservation " + reservationId + " cancelled");
    }

    private Map<String, Object> verifyAddress(Map<?, ?> address) {
        // Stateless — no compensation needed
        return Map.of("valid", "true");
    }

    private Map<String, Object> chargePayment(double amount) {
        return Map.of("id", "PAY-" + amount);
    }

    private void refundPayment(String paymentId) {
        System.out.println("Payment " + paymentId + " refunded");
    }

    private Map<String, Object> createShipment(String orderId) {
        return Map.of("tracking_id", "TRACK-" + orderId);
    }
}
