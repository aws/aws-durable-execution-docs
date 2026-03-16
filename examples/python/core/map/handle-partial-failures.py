batch_result = context.map(items, process_item)
successful = [r for r in batch_result.results if r.status == "SUCCEEDED"]
failed = [r for r in batch_result.results if r.status == "FAILED"]
