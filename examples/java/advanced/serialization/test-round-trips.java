@Test
void testSerializationRoundTrip() {
    ObjectMapper mapper = new ObjectMapper();

    var original = Map.of("amount", new BigDecimal("99.99"));
    String serialized = mapper.writeValueAsString(original);
    Map deserialized = mapper.readValue(serialized, Map.class);

    assertEquals(original.get("amount").toString(), deserialized.get("amount").toString());
}
