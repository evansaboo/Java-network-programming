/* 
 * Created and modified by Evan Saboo
 */
package client.view;

import java.util.Scanner;
import common.Commands;
import common.Constants;
import client.controller.Controller;
import client.net.ServerResponses;
/**
 *
 * @author Evan
 */
public class responsiveFrontEnd implements Runnable {
    
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private boolean receivingCmds = false;
    private Controller contr;
    private final synchronizedPrintLine printOut = new synchronizedPrintLine();
    private final String ipAddress = "127.0.0.1";

 
 public void start() {
        if (receivingCmds) {
            return;
        }
        contr = new Controller();
        contr.connect(ipAddress, Constants.NETWORK_PORT, new msgToPlayer());
        receivingCmds = true;
        new Thread(this).start();
    }
    @Override
    public void run() {       
        
        while(receivingCmds){
            try{
                String [] cmdLine = readNextLine().split(" ");
                if(!checkArrayLength(cmdLine)){
                    wrongInputMsg();
                    continue;
                }
                Commands cmd = Commands.valueOf(cmdLine[0].toUpperCase());
                switch(cmd){
                    case START:
                        contr.sendToServer(cmdLine[0]);
                        break;
                    case QUIT:
                        receivingCmds = false;
                        contr.disconnect();
                        break;
                    default:
                        if(cmdLine.length < 2){
                            wrongInputMsg();
                            continue; 
                        }
                        contr.sendToServer(cmdLine[Constants.STRING_TYPE_INDEX], cmdLine[Constants.STRING_BODY_INDEX]);
                        break;
                }
               } catch (Exception e){
                   wrongInputMsg();
                }
            }
        }
    
    /**
     * Prints out the symbol '>' and waits for user input 
     * @return Scanner nextline for user input
     */
    private String readNextLine() {
        printOut.print(PROMPT);
        return console.nextLine();
    }
    
    /**
     * Check if string array size is between 1 and 2
     * @param cmd string array
     * @return true if array size is between 1 and 2, else false
     */
    private boolean checkArrayLength(String[] cmd){
        return !(cmd.length > 2 || cmd.length < 1);
    }
    
    private void wrongInputMsg(){
        printOut.print(PROMPT);
        printOut.println("Worng input, try again");
    }
    /**
     * Class which implements an interface to handle printout Server messages to the user
     */
    private class msgToPlayer implements ServerResponses {
        @Override
        public void msgHandler(String msg){
            printOut.println((String) msg);
            printOut.print(PROMPT);
        }
    }


}
