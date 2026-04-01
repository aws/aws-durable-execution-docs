var emails = List.of("jane_doe@example.com", "john_doe@example.org", "invalid");

var result = ctx.map("validate-emails", emails, Map.class,
    (email, index, childCtx) -> Map.of(
        "email", email,
        "valid", email.contains("@") && email.contains("."),
        "position", index,
        "total", emails.size()));

return Map.of("results", result.results());
