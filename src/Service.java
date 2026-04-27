public class Service {
    public int serviceId, locationId;
    public String serviceName, description, department, contactInfo;

    public Service(int id, String name, String desc, String dept, int locId, String contact) {
        this.serviceId = id;
        this.serviceName = name;
        this.description = desc;
        this.department = dept;
        this.locationId = locId;
        this.contactInfo = contact;
    }

    @Override
    public String toString() {
        return serviceName;
    }
}
