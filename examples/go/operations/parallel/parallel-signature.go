// Branches of the same result type.
func Parallel[O any](ctx Context, name string, branches []Branch[O], opts ...BatchOption) (BatchResult[O], error)

type Branch[O any] struct {
	Name string
	Func func(Context) (O, error)
}

// Branches of different result types.
func ParallelMixed(ctx Context, name string, branches []AnyBranch, opts ...BatchOption) (BatchResult[json.RawMessage], error)

func NewTypedBranch[T any](name string, fn func(Context) (T, error), opts ...TypedBranchOption) *TypedBranch[T]

func (b *TypedBranch[T]) Result(res BatchResult[json.RawMessage]) (T, error)
