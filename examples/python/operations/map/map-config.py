import urllib.request

from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import CompletionConfig, MapConfig


def fetch_url(
    ctx: DurableContext, url: str, index: int, urls: list[str]
) -> str:
    def do_fetch(_):
        with urllib.request.urlopen(url) as response:
            return response.read().decode()

    return ctx.step(do_fetch, name=f"fetch-{index}")


@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    config = MapConfig(
        max_concurrency=5,
        completion_config=CompletionConfig(tolerated_failure_count=2),
    )
    result: BatchResult[str] = context.map(
        event["urls"],
        fetch_url,
        name="fetch-urls",
        config=config,
    )
    return result.to_dict()
