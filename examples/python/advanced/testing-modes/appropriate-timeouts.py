# Local mode: short timeout
result = runner.run(input="test", timeout=10)

# Cloud mode: longer timeout
result = runner.run(input="test", timeout=60)
