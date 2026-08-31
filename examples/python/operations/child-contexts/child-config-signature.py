@dataclass(frozen=True)
class ChildConfig(Generic[T]):
    serdes: SerDes | None = None
    sub_type: OperationSubType | None = None
    summary_generator: SummaryGenerator | None = None
    is_virtual: bool = False
