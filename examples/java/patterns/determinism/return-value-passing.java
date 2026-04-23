// Right: each step returns the new running total.
double total = 0.0;
for (Item item : input.items()) {
    final double running = total;
    total = context.step(
        "save-" + item.id(),
        Double.class,
        ctx -> {
            saveItem(item);
            return running + item.price();
        });
}
return new Result(total);
