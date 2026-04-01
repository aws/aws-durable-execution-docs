// Break large operation into smaller steps
var items = (List<?>) event.get("items");
int chunkSize = 100;

var results = new ArrayList<Map>();
for (int i = 0; i < items.size(); i += chunkSize) {
    int start = i;
    int end = Math.min(i + chunkSize, items.size());
    var chunk = items.subList(start, end);

    var result = ctx.step("process_chunk_" + i, List.class,
        stepCtx -> processChunk(chunk));
    results.addAll(result);
}

return Map.of("processed", results.size());
