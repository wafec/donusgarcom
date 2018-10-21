package donusgarcom.api.resource;

import donusgarcom.api.common.exceptions.ServiceInvalidParamException;
import donusgarcom.api.service.UserService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dozer.Mapper;
import org.glassfish.jersey.server.CloseableService;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;

@Path("/user")
public class UserResource {
    static final Logger log = LogManager.getLogger(UserResource.class);

    @Inject
    Mapper mapper;
    @Context
    CloseableService closeableService;

    UserService userService;

    @Inject
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @POST
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserService.UserGenericView createUser(NewBasicUser newBasicUser) {
        try {
            UserService.NewUser newUser = mapper.map(newBasicUser, UserService.NewUser.class);
            newUser.role = newBasicUser.getRole();
            return userService.createUser(newUser);
        } catch (ServiceInvalidParamException exception) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    public static class NewBasicUser {
        public String name;
        public String pass;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPass() {
            return pass;
        }

        public void setPass(String pass) {
            this.pass = pass;
        }

        public String getRole() {
            return "basic";
        }
    }
}
