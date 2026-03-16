from aws_durable_execution_sdk_python_testing.runner import (
    DurableFunctionCloudTestRunner,
    DurableFunctionCloudTestRunnerConfig,
)
from aws_durable_execution_sdk_python_testing.stores.base import StoreType

# In-memory store (default)
config = DurableFunctionCloudTestRunnerConfig(
    function_name="my-function",
    region="us-west-2",
    store_type=StoreType.MEMORY,
)

# Filesystem store
config = DurableFunctionCloudTestRunnerConfig(
    function_name="my-function",
    region="us-west-2",
    store_type=StoreType.FILESYSTEM,
    store_path="./my-executions",
)

runner = DurableFunctionCloudTestRunner(config=config)
