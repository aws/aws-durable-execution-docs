MapResult<String> result = ctx.map("process", items, String.class,
    (item, index, childCtx) ->
        childCtx.step("step-" + index, String.class, stepCtx -> process(item)));

for (int i = 0; i < result.size(); i++) {
    String value = result.getResult(i);   // null if failed
    MapError error = result.getError(i);  // null if succeeded
    System.out.println("Item " + i + ": " + value);
}
