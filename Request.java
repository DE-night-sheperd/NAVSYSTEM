public class Request {
    public int requestId, userId, serviceId, locationId;
    public String description, status, requestDate;

    public Request(int id, int uid, int sid, int lid, String desc, String status, String date) {
        this.requestId = id;
        this.userId = uid;
        this.serviceId = sid;
        this.locationId = lid;
        this.description = desc;
        this.status = status;
        this.requestDate = date;
    }
}
