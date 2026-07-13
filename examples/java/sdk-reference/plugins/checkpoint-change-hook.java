@Override
public void onOperationChange(OperationChangeInfo info) {
    System.out.println("operations changed, ids: " + info.updatedOperations().keySet());
}
