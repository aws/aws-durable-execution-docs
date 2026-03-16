# Default serialization preserves tuple
result = context.step(process_data, (1, 2, 3))  # Stays as tuple

# If using custom SerDes, ensure it preserves types
class TypePreservingSerDes(SerDes[Any]):
    def serialize(self, value: Any, context: SerDesContext) -> str:
        # Implement type preservation logic
        pass
