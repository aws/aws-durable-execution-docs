functions = [
    lambda ctx, val=value: process(ctx, val)
    for value in values
]
