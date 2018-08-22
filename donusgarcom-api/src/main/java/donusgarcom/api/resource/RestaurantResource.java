package donusgarcom.api.resource;

import donusgarcom.api.common.exceptions.ServiceInvalidParamException;
import donusgarcom.api.service.RestaurantService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/restaurant")
public class RestaurantResource {
    RestaurantService restaurantService;

    @Inject
    public RestaurantResource(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RestaurantService.RestaurantGenericView createRestaurant(RestaurantService.NewRestaurant newRestaurant) {
        try {
            return restaurantService.createRestaurant(newRestaurant);
        } catch (ServiceInvalidParamException exception) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).build());
        }
    }
}
