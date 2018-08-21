package donusgarcom.api.resource;

import donusgarcom.api.database.domain.AuthDao;
import donusgarcom.api.database.domain.UserDao;
import donusgarcom.api.database.manager.MySqlManager;
import donusgarcom.api.service.AuthService;
import donusgarcom.api.service.exception.AuthenticationFailException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auth")
public class AuthResource {
    static Logger log = LogManager.getLogger(AuthResource.class);

    AuthService authService;

    public AuthResource() {
        MySqlManager sqlManager = new MySqlManager();
        authService = new AuthService(
                new UserDao(sqlManager),
                new AuthDao(sqlManager)
        );
    }

    @POST
    @PermitAll
    @Path("/token")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AuthService.AuthToken authenticateUser(AuthService.AuthUser authUser) {
        log.debug("Trying to authenticate user " + authUser.username);
        try {
            return authService.requestToken(authUser);
        } catch (AuthenticationFailException exception) {
            log.error(exception);
            Response r = Response.status(Response.Status.UNAUTHORIZED).build();
            throw new WebApplicationException(r);
        } catch (Exception exception) {
            log.error(exception);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        }
    }
}
