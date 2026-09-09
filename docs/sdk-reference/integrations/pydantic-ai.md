# Pydantic AI

[Pydantic AI](https://ai.pydantic.dev/) is a Python framework for building AI
agents. An agent calls a language model in a loop, invokes tools, and produces
a structured result. The
[`pydantic-ai-harness`](https://pypi.org/project/pydantic-ai-harness/) package
runs these agents on AWS Lambda durable functions. It checkpoints each model
request and tool call as a durable step. An interrupted or retried run resumes
from the last completed step instead of starting over.

For requirements, per-tool configuration, and the full API, see the
[Pydantic AI AWS Lambda documentation](https://pydantic.dev/docs/ai/harness/aws-lambda/).

## Installation

```console
pip install "pydantic-ai-harness[aws-lambda]" "pydantic-ai-slim[bedrock]"
```

The Durable Execution SDK requires Python 3.11 or newer.

## Quick start

Add the `AWSLambdaDurability` capability when you build the agent. Adapt an
async handler with `@durable_agent_handler`. `@durable_execution` must be the
outermost decorator, because it creates the handler that Lambda invokes.
`durable_agent_handler` raises an error if you reverse the order.

```python
--8<-- "examples/python/sdk-reference/integrations/pydantic-ai/quick-start.py"
```

!!! warning

    Attaching the capability alone does not make a run durable. A run started
    outside the durable agent handler checkpoints nothing and raises no
    warning.
