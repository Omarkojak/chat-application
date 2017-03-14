package Misc;

import java.io.Serializable;

/**
 * Created by mohamedelzarei on 11/22/16.
 * mohamedelzarei@gmail.com
 */
public enum MessageType implements Serializable{
    ADD_USER,REMOVE_USER,USER_EXISTS,USER_APPROVED,
    LOCAL_MEMBERS,ALL_MEMBERS,CHAT_MESSAGE,USER_NOT_FOUND,
    ERROR_OCCURED
}
