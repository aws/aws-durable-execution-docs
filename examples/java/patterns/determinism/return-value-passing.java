// Wrong: the step body adds to an outer list through a closure.
// Replay returns the cached null without running the body, so the
// list stays empty and the handler returns an empty receipts list
// on replay.
List<String> receipts = new ArrayList<>();
for (Item item : input.items()) {
    context.step(
        "save-" + item.id(),
        Void.class,
        ctx -> {
            String receiptId = saveItem(item);
            receipts.add(receiptId);
            return null;
        });
}
return new Result(receipts);

// Right: the step returns the receipt id. The handler adds the
// returned value to the outer list, which replay rebuilds from the
// cached step results.
List<String> receipts = new ArrayList<>();
for (Item item : input.items()) {
    String receiptId = context.step(
        "save-" + item.id(),
        String.class,
        ctx -> saveItem(item));
    receipts.add(receiptId);
}
return new Result(receipts);
