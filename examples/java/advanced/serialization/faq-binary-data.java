// Encode binary data as Base64 string
byte[] binaryData = "binary content".getBytes();
String encoded = Base64.getEncoder().encodeToString(binaryData);
var result = ctx.step("process-binary", String.class, stepCtx -> processBinary(encoded));
