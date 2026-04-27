public class Service {
    public int serviceId, locationId;
    public String serviceName, description, department;

    public Service(int id, String name, String desc, String dept, int locId) {
        this.serviceId = id;
        this.serviceName = name;
        this.description = desc;
        this.department = dept;
        this.locationId = locId;
    }

    @Override
    public String toString() {
        return serviceName;
    }
}
