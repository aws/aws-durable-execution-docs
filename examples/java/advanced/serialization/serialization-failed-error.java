// Circular reference - fails
var data = new HashMap<String, Object>();
data.put("self", data);  // Jackson throws JsonMappingException

// Fix - remove circular reference
var data = Map.of("id", 123, "name", "test");
