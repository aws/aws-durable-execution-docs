// Message processor side (separate Lambda or service)
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

LambdaClient lambdaClient = LambdaClient.create();
ObjectMapper mapper = new ObjectMapper();

public void processPaymentMessage(Map<String, Object> event) {
    String callbackId = (String) event.get("callback_id");
    Number amount = (Number) event.get("amount");
    String customerId = (String) event.get("customer_id");

    try {
        // Process payment with external system
        var result = paymentProcessor.charge(customerId, amount);

        // Notify success
        byte[] resultData = mapper.writeValueAsBytes(Map.of(
            "status", "completed",
            "transaction_id", result.getTransactionId()
        ));

        lambdaClient.sendDurableExecutionCallbackSuccess(
            SendDurableExecutionCallbackSuccessRequest.builder()
                .callbackId(callbackId)
                .result(SdkBytes.fromByteArray(resultData))
                .build());
    } catch (PaymentException e) {
        // Notify failure
        lambdaClient.sendDurableExecutionCallbackFailure(
            SendDurableExecutionCallbackFailureRequest.builder()
                .callbackId(callbackId)
                .error(CallbackError.builder()
                    .errorType("PaymentError")
                    .errorMessage(e.getErrorCode() + ": " + e.getMessage())
                    .build())
                .build());
    }
}
