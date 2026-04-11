from collections.abc import Mapping
from typing import Protocol


class LoggerInterface(Protocol):
    def debug(
        self, msg: object, *args: object, extra: Mapping[str, object] | None = None
    ) -> None: ...

    def info(
        self, msg: object, *args: object, extra: Mapping[str, object] | None = None
    ) -> None: ...

    def warning(
        self, msg: object, *args: object, extra: Mapping[str, object] | None = None
    ) -> None: ...

    def error(
        self, msg: object, *args: object, extra: Mapping[str, object] | None = None
    ) -> None: ...

    def exception(
        self, msg: object, *args: object, extra: Mapping[str, object] | None = None
    ) -> None: ...
