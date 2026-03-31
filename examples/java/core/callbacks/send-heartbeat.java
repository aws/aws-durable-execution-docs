// Send heartbeat for long-running operations
String callbackId = "abc123-callback-id-from-durable-function";

lambdaClient.sendDurableExecutionCallbackHeartbeat(
    SendDurableExecutionCallbackHeartbeatRequest.builder()
        .callbackId(callbackId)
        .build());
