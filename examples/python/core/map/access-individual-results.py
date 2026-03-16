batch_result = context.map(items, process_item)
for item_result in batch_result.results:
    print(item_result.result)
