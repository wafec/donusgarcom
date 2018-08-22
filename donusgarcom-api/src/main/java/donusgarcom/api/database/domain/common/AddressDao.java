package donusgarcom.api.database.domain.common;

import donusgarcom.api.database.domain.CoreDao;

public class AddressDao extends CoreDao {
    @Override
    public SqlField[] getManagedSqlFields() {
        return new SqlField[] {
            new SqlField("street", SqlFieldType.STRING),
            new SqlField("neighborhood", SqlFieldType.STRING),
            new SqlField("city", SqlFieldType.STRING),
            new SqlField("state", SqlFieldType.STRING),
            new SqlField("zip", SqlFieldType.STRING),
            new SqlField("country", SqlFieldType.STRING)
        };
    }

    public static class Address extends CoreData {
        public String street;
        public String neighborhood;
        public String city;
        public String state;
        public String zip;
        public String country;

        public String getStreet() {
            return street;
        }

        public String getNeighborhood() {
            return neighborhood;
        }

        public String getCity() {
            return city;
        }

        public String getState() {
            return state;
        }

        public String getZip() {
            return zip;
        }

        public String getCountry() {
            return country;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public void setNeighborhood(String neighborhood) {
            this.neighborhood = neighborhood;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setState(String state) {
            this.state = state;
        }

        public void setZip(String zip) {
            this.zip = zip;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }
}
