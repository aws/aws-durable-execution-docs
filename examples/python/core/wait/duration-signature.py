# module: aws_durable_execution_sdk_python.config


@dataclass
class Duration:
    seconds: int = 0

    @classmethod
    def from_seconds(cls, value: float) -> Duration: ...

    @classmethod
    def from_minutes(cls, value: float) -> Duration: ...

    @classmethod
    def from_hours(cls, value: float) -> Duration: ...

    @classmethod
    def from_days(cls, value: float) -> Duration: ...
