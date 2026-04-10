results = []
for item in items:
    try:
        result = context.step(lambda _, i=item: process(i), name=f"process_{item}")
        results.append(result)
    except Exception as e:
        results.append({"error": str(e)})
