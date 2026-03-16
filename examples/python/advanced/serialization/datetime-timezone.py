from datetime import datetime, UTC

# Good - timezone aware
timestamp = datetime.now(UTC)

# Avoid - naive datetime
timestamp = datetime.now()  # No timezone
