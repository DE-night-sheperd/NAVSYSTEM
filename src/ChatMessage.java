public class ChatMessage {
    public String sender;
    public String text;
    public String timestamp;

    public ChatMessage(String sender, String text, String timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
    }
}