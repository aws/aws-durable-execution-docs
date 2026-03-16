# Good - log identifiers only
context.logger.info("User authenticated", extra={"user_id": user_id})

# Avoid - don't log sensitive data
context.logger.info("User authenticated", extra={"password": password})
