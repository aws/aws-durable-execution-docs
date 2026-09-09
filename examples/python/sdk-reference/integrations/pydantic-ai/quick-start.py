from typing import Any

from aws_durable_execution_sdk_python import DurableContext, durable_execution
from pydantic_ai import Agent

from pydantic_ai_harness.aws_lambda import AWSLambdaDurability, durable_agent_handler

agent = Agent(
    "bedrock:us.amazon.nova-pro-v1:0",
    name="support",
    capabilities=[AWSLambdaDurability()],
)


@durable_execution
@durable_agent_handler
async def handler(event: dict[str, Any], context: DurableContext) -> str:
    result = await agent.run(str(event["prompt"]))
    return result.output
