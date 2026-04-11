import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.lambda.powertools.logging.Logging;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class MyHandler implements RequestHandler<Object, String> {

    private static final Logger logger = LoggerFactory.getLogger(MyHandler.class);

    @Logging
    public String handleRequest(Object event, Context context) {
        logger.info("Running handler");
        return "ok";
    }
}
