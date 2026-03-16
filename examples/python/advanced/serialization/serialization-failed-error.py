# Circular reference - fails
data = {"self": None}
data["self"] = data

# Fix - remove circular reference
data = {"id": 123, "name": "test"}
