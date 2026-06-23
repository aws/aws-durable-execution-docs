@Override
public void onOperationStart(OperationInfo info) {
    System.out.println("operation " + info.name() + " start, type: " + info.type());
}

@Override
public void onOperationEnd(OperationEndInfo info) {
    System.out.println("operation " + info.name() + " end, error: " + info.error());
}
