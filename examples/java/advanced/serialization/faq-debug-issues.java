// Test serialization round-trip with Jackson
ObjectMapper mapper = new ObjectMapper();

try {
    String serialized = mapper.writeValueAsString(myData);
    Object deserialized = mapper.readValue(serialized, Object.class);
    System.out.println("Serialization successful");
} catch (Exception e) {
    System.out.println("Serialization failed: " + e.getMessage());
}
