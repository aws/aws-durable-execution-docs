# tests/test_order_processing.py
class TestOrderValidation:
    """Tests for order validation."""
    
    @pytest.mark.durable_execution(handler=handler, lambda_function_name="orders")
    def test_valid_order(self, durable_runner):
        """Test valid order is accepted."""
        pass
    
    @pytest.mark.durable_execution(handler=handler, lambda_function_name="orders")
    def test_invalid_order_id(self, durable_runner):
        """Test invalid order ID is rejected."""
        pass

class TestOrderFulfillment:
    """Tests for order fulfillment."""
    
    @pytest.mark.durable_execution(handler=handler, lambda_function_name="orders")
    def test_fulfillment_success(self, durable_runner):
        """Test successful order fulfillment."""
        pass
