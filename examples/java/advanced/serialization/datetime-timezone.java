// Good - timezone-aware Instant (always UTC)
Instant timestamp = Instant.now();

// Good - explicit zone
ZonedDateTime zoned = ZonedDateTime.now(ZoneOffset.UTC);

// Avoid - LocalDateTime has no timezone info
LocalDateTime naive = LocalDateTime.now();  // No timezone
