@durable_execution
def parent_workflow(event: dict, context: DurableContext) -> dict:
    """Parent workflow that coordinates sub-workflows."""
    project_id = event["project_id"]
    
    # Invoke sub-workflow for data collection
    data = context.invoke(
        function_name="collect-data-workflow",
        payload={"project_id": project_id},
        name="collect_data",
    )
    
    # Invoke sub-workflow for data processing
    processed = context.invoke(
        function_name="process-data-workflow",
        payload=data,
        name="process_data",
    )
    
    # Invoke sub-workflow for reporting
    report = context.invoke(
        function_name="generate-report-workflow",
        payload=processed,
        name="generate_report",
    )
    
    return report
