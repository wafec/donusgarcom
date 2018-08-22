package donusgarcom.api;

import donusgarcom.api.resource.AuthResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ContextResolver;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class App {
    static final Logger log = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        ResourceConfig rc = new ApiResourceConfig();
        URI uri = getBaseURI();
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                uri,
                rc,
                false
        );
        try {
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            Thread.currentThread().join();
        }
        catch (IOException exception) {
            log.error(exception);
        } catch (InterruptedException exception) {
            log.error(exception);
        }
    }

    public static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/api").port(8080).build();
    }
}