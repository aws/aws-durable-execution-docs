from aws_durable_execution_sdk_python import (
    DagContext,
    DagResult,
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import Duration


@durable_execution
def handler(event: dict, context: DurableContext) -> int:
    def register(dag: DagContext) -> None:
        dag.step(lambda deps, sc: 1, name="compute")

        def square(
            ctx: DurableContext, item: int, index: int, items: list[int]
        ) -> int:
            return ctx.step(lambda sc: item * item, name=f"square-{index}")

        dag.map([1, 2, 3], square, name="square-each")

        dag.wait(Duration.from_seconds(1), name="cooldown")

        def group(deps, child: DurableContext) -> str:
            return child.step(lambda sc: "done", name="inner")

        dag.run_in_child_context(group, name="group")

    result: DagResult = context.dag(register, name="mixed-kinds")
    return result.success_count
