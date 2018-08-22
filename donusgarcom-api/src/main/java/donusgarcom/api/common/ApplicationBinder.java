package donusgarcom.api.common;

import donusgarcom.api.database.domain.business.RestaurantDao;
import donusgarcom.api.database.domain.identity.AuthDao;
import donusgarcom.api.database.domain.identity.UserDao;
import donusgarcom.api.database.util.DbManager;
import donusgarcom.api.service.AuthService;
import donusgarcom.api.service.RestaurantService;
import donusgarcom.api.service.UserService;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.io.InputStream;

public class ApplicationBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(DozerBeanMapper.class).to(Mapper.class);
        bind(DbManagerFromFile.class).to(DbManager.class);
        bind(AuthService.class).to(AuthService.class);
        bind(UserService.class).to(UserService.class);
        bind(RestaurantService.class).to(RestaurantService.class);
        bind(AuthDao.class).to(AuthDao.class);
        bind(UserDao.class).to(UserDao.class);
        bind(RestaurantDao.class).to(RestaurantDao.class);
    }

    public static class DbManagerFromFile extends DbManager {
        public DbManagerFromFile() {
            super();
            initialize();
        }

        final void initialize() {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("donusdb.conf");
            DbConfig dbConfig = getDbConfigFromStream(inputStream);
            setConnectionParameters(
                    dbConfig.driver,
                    dbConfig.url,
                    dbConfig.user,
                    dbConfig.password
            );
        }
    }
}
