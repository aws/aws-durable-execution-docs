func NewLocalRunner[I, O any](handler durable.Handler[I, O], opts ...durable.HandlerOption) *LocalRunner[I, O]
func (r *LocalRunner[I, O]) RunUntilComplete(event I, opts ...RunnerOption) (*TestResult, error)
func ResultAs[O any](r *TestResult) (O, error)
