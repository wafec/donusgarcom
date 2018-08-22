package donusgarcom.api.service;

import donusgarcom.api.common.exceptions.ServiceInvalidParamException;
import donusgarcom.api.database.domain.business.RestaurantDao;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlType;

public class RestaurantService extends GenericService {
    static final Logger log = LogManager.getLogger(RestaurantService.class);

    @Inject
    Mapper mapper;

    RestaurantDao restaurantDao;

    @Inject
    public RestaurantService(RestaurantDao restaurantDao) {
        this.restaurantDao = restaurantDao;
    }

    public RestaurantGenericView createRestaurant(NewRestaurant newRestaurant) {
        if (newRestaurant != null && !restaurantExists()) {
            RestaurantDao.Restaurant restaurant = mapper.map(newRestaurant, RestaurantDao.Restaurant.class);
            restaurantDao.insert(restaurant);
            restaurantDao.commit();
            log.debug("Created restaurant: '" + new ReflectionToStringBuilder(restaurant, new RecursiveToStringStyle()).toString() + "'");
            return mapper.map(restaurant, RestaurantGenericView.class);
        } else {
            throw new ServiceInvalidParamException();
        }
    }

    boolean restaurantExists() {
        return false;
    }

    public static class NewRestaurant {
        public String name;
        public NewAddress address;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public NewAddress getAddress() {
            return address;
        }

        public void setAddress(NewAddress address) {
            this.address = address;
        }
    }

    @XmlType(name="")
    public static class RestaurantGenericView extends GenericView { }
}
