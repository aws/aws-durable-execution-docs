func All[O any](ctx Context, name string, fs []*Future[O], opts ...ChildOption) ([]O, error)
func AllSettled[O any](ctx Context, name string, fs []*Future[O], opts ...ChildOption) ([]Settled[O], error)
func Any[O any](ctx Context, name string, fs []*Future[O], opts ...ChildOption) (O, error)
func Race[O any](ctx Context, name string, fs []*Future[O], opts ...ChildOption) (O, error)
func Join(ctx Context, name string, fs []Awaitable, opts ...ChildOption) error
func Select[O any](ctx Context, name string, branches []Branch[O], opts ...ChildOption) (winner string, value O, err error)
