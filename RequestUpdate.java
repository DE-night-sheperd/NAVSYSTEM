public class RequestUpdate {
    public int updateId, requestId, staffId;
    public String comment, updateDate;

    public RequestUpdate(int id, int reqId, int staffId, String comment, String date) {
        this.updateId = id;
        this.requestId = reqId;
        this.staffId = staffId;
        this.comment = comment;
        this.updateDate = date;
    }
}
