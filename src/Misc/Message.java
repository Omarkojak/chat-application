package Misc;

import java.io.Serializable;

/**
 * Created by mohamedelzarei on 11/22/16.
 * mohamedelzarei@gmail.com
 */
public class Message implements Serializable{
    public String from;
    public String to;
    public Object data;
    public MessageType type;
    public int TTL;
    public int loginClient;

    public Message(MessageType type, String from, String to, Object data) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.data = data;
        this.TTL = 4;
    }

    public Message(MessageType type, Object data) {
        this.type = type;
        this.data = data;
        this.TTL = 4;
    }

    public boolean isAlive(){
        return TTL > 0;
    }

    public void decreaseTTL() {
        TTL--;
    }
}
