@durable_execution
def process_document(event: dict, context: DurableContext) -> dict:
    """Process a document based on its type."""
    document_type = event["document_type"]
    document_data = event["data"]
    
    if document_type == "pdf":
        result = context.invoke(
            function_name="process-pdf",
            payload=document_data,
            name="process_pdf",
        )
    elif document_type == "image":
        result = context.invoke(
            function_name="process-image",
            payload=document_data,
            name="process_image",
        )
    else:
        result = context.invoke(
            function_name="process-generic",
            payload=document_data,
            name="process_generic",
        )
    
    return result
