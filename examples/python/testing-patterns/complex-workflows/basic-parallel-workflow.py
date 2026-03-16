@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    task1 = context.step(lambda _: "Task 1 complete", name="task1")
    task2 = context.step(lambda _: "Task 2 complete", name="task2")
    task3 = context.step(lambda _: "Task 3 complete", name="task3")
    
    return [task1, task2, task3]
