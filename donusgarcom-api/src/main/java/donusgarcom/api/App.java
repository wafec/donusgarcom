package donusgarcom.api;

import donusgarcom.api.resource.AuthResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

public class App {
    static final Logger log = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        ResourceConfig rc = new ResourceConfig();
        rc.registerClasses(AuthResource.class);

        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                getBaseURI(),
                rc
        );
        try {
            server.start();
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