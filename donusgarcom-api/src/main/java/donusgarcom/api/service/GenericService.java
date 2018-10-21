package donusgarcom.api.service;

public abstract class GenericService {
    protected void checkString(String str) {

    }

    public static class NewAddress {
        public String street;
        public String neighborhood;
        public String city;
        public String zip;
        public String country;
        public String state;

        public void setStreet(String street) {
            this.street = street;
        }

        public void setNeighborhood(String neighborhood) {
            this.neighborhood = neighborhood;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setZip(String zip) {
            this.zip = zip;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getStreet() {
            return street;
        }

        public String getNeighborhood() {
            return neighborhood;
        }

        public String getCity() {
            return city;
        }

        public String getZip() {
            return zip;
        }

        public String getCountry() {
            return country;
        }

        public String getState() {
            return state;
        }
    }

    public static class NewGeoLocalization {
        public double latitude;
        public double longitude;

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return this.latitude;
        }

        public double getLongitude() {
            return this.longitude;
        }
    }

    public static class GenericView {
        public int id;

        public GenericView() {

        }

        public GenericView(int id) {
            this.id = id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
