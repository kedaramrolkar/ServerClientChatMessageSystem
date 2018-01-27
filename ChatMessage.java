import java.io.*;

public class ChatMessage implements Serializable {
    protected static final long serialVersionUID = 1112122200L;

    static final int Broadcast = 1, Unicast = 2, Blockcast = 3, Logout=4;

    private int type;
    private String message;
    private String userName;
    private boolean fileOperation;

    public byte[] fileBytes;	//contains file contents
    
    ChatMessage(int type, String userName, String message, boolean op) {
        this.type = type;
        this.message = message;
        this.userName = userName;
        this.fileOperation = op;
    } 

    void setMessage(String m) {
    	this.message = m;
    }
    
    int getType() {
        return type;
    }
    
    String getMessage() {
        return message;
    }
    
    String getUserName() {
        return userName;
    }
    
    boolean getOperation() {
    	return fileOperation;
    }
    
}