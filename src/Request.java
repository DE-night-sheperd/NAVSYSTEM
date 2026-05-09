public class Request {
    public int requestId, userId, serviceId, locationId;
    public String description, status, requestDate;
    public String firstSeenDate; // Track when staff first saw the request

    public Request(int id, int uid, int sid, int lid, String desc, String status, String date) {
        this(id, uid, sid, lid, desc, status, date, null);
    }

    public Request(int id, int uid, int sid, int lid, String desc, String status, String date, String firstSeen) {
        this.requestId = id;
        this.userId = uid;
        this.serviceId = sid;
        this.locationId = lid;
        this.description = desc;
        this.status = status;
        this.requestDate = date;
        this.firstSeenDate = firstSeen;
    }
}
