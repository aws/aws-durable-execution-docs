import copy as copy_mod

# The step return value is checkpointed as durable state.
value = context.step(fetch_value())

# Assigning or copying the local variable does NOT add to durable state.
alias = value
dup = copy_mod.deepcopy(value)

# Returning the local variable from the handler DOES add it to durable state
# (the handler output is serialized and stored).
return {"value": value}
