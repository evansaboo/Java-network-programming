/* 
 * Created and modified by Evan Saboo
 */
package client.view;

import java.util.Scanner;
import common.Commands;
import common.Constants;
import client.net.ServerCommunication;
import client.net.ServerResponses;

/**
 *
 * @author Evan
 */
public class responsiveFrontEnd implements Runnable {

    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private boolean receivingCmds = false;
    private final synchronizedPrintLine printOut = new synchronizedPrintLine();
    private final String ipAddress = "127.0.0.1";
    private ServerCommunication server;

    public void start() {
        if (receivingCmds) {
            return;
        }
        server = new ServerCommunication();
        server.assignListener(new msgToPlayer());
        server.connectToServer(ipAddress, Constants.NETWORK_PORT);
        receivingCmds = true;
        new Thread(this).start();
    }

    @Override
    public void run() {

        while (receivingCmds) {
            try {
                String[] cmdLine = readNextLine().split(" ");
                if (!checkArrayLength(cmdLine)) {
                    wrongInputMsg();
                    continue;
                }
                Commands cmd = Commands.valueOf(cmdLine[0].toUpperCase());
                if (!server.canSendMsg() && cmd != Commands.QUIT) {
                    printOut.print(PROMPT);
                    printOut.println("You can't send a message yet, wait for a answer.");
                    continue;
                }
                switch (cmd) {
                    case START:
                        server.prepareToSendMsg(cmdLine[0]);
                        break;
                    case QUIT:
                        receivingCmds = false;
                        server.sendDisconenctSignal();
                        break;
                    default:
                        if (cmdLine.length < 2) {
                            wrongInputMsg();
                            continue;
                        }
                        server.prepareToSendMsg(cmdLine[Constants.STRING_TYPE_INDEX], cmdLine[Constants.STRING_BODY_INDEX]);
                        break;
                }
            } catch (Exception e) {
                wrongInputMsg();
            }
        }
    }

    /**
     * Prints out the symbol '>' and waits for user input
     *
     * @return Scanner nextline for user input
     */
    private String readNextLine() {
        printOut.print(PROMPT);
        return console.nextLine();
    }

    /**
     * Check if string array size is between 1 and 2
     *
     * @param cmd string array
     * @return true if array size is between 1 and 2, else false
     */
    private boolean checkArrayLength(String[] cmd) {
        return !(cmd.length > 2 || cmd.length < 1);
    }

    private void wrongInputMsg() {
        printOut.print(PROMPT);
        printOut.println("Worng input, try again");
    }

    /**
     * Class which implements an interface to handle printout Server messages to
     * the user
     */
    private class msgToPlayer implements ServerResponses {

        @Override
        public void msgHandler(String msg) {
            printOut(msg);
        }

        private void printOut(String msg) {
            printOut.println((String) msg);
            printOut.print(PROMPT);
        }
    }

}
