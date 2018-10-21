package donusgarcom.api.common;

import donusgarcom.api.common.exceptions.DbConfigurationFileNotFoundException;
import donusgarcom.api.database.domain.business.RestaurantDao;
import donusgarcom.api.database.domain.identity.AuthDao;
import donusgarcom.api.database.domain.identity.UserDao;
import donusgarcom.api.database.util.DbManager;
import donusgarcom.api.service.AuthService;
import donusgarcom.api.service.RestaurantService;
import donusgarcom.api.service.UserService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.process.internal.RequestScoped;

import java.io.IOException;
import java.io.InputStream;

public class ApplicationBinder extends AbstractBinder {
    static final Logger log = LogManager.getLogger(ApplicationBinder.class);

    @Override
    protected void configure() {
        bind(DozerBeanMapper.class).to(Mapper.class);
        bindFactory(DbManagerFactory.class).to(DbManager.class).in(RequestScoped.class);
        bind(AuthService.class).to(AuthService.class);
        bind(UserService.class).to(UserService.class);
        bind(RestaurantService.class).to(RestaurantService.class);
        bind(AuthDao.class).to(AuthDao.class);
        bind(UserDao.class).to(UserDao.class);
        bind(RestaurantDao.class).to(RestaurantDao.class);

    }

    public static class DbManagerFactory implements Factory<DbManager> {
        static final InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("donusdb.conf");

        @Override
        public DbManager provide() {
            DbManager dbManager = new DbManager(new DbManager.DbConfig(inputStream));
            dbManager.setDisposable(true);
            return dbManager;
        }

        @Override
        public void dispose(DbManager dbManager) {
            dbManager.dispose();
        }
    }
}
