// The step return value is checkpointed as durable state.
Value value = context.step("fetch", Value.class, ctx -> fetchValue());

// Assigning or copying the local variable does NOT add to durable state.
Value alias = value;
Value copy = new Value(value); // copy constructor

// Returning the local variable from the handler DOES add it to durable state
// (the handler output is serialized and stored).
return new Result(value);
