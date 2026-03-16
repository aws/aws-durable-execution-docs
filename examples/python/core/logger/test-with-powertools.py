import json
import pytest
from aws_lambda_powertools import Logger

@pytest.mark.durable_execution(handler=my_handler)
def test_powertools_logging(durable_runner, caplog):
    """Test Powertools for AWS Lambda (Python) integration."""
    logger = Logger(service="test-service")
    
    with caplog.at_level(logging.INFO):
        with durable_runner:
            result = durable_runner.run(input={"id": "test-123"}, timeout=10)
    
    # Parse JSON log entries
    for record in caplog.records:
        if hasattr(record, 'msg'):
            try:
                log_entry = json.loads(record.msg)
                # Verify Powertools for AWS Lambda (Python) fields
                assert "service" in log_entry
                # Verify SDK enrichment fields
                assert "execution_arn" in log_entry
            except json.JSONDecodeError:
                pass  # Not a JSON log entry
