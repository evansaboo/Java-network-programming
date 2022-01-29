/*
 * Created and modified by Evan Saboo
 */
package common;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.StringJoiner;

/**
 *
 * @author Evan
 */
public class MsgExtractor {

    public Commands cmdType(String msg) {
        String[] msgParts = msg.split(Constants.STRING_DELIMETER);
        return Commands.valueOf(msgParts[Constants.STRING_TYPE_INDEX].toUpperCase());
    }

    public String bodyOfMsg(String msg) {
        String[] msgParts = msg.split(Constants.STRING_DELIMETER);
        return msgParts[Constants.STRING_BODY_INDEX];
    }

}
