public class EncryptedSerDes implements SerDes {

    private final String encryptionKey;

    public EncryptedSerDes(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    @Override
    public String serialize(Object value) {
        String json = new ObjectMapper().writeValueAsString(value);
        // In production, use proper encryption like AWS KMS
        return Base64.getEncoder().encodeToString(json.getBytes());
    }

    @Override
    public <T> T deserialize(String data, Class<T> clazz) {
        // In production, use proper decryption
        String decrypted = new String(Base64.getDecoder().decode(data));
        return new ObjectMapper().readValue(decrypted, clazz);
    }

    @Override
    public <T> T deserialize(String data, TypeToken<T> typeToken) {
        String decrypted = new String(Base64.getDecoder().decode(data));
        return new ObjectMapper().readValue(decrypted,
            new ObjectMapper().constructType(typeToken.getType()));
    }
}

// Usage: encrypt sensitive data in checkpoints
var sensitiveData = Map.of("ssn", "123-45-6789", "credit_card", "4111-1111-1111-1111");

var config = StepConfig.builder()
    .serDes(new EncryptedSerDes("my-key"))
    .build();

var result = ctx.step("process-sensitive", Map.class,
    stepCtx -> Map.of("processed", true), config);
