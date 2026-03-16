from aws_durable_execution_sdk_python.config import MapConfig


def fetch_data(context: DurableContext, url: str, index: int, urls: list[str]) -> dict:
    """Fetch data from a URL."""
    # Network call that might be rate-limited
    return {"url": url, "data": "..."}


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    urls = [f"https://example.com/api/{i}" for i in range(100)]

    # Process only 5 URLs at a time
    config = MapConfig(max_concurrency=5)

    result = context.map(urls, fetch_data, config=config)
    return result.to_dict()
