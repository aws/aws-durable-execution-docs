import pytest
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner

@pytest.fixture
def durable_runner(request):
    """Pytest fixture that provides a test runner."""
    marker = request.node.get_closest_marker("durable_execution")
    if not marker:
        pytest.fail("Test must be marked with @pytest.mark.durable_execution")
    
    handler = marker.kwargs.get("handler")
    runner = DurableFunctionTestRunner(handler=handler)
    
    yield runner
