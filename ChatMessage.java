import java.io.Serializable;

public class ChatMessage implements Serializable{
	
	public final static int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;

	private int type;

	private String text;

	public ChatMessage(int type, String text) {
		this.type = type;
		this.text = text;
	}

	public int getType() {
		return type;
	}

	public String getText() {
		return text;
	}
}
