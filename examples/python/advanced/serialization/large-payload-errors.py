# Option 1: Reduce data
small_data = {"id": order.id, "status": order.status}
result = context.step(process_order, small_data)

# Option 2: Use summary generator (for map/parallel)
def generate_summary(result):
    return json.dumps({"count": len(result.all)})

config = MapConfig(summary_generator=generate_summary)
result = context.map(process_item, items, config=config)
