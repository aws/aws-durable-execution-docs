from aws_durable_execution_sdk_python import BatchResult

result: BatchResult = context.parallel(functions)

if result.failure_count > 0:
    # Some branches failed
    return {
        "status": "partial_failure",
        "successful": result.get_results(),
        "failed_count": result.failure_count,
    }

# All branches succeeded
return {
    "status": "success",
    "results": result.get_results(),
}
