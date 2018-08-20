package donusgarcom.api.resource;

import donusgarcom.api.service.AuthService;
import donusgarcom.api.service.exception.AuthenticationFailException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auth")
public class AuthResource {
    static Logger log = LogManager.getLogger(AuthResource.class);

    AuthService authService;

    @Inject
    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @POST
    @PermitAll
    @Path("/token")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AuthService.AuthToken authenticateUser(AuthService.AuthUser authUser) {
        try {
            return authService.requestToken(authUser);
        } catch (AuthenticationFailException exception) {
            log.error(exception);
            Response r = Response.status(Response.Status.UNAUTHORIZED).build();
            throw new WebApplicationException(r);
        }
    }
}
