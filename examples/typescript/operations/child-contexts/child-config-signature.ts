interface ChildConfig<T> {
  serdes?: Serdes<T>;
  subType?: string;
  summaryGenerator?: (result: T) => string;
  errorMapper?: (originalError: DurableOperationError) => DurableOperationError;
  virtualContext?: boolean;
}
