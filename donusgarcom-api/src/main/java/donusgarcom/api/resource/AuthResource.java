package donusgarcom.api.resource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/auth")
public class AuthResource {
    @POST
    @PermitAll
    @Path("/token")
    public Object token() {
        return null;
    }
}
