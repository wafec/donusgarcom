package donusgarcom.api.database.domain.common;

import donusgarcom.api.database.domain.CoreDao;
import donusgarcom.api.database.domain.GenericDao;

public class GeoLocalizationDao extends CoreDao {
    @Override
    public SqlField[] getManagedSqlFields() {
        return new SqlField[] {
                new SqlField("latitude", SqlFieldType.DOUBLE),
                new SqlField("longitude", SqlFieldType.DOUBLE)
        };
    }

    public static class GeoLocalization extends GenericDao.GenericData {
        public double latitude;
        public double longitude;

        public GeoLocalization() {

        }

        public GeoLocalization(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
