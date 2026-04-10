# Reduce payload size by returning only necessary data
@durable_step
def large_operation(step_context: StepContext) -> dict:
    # Process large data
    large_result = process_data()
    
    # Return only summary, not full data
    return {"summary": large_result["summary"], "count": len(large_result["items"])}
