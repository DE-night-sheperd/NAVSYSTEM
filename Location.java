public class Location {
    public int locationId;
    public String locationName, building, category, qrCodeData;
    public double latitude, longitude;

    public Location(int id, String name, String building, String cat, double lat, double lon, String qr) {
        this.locationId = id;
        this.locationName = name;
        this.building = building;
        this.category = cat;
        this.latitude = lat;
        this.longitude = lon;
        this.qrCodeData = qr;
    }

    @Override
    public String toString() {
        return locationName;
    }
}
