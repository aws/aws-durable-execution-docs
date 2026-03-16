@durable_execution
def orchestrate_workflow(event: dict, context: DurableContext) -> dict:
    """Orchestrate a multi-step workflow."""
    user_id = event["user_id"]
    
    # Step 1: Fetch user data
    user = context.invoke(
        function_name="fetch-user",
        payload={"user_id": user_id},
        name="fetch_user",
    )
    
    # Step 2: Enrich user data
    enriched_user = context.invoke(
        function_name="enrich-user-data",
        payload=user,
        name="enrich_user",
    )
    
    # Step 3: Generate report
    report = context.invoke(
        function_name="generate-report",
        payload=enriched_user,
        name="generate_report",
    )
    
    return report
