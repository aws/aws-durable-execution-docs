// When external system fails
String callbackId = "abc123-callback-id-from-durable-function";

lambdaClient.sendDurableExecutionCallbackFailure(
    SendDurableExecutionCallbackFailureRequest.builder()
        .callbackId(callbackId)
        .error(CallbackError.builder()
            .errorType("PaymentDeclined")
            .errorMessage("Insufficient funds")
            .build())
        .build());
