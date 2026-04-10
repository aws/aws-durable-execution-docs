from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Generic, TypeVar

T = TypeVar("T")


@dataclass(frozen=True)
class SerDesContext:
    operation_id: str = ""
    durable_execution_arn: str = ""


class SerDes(ABC, Generic[T]):
    @abstractmethod
    def serialize(self, value: T, serdes_context: SerDesContext) -> str: ...

    @abstractmethod
    def deserialize(self, data: str, serdes_context: SerDesContext) -> T: ...
