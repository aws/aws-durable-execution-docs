public class ProcessDocumentHandler extends DurableHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
        String documentType = (String) event.get("document_type");
        var documentData = event.get("data");

        Map result;
        if ("pdf".equals(documentType)) {
            result = ctx.invoke("process_pdf", "process-pdf", documentData, Map.class);
        } else if ("image".equals(documentType)) {
            result = ctx.invoke("process_image", "process-image", documentData, Map.class);
        } else {
            result = ctx.invoke("process_generic", "process-generic", documentData, Map.class);
        }

        return result;
    }
}
