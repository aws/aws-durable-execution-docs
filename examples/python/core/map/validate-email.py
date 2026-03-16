def validate_email(
    context: DurableContext,
    item: str,
    index: int,
    items: list[str]
) -> dict:
    """Validate an email address."""
    is_valid = "@" in item and "." in item
    return {
        "email": item,
        "valid": is_valid,
        "position": index,
        "total": len(items)
    }

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    emails = ["jane_doe@example.com", "john_doe@example.org", "invalid"]
    result = context.map(emails, validate_email)
    return result.to_dict()
