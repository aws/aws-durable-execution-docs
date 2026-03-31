import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.SendDurableExecutionCallbackSuccessRequest;

LambdaClient lambdaClient = LambdaClient.create();
ObjectMapper mapper = new ObjectMapper();

// When external system succeeds
String callbackId = "abc123-callback-id-from-durable-function";
byte[] resultData = mapper.writeValueAsBytes(Map.of("status", "approved", "amount", 1000));

lambdaClient.sendDurableExecutionCallbackSuccess(
    SendDurableExecutionCallbackSuccessRequest.builder()
        .callbackId(callbackId)
        .result(SdkBytes.fromByteArray(resultData))
        .build());
