var items = List.of(1, 2, 3, 4, 5);

var result = ctx.map("square-numbers", items, Integer.class,
    (item, index, childCtx) -> item * item);

// result.results() returns [1, 4, 9, 16, 25]
return Map.of("results", result.results());
