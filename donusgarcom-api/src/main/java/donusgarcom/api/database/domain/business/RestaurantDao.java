package donusgarcom.api.database.domain.business;

import donusgarcom.api.database.domain.GenericDao;
import donusgarcom.api.database.domain.common.AddressDao;
import donusgarcom.api.database.domain.common.GeoLocalizationDao;
import donusgarcom.api.database.util.DbManager;

import javax.inject.Inject;

public class RestaurantDao extends GenericDao<RestaurantDao.Restaurant> {
    @Inject
    public RestaurantDao(DbManager dbManager) {
        super(dbManager);
    }

    @Override
    public String getTableName() {
        return "restaurants";
    }

    @Override
    public Class<Restaurant> getClazz() {
        return Restaurant.class;
    }

    @Override
    public SqlField[] getManagedSqlFields() {
        return unionOfManagedSqlFields(
                normalizeSubManagedSqlFields("address.", new AddressDao().getManagedSqlFields()),
                new SqlField[] {
                        new SqlField("name", SqlFieldType.STRING)
                },
                normalizeSubManagedSqlFields("geoLocalization.", new GeoLocalizationDao().getManagedSqlFields())
        );
    }

    public static class Restaurant extends GenericDao.GenericData {
        public String name;
        public AddressDao.Address address;
        public GeoLocalizationDao.GeoLocalization geoLocalization;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AddressDao.Address getAddress() {
            return address;
        }

        public void setAddress(AddressDao.Address address) {
            this.address = address;
        }

        public void setGeoLocalization(GeoLocalizationDao.GeoLocalization geoLocalization) {
            this.geoLocalization = geoLocalization;
        }

        public GeoLocalizationDao.GeoLocalization getGeoLocalization() {
            return geoLocalization;
        }
    }
}
