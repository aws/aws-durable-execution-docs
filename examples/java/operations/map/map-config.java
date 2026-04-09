import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.CompletionConfig;
import software.amazon.lambda.durable.config.MapConfig;
import software.amazon.lambda.durable.model.MapResult;

public class MapConfigExample extends DurableHandler<List<String>, List<String>> {
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @Override
    public List<String> handleRequest(List<String> urls, DurableContext context) {
        var config = MapConfig.builder()
                .maxConcurrency(5)
                .completionConfig(CompletionConfig.toleratedFailureCount(2))
                .build();

        MapResult<String> result = context.map(
                "fetch-urls",
                urls,
                String.class,
                (url, index, ctx) -> ctx.step("fetch-" + index, String.class, s -> {
                    var request = HttpRequest.newBuilder(URI.create(url)).build();
                    return HTTP.send(request, HttpResponse.BodyHandlers.ofString()).body();
                }),
                config);

        return result.succeeded();
    }
}
