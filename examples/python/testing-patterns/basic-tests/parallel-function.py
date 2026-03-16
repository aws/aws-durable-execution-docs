from aws_durable_execution_sdk_python import DurableContext, durable_execution

@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    # Execute multiple operations
    task1 = context.step(lambda _: "Task 1 complete", name="task1")
    task2 = context.step(lambda _: "Task 2 complete", name="task2")
    task3 = context.step(lambda _: "Task 3 complete", name="task3")
    
    # All tasks execute concurrently and results are collected
    return [task1, task2, task3]
